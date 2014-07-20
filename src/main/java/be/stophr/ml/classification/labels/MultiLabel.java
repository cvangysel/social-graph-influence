package be.stophr.ml.classification.labels;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.Arrays;

import be.stophr.ml.InformationTheory;
import be.stophr.ml.classification.Alphabet;

/**
 * Multilabel implementation of the Label interface. Mathematically instances of
 * this class can be seen as vectors.
 * 
 * @author Christophe Van Gysel
 */
@SuppressWarnings("serial")
public class MultiLabel implements Label {

    public static class Factory implements Label.Factory {

        final int numClasses;
        final Alphabet alphabet;

        public Factory(final int numClasses) {
            this.numClasses = numClasses;
            this.alphabet = null;
        }

        public Factory(final Alphabet alphabet) {
            assert alphabet.isLocked();

            this.numClasses = alphabet.size();
            this.alphabet = alphabet;
        }

        @Override
        public Label create() {
            return new MultiLabel(this.numClasses);
        }

        public MultiLabel create(final double... classes) {
            assert classes.length == this.numClasses;

            return new MultiLabel(classes);
        }

        public Label createSingleton(final long index) {
            assert index >= 0
                    && index <= Math.min(Integer.MAX_VALUE, this.numClasses);

            final double[] classes = new double[this.numClasses];
            classes[(int) index] = 1.0;

            return new MultiLabel(classes);
        }

        @Override
        public Class<? extends Label> getLabelClass() {
            return MultiLabel.class;
        }

    }

    protected double[] classes;

    public MultiLabel() {
    }

    private MultiLabel(final int numClasses) {
        assert numClasses >= 2;

        this.classes = new double[numClasses];
    }

    public MultiLabel(MultiLabel label) {
        assert label != null && label.classes != null;

        this.classes = label.classes;
    }

    protected MultiLabel(final double... classes) {
        assert classes != null;

        this.classes = classes;
    }

    public Label multiply(double constant) {
        assert this.classes != null;

        final double[] classes = new double[this.classes.length];

        for (int i = 0; i < this.classes.length; i++) {
            classes[i] = this.classes[i] * constant;
        }

        return new MultiLabel(classes);
    }

    public Label add(Label label) {
        assert label instanceof MultiLabel;

        MultiLabel other = (MultiLabel) label;

        assert this.classes != null;
        assert other.classes != null;

        assert this.classes.length == other.classes.length;

        final double[] classes = new double[this.classes.length];

        for (int i = 0; i < this.classes.length; i++) {
            classes[i] = this.classes[i] + other.classes[i];
        }

        return new MultiLabel(classes);
    }

    @Override
    public Label diff(Label label) {
        assert label instanceof MultiLabel;

        MultiLabel other = (MultiLabel) label;

        assert this.classes != null;
        assert other.classes != null;

        assert this.classes.length == other.classes.length;

        final double[] classes = new double[this.classes.length];

        for (int i = 0; i < this.classes.length; i++) {
            classes[i] = this.classes[i] - other.classes[i];
        }

        return new MultiLabel(classes);
    }

    @Override
    public double manhattanNorm() {
        double sum = 0.0;

        for (final double clazz : this.classes) {
            sum += clazz;
        }

        return sum;
    }

    @Override
    public double euclideanNorm() {
        double sumOfSquares = 0.0;

        for (final double clazz : this.classes) {
            sumOfSquares += clazz * clazz;
        }

        return Math.sqrt(sumOfSquares);
    }

    @Override
    public double entropy() {
        return InformationTheory.entropy(this.classes);
    }

    @Override
    public Label normalize(final NormType type) {
        assert this.classes != null;

        final double norm = (type == NormType.MANHATTAN) ? this.manhattanNorm()
                : this.euclideanNorm();
        final double[] classes = new double[this.classes.length];

        if (norm > 0.0) {
            for (int i = 0; i < this.classes.length; i++) {
                classes[i] = this.classes[i] / norm;
            }

            return new MultiLabel(classes);
        } else {
            return null;
        }
    }

    @Override
    public int maxArg() {
        int index = -1;
        double max = Double.MIN_VALUE;

        for (int i = 0; i < this.classes.length; i++) {
            if (this.classes[i] > Math.max(max, 0.0)) {
                index = i;
                max = this.classes[i];
            }
        }

        return index;
    }

    public void readFields(DataInput input) throws IOException {
        final int size = input.readInt();

        this.classes = new double[size];

        for (int i = 0; i < size; i++) {
            this.classes[i] = input.readDouble();
        }
    }

    public void write(DataOutput output) throws IOException {
        if (this.classes == null) {
            output.writeInt(0);
        } else {
            output.writeInt(this.classes.length);

            for (final double label : this.classes) {
                output.writeDouble(label);
            }
        }
    }

    @Override
    public String toString() {
        return this.toString(null);
    }

    @Override
    public String toString(Alphabet alphabet) {
        final StringBuilder builder = new StringBuilder();
        builder.append("(");

        if (this.classes != null) {
            int i = 0;

            for (final double label : this.classes) {
                if (alphabet != null) {
                    builder.append(alphabet.getToken(i));
                    builder.append(": ");
                }

                builder.append(String.format("%.25f", label));
                builder.append(", ");

                i++;
            }

            // Rollback last two characters.
            builder.setLength(builder.length() - 2);
        }

        builder.append(") = ");

        if (alphabet != null) {
            builder.append(alphabet.getToken(this.maxArg()));
        } else {
            builder.append(this.maxArg());
        }

        return builder.toString();
    }

    @Override
    public boolean equals(Object other) {
        if (other instanceof MultiLabel) {
            final MultiLabel otherLabel = (MultiLabel) other;

            return Arrays.equals(this.classes, otherLabel.classes);
        } else {
            return false;
        }
    }

}
