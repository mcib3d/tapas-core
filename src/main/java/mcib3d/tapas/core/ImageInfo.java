package mcib3d.tapas.core;


import java.io.File;

public class ImageInfo {
    private String project;
    private String dataset;
    private String image;
    private int channel = 1; // starts at 1
    private int frame = 1; // starts at 1
    private String rootDir = "OMERO"; // in case of folder, should end with File.separator character

    public ImageInfo() {
    }

    public ImageInfo(String project, String dataset, String image) {
        this.project = project;
        this.dataset = dataset;
        this.image = image;
    }

    public ImageInfo(String project, String dataset, String image, int channel, int frame) {
        this.rootDir = "OMERO";
        this.project = project;
        this.dataset = dataset;
        this.image = image;
        this.channel = channel;
        this.frame = frame;
    }

    public ImageInfo(String rootDir, String project, String dataset, String image, int channel, int frame) {
        setRootDir(rootDir);
        this.rootDir = rootDir;
        this.project = project;
        this.dataset = dataset;
        this.image = image;
        this.channel = channel;
        this.frame = frame;
    }

    public ImageInfo(String rootDir, String project, String dataset, String image) {
        setRootDir(rootDir);
        this.rootDir = rootDir;
        this.project = project;
        this.dataset = dataset;
        this.image = image;
    }



    public boolean isOmero() {
        return rootDir.equalsIgnoreCase("OMERO");
    }

    public boolean isFile() {
        return !isOmero();
    }

    public String getFilePath() {
        if (isOmero()) return rootDir;
        return rootDir + project + File.separator + dataset + File.separator + image;
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

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
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
        return project + "/" + dataset + "/" + image + "-c" + channel + "-t" + frame;
    }
}