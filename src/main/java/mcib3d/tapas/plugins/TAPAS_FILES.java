package mcib3d.tapas.plugins;

import ij.IJ;
import ij.WindowManager;
import ij.plugin.frame.Recorder;
import mcib3d.tapas.IJ.TapasProcessorIJ;
import mcib3d.tapas.core.TapasBatchProcess;
import mcib3d.tapas.core.TapasBatchUtils;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class TAPAS_FILES extends JFrame {
    private JTextField textFieldRoot;
    private JButton browseButtonRoot;
    private JTextField textFieldProject;
    private JComboBox comboProjects;
    private JTextField textFieldDataset;
    private JComboBox comboDatasets;
    private JList listImages;
    private JTextField textFieldImage;
    private JButton runProcessingButton;
    //private JTextField textFieldChannel;
    //private JTextField textFieldFrame;
    private JTextField textFieldProcess;
    private JButton browseButtonProcess;
    private JPanel panel1;

    //
    String rootProject;
    DefaultListModel model = new DefaultListModel();
    File tapasFile;

    public TAPAS_FILES() {
        // FIXME will be deprecated in 0.7, will be in tapas folder with name .tpm
        tapasFile = TapasBatchUtils.getTapasMenuFile();
        if (tapasFile == null) {
            return;
        }
        listImages.setModel(model);
        //textFieldFrame.setText("0-0");
        //textFieldChannel.setText("0-0");
        panel1.setMinimumSize(new Dimension(800, 600));
        panel1.setPreferredSize(new Dimension(800, 600));
        setContentPane(panel1);
        setMinimumSize(new Dimension(800, 600));
        setPreferredSize(new Dimension(800, 600));
        setContentPane(panel1);
        setTitle("TAPAS FILES " + TapasBatchProcess.version);
        pack();
        setVisible(true);

        // register with Image
        WindowManager.addWindow(this);
        WindowManager.setWindow(this);

        browseButtonRoot.addActionListener(e -> browseRoot());
        comboProjects.addActionListener(e -> selectProject());
        comboDatasets.addActionListener(e -> selectDataset());
        listImages.addListSelectionListener(e -> selectimage());
        runProcessingButton.addActionListener(e -> {
            runProcessingButton.setEnabled(false);
            processing();
        });
        browseButtonProcess.addActionListener(e -> browseProcess());
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
            batchProcess.setProcessor(new TapasProcessorIJ());
            batchProcess.initBatchFiles(rootProject, project, dataset, imageFinal, cmin, cmax, tmin, tmax);
            batchProcess.processAllImages();
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            SwingUtilities.invokeLater(() -> {
                // Macro
                if (Recorder.record) {
                    Recorder.setCommand(null);
                    Recorder.record("run", "TAPAS BATCH", "root=[" + rootProject + "] project=[" + project + "] dataset=[" + dataset + "] image=[" + imageFinal
                            + "] channel=[" + cmin + "-" + cmax + "] frame=[" + tmin + "-" + tmax + "] processing=[" + processFile + "]");
                }
                IJ.log("Done");
                runProcessingButton.setEnabled(true);
            });
        });
        thread.start();
    }

    private void browseProcess() {
        String file = IJ.getFilePath("Choose process");
        textFieldProcess.setText(file);
    }

    private void selectimage() {
        int[] indices = listImages.getSelectedIndices();
        String text;
        if (indices.length == 1) text = model.get(indices[0]).toString();
        else text = indices.length + " images selected";
        textFieldImage.setText(text);
    }

    private void selectDataset() {
        String project = comboProjects.getSelectedItem().toString();
        if (comboDatasets.getSelectedItem() == null) return;
        String dataset = comboDatasets.getSelectedItem().toString();
        textFieldDataset.setText(dataset);
        // fill images
        File folder = new File(rootProject + project + File.separator + dataset);
        IJ.log("Selected dataset : " + folder);
        File[] listOfFiles = folder.listFiles();
        ArrayList<File> files = new ArrayList(listOfFiles.length);
        for (File file : listOfFiles) {
            if (file.isFile()) {
                files.add(file);
            }
        }
        Collections.sort(files, new compareFile());
        model.removeAllElements();
        for (File file : files) {
            model.addElement(file.getName());
        }
    }

    private void selectProject() {
        if (comboProjects.getSelectedItem() == null) return;
        String project = comboProjects.getSelectedItem().toString();
        textFieldProject.setText(project);
        // fill datasets
        File folder = new File(rootProject + project);
        IJ.log("Selected project : " + folder);
        File[] listOfFiles = folder.listFiles();
        ArrayList<File> files = new ArrayList(listOfFiles.length);
        comboDatasets.removeAllItems();
        for (File file : listOfFiles) {
            if (file.isDirectory()) {
                files.add(file);
            }
        }
        Collections.sort(files, new compareFile());
        for (File file : files) {
            comboDatasets.addItem(file.getName());
        }
        if (!files.isEmpty()) textFieldDataset.setText(files.get(0).getName());
    }

    private void browseRoot() {
        browseButtonRoot.setEnabled(false);
        String dir = IJ.getDirectory("Select root folder for projects");
        if (dir == null) {
            browseButtonRoot.setEnabled(true);
            return;
        }
        textFieldRoot.setText(dir);
        rootProject = dir;
        IJ.log("Found root project : " + rootProject);
        // fill projects
        File folder = new File(rootProject);
        File[] listOfFiles = folder.listFiles();
        ArrayList<File> files = new ArrayList(listOfFiles.length);
        comboProjects.removeAllItems();
        for (File file : listOfFiles) {
            if (file.isDirectory()) {
                files.add(file);
            }
        }
        Collections.sort(files, new compareFile());
        for (File file : files) {
            comboProjects.addItem(file.getName());
        }
        textFieldProject.setText(files.get(0).getName());
        browseButtonRoot.setEnabled(true);
    }

    private class compareFile implements Comparator<File> {
        @Override
        public int compare(File o1, File o2) {
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
