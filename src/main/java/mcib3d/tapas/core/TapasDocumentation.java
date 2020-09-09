package mcib3d.tapas.core;

import ij.IJ;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class TapasDocumentation {
    HashMap<String, List<String>> categories;
    HashMap<String, String> docs; // class name and documentation text
    private static final String DEFAULT = "Documentation not provided.";

    public TapasDocumentation() {
        docs = new HashMap<>();
        categories = new HashMap<>();
        categories.put("Misc.",new ArrayList<>());
    }

    public void setDocumentation(String className, String doc) {
        docs.put(className, doc);
    }

    public String getDocumentation(String className) {
        String doc = docs.get(className);
        if (doc == null) return DEFAULT;
        else return doc;
    }

    public void printCategories(){
        for(String category:categories.keySet()){
            System.out.println("CATEGORY "+category);
            for(String module:categories.get(category)){
                System.out.println(module);
            }
        }
    }

    public void loadDocumentation(String fileName) {
        String data1, data2;
        String currentClass = null;
        String currentDoc = null;
        String currentCategory = "Misc.";

        IJ.log("Reading tapas documentation file " + fileName);
        try {
            BufferedReader reader = new BufferedReader(new FileReader(fileName));
            String line = reader.readLine();
            while (line != null) {
                int idx = line.indexOf("//");
                if ((idx < 0) && (!line.isEmpty())) { // strange error on linux ??
                    String[] info = line.split(":");
                    if (info.length == 2) {
                        data1 = info[0].trim(); // class: or category:
                        data2 = info[1].trim(); // doc or category name
                        if (data1.equalsIgnoreCase("class")) { // class + doc
                            // update previous doc
                            if (currentClass != null) {
                                if (currentDoc != null) {
                                    setDocumentation(currentClass, currentDoc);
                                    categories.get(currentCategory).add(currentClass);
                                }
                            }
                            currentClass = data2;
                            currentDoc = null;
                        }
                        else if(data1.equalsIgnoreCase("category")){ // category
                            String category = data2;
                            if(!categories.containsKey(category)){
                                categories.put(category,new ArrayList<>());
                                currentCategory = category;
                            }
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
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
