package be.stophr.ml.classification;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;

import junit.framework.Assert;
import junit.framework.TestCase;

import org.apache.crunch.PTable;
import org.apache.crunch.Pair;
import org.apache.crunch.types.writable.Writables;

import be.stophr.ml.classification.labels.MultiLabel;

public class ConvergenceTest extends TestCase {

    @SuppressWarnings("serial")
    public void testSelfLoops() {
        final MultiLabel.Factory factory = new MultiLabel.Factory(new Alphabet(
                "-1", "+1"));

        final Map<Long, MultiLabel> initialLabels = new LinkedHashMap<Long, MultiLabel>() {
            {
                put(4L, factory.create(1.0, 0.0));
                put(5L, factory.create(0.0, 1.0));
            }
        };

        final GraphAdsorption<MultiLabel> classifier = new GraphAdsorption<MultiLabel>(
                MultiLabel.class, factory);

        final PTable<Long, Pair<Long, Double>> nodes = Utils
                .CreatePTable(
                        Writables.tableOf(
                                Writables.longs(),
                                Writables.pairs(Writables.longs(),
                                        Writables.doubles())),
                        new LinkedList<Pair<Long, Pair<Long, Double>>>() {
                            {
                                add(Pair.of(4L, Pair.of(1L, 2.0)));
                                add(Pair.of(5L, Pair.of(2L, 1.0)));
                                add(Pair.of(2L, Pair.of(1L, 8.0)));
                                add(Pair.of(1L, Pair.of(3L, 1.0)));
                                add(Pair.of(3L, Pair.of(1L, 5000.0)));

                                // Adding self-loops solves the problem.
                                add(Pair.of(1L, Pair.of(1L, 1.0)));
                                add(Pair.of(2L, Pair.of(2L, 1.0)));
                                add(Pair.of(3L, Pair.of(3L, 1.0)));
                            }
                        });

        final Pair<PTable<Long, MultiLabel>, PTable<Integer, Pair<Long, Double>>> result = classifier
                .run(nodes, Utils.CreatePTable(
                        Writables.tableOf(Writables.longs(),
                                Writables.records(MultiLabel.class)),
                        initialLabels), 1.0, 30);

        final Map<Integer, Long> convergence = result.second().keys().count()
                .materializeToMap();

        Assert.assertTrue(!convergence.containsKey(10));
    }

    @SuppressWarnings("serial")
    public void testDev() {
        final MultiLabel.Factory factory = new MultiLabel.Factory(new Alphabet(
                "-1", "+1"));

        final Map<Long, MultiLabel> initialLabels = new LinkedHashMap<Long, MultiLabel>() {
            {
                put(4L, factory.create(1.0, 0.0));
                put(5L, factory.create(0.0, 1.0));
            }
        };

        final GraphAdsorption<MultiLabel> classifier = new GraphAdsorption<MultiLabel>(
                MultiLabel.class, factory);

        final PTable<Long, Pair<Long, Double>> nodes = Utils
                .CreatePTable(
                        Writables.tableOf(
                                Writables.longs(),
                                Writables.pairs(Writables.longs(),
                                        Writables.doubles())),
                        new LinkedList<Pair<Long, Pair<Long, Double>>>() {
                            {
                                add(Pair.of(4L, Pair.of(1L, Math.exp(2.0))));
                                add(Pair.of(5L, Pair.of(2L, Math.exp(1.0))));
                                add(Pair.of(2L, Pair.of(1L, Math.exp(8.0))));
                                add(Pair.of(1L, Pair.of(3L, Math.exp(50.0))));
                                add(Pair.of(3L, Pair.of(1L, Math.exp(50.0))));

                                // Adding self-loops solves the problem.
                                add(Pair.of(1L, Pair.of(1L, Math.exp(1.0))));
                                add(Pair.of(2L, Pair.of(2L, 1.0)));
                                add(Pair.of(3L, Pair.of(3L, Math.exp(99.0))));
                            }
                        });

        final Pair<PTable<Long, MultiLabel>, PTable<Integer, Pair<Long, Double>>> result = classifier
                .run(nodes, Utils.CreatePTable(
                        Writables.tableOf(Writables.longs(),
                                Writables.records(MultiLabel.class)),
                        initialLabels), 1.0, 30);

        final Map<Integer, Long> convergence = result.second().keys().count()
                .materializeToMap();

        Assert.assertTrue(!convergence.containsKey(10));
    }

}
