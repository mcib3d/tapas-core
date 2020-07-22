package mcib3d.tapas.core;

import java.util.HashMap;

public class KeyValues {
    private String key;
    private HashMap<ImageInfo,String> pairs;

    public KeyValues(String key) {
        this.key = key;
        pairs = new HashMap<>();
    }

    public void addKeyValue(ImageInfo info,String value){
        pairs.put(info,value);
    }

    public String getValue(ImageInfo info){
        return pairs.get(info);
    }

    public String getKey() {
        return key;
    }
}
