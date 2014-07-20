package be.stophr.ml.classification.labels;

import java.io.Serializable;

import org.apache.hadoop.io.Writable;

import be.stophr.ml.classification.Alphabet;

/**
 * Abstract interface representing a vector of labels. Implementations of this
 * interface should be default constructible.
 * 
 * @author Christophe Van Gysel
 */
public interface Label extends Serializable, Writable {

    public static enum NormType {
        MANHATTAN, EUCLIDEAN;
    }

    public static interface Factory extends Serializable {

        public Label createSingleton(final long index);

        public Label create();

        public Class<? extends Label> getLabelClass();

    }

    public Label multiply(final double constant);

    public Label add(final Label other);

    public Label diff(final Label other);

    public double manhattanNorm();

    public double euclideanNorm();

    public double entropy();

    public Label normalize(final NormType type);

    public int maxArg();

    public String toString();

    public String toString(Alphabet alphabet);

}
