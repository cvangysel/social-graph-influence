package be.stophr.ml.classification;

import java.io.Serializable;

import com.google.common.collect.HashBiMap;

/**
 * An alphabet provides a mapping between strings and tokens.
 * 
 * It is commonly used as a mapping between internal label identifiers
 * and human-readable representations.
 * 
 * @author Christophe Van Gysel
 */
@SuppressWarnings("serial")
public class Alphabet implements Serializable {

    protected final HashBiMap<Long, String> map = HashBiMap.create();
    protected long nextIndex = 0;

    protected boolean locked = false;

    public Alphabet() {
    }

    public Alphabet(String... tokens) {
        for (final String token : tokens) {
            this.add(token);
        }

        this.finalize();
    }

    public long add(final String token) {
        assert !this.locked;
        assert !this.map.containsValue(token);

        return this.get(token);
    }

    public long get(final String token) {
        if (this.map.containsValue(token)) {
            return this.map.inverse().get(token);
        } else {
            assert !this.locked;

            final long index = this.nextIndex;

            this.map.put(index, token);
            this.nextIndex = Math.max(this.nextIndex + 1, this.map.size());

            return index;
        }
    }

    public void finalize() {
        this.locked = true;
    }

    public String getToken(final long index) {
        return this.map.get(index);
    }

    public int size() {
        return this.map.size();
    }

    public boolean isLocked() {
        return this.locked;
    }

    @Override
    public String toString() {
        return this.map.toString();
    }

}
