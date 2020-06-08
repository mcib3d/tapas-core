package mcib3d.tapas.core;

import ij.IJ;
import ij.ImagePlus;
import mcib3d.image3d.ImageInt;
import omero.gateway.exception.DSAccessException;
import omero.gateway.exception.DSOutOfServiceException;
import omero.gateway.model.ImageData;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

public class TapasBatchUtils {
    public static File getTapasMenuFile() {
        // FIXME will be deprecated in 0.7, will be in tapas folder with name .tpm
        File tapasFile = new File(IJ.getDirectory("imagej") + File.separator + "tapas.txt");
        IJ.log("Checking tapas file " + tapasFile.getAbsolutePath());
        if (!tapasFile.exists()) {
            IJ.log("No tapas file found : " + tapasFile.getPath());
            IJ.log("Cannot run TAPAS. Please check installation.");
            return null;
        }

        return tapasFile;
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
        if (file.contains("?name?")) { // deprecated --> ?image?
            file = file.replace("?name?", info.getImage());
            return analyseFileName(file, info);
        }
        if (file.contains("?image?")) {
            file = file.replace("?image?", info.getImage());
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


    public static int analyseChannelFrameName(String s, ImageInfo info) {
        if (s.contains("?channel?")) return info.getC();
        else if (s.contains("?frame?")) return info.getT();
        else return Integer.parseInt(s);
    }


    public static boolean attach(ImageInfo info, File file, String project, String dataset, String name) {
        boolean ok = false;
        if (info.isFile()) { // if file copy in same dataset directory
            ok = attachFiles(info, file, project, dataset);
        } else {
            ok = attachOMERO(file, project, dataset, name);
        }

        return ok;
    }

    public static boolean attachFiles(ImageInfo info, File file, String project, String dataset) {
        String name = file.getName();
        String path = info.getRootDir() + project + File.separator + dataset + File.separator + name;
        // new 0.6.3, put in a folder "attachments"
        File attachFolder = new File(info.getRootDir() + project + File.separator + dataset + File.separator + "attachments" + File.separator);
        if (!attachFolder.exists()) {
            IJ.log("Creating folder " + attachFolder + " to store attachments");
            attachFolder.mkdir();
        }
        path = attachFolder.getPath() + File.separator + name;
        try {
            IJ.log("Attaching to FILES");
            File file2 = new File(path);
            // delete if exist
            if (file2.exists()) {
                IJ.log("File " + file2.getPath() + " exists. Overwriting");
                file2.delete();
            }
            Files.copy(file.toPath(), file2.toPath());
        } catch (IOException e) {
            IJ.log("Could not copy " + file.getPath() + " to " + path);
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public static boolean attachOMERO(File file, String project, String dataset, String name) {
        try {
            IJ.log("Attaching to OMERO");
            OmeroConnect connect = new OmeroConnect();
            connect.setLog(false);
            connect.connect();
            connect.addFileAnnotation(connect.findOneImage(project, dataset, name, true), file);
            connect.disconnect();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return true;
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
                ImageData imageData = connect.findOneImage(info.getProject(), info.getDataset(), info.getImage(), true);
                String value = analyseKeyValue(keyS, imageData, info, connect, users);
                connect.disconnect();
                return value;
            } catch (Exception e) {
                IJ.log("Pb with key " + keyS);
            }
        }

        return keyS;
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
            IJ.log("No key " + keyS);
            return null;
        }

        return value;
    }
}
