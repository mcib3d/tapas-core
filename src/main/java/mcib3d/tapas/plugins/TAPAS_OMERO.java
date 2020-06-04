package mcib3d.tapas.plugins;

import ij.IJ;
import ij.WindowManager;
import ij.plugin.frame.Recorder;
import mcib3d.tapas.IJ.TapasProcessorIJ;
import mcib3d.tapas.core.TapasBatchProcess;
import mcib3d.tapas.core.OmeroConnect;
import mcib3d.tapas.core.TapasProcessorAbstract;
import mcib3d.tapas.utils.JobsGenerate;
import omero.gateway.model.DatasetData;
import omero.gateway.model.ImageData;
import omero.gateway.model.ProjectData;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class TAPAS_OMERO extends JFrame {
    private JPanel panel1;
    private JTextField textFieldProject;
    private JComboBox comboProjects;
    private JTextField textFieldDataset;
    private JComboBox comboDatasets;
    private JTextField textFieldImage;
    private JList listImages;
    //private JTextField textFieldChannel;
    //private JTextField textFieldFrame;
    private JTextField textFieldProcess;
    private JButton browseButton;
    private JButton runProcessingButton;
    private JTextField textFieldRoot;
    private JCheckBox jobsCheckBox;
    //
    OmeroConnect omero;
    DefaultListModel model = new DefaultListModel();
    File tapasFile;
    // processor
    TapasProcessorAbstract processor;

    public static void main(String args[]) {
        new ij.ImageJ();
        new TAPAS_OMERO();
    }

    public TapasProcessorAbstract getProcessor() {
        return processor;
    }

    public void setProcessor(TapasProcessorAbstract processorAbstract) {
        this.processor = processorAbstract;
    }

    public TAPAS_OMERO() {
        // by default IJ processor
        setProcessor(new TapasProcessorIJ());
        tapasFile = new File(IJ.getDirectory("imagej") + File.separator + "tapas.txt");
        IJ.log("Checking tapas file " + tapasFile.getAbsolutePath());
        if (!tapasFile.exists()) {
            IJ.log("No tapas found");
        }
        IJ.log("Getting list of projects and datasets from OMERO, please wait ...");
        listImages.setModel(model);
        //textFieldFrame.setText("0-0");
        //textFieldChannel.setText("0-0");
        panel1.setMinimumSize(new Dimension(800, 600));
        panel1.setPreferredSize(new Dimension(800, 600));
        setContentPane(panel1);
        setMinimumSize(new Dimension(800, 600));
        setPreferredSize(new Dimension(800, 600));
        setTitle("TAPAS OMERO " + TapasBatchProcess.version);
        jobsCheckBox.setVisible(false);// jobs is highly experimental
        pack();
        setVisible(true);

        // register with Image
        WindowManager.addWindow(this);
        WindowManager.setWindow(this);

        // fill projects
        omero = new OmeroConnect();
        try {
            omero.connect();
            // projects
            List<ProjectData> projects = omero.findAllProjects();
            projects.sort(new compareProject());
            for (int i = 0; i < projects.size(); i++) {
                comboProjects.addItem(projects.get(i).getName());
            }
            textFieldProject.setText(projects.get(0).getName());
            omero.disconnect();
            selectProject();
        } catch (Exception e1) {
            e1.printStackTrace();
        }

        // browse project file
        browseButton.addActionListener(e -> {
            browseProcess();
        });

        // update projects
        comboProjects.addActionListener(e -> {
            selectProject();
        });

        // update datasets
        comboDatasets.addActionListener(e -> {
            selectDataset();
        });

        // update images
        listImages.addListSelectionListener(e -> {
            selectImage();
        });

        // processing button
        runProcessingButton.addActionListener(e -> {
            runProcessingButton.setEnabled(false);
            processing();
        });
    }

    private void browseRoot() {
        String file = IJ.getFilePath("Choose root");
        textFieldRoot.setText(file);
    }

    private void browseProcess() {
        String file = IJ.getFilePath("Choose process");
        textFieldProcess.setText(file);
    }

    private void selectProject() {
        String project = comboProjects.getSelectedItem().toString();
        textFieldProject.setText(project);
        // fill datasets
        try {
            omero.connect();
            // project
            ProjectData projectData = omero.findProject(project, true);
            List<DatasetData> datasets = omero.findDatasets(projectData);
            datasets.sort(new compareDataset());
            comboDatasets.removeAllItems();
            for (int i = 0; i < datasets.size(); i++) {
                comboDatasets.addItem(datasets.get(i).getName());
            }
            if (!datasets.isEmpty())
                textFieldDataset.setText(datasets.get(0).getName());
            omero.disconnect();
        } catch (Exception e2) {
            e2.printStackTrace();
        }
    }

    private void selectDataset() {
        String project = comboProjects.getSelectedItem().toString();
        String dataset = "";
        if (comboDatasets.getSelectedItem() != null) {
            dataset = comboDatasets.getSelectedItem().toString();
            textFieldDataset.setText(dataset);
            // fill images
            try {
                omero.connect();
                // project
                ProjectData projectData = omero.findProject(project, true);
                DatasetData datasetData = omero.findDataset(dataset, projectData, true);
                List<ImageData> images = omero.findAllImages(datasetData);
                images.sort(new compareImages());
                model.removeAllElements();
                for (int i = 0; i < images.size(); i++) {
                    model.addElement(images.get(i).getName());
                }
                textFieldImage.setText("");
                listImages.updateUI();
                listImages.repaint();
                repaint();
                omero.disconnect();
            } catch (Exception e2) {
                e2.printStackTrace();
            }
        }
    }

    private void selectImage() {
        int[] indices = listImages.getSelectedIndices();
        String text;
        if (indices.length == 1) text = model.get(indices[0]).toString();
        else text = indices.length + " images selected";
        textFieldImage.setText(text);
    }

    private void processing() {
        // batch process
        TapasBatchProcess batchProcess = new TapasBatchProcess();
        String project = textFieldProject.getText();
        String dataset = textFieldDataset.getText();
        // images
        String image = "";
        int[] indices = listImages.getSelectedIndices();
        if (indices.length == 1) image = textFieldImage.getText(); // by default selected image
        if (indices.length == model.getSize()) image = "*";
        if (indices.length == 0) {
            String rep = IJ.getString("Process all files (y/n) ?", "n");
            if (rep.equalsIgnoreCase("y")) image = "*";
            else {
                runProcessingButton.setEnabled(true);
                return;
            }
        }
        if ((indices.length > 1) && (indices.length < model.size())) {
            image = "";
            for (int i = 0; i < indices.length - 1; i++) {
                image = image.concat(model.get(indices[i]).toString() + ",");
            }
            image = image.concat(model.get(indices[indices.length - 1]).toString());
        }
        String imageFinal = image;
        String processFile = textFieldProcess.getText();
        if (!batchProcess.init(processFile, tapasFile.getAbsolutePath())) {
            IJ.log("Aborting");
            return;
        }
        // get processor
        processor = TapasBatchProcess.getProcessor(processFile);
        IJ.log("Processing with "+processor.getNameProcessor());
        setProcessor(processor);

        // TEST JOB
        if (jobsCheckBox.isSelected()) {
            // get image list from model and selected indices
            IJ.log("Creating jobs");
            ArrayList<String> imageJobs = new ArrayList<>(indices.length);
            for (int i : indices) {
                imageJobs.add(model.get(i).toString());
                IJ.log("Added image " + model.get(i).toString());
            }
            JobsGenerate job = new JobsGenerate();
            job.setRoot("OMERO");
            job.setProject(project);
            job.setDataset(dataset);
            job.setImageList(imageJobs);
            job.setProcessing(processFile);
            new JobsScripts(job);
        }
        // TEST JOB
        // channel
        //String channel = textFieldChannel.getText();
        //int[] channels = processTextForTimeChannel(channel);
        int cmin = 1;
        int cmax = 1;
        // frame
        //String frame = textFieldFrame.getText();
        //int[] frames = processTextForTimeChannel(frame);
        int tmin = 1;
        int tmax = 1;
        // init to find images
        Thread thread = new Thread(() -> {
            batchProcess.setProcessor(processor);
            batchProcess.initBatchOmero(project, dataset, imageFinal, cmin, cmax, tmin, tmax);
            batchProcess.processAllImages();
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                runProcessingButton.setEnabled(true);
                e.printStackTrace();
            }
            SwingUtilities.invokeLater(() -> {
                IJ.log("Done");
                // Macro
                if (Recorder.record) {
                    Recorder.setCommand(null);
                    Recorder.record("run", "TAPAS BATCH", "root=[OMERO] project=[" + project + "] dataset=[" + dataset + "] image=[" + imageFinal
                            + "] channel=[" + cmin + "-" + cmax + "] frame=[" + tmin + "-" + tmax + "] processing=[" + processFile + "]");
                }
            });
        });
        if (!jobsCheckBox.isSelected()) thread.start();
        runProcessingButton.setEnabled(true);
    }

    class compareProject implements Comparator<ProjectData> {
        @Override
        public int compare(ProjectData o1, ProjectData o2) {
            return o1.getName().compareToIgnoreCase(o2.getName());
        }
    }

    class compareDataset implements Comparator<DatasetData> {
        @Override
        public int compare(DatasetData o1, DatasetData o2) {
            return o1.getName().compareToIgnoreCase(o2.getName());
        }
    }

    class compareImages implements Comparator<ImageData> {
        @Override
        public int compare(ImageData o1, ImageData o2) {
            return o1.getName().compareToIgnoreCase(o2.getName());
        }
    }

    private int[] processTextForTimeChannel(String nextString) {
        int[] vals = new int[2];
        if (nextString.contains("-")) {
            String[] cs = nextString.split("-");
            vals[0] = Integer.parseInt(cs[0]);
            vals[1] = Integer.parseInt(cs[1]);
        } else {
            vals[0] = Integer.parseInt(nextString);
            vals[1] = vals[0];
        }

        return vals;
    }
}


