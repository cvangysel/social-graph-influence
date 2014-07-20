package be.stophr.ml.classification.crunch;

import java.io.Serializable;

import org.apache.crunch.Aggregator;
import org.apache.crunch.MapFn;
import org.apache.crunch.PGroupedTable;
import org.apache.crunch.PTable;
import org.apache.crunch.types.writable.Writables;
import org.apache.hadoop.conf.Configuration;

import be.stophr.ml.classification.labels.Label;
import be.stophr.ml.classification.labels.Label.NormType;

import com.google.common.collect.ImmutableList;

/**
 * Apache Crunch operation which aggregates (average) a large number of label
 * instances.
 * 
 * It uses Kahan summation (compensated summation) as an attempt to have some
 * cheap performance gains in terms of floating point precision.
 * 
 * @author Christophe Van Gysel
 */
@SuppressWarnings("serial")
public class AggregateLabelsOp<LabelT extends Label> implements Serializable {

    protected final Class<LabelT> labelClass;
    protected final Label.Factory labelFactory;

    public AggregateLabelsOp(final Class<LabelT> labelClass,
            final Label.Factory labelFactory) {
        this.labelClass = labelClass;
        this.labelFactory = labelFactory;
    }

    public PTable<Long, LabelT> run(final PGroupedTable<Long, LabelT> table) {
        return table
        // Implements the Kahan summation algorithm.
                .combineValues(new Aggregator<LabelT>() {

                    protected Label sum = null;
                    protected Label c = null;

                    public void initialize(Configuration arg0) {
                    }

                    public void reset() {
                        this.sum = labelFactory.create();
                        this.c = labelFactory.create();
                    }

                    public void update(LabelT other) {
                        final Label y = other.diff(this.c);
                        final Label t = this.sum.add(y);

                        this.c = t.diff(this.sum).diff(y);

                        this.sum = t;
                    }

                    @SuppressWarnings("unchecked")
                    public Iterable<LabelT> results() {
                        return (Iterable<LabelT>) ImmutableList.of(this.sum);
                    }

                }).mapValues("NormalizeLabels", new MapFn<LabelT, LabelT>() {

                    @Override
                    public LabelT map(LabelT label) {
                        return AggregateLabelsOp.this.labelClass.cast(label
                                .normalize(NormType.MANHATTAN));
                    }
                }, Writables.records(this.labelClass));
    }

}
