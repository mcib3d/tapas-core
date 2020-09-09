package mcib3d.tapas.core;

import ij.IJ;

import java.io.*;
import java.util.HashMap;

public class KeyValueManager {
    private static final HashMap<String, KeyValues> keys = new HashMap<>();
    private static final String DIR_KEY = "keyValuePairs";

    public static String getKeyValue(ImageInfo info, String key) {
        if (keys.containsKey(key)) {
            return keys.get(key).getValue(info);
        } else {
            File file = new File(info.getDatasetPath() + DIR_KEY + File.separator + "KEY_" + key + ".txt");
            readKeyValues(info, file, key, true);

        }
        return keys.get(key).getValue(info);
    }

    public static void addKeyValue(ImageInfo info, String key, String value) {
        if (!keys.containsKey(key)) {
            KeyValues keyValue = new KeyValues(key);
            keys.put(key, keyValue);
        }
        keys.get(key).addKeyValue(info, value);
    }

    public static void readKeyValues(ImageInfo info, File file, String key, boolean verbose) {
        IJ.log("Reading key file " + file);
        final String root = info.getRootDir();
        final String project = info.getProject();
        final String dataset = info.getDataset();
        try {
            BufferedReader reader = new BufferedReader(new FileReader(file));
            String line = reader.readLine();
            int c = 0;
            while (line != null) {
                c++;
                int idx = line.indexOf("//");
                if ((idx < 0) && (!line.isEmpty())) { // strange error on linux ??
                    String[] data = line.split(":");
                    if (data.length != 2) {
                        IJ.log("Pb for key with line " + c + ":" + line + " ");
                    }
                    // convert to ImageInfo
                    ImageInfo info2 = new ImageInfo(root, project, dataset, data[0].trim());
                    addKeyValue(info2, key, data[1].trim());
                    if (verbose) IJ.log("Found key " + key + " for " + info2 + " : " + data[1].trim());
                }
                line = reader.readLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
