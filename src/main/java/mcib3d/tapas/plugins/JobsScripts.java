package mcib3d.tapas.plugins;

import ij.IJ;
import mcib3d.tapas.utils.JobsGenerate;

import javax.persistence.criteria.CriteriaBuilder;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class JobsScripts extends JFrame {
    // GUI
    private JPanel panel1;
    private JTextField textFieldPathFiji;
    private JTextField textFieldExeFiji;
    private JTextField textFieldPathScript;
    private JTextField textFieldOutput;
    private JTextField textFieldNbImages;
    private JTextField textFieldPause;
    private JTextField textFieldCPU;
    private JTextField textFieldMem;
    private JTextField textFieldTimeH;
    private JTextField textFieldTimeM;
    private JButton generateScriptsButton;
    private JTextField textFieldSubmit;
    // job
    JobsGenerate jobsGenerate;


    public JobsScripts(JobsGenerate job) throws HeadlessException {
        // job
        jobsGenerate = job;
        // init
        textFieldPathFiji.setText(jobsGenerate.getPathFiji());
        textFieldExeFiji.setText(jobsGenerate.getExeFiji());
        textFieldPathScript.setText(jobsGenerate.getPathToProcessing());
        textFieldOutput.setText(jobsGenerate.getOutputDir());
        textFieldSubmit.setText(jobsGenerate.getNameSubmit());
        textFieldNbImages.setText("" + jobsGenerate.getNbImagesJob());
        textFieldPause.setText("" + jobsGenerate.getJobPause());
        textFieldCPU.setText("" + jobsGenerate.getJobCpus());
        textFieldMem.setText("" + jobsGenerate.getJobMem());
        textFieldTimeH.setText("" + jobsGenerate.getMaxTimeH());
        textFieldTimeM.setText("" + jobsGenerate.getMaxTimeM());
        panel1.setMinimumSize(new Dimension(800, 600));
        panel1.setPreferredSize(new Dimension(800, 600));
        setContentPane(panel1);
        setMinimumSize(new Dimension(800, 600));
        setPreferredSize(new Dimension(800, 600));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        pack();
        setVisible(true);
        // generate scripts
        generateScriptsButton.addActionListener(e -> generate());
    }

    private void generate() {
        jobsGenerate.setPathFiji(textFieldPathFiji.getText());
        jobsGenerate.setExeFiji(textFieldExeFiji.getText());
        jobsGenerate.setPathToProcessing(textFieldPathScript.getText());
        jobsGenerate.setOutputDir(textFieldOutput.getText());
        jobsGenerate.setNameSubmit(textFieldSubmit.getText());
        jobsGenerate.setNbImagesJob(Integer.parseInt(textFieldNbImages.getText()));
        jobsGenerate.setJobCpus(Integer.parseInt(textFieldCPU.getText()));
        jobsGenerate.setJobMem(Integer.parseInt(textFieldMem.getText()));
        jobsGenerate.setJobPause(Integer.parseInt(textFieldPause.getText()));
        jobsGenerate.setMaxTimeH(Integer.parseInt(textFieldTimeH.getText()));
        jobsGenerate.setMaxTimeM(Integer.parseInt(textFieldTimeM.getText()));

        jobsGenerate.generateScripts();
    }

}
