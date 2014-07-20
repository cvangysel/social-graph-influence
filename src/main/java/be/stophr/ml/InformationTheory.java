package be.stophr.ml;

/**
 * Information theory-based entropy measures.
 * 
 * @author Christophe Van Gysel
 */
public class InformationTheory {

    public static double entropy(String input) {
        long[] statistics = new long[36];

        for (char i : input.toLowerCase().toCharArray()) {
            if (i >= 48 && i <= 57) {
                statistics[i - 48]++;
            } else if (i >= 97 && i <= 122) {
                statistics[i - 97 + 10]++;
            }
        }

        return entropy(statistics);
    }

    public static double entropy(long[] input) {
        double sum = 0;
        for (long i : input) {
            sum += i;
        }

        final double[] probabilities = new double[input.length];
        for (int i = 0; i < probabilities.length; i++) {
            probabilities[i] = input[i] / sum;
        }

        return entropy(probabilities);
    }

    public static double entropy(double[] probabilities) {
        double entropy = 0.0;

        for (final double probability : probabilities) {
            assert probability >= 0.0;

            if (probability > 0.0) {
                entropy -= probability * (Math.log(probability) / Math.log(2));
            }
        }

        return entropy;
    }

}
