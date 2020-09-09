package mcib3d.tapas.plugins;

import ij.IJ;
import ij.WindowManager;
import ij.plugin.BrowserLauncher;
import mcib3d.tapas.core.TapasBatchProcess;
import mcib3d.tapas.core.TapasBatchUtils;
import mcib3d.tapas.core.TapasDocumentation;
import mcib3d.tapas.core.TapasProcessingAbstract;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

public class TAPAS_MENU extends JFrame {
    private JComboBox comboBoxPlugins;
    private JTextField textField1;
    private JTextField textField2;
    private JTextField textField3;
    private JTextField textField4;
    private JTextField textField5;
    private JTextField textField6;
    private JTextField textField7;
    private JTextField textField8;
    private JTextField textField9;
    private JTextField textField10;
    private JButton createTextButton;
    private JTextArea textArea1;
    private JPanel panel1;
    private JLabel Param1;
    private JLabel Param2;
    private JLabel Param3;
    private JLabel Param4;
    private JLabel Param5;
    private JLabel Param6;
    private JLabel Param7;
    private JLabel Param8;
    private JLabel Param9;
    private JLabel Param10;
    private JTextField textFieldDescription;
    private JButton documentationButton;
    private JButton websiteButton;
    private JLabel selectTapas;
    private JTextPane descriptionTAPAS;

    TapasDocumentation documentation;

    File tapasFile;
    HashMap<String, String> plugins;
    int maxParam = 10;
    JTextField paramsText[] = new JTextField[maxParam];
    JLabel paramsLabel[] = new JLabel[maxParam];
    TapasProcessingAbstract currentTapas;

    public TAPAS_MENU() {
        // init parameters fields // FIXME dynamic
        paramsText[0] = textField1;
        paramsLabel[0] = Param1;
        paramsText[1] = textField2;
        paramsLabel[1] = Param2;
        paramsText[2] = textField3;
        paramsLabel[2] = Param3;
        paramsText[3] = textField4;
        paramsLabel[3] = Param4;
        paramsText[4] = textField5;
        paramsLabel[4] = Param5;
        paramsText[5] = textField6;
        paramsLabel[5] = Param6;
        paramsText[6] = textField7;
        paramsLabel[6] = Param7;
        paramsText[7] = textField8;
        paramsLabel[7] = Param8;
        paramsText[8] = textField9;
        paramsLabel[8] = Param9;
        paramsText[9] = textField10;
        paramsLabel[9] = Param10;

        // test
        descriptionTAPAS.setContentType("text/html");
        descriptionTAPAS.setText("Documentation will appear here.");

        // read list of Tapas
        tapasFile = TapasBatchUtils.getTapasMenuFile();
        if (tapasFile == null) return;
        plugins = TapasBatchProcess.readPluginsFile(tapasFile.getAbsolutePath(), false);
        ArrayList<String> pluginsName = new ArrayList<>(plugins.size());
        for (String key : plugins.keySet()) {
            pluginsName.add(key);
        }
        Collections.sort(pluginsName);

        // documentation
        documentation = new TapasDocumentation();
        documentation.loadDocumentation(tapasFile.getParent() + File.separator + "tapasDocumentation.txt");

        // fill the combo
        comboBoxPlugins.removeAllItems();
        for (String key : pluginsName) {
            comboBoxPlugins.addItem(key);
        }

        // display the frame
        panel1.setMinimumSize(new Dimension(800, 600));
        panel1.setPreferredSize(new Dimension(800, 600));
        setContentPane(panel1);
        setTitle("TAPAS MENU " + TapasBatchProcess.version);
        pack();
        setVisible(true);

        // register with Image
        WindowManager.addWindow(this);
        WindowManager.setWindow(this);

        comboBoxPlugins.setSelectedIndex(0);
        selectPlugins();

        initText();

        comboBoxPlugins.addActionListener(e -> selectPlugins());
        createTextButton.addActionListener(e -> createText());
        documentationButton.addActionListener(e -> getDocumentation());
        websiteButton.addActionListener(e -> launchWebsite());
    }

    private void getDocumentation() {
        try {
            BrowserLauncher.openURL("https://www.dropbox.com/s/mzcp8lyqbsz8t73/TapasDescription0.6.3.pdf?dl=0");
        } catch (IOException e) {
            IJ.log("Cannot find documentation");
        }
    }

    private void launchWebsite() {
        try {
            BrowserLauncher.openURL("https://imagej.net/TAPAS");
        } catch (IOException e) {
            IJ.log("Cannot find website");
        }
    }


    private void initText() {
        String process = "";
        process = process.concat("// first process should be input \n");
        process = process.concat("// to read image from OMERO \n");
        process = process.concat("// or from file \n");
        process = process.concat("process:input \n");
        textArea1.append(process);
        textArea1.append("\n");
    }

    private void createText() {
        String process = "";
        process = process.concat("// " + currentTapas.getName() + "\n");
        process = process.concat("process:" + comboBoxPlugins.getSelectedItem().toString() + "\n");
        // parameters
        String[] parameters = currentTapas.getParameters();
        int np = parameters.length;
        for (int i = 0; i < np; i++) {
            process = process.concat(parameters[i] + ":" + paramsText[i].getText() + "\n");
        }
        textArea1.append(process);
        textArea1.append("\n");

        // TEST
        documentation.printCategories();
    }

    private void selectPlugins() {
        String plugin = comboBoxPlugins.getSelectedItem().toString();
        String className = plugins.get(plugin);
        //IJ.log("Selected " + plugin + " " + className);
        // create plugin
        Class cls;
        try {
            cls = Class.forName(className);
            Object object = cls.newInstance();
            currentTapas = (TapasProcessingAbstract) object;
            textFieldDescription.setText(currentTapas.getName());
            // documentation
            String doc = documentation.getDocumentation(currentTapas.getClass().getName());
            descriptionTAPAS.setText(doc);

            // parameters
            String[] parameters = currentTapas.getParameters();
            int np = parameters.length;
            for (int i = 0; i < np; i++) {
                paramsLabel[i].setText(parameters[i]);
                paramsLabel[i].setVisible(true);
                paramsText[i].setEnabled(true);
                String par = currentTapas.getParameter(parameters[i]);
                if ((par != null) && (!par.isEmpty())) paramsText[i].setText(par);
                else paramsText[i].setText("");
            }
            for (int i = np; i < maxParam; i++) {
                paramsLabel[i].setText("");
                paramsLabel[i].setVisible(false);
                paramsText[i].setEnabled(false);
                paramsText[i].setText("");
            }
        } catch (ClassNotFoundException e) {
            IJ.log("No class " + className);
        } catch (IllegalAccessException e) {
            IJ.log("Pb class " + className);
        } catch (InstantiationException e) {
            IJ.log("Pb init " + className);
        }
    }

    private void createUIComponents() {
    }
}
