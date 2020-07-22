package mcib3d.tapas.core;

import ij.IJ;

import javax.xml.bind.annotation.XmlType;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;

public class TapasDocumentation {
    HashMap<String, String> docs;
    private static final String DEFAULT = "Documentation not provided.";

    public TapasDocumentation() {
        docs = new HashMap<>();
    }

    public void setDocumentation(String className, String doc) {
        docs.put(className, doc);
    }

    public String getDocumentation(String className) {
        String doc = docs.get(className);
        if (doc == null) return DEFAULT;
        else return doc;
    }

    public void loadDocumentation(String fileName) {
        String data1, data2;
        String currentClass = null;
        String currentDoc = null;
        IJ.log("Reading tapas documentation file " + fileName);
        try {
            BufferedReader reader = new BufferedReader(new FileReader(fileName));
            String line = reader.readLine();
            int c = 0;
            while (line != null) {
                c++;
                int idx = line.indexOf("//");
                if ((idx < 0) && (!line.isEmpty())) { // strange error on linux ??
                    String info[] = line.split(":");
                    if (info.length == 2) {
                        data1 = info[0].trim(); // class
                        data2 = info[1].trim(); // className
                        if (data1.equalsIgnoreCase("class")) {
                            // update previous doc
                            if (currentClass != null) {
                                if (currentDoc != null) {
                                    setDocumentation(currentClass, currentDoc);
                                    //imagej sIJ.log("Updated doc for "+currentClass+" : "+currentDoc);
                                }
                            }
                            currentClass = data2;
                            currentDoc = null;
                        }
                    } else { // documentation line
                        if (currentClass != null) {
                            if (currentDoc != null) {
                                currentDoc = currentDoc.concat("\n" + line);
                            } else currentDoc = line;
                        }
                    }
                }
                line = reader.readLine();
            }
            // update last class
            if (currentClass != null) {
                if (currentDoc != null) {
                    setDocumentation(currentClass, currentDoc);
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
