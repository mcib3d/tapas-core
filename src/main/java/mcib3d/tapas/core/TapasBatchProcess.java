package mcib3d.tapas.core;


import ij.IJ;
import ij.ImagePlus;
import mcib3d.image3d.ImageInt;
import mcib3d.tapas.TapasProcessing;
import omero.gateway.exception.DSAccessException;
import omero.gateway.exception.DSOutOfServiceException;
import omero.gateway.model.DatasetData;
import omero.gateway.model.ImageData;
import omero.gateway.model.ProjectData;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ExecutionException;

public class TapasBatchProcess {
    // list of global links (tapas variables), pb if many instances of this class ?
    private static HashMap<String, ImageInfo> links;
    private File process;
    private ArrayList<ImageInfo> allImages;
    private ArrayList<TapasProcessing> processings;
    private HashMap<String, String> plugins;
    // additional connection information
    private ArrayList<Long> addUsers;
    private ArrayList<Long> addGroups; // not used

    public static String version = "0.5";

    public TapasBatchProcess() {
        addUsers = new ArrayList<>();
        links = new HashMap<>();
    }

    public static String analyseFileName(String s, ImageInfo info) {
        String file = s;
        if ((s == null) || (s.isEmpty())) return s;
        if (file.contains("?project?")) {
            file = file.replace("?project?", info.getProject());
            return analyseFileName(file, info);
        }
        if (file.contains("?dataset?")) {
            file = file.replace("?dataset?", info.getDataset());
            return analyseFileName(file, info);
        }
        if (file.contains("?name?")) {
            file = file.replace("?name?", info.getName());
            return analyseFileName(file, info);
        }
        if (file.contains("?channel?")) {
            file = file.replace("?channel?", "" + info.getC());
            return analyseFileName(file, info);
        }
        if (file.contains("?channel+1?")) {
            file = file.replace("?channel+1?", "" + (info.getC() + 1));
            return analyseFileName(file, info);
        }
        if (file.contains("?channel-1?")) {
            file = file.replace("?channel-1?", "" + (info.getC() - 1));
            return analyseFileName(file, info);
        }
        if (file.contains("?frame?")) {
            file = file.replace("?frame?", "" + info.getT());
            return analyseFileName(file, info);
        }
        if (file.contains("?frame+1?")) {
            file = file.replace("?frame+1?", "" + (info.getT() + 1));
            return analyseFileName(file, info);
        }
        if (file.contains("?frame-1?")) {
            file = file.replace("?frame-1?", "" + (info.getT() - 1));
            return analyseFileName(file, info);
        }

        return file;
    }

    public static String analyseStringKeywords(String s, ImageInfo info) {
        // first analyse names
        String res = analyseFileName(s, info);
        // system directories
        String home = System.getProperty("user.home");
        String ij = IJ.getDirectory("imagej");
        String tmp = System.getProperty("java.io.tmpdir");
        // we want these dir to NOT ends with /
        if (home.endsWith(File.separator)) home = home.substring(0, home.length() - 1);
        if (ij.endsWith(File.separator)) ij = ij.substring(0, ij.length() - 1);
        if (tmp.endsWith(File.separator)) tmp = tmp.substring(0, tmp.length() - 1);
        // analyse dir names
        res = res.replace("?home?", home);
        res = res.replace("?ij?", ij);
        res = res.replace("?tmp?", tmp);

        return res;
    }

    public static String analyseDirName(String s) {
        if (s == null) return s;
        if (s.isEmpty()) return s;
        String dir = new String(s);
        String home = System.getProperty("user.home");
        String ij = IJ.getDirectory("imagej");
        String tmp = System.getProperty("java.io.tmpdir");
        // we want these dir to NOT ends with /
        if (home.endsWith(File.separator)) home = home.substring(0, home.length() - 1);
        if (ij.endsWith(File.separator)) ij = ij.substring(0, ij.length() - 1);
        if (tmp.endsWith(File.separator)) tmp = tmp.substring(0, tmp.length() - 1);
        // but we want final dir name to ends with /
        if (dir.contains("?home?")) dir = dir.replace("?home?", home);
        if (dir.contains("?ij?")) dir = dir.replace("?ij?", ij);
        if (dir.contains("?tmp?")) dir = dir.replace("?tmp?", tmp);
        if (!dir.endsWith(File.separator)) dir = dir.concat(File.separator);

        return dir;
    }

    public static int analyseChannelFrameName(String s, ImageInfo info) {
        if (s.contains("?channel?")) return info.getC();
        else if (s.contains("?frame?")) return info.getT();
        else return Integer.parseInt(s);
    }

    public static String getKey(String keyS, ImageInfo info, String usersS) {
        ArrayList<String> users = new ArrayList<>();
        if ((!usersS.isEmpty()) && (!usersS.equalsIgnoreCase("-"))) {
            String[] line = usersS.split(",");
            for (String st : line) {
                users.add(st);
            }
        }
        if (keyS.contains("KEY_")) {
            try {
                OmeroConnect connect = new OmeroConnect();
                connect.connect();
                ImageData imageData = connect.findOneImage(info.getProject(), info.getDataset(), info.getName(), true);
                String value = TapasBatchProcess.analyseKeyValue(keyS, imageData, info, connect, users);
                connect.disconnect();
                return value;
            } catch (Exception e) {
                IJ.log("Pb with key " + keyS);
            }
        }

        return keyS;
    }

    @Deprecated
    public static String analyseKeyValueOld(String s, ImageData image, OmeroConnect connect, ArrayList users) throws ExecutionException, DSAccessException, DSOutOfServiceException {
        if (!s.contains("?key_")) return null;
        String result = new String(s);
        int pos0 = result.indexOf("?key");
        int pos1 = result.indexOf("_", pos0);
        if (pos1 < 0) return null;
        int pos2 = result.indexOf("?", pos1);
        if (pos2 < 0) return null;
        String keyValue = s.substring(pos0, pos2 + 1);
        String key = s.substring(pos1 + 1, pos2);
        //IJ.log("checking value for key "+key);
        String value = connect.getValuePair(image, key, users);
        if (value == null) return null;

        return s.replace(keyValue, value);
    }

    public static String analyseKeyValue(String keyS, ImageData image, ImageInfo info, OmeroConnect connect, ArrayList users) throws ExecutionException, DSAccessException, DSOutOfServiceException {
        if (!keyS.contains("KEY_")) return null;
        String result = new String(keyS);
        int pos0 = result.indexOf("KEY_");
        int pos1 = result.indexOf("_", pos0);
        if (pos1 < 0) return null;
        int pos2 = result.length();
        if (pos2 < 0) return null;
        String key = keyS.substring(pos1 + 1, pos2);
        // analysing key value
        String keyValue = analyseFileName(key, info);
        String value = connect.getValuePair(image, keyValue, users);
        if (value == null) {
            //IJ.log("No key "+keyS);
            return null;
        }

        return value;
    }

    private static ImageInfo analyseLinkName(String s, ImageInfo info) {
        //IJ.log("Analysing link : " + s);
        if (!s.contains("LINK_")) return null;
        String result = s;
        int pos0 = result.indexOf("LINK_");
        int pos1 = result.indexOf("_", pos0);
        if (pos1 < 0) return null;
        int pos2 = result.length();
        if (pos2 < 0) return null;
        String link = s.substring(pos1 + 1, pos2);
        ImageInfo image = getLink(link);
        IJ.log("Found link : " + link + " to " + image.toString());

        return image;
    }


    public static ImagePlus getImageFromFileParameters(String dir, String file, ImageInfo current) {
        ImageInt img;
        if (file.contains("LINK_")) {
            // core link
            ImageInfo omero = analyseLinkName(file, current);
            if (omero == null) return null;
            try {
                OmeroConnect connect = new OmeroConnect();
                connect.connect();
                ImageData image = connect.findOneImage(omero);
                if (image == null) return null;
                img = (ImageInt) connect.getImage(image, omero.getT(), omero.getC());
                connect.disconnect();
                return img.getImagePlus();
            } catch (Exception e) {
                IJ.log("Pb with image " + omero);
                e.printStackTrace();
            }
        } else {
            // file
            String nameF = TapasBatchProcess.analyseFileName(file, current);
            String dirF = TapasBatchProcess.analyseDirName(dir);
            IJ.log("Opening " + dirF + nameF);
            ImagePlus plus = IJ.openImage(dirF + nameF);
            if (plus == null) return null;
            return plus;
        }

        return null;
    }


    public static void setlink(String name, ImageInfo info) {
        links.put(name, info);
    }

    public static ImageInfo getLink(String name) {
        return links.get(name);
    }

    public boolean init(String process, String tapas) {
        this.process = new File(process);
        if (!readPlugins(tapas)) return false;
        if (!readProcessing(process, plugins)) return false;

        return true;
    }

    public ImagePlus processOneImage(ImageInfo info) {
        ImagePlus img = null;
        IJ.log("PROCESSING " + info);
        Instant start = Instant.now();
        for (TapasProcessing processing : processings) {
            processing.setCurrentImage(info);
            IJ.log("* " + processing.getName());
            img = processing.execute(img);
            if (img == null) {
                IJ.log("Processing stopped.");
                return null;
            }
            System.gc();
        }
        Instant end = Instant.now();
        IJ.log("Processing took " + java.time.Duration.between(start, end));

        return img;
    }

    public ImagePlus testProcessing() {
        IJ.log("TEST");
        ImageInfo imageInfo = allImages.get(0);
        ImagePlus res = processOneImage(imageInfo);
        IJ.log("PROCESSING DONE");

        return res;
    }

    public boolean processAllImages() {
        IJ.log("PROCESSING " + allImages.size() + " images");
        int c = 1;
        for (ImageInfo imageInfo : allImages) {
            IJ.log(" ");
            IJ.log("---------- Processing " + c + "/" + allImages.size() + " ----------");
            if (processOneImage(imageInfo) == null) {
                IJ.log("***************** Pb after processing " + imageInfo.getName());
                //return false;
            }
            c++;
        }
        IJ.log("********** PROCESSING DONE **********");

        return true;
    }

    public boolean setProcessing(ArrayList<TapasProcessing> process) {
        processings = process;

        return true;
    }

    private ArrayList<DatasetData> getInitDatasets(String project, String dataset) {
        ArrayList<DatasetData> datasets = null;
        try {
            OmeroConnect omeroConnect = new OmeroConnect();
            omeroConnect.connect();
            omeroConnect.setLog(true);
            ProjectData projectData = omeroConnect.findProject(project, true);
            if (dataset.equals("*")) datasets = omeroConnect.findDatasets(projectData);
            else {
                datasets = new ArrayList<>();
                DatasetData datasetData = omeroConnect.findDataset(dataset, projectData, true);
                if (datasetData == null) IJ.log("No dataset " + dataset + " found");
                else datasets.add(datasetData);
            }
            omeroConnect.disconnect();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return datasets;
    }

    private ArrayList<File> getDatasetsInProjectFile(File project, String dataset) {
        ArrayList<File> datasets = new ArrayList<>();
        File[] files = project.listFiles();
        for (int i = 0; i < files.length; i++) {
            if (files[i].isDirectory()) {
                if (dataset.equals("*")) {
                    datasets.add(files[i]);
                } else {
                    if (files[i].getName().contains(dataset))
                        datasets.add(files[i]);
                }
            }
        }

        return datasets;
    }

    private ArrayList<ImageData> getInitImagesInDataset(DatasetData dataset, String image, ArrayList<String> exclude, boolean strict) {
        ArrayList<ImageData> images = new ArrayList<>();
        try {
            OmeroConnect omeroConnect = new OmeroConnect();
            omeroConnect.connect();
            omeroConnect.setLog(true);
            if (image.equals("*")) {
                // all images
                if (exclude.isEmpty()) {
                    images = omeroConnect.findAllImages(dataset);
                }
                // all images + exclude
                else {
                    images = omeroConnect.findAllImagesExclude(dataset, exclude);
                }
            } else {
                if (!image.contains(",")) { // only one image name
                    // some images
                    if (exclude.isEmpty()) {
                        images = omeroConnect.findImagesContainsName(dataset, image, strict);
                        // some images + exclude
                    } else {
                        images = omeroConnect.findImagesContainsNameExclude(dataset, image, exclude, strict);
                    }
                } else { // many images name separated by ,
                    images = new ArrayList<>();
                    String[] imagesStrings = image.split(",");
                    for (String imageString : imagesStrings) {
                        if (exclude.isEmpty()) {
                            //IJ.log("Looking for " + dataset.getName() + " " + imageString);
                            ArrayList<ImageData> imgs = omeroConnect.findImagesContainsName(dataset, imageString, strict);
                            if (!imgs.isEmpty()) images.addAll(imgs);
                            // some images + exclude
                        } else {
                            images.addAll(omeroConnect.findImagesContainsNameExclude(dataset, imageString, exclude, strict));
                        }
                    }
                }
            }
            omeroConnect.disconnect();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return images;
    }

    private ArrayList<File> getFilesInDatasetFile(File dataset, String image, ArrayList<String> exclude) {
        ArrayList<File> images = new ArrayList<>();
        File[] files = dataset.listFiles();
        for (int i = 0; i < files.length; i++) {
            if (files[i].isDirectory()) continue;
            String fileName = files[i].getName();
            if ((exclude != null) && (!exclude.isEmpty()) && (!OmeroConnect.notInExcludeList(fileName, exclude)))
                continue;
            if ((!image.equals("*")) && (!fileName.contains(image))) continue;
            images.add(files[i]);
        }
        return images;
    }

    public ArrayList<File> getAllImagesInProjectDatasetFile(File project, String dataset, String image, ArrayList<String> exclude) {
        ArrayList<File> allFiles = new ArrayList<>();
        ArrayList<File> datasets = getDatasetsInProjectFile(project, dataset);
        for (File data : datasets) {
            ArrayList<File> files = getFilesInDatasetFile(data, image, exclude);
            for (File file : files) {
                allFiles.add(file);
            }
        }

        return allFiles;
    }

    private ArrayList<ImageInfo> initOmeroInfoFromImages(String project, DatasetData
            dataset, ArrayList<ImageData> images, int c0, int c1, int t0, int t1) {
        ArrayList<ImageInfo> infos = new ArrayList<>();
        for (ImageData image : images)
            for (int c = c0; c <= c1; c++) {
                for (int t = t0; t <= t1; t++) {
                    ImageInfo info = new ImageInfo(project, dataset.getName(), image.getName(), c, t);
                    infos.add(info);
                }
            }
        return infos;
    }

    public void initBatchOmero(String project, String datasetName, String image, int c0, int c1, int t0, int t1) {
        ArrayList<String> exclude = new ArrayList<>(0);
        initBatchOmero(project, datasetName, image, exclude, c0, c1, t0, t1, true);
    }


    public void initBatchOmero(String project, String datasetName, String image, ArrayList<String> exclude, int c0, int c1, int t0, int t1, boolean strict) {
        allImages = new ArrayList<>();
        // get all datasets
        ArrayList<DatasetData> datasets = getInitDatasets(project, datasetName);
        if ((datasets == null) || (datasets.isEmpty())) IJ.log("No datasets found");
        for (DatasetData dataset : datasets) {
            IJ.log("Searching in dataset " + dataset);
            ArrayList<ImageData> images = getInitImagesInDataset(dataset, image, exclude, strict);
            if (images == null) IJ.log("No images found");
            else {
                IJ.log("Tapas found " + images.size() + " images to process");
                allImages.addAll(initOmeroInfoFromImages(project, dataset, images, c0, c1, t0, t1));
            }
        }
    }

    public void initBatchFiles(String root, String project, String datasetName, String image, int c0, int c1, int t0, int t1) {
        allImages = new ArrayList<>();
        ArrayList<String> files = new ArrayList<>();
        // get all files
        if (!root.endsWith(File.separator)) root = root.concat(File.separator);
        File folder = new File(root + project + File.separator + datasetName);
        // all files
        if (image.equals("*")) {
            String[] fileList = folder.list();
            for (String file : fileList) files.add(file);
        }
        // list of images separated by ,
        else if (image.contains(",")) {
            String[] fileList = image.split(",");
            for (String file : fileList) files.add(file);
        }
        // only one image
        else {
            files.add(image);
        }
        // create image informations
        for (String file : files) {
            for (int t = t0; t <= t1; t++) {
                for (int c = c0; c <= c1; c++) {
                    IJ.log("Adding : " + root + "/" + project + "/" + datasetName + "/" + file + "-" + c + "-" + t);
                    allImages.add(new ImageInfo(root, project, datasetName, file, c, t));
                }
            }
        }
    }


    public boolean addUser(String name) throws ExecutionException, DSAccessException, DSOutOfServiceException {
        try {
            if ((name == null) || (name.isEmpty()) || (name.equals("-"))) return true;
            OmeroConnect connect = new OmeroConnect();
            connect.connect();
            long id = connect.getUserId(name);
            if (id > 0) {
                if (!addUsers.contains(id))
                    addUsers.add(id);
            } else {
                IJ.log("Could not find user " + name);
                return false;
            }
            connect.disconnect();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return true;
    }


    public ArrayList<Long> getAddUsers() {
        return addUsers;
    }

    public long getGroupId(String group) throws ExecutionException, DSAccessException, DSOutOfServiceException {
        OmeroConnect connect = new OmeroConnect();
        long groupId = connect.getGroupId(group);
        connect.disconnect();

        return groupId;
    }

    public static ArrayList<TapasProcessing> readProcessings(String file, HashMap<String, String> plugins) {
        ArrayList<TapasProcessing> tapasProcessings = new ArrayList<>();
        try {
            BufferedReader reader = new BufferedReader(new FileReader(file));
            String line = reader.readLine();
            String info[] = new String[2];
            TapasProcessing processing = null;
            while (line != null) {
                if ((!line.startsWith("//")) && (!line.isEmpty())) {
                    //String info[] = line.split(":"); // FIXME, finds first : , in case : in dir name (Windows)
                    int pos0 = line.indexOf(":");
                    if (pos0 == -1) IJ.log("Pb line does not contain \":\" " + line);
                    info[0] = line.substring(0, pos0);
                    info[1] = line.substring(pos0 + 1);
                    if ((info[0].isEmpty()) || (info[1].isEmpty())) IJ.log("Pb with tapas processing line " + line);
                    else {
                        if (info[0].equalsIgnoreCase("process")) {
                            processing = createProcess(info[1], plugins);
                            if (processing == null) {
                                IJ.log("No tapas with name " + info[1]);
                                return null;
                            }
                            tapasProcessings.add(processing);

                        } else {
                            if (!processing.setParameter(info[0].trim(), info[1].trim())) {
                                IJ.log("Pb when processing parameters : " + info[0] + " with value " + info[1]);
                                return null;
                            }
                        }
                    }
                }
                line = reader.readLine();
            }
        } catch (FileNotFoundException e) {
            IJ.log("Process File not found " + e.getMessage());
            e.printStackTrace();
            return null;
        } catch (IOException e) {
            IJ.log("Process File pb " + e.getMessage());
            e.printStackTrace();
            return null;
        }

        return tapasProcessings;
    }

    public boolean readProcessing(String file, HashMap<String, String> plugins) {
        ArrayList<TapasProcessing> tapasProcessings = readProcessings(file, plugins);
        if (tapasProcessings == null) return false;
        if (processings == null) processings = new ArrayList<>(tapasProcessings.size());
        processings.addAll(tapasProcessings);
        return true;
    }

    private static TapasProcessing createProcess(String info, HashMap<String, String> plugins) {
        try {
            info = info.trim();
            String processClass = plugins.get(info);
            IJ.log("Creating process " + info + ":" + processClass);
            if (processClass == null) return null;
            Class cls = Class.forName(processClass);
            Object object = cls.newInstance();
            TapasProcessing processing = (TapasProcessing) object;
            return processing;
        } catch (ClassNotFoundException e) {
            IJ.log("Error Class Not Found " + plugins.get(info));
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            IJ.log("Access Class Not Found " + plugins.get(info));
            e.printStackTrace();
        } catch (InstantiationException e) {
            IJ.log("Error cannot create object " + plugins.get(info));
            e.printStackTrace();
        }

        return null;
    }

    public static HashMap<String, String> readPluginsFile(String file, boolean verbose) {
        HashMap<String, String> map = new HashMap<>();
        IJ.log("Reading tapas file " + file);
        try {
            BufferedReader reader = new BufferedReader(new FileReader(file));
            String line = reader.readLine();
            int c = 0;
            while (line != null) {
                c++;
                int idx = line.indexOf("//");
                if ((idx < 0)&& (!line.isEmpty())) { // strange error on linux ??
                    String info[] = line.split(":");
                    if (info.length != 2) {
                        IJ.log("Pb for tapas plugins with line " + c + ":" + line + " ");
                        return null;
                    }
                    map.put(info[0].trim(), info[1].trim());
                    if (verbose) IJ.log("Found plugin " + info[0].trim() + " " + info[1].trim());
                }
                line = reader.readLine();
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return null;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }

        return map;
    }

    public boolean readPlugins(String file) {
        HashMap<String, String> map = readPluginsFile(file, true);
        if (map == null) return false;
        plugins = map;

        return true;
    }


    private void TESTJAR() {
        File file = new File("c:\\myjar.jar");
        URL url = null;
        try {
            url = file.toURI().toURL();
            URL[] urls = new URL[]{url};
            ClassLoader cl = new URLClassLoader(urls);
            Class cls = cl.loadClass("com.mypackage.myclass");
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }


}
