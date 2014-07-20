package be.stophr.ml.classification;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.Normalizer;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import org.apache.crunch.PCollection;
import org.apache.crunch.PTable;
import org.apache.crunch.Pair;
import org.apache.crunch.impl.mem.MemPipeline;
import org.apache.crunch.types.PTableType;
import org.apache.crunch.types.PType;

/**
 * Utility classes that provide helper functions for Crunch-oriented
 * operations, string clean-up and simple file parsing.
 * 
 * @author Christophe Van Gysel
 */
public class Utils {

    public static <T> PCollection<T> CreatePCollection(
            final Iterable<T> iterable) {
        return CreatePCollection(null, iterable);
    }

    public static <T> PCollection<T> CreatePCollection(final PType<T> ptype,
            final Iterable<T> iterable) {
        return MemPipeline.typedCollectionOf(ptype, iterable);
    }

    public static <K, V> PTable<K, V> CreatePTable(final Map<K, V> map) {
        return CreatePTable(null, map);
    }

    public static <K, V> PTable<K, V> CreatePTable(
            final PTableType<K, V> ptype, final Map<K, V> map) {
        List<Pair<K, V>> pairs = new LinkedList<Pair<K, V>>();

        for (Entry<K, V> entry : map.entrySet()) {
            pairs.add(Pair.of(entry.getKey(), entry.getValue()));
        }

        return MemPipeline.typedTableOf(ptype, pairs);
    }

    public static <K, V> PTable<K, V> CreatePTable(
            final PTableType<K, V> ptype, final Iterable<Pair<K, V>> list) {
        return MemPipeline.typedTableOf(ptype, list);
    }

    public static Map<String, String> readStringTable(final InputStream stream)
            throws IOException {
        final BufferedReader reader = new BufferedReader(new InputStreamReader(
                stream));

        final Map<String, String> data = new TreeMap<String, String>();

        String line;
        while ((line = reader.readLine()) != null) {
            String[] values = line.split("\t");

            assert values.length >= 2;

            data.put(values[0], values[1]);
        }

        reader.close();

        return data;
    }

    public static String sanitizeString(final String str) {
        return Normalizer.normalize(str, Normalizer.Form.NFD)
                .replaceAll("[^\\p{ASCII}]", "")
                .replaceAll("[\\t\\n\\r\\f\\v]", " ");
    }

    public static String normalizeString(final String str) {
        if (str == null) {
            return "";
        }

        return sanitizeString(str).replaceAll("[^a-zA-Z ]", "")
                .replaceAll("\\s{2,}", " ").toLowerCase();
    }

}
