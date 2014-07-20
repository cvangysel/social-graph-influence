package be.stophr.ml.classification;

import junit.framework.Assert;
import junit.framework.TestCase;
import be.stophr.ml.crunch.SanitizeStringFn;

public class UtilsTest extends TestCase {

    public void testNonASCII() {
        final SanitizeStringFn fn = new SanitizeStringFn();

        Assert.assertEquals("This is a funky String",
                fn.map("Tĥïŝ ĩš â fůňķŷ Šťŕĭńġ"));
    }

    public void testTab() {
        final SanitizeStringFn fn = new SanitizeStringFn();

        Assert.assertEquals("This is a funky String",
                fn.map("This is a\tfunky\tString"));
    }

    public void testNewline() {
        final SanitizeStringFn fn = new SanitizeStringFn();

        Assert.assertEquals("This is a funky String. This one too.",
                fn.map("This is a funky String.\nThis one too."));
    }

    public void testNormalizatin() {
        Assert.assertEquals("cd v", Utils.normalizeString("CD & V"));
        Assert.assertEquals("nva", Utils.normalizeString("N-VA"));
        Assert.assertEquals("spa", Utils.normalizeString("SP.a"));
        Assert.assertEquals("groen", Utils.normalizeString("Groen!"));
    }
}
