package be.stophr.ml.classification;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;

import junit.framework.Assert;
import junit.framework.TestCase;

import org.apache.crunch.PTable;
import org.apache.crunch.Pair;
import org.apache.crunch.types.writable.Writables;

import be.stophr.ml.classification.labels.MultiLabel;

@SuppressWarnings("serial")
public class GraphAdsorptionTest extends TestCase {

    public void testPGraphClassificationOp() {
        final MultiLabel.Factory factory = new MultiLabel.Factory(new Alphabet(
                "-1", "+1"));

        final PTable<Long, Pair<Pair<Long, Double>, MultiLabel>> instances = Utils
                .CreatePTable(
                        Writables.tableOf(Writables.longs(), Writables.pairs(
                                Writables.pairs(Writables.longs(),
                                        Writables.doubles()),
                                Writables.records(MultiLabel.class))),
                        new LinkedList<Pair<Long, Pair<Pair<Long, Double>, MultiLabel>>>() {
                            {
                                add(Pair.of(
                                        1L,
                                        Pair.of(Pair.of(2L, 0.20),
                                                factory.create(0.0, 1.0))));
                                add(Pair.of(
                                        1L,
                                        Pair.of(Pair.of(3L, 0.05),
                                                factory.create(0.0, 1.0))));
                                add(Pair.of(
                                        2L,
                                        Pair.of(Pair.of(3L, 0.10),
                                                factory.create(1.0, 0.0))));
                                add(Pair.of(3L, Pair.of(Pair.of(1L, 0.20),
                                        MultiLabel.class.cast(null))));
                            }
                        });

        GraphAdsorption<MultiLabel> classificationOp = new GraphAdsorption<MultiLabel>(
                MultiLabel.class, factory);

        PTable<Long, MultiLabel> iteration = classificationOp
                .computeLabels(instances);

        Map<Long, MultiLabel> expected = new HashMap<Long, MultiLabel>();
        expected.put(2L, factory.create(0.0, 1.0));
        expected.put(3L, factory.create(2 / 3.0, 1 / 3.0));

        Map<Long, MultiLabel> actual = iteration.materializeToMap();

        Assert.assertEquals(expected, actual);
    }

    public void testChangingSeeds() {
        final MultiLabel.Factory factory = new MultiLabel.Factory(new Alphabet(
                "-1", "+1"));

        final Map<Long, MultiLabel> initialLabels = new LinkedHashMap<Long, MultiLabel>() {
            {
                put(1L, factory.create(0.0, 1.0));
                put(2L, factory.create(1.0, 0.0));
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
                                add(Pair.of(1L, Pair.of(2L, 0.20)));
                                add(Pair.of(1L, Pair.of(3L, 0.05)));
                                add(Pair.of(2L, Pair.of(3L, 0.10)));
                                add(Pair.of(3L, Pair.of(1L, 0.20)));
                            }
                        });

        final Pair<PTable<Long, MultiLabel>, PTable<Integer, Pair<Long, Double>>> result = classifier
                .run(nodes, Utils.CreatePTable(
                        Writables.tableOf(Writables.longs(),
                                Writables.records(MultiLabel.class)),
                        initialLabels), 1.0, 30);

        final Map<Long, MultiLabel> actual = result.first().materializeToMap();
        final PTable<Integer, Pair<Long, Double>> convergence = result.second();

        Map<Long, MultiLabel> expected = new HashMap<Long, MultiLabel>() {
            {
                put(-1L, factory.create(0.0, 1.0));
                put(1L, factory.create(0.09999999999999855, 0.9000000000000014));
                put(-2L, factory.create(1.0, 0.0));
                put(2L, factory.create(0.8499999999999993, 0.15000000000000074));
                put(3L, factory.create(0.5999999999999971, 0.40000000000000290));
            }
        };

        Assert.assertEquals(expected, actual);
        Assert.assertTrue(convergence.materializeToMap().isEmpty());
    }

    public void testFixedSeeds() {
        final MultiLabel.Factory factory = new MultiLabel.Factory(new Alphabet(
                "-1", "+1"));

        final Map<Long, MultiLabel> initialLabels = new LinkedHashMap<Long, MultiLabel>() {
            {
                put(1L, factory.create(1.0, 0.0));
                put(2L, factory.create(0.0, 1.0));
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
                                add(Pair.of(1L, Pair.of(3L, 0.20)));
                                add(Pair.of(1L, Pair.of(5L, 0.80)));
                                add(Pair.of(2L, Pair.of(4L, 2.00)));
                                add(Pair.of(3L, Pair.of(3L, 0.50)));
                                add(Pair.of(3L, Pair.of(4L, 5.00)));
                                add(Pair.of(4L, Pair.of(5L, 1.00)));
                                add(Pair.of(5L, Pair.of(3L, 1.00)));
                                add(Pair.of(5L, Pair.of(4L, 1.00)));
                                add(Pair.of(5L, Pair.of(5L, 2.00)));
                            }
                        });

        final Pair<PTable<Long, MultiLabel>, PTable<Integer, Pair<Long, Double>>> result = classifier
                .run(nodes, Utils.CreatePTable(
                        Writables.tableOf(Writables.longs(),
                                Writables.records(MultiLabel.class)),
                        initialLabels), 1.0, 30);

        final Map<Long, MultiLabel> actual = result.first().materializeToMap();
        final PTable<Integer, Pair<Long, Double>> convergence = result.second();

        Map<Long, MultiLabel> expected = new HashMap<Long, MultiLabel>() {
            {
                put(-1L, factory.create(1.0, 0.0));
                put(1L, factory.create(1.0, 0.0));
                put(-2L, factory.create(0.0, 1.0));
                put(2L, factory.create(0.0, 1.0));
                put(3L, factory.create(0.8196195429698212, 0.18038045703017877));
                put(4L, factory.create(0.6102223269617368, 0.38977767303826316));
                put(5L, factory.create(0.7835017256110393, 0.21649827438896080));
            }
        };

        Assert.assertEquals(expected, actual);
        Assert.assertTrue(!convergence.materializeToMap().containsKey(10));
    }

}
