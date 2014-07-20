package be.stophr.ml.classification.labels.crunch;

import org.apache.crunch.MapFn;

import be.stophr.ml.classification.Alphabet;
import be.stophr.ml.classification.labels.Label;

/**
 * Apache Crunch function that translates Label instances to their
 * human-readable representation, given an Alphabet instance.
 * 
 * @author Christophe Van Gysel
 */
@SuppressWarnings("serial")
public class HumanReadableLabelFn<LabelT extends Label> extends
        MapFn<LabelT, String> {

    protected final Alphabet alphabet;

    public HumanReadableLabelFn(final Alphabet alphabet) {
        this.alphabet = alphabet;
    }

    @Override
    public String map(LabelT label) {
        return label.toString(this.alphabet);
    }

}
