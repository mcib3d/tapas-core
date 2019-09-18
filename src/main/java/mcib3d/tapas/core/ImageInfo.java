package mcib3d.tapas.core;

import java.io.File;

public class ImageInfo {
    private String project;
    private String dataset;
    private String name;
    private int channel = 1; // starts at 1
    private int frame = 1; // starts at 1
    // for files-based batch, should be "core" for core images
    private String rootDir = "OMERO"; // in case of folder, should end with File.separator character

    public ImageInfo() {
    }

    public ImageInfo(String project, String dataset, String image) {
        this.project = project;
        this.dataset = dataset;
        this.name = image;
    }

    public ImageInfo(String project, String dataset, String image, int channel, int frame) {
        this.rootDir = "OMERO";
        this.project = project;
        this.dataset = dataset;
        this.name = image;
        this.channel = channel;
        this.frame = frame;
    }

    public ImageInfo(String rootDir, String project, String dataset, String image, int channel, int frame) {
        setRootDir(rootDir);
        this.rootDir = rootDir;
        this.project = project;
        this.dataset = dataset;
        this.name = image;
        this.channel = channel;
        this.frame = frame;
    }

    public boolean isOmero() {
        return rootDir.equalsIgnoreCase("OMERO");
    }

    public boolean isFile() {
        return !isOmero();
    }

    public String getFilePath() {
        if (isOmero()) return rootDir;
        return rootDir + project + File.separator + dataset + File.separator + name;
    }

    public String getDirPath() {
        if (isOmero()) return rootDir;
        return rootDir + project + File.separator + dataset;
    }

    public String getDatasetPath() {
        if (isOmero()) return rootDir;
        return rootDir + project + File.separator + dataset + File.separator;
    }

    public String getProject() {
        return project;
    }

    public void setProject(String project) {
        this.project = project;
    }

    public String getDataset() {
        return dataset;
    }

    public void setDataset(String dataset) {
        this.dataset = dataset;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getC() {
        return channel;
    }

    public void setC(int c) {
        this.channel = c;
    }

    public int getT() {
        return frame;
    }

    public void setT(int time) {
        this.frame = time;
    }

    public String getRootDir() {
        return rootDir;
    }

    public void setRootDir(String rootDir) {
        this.rootDir = rootDir;
        //IJ.log("Setting root dir : "+this.rootDir);
    }

    public String toString() {
        return project + "/" + dataset + "/" + name + "-c" + channel + "-t" + frame;
    }
}