package be.stophr.ml.crunch;

import org.apache.crunch.MapFn;

import be.stophr.ml.classification.Utils;

@SuppressWarnings("serial")
public class SanitizeStringFn extends MapFn<String, String> {

    @Override
    public String map(String str) {
    return Utils.sanitizeString(str);
    }

}
