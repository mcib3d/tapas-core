package mcib3d.tapas.core;

import ij.IJ;
import ij.ImagePlus;
import mcib3d.image3d.ImageInt;
import omero.gateway.model.ImageData;

import java.io.File;
import java.util.ArrayList;

public class TapasBatchUtils {

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
        if (file.contains("?name?")) {
            file = file.replace("?name?", info.getImage());
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


}
