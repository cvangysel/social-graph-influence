package be.stophr.ml.classification;

import java.io.Serializable;
import java.util.ArrayList;

import org.apache.crunch.DoFn;
import org.apache.crunch.Emitter;
import org.apache.crunch.FilterFn;
import org.apache.crunch.MapFn;
import org.apache.crunch.PGroupedTable;
import org.apache.crunch.PTable;
import org.apache.crunch.Pair;
import org.apache.crunch.lib.Join;
import org.apache.crunch.types.writable.Writables;

import be.stophr.ml.classification.crunch.AggregateLabelsOp;
import be.stophr.ml.classification.labels.Label;

/**
 * Implementation of the Adsorption algorithm by Baluja et al. (2008) using the
 * Apache Crunch framework.
 * 
 * The Adsorption algorithm performs classification on graph nodes through
 * the simulation of Markov random walks. Seed nodes are assigned an initial
 * label. During every iteration each node takes the weighted average of the labels
 * of their neighbors.
 * 
 * Apache Crunch provides an API similar to that of FlumeJava (Chambers et al., 2010).
 * It allows us to easily write efficient data-parallel pipelines with relative ease
 * and comfort.
 * 
 * @author Christophe Van Gysel
 */
@SuppressWarnings("serial")
public class GraphAdsorption<LabelT extends Label> implements Serializable {

    protected final Class<LabelT> labelClass;
    protected final Label.Factory labelFactory;

    public GraphAdsorption(final Class<LabelT> labelClass,
            final Label.Factory labelFactory) {
        assert labelClass != null;
        assert labelFactory != null;

        this.labelClass = labelClass;
        this.labelFactory = labelFactory;
    }

    public Pair<PTable<Long, LabelT>, PTable<Integer, Pair<Long, Double>>> run(
            final PTable<Long, Pair<Long, Double>> initialWeights,
            final PTable<Long, LabelT> initialLabels,
            final Double updateConstant,
            final int iterations) {
        assert initialWeights.getTypeFamily() != null;
        assert initialLabels.getTypeFamily() != null;
        assert updateConstant > 0;
        assert iterations >= 1;

        final PTable<Long, Pair<Long, Double>> weights = this
                .preprocessWeights(initialWeights, initialLabels,
                        updateConstant);

        PTable<Integer, Pair<Long, Double>> convergence = null;

        final ArrayList<PTable<Long, LabelT>> labels = new ArrayList<PTable<Long, LabelT>>(
                iterations + 1);

        labels.add(this.preprocessLabels(initialLabels));

        for (int i = 1; i <= iterations; i++) {
            final PTable<Long, Pair<Pair<Long, Double>, LabelT>> instances = Join
                    .leftJoin(weights, labels.get(i - 1));

            labels.add(this.computeLabels(instances));

            if (i % 5 == 0) {
                final PTable<Integer, Pair<Long, Double>> intermediateConvergence = this
                        .computeConvergence(labels.get(i), labels.get(i - 1),
                                i, 0.05);

                if (convergence == null) {
                    convergence = intermediateConvergence;
                } else {
                    convergence = convergence.union(intermediateConvergence);
                }
            }

            assert labels.size() - 1 == i;
        }

        return Pair.of(labels.get(iterations), convergence);
    }

    protected PTable<Long, LabelT> preprocessLabels(
            final PTable<Long, LabelT> initialLabels) {
        return initialLabels.mapKeys("MoveToShadowNode",
                new MapFn<Long, Long>() {

                    @Override
                    public Long map(Long node) {
                        assert node > 0;

                        return -node;
                    }
                }, Writables.longs());
    }

    protected PTable<Long, Pair<Long, Double>> preprocessWeights(
            final PTable<Long, Pair<Long, Double>> initialWeights,
            final PTable<Long, LabelT> initialLabels,
            final double updateConstant) {
        assert updateConstant > 0.0;

        final PTable<Long, Pair<Long, Double>> shadowWeights = initialLabels
                .parallelDo(
                        "ShadowEdges",
                        new DoFn<Pair<Long, LabelT>, Pair<Long, Pair<Long, Double>>>() {

                            @Override
                            public void process(
                                    Pair<Long, LabelT> instance,
                                    Emitter<Pair<Long, Pair<Long, Double>>> emitter) {
                                final Long node = instance.first();

                                // Edge from shadow node to seed node with updateConstant weight.
                                emitter.emit(Pair.of(-node,
                                        Pair.of(node, updateConstant)));

                                // Edge from shadow node to shadow node with unit weight.
                                emitter.emit(Pair.of(-node, Pair.of(-node, 1.0)));
                            }

                        }, Writables.tableOf(
                                Writables.longs(),
                                Writables.pairs(Writables.longs(),
                                        Writables.doubles())));

        return initialWeights.union(shadowWeights);
    }

    protected PTable<Long, LabelT> computeLabels(
            PTable<Long, Pair<Pair<Long, Double>, LabelT>> instances) {
        final AggregateLabelsOp<LabelT> aggregateLabels = new AggregateLabelsOp<LabelT>(
                this.labelClass, this.labelFactory);

        final PGroupedTable<Long, LabelT> incomingEdges = instances
                .parallelDo(
                        "PropagateLabels",
                        new DoFn<Pair<Long, Pair<Pair<Long, Double>, LabelT>>, Pair<Long, LabelT>>() {

                            @Override
                            public void process(
                                    Pair<Long, Pair<Pair<Long, Double>, LabelT>> instance,
                                    Emitter<Pair<Long, LabelT>> emitter) {
                                final Long source = instance.first();
                                final Pair<Pair<Long, Double>, LabelT> data = instance
                                        .second();
                                assert source != null && data != null;

                                final Pair<Long, Double> edge = data.first();
                                final LabelT label = data.second();
                                assert edge != null;

                                final long target = edge.first();
                                final double weight = edge.second();

                                if (label != null) {
                                    final Label influence = label
                                            .multiply(weight);

                                    emitter.emit(Pair.of(target,
                                            GraphAdsorption.this.labelClass
                                                    .cast(influence)));
                                }
                            }

                        },
                        Writables.tableOf(Writables.longs(),
                                Writables.records(this.labelClass)))
                .groupByKey();

        return aggregateLabels.run(incomingEdges);
    }

    protected PTable<Integer, Pair<Long, Double>> computeConvergence(
            final PTable<Long, LabelT> next,
            final PTable<Long, LabelT> previous, final int iteration,
            final double convergence_threshold) {
        assert next.getTypeFamily() != null;
        assert previous.getTypeFamily() != null;
        assert iteration >= 1;
        assert convergence_threshold >= 0.0
                && convergence_threshold <= Math.sqrt(2.0);

        final PTable<Long, Double> differenceNorms = Join
                .leftJoin(next, previous)
                .parallelDo(
                        "CompareIterations",
                        new MapFn<Pair<Long, Pair<LabelT, LabelT>>, Pair<Long, Double>>() {

                            @Override
                            public Pair<Long, Double> map(
                                    Pair<Long, Pair<LabelT, LabelT>> data) {
                                final Long node = data.first();

                                final Pair<LabelT, LabelT> labels = data
                                        .second();

                                final LabelT next = labels.first();
                                final LabelT previous = labels.second();

                                if (previous != null) {
                                    final double euclideanDistance = previous
                                            .diff(next).euclideanNorm();

                                    return Pair.of(node, euclideanDistance);
                                } else {
                                    // Longest possible distance.
                                    return Pair.of(node, Math.sqrt(2.0));
                                }
                            }

                        },
                        Writables.tableOf(Writables.longs(),
                                Writables.doubles()));

        return differenceNorms
                .filter(new FilterFn<Pair<Long, Double>>() {

                    @Override
                    public boolean accept(Pair<Long, Double> pair) {
                        return pair.second() > convergence_threshold;
                    }

                })
                .parallelDo(
                        new MapFn<Pair<Long, Double>, Pair<Integer, Pair<Long, Double>>>() {

                            @Override
                            public Pair<Integer, Pair<Long, Double>> map(
                                    Pair<Long, Double> pair) {
                                return Pair.of(iteration, pair);
                            }

                        },
                        Writables.tableOf(
                                Writables.ints(),
                                Writables.pairs(Writables.longs(),
                                        Writables.doubles())));
    }

}
