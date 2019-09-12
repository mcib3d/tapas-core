package mcib3d.tapas.utils;

import ij.IJ;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class JobsGenerate {
    // server
    private int nbImagesJob = 1;
    private String pathFiji = "/stornext/Home/data/allstaff/b/boudier.t/App/Fiji.app/";
    private String exeFiji = "ImageJ-linux64";
    private String pathToProcessing = "/stornext/Home/data/allstaff/b/boudier.t/DATA/";
    // local
    private String outputDir = "/home/boudier/WEHI/DATA/MILTON/";
    private String nameSubmit = "submitAll.sh";
    // parameter for each job
    private int jobCpus = 2;
    private int jobMem = 4; // Gb
    private int maxTimeM = 0; // minutes
    private int maxTimeH = 24; // hours
    private int jobPause = 10; // pause between two submit (sec)
    // data
    private String root;
    private String project;
    private String dataset;
    private ArrayList<String> imageList;
    private String processing;


    public void generateScripts() {
        // open file for submit_all file
        try {
            BufferedWriter bw = new BufferedWriter(new FileWriter(new File(outputDir + nameSubmit)));
            // new path for processing
            String processName = new File(processing).getName();
            // separate the image with ,
            String macro = "run(\"TAPAS BATCH\", \"root=[?root?] project=[?project?] dataset=[?dataset?] image=[?name?] channel=[1-1] frame=[1-1] processing=[?processing?]\")";
            int nJobs = (int) Math.ceil((double) imageList.size() / (double) nbImagesJob);
            // write the macros first
            for (int i = 0; i < nJobs; i++) {
                String imageMacro = imageList.get(i * nbImagesJob);
                String fileName = imageMacro.replace(" ", "_"); // remove spaces
                for (int j = 1; j < nbImagesJob; j++) {
                    int ij = i * nbImagesJob + j;
                    if (ij >= imageList.size()) break;
                    String name = imageList.get(ij);
                    imageMacro = imageMacro.concat("," + name);
                }
                String macroTmp = macro.replace("?root?", root).replace("?project?", project).replace("?dataset?", dataset).replace("?name?", imageMacro).replace("?processing?", pathToProcessing + processName);
                writeFile(outputDir, fileName, ".ijm", macroTmp);
                String shell = " #!/bin/bash\n" + pathFiji + exeFiji + " --allow-multiple --headless --console -macro $PWD/" + fileName + ".ijm";
                writeFile(outputDir, fileName, ".sh", shell);
                bw.write("qsub -d $PWD -q standard -l nodes=1:ppn=" + jobCpus + ",mem=" + jobMem + "gb,walltime=" + maxTimeH + ":" + maxTimeM + ":00 -N " + fileName + " " + fileName + ".sh;sleep " + jobPause + "\n");
            }
            bw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setRoot(String root) {
        this.root = root;
    }

    public void setProject(String project) {
        this.project = project;
    }

    public void setDataset(String dataset) {
        this.dataset = dataset;
    }

    public void setImageList(ArrayList<String> list) {
        this.imageList = list;
    }

    public void setProcessing(String processing) {
        this.processing = processing;
    }

    private void writeFile(String dir, String file, String ext, String macro) {
        try {
            BufferedWriter bw = new BufferedWriter(new FileWriter(new File(dir + File.separator + file + ext)));
            bw.write(macro);
            bw.close();
        } catch (IOException e) {
            IJ.log("Pb with file " + dir + " " + file + " : " + e);
        }
    }

    public int getNbImagesJob() {
        return nbImagesJob;
    }

    public void setNbImagesJob(int nbImagesJob) {
        this.nbImagesJob = nbImagesJob;
    }

    public String getPathFiji() {
        return pathFiji;
    }

    public void setPathFiji(String pathFiji) {
        this.pathFiji = pathFiji;
    }

    public String getExeFiji() {
        return exeFiji;
    }

    public void setExeFiji(String exeFiji) {
        this.exeFiji = exeFiji;
    }

    public String getOutputDir() {
        return outputDir;
    }

    public void setOutputDir(String outputDir) {
        this.outputDir = outputDir;
    }

    public String getNameSubmit() {
        return nameSubmit;
    }

    public void setNameSubmit(String nameSubmit) {
        this.nameSubmit = nameSubmit;
    }

    public String getPathToProcessing() {
        return pathToProcessing;
    }

    public void setPathToProcessing(String pathToProcessing) {
        this.pathToProcessing = pathToProcessing;
    }

    public int getJobCpus() {
        return jobCpus;
    }

    public void setJobCpus(int jobCpus) {
        this.jobCpus = jobCpus;
    }

    public int getJobMem() {
        return jobMem;
    }

    public void setJobMem(int jobMem) {
        this.jobMem = jobMem;
    }

    public int getMaxTimeM() {
        return maxTimeM;
    }

    public void setMaxTimeM(int maxTimeM) {
        this.maxTimeM = maxTimeM;
    }

    public int getMaxTimeH() {
        return maxTimeH;
    }

    public void setMaxTimeH(int maxTimeH) {
        this.maxTimeH = maxTimeH;
    }

    public int getJobPause() {
        return jobPause;
    }

    public void setJobPause(int jobPause) {
        this.jobPause = jobPause;
    }
}
