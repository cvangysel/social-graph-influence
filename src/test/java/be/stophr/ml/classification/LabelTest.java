package be.stophr.ml.classification;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInput;
import java.io.DataInputStream;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;

import junit.framework.Assert;
import junit.framework.TestCase;
import be.stophr.ml.classification.labels.Label;
import be.stophr.ml.classification.labels.MultiLabel;

public class LabelTest extends TestCase {

    public void testMultiLabelWritable() {
        final MultiLabel.Factory factory = new MultiLabel.Factory(8);
        final MultiLabel expected = factory.create(1.0, 2.0, 3.0, 4.0, 5.0,
                6.0, 7.0, 8.0);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        DataOutput output = new DataOutputStream(outputStream);

        try {
            expected.write(output);
        } catch (IOException e) {
            e.printStackTrace();
        }

        InputStream inputStream = new ByteArrayInputStream(
                outputStream.toByteArray());
        DataInput input = new DataInputStream(inputStream);

        MultiLabel actual = new MultiLabel();

        try {
            actual.readFields(input);
        } catch (IOException e) {
            e.printStackTrace();
        }

        Assert.assertEquals(expected, actual);
    }

    public void testMultiLabelMultiply() {
        final MultiLabel.Factory factory = new MultiLabel.Factory(8);

        final Label actual = factory.create(1.0, 2.0, 3.0, 4.0, 5.0, 6.0, 7.0,
                8.0).multiply(3.0);

        final MultiLabel expected = factory.create(3.0, 6.0, 9.0, 12.0, 15.0,
                18.0, 21.0, 24.0);

        Assert.assertEquals(expected, actual);
    }

    public void testMultiLabelAdd() {
        final MultiLabel.Factory factory = new MultiLabel.Factory(8);

        final Label actual = factory.create(1.0, 2.0, 3.0, 4.0, 5.0, 6.0, 7.0,
                8.0)
                .add(factory.create(0.1, 0.2, 0.3, 0.4, 0.5, 0.6, 0.7, 0.8));

        final MultiLabel expected = factory.create(1.1, 2.2, 3.3, 4.4, 5.5,
                6.6, 7.7, 8.8);

        Assert.assertEquals(expected, actual);
    }

    public void testMultiLabelDiff() {
        final MultiLabel.Factory factory = new MultiLabel.Factory(8);

        final Label actual = factory.create(1.0, 2.0, 3.0, 4.0, 5.0, 6.0, 7.0,
                8.0).diff(
                factory.create(0.1, 0.2, 0.3, 0.4, 0.5, 0.6, 0.7, 0.8));

        final MultiLabel expected = factory.create(0.9, 1.8, 2.7, 3.6, 4.5,
                5.4, 6.3, 7.2);

        Assert.assertEquals(expected, actual);
    }

}
