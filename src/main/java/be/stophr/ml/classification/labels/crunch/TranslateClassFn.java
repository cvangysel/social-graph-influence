package be.stophr.ml.classification.labels.crunch;

import org.apache.crunch.MapFn;

import be.stophr.ml.classification.Alphabet;

/**
 * Apache Crunch function that maps token to their human-readable
 * representation.
 * 
 * @author Christophe Van Gysel
 * 
 */
@SuppressWarnings("serial")
public class TranslateClassFn extends MapFn<Long, String> {

    protected final Alphabet alphabet;

    public TranslateClassFn(final Alphabet alphabet) {
        this.alphabet = alphabet;
    }

    @Override
    public String map(Long index) {
        final String token = this.alphabet.getToken(index);

        return token == null ? "null" : token;
    }

}
