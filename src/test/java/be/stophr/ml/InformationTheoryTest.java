package be.stophr.ml;

import be.stophr.ml.InformationTheory;
import junit.framework.Assert;
import junit.framework.TestCase;

public class InformationTheoryTest extends TestCase {

    public void testEntropy() {
        // A Bernoulli random variable where P(X = 0) = P(X = 1), i.e. uniformly
        // distributed.
        // The actual count of occurrences should not influence the entropy of
        // X, it should always
        // be one bit of entropy, as the variable is not biased towards any
        // class and we just don't
        // know anything.
        Assert.assertEquals(1.0, InformationTheory.entropy(new long[] { 5, 5 }));
        Assert.assertEquals(1.0,
                InformationTheory.entropy(new long[] { 15, 15 }));

        // When one class has probability 1.0 then there is no uncertainty about
        // the random variable,
        // thus the variable has zero bits of entropy.
        Assert.assertEquals(0.0,
                InformationTheory.entropy(new long[] { 5, 0, 0 }));

        // Given a random variable X, with P(X = 0) = 0.25 and P(X = 1) = 0.75.
        // The entropy of X will be less
        // than 1.0 as that is the case with the most uncertainty. The entropy
        // is 0.81127812445.
        Assert.assertEquals(0.8112781244591328,
                InformationTheory.entropy(new long[] { 1, 3 }));

        // Tests with strings.
        Assert.assertEquals(0.0, InformationTheory.entropy("aaaAAAaaaAaAaaAA"));
        Assert.assertEquals(1.0, InformationTheory.entropy("abababab"));
        Assert.assertEquals(1.584962500721156,
                InformationTheory.entropy("aBCAbc"));
        Assert.assertEquals(0.8112781244591328,
                InformationTheory.entropy("AbBb"));
    }

}
