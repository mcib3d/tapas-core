package mcib3d.tapas.plugins;

import ij.*;
import mcib3d.tapas.utils.Encrypt;
import mcib3d.tapas.core.OmeroConnect;
import omero.gateway.model.ProjectData;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;

public class OmeroPassword extends JFrame {

    public OmeroPassword(String title) throws HeadlessException {
        super(title);
        JPanel panel = new JPanel();
        GridLayout layout = new GridLayout(0, 2, 1, 1);
        JLabel userLabel = new JLabel("User");
        JTextField userField = new JTextField(Prefs.get("OMERO.TB.user.string", "user"), 20);
        JLabel serverLabel = new JLabel("Server");
        JTextField serverField = new JTextField(Prefs.get("OMERO.TB.server.string", "server"), 100);
        JLabel portLabel = new JLabel("Port");
        JTextField portField = new JTextField(Prefs.get("OMERO.TB.port.int", "4064"), 10);
        JLabel passwordlabel = new JLabel("Password");
        JPasswordField passwordField = new JPasswordField(" ", 20);
        JButton cancelButton = new JButton("Cancel");
        JButton okButton = new JButton("OK");
        panel.setLayout(layout);
        panel.add(userLabel);
        panel.add(userField);
        panel.add(serverLabel);
        panel.add(serverField);
        panel.add(portLabel);
        panel.add(portField);
        panel.add(passwordlabel);
        panel.add(passwordField);
        panel.add(okButton);
        panel.add(cancelButton);
        add(panel);
        setSize(600, 200);
        setVisible(true);

        okButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // generate and save key
                generateAndSaveKey();
                // save information except password
                saveInformation(userField.getText(), serverField.getText(), portField.getText());
                // encrypt and save password
                char[] pass = passwordField.getPassword();
                byte[] pass2 = new byte[pass.length];
                for (int i = 0; i < pass.length; i++) pass2[i] = (byte) pass[i];
                Encrypt encrypt = new Encrypt();
                try {
                    String passE = encrypt.encrypt(pass2);
                    //IJ.log("ENC "+passE);
                    ij.Prefs.set("OMERO.TB.pass.string", passE);
                    passE = "";
                } catch (Exception e1) {
                    e1.printStackTrace();
                }
                dispose();
                // test connection
                IJ.log("Connection to OMERO OK");

                try {
                    OmeroConnect connect = new OmeroConnect();
                    connect.connect();
                    ArrayList<ProjectData> list = connect.findAllProjects();
                    if (list.isEmpty()) IJ.log("No projects found");
                    else IJ.log(list.size() + " projects found");
                    for (ProjectData project : list) {
                        IJ.log("Found project : " + project.getName());
                    }
                    connect.disconnect();
                } catch (Exception e1) {
                    IJ.log("Error occured when trying to connect to OMERO "+e1.getMessage());
                    e1.printStackTrace();
                }
            }
        });

        cancelButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                IJ.log("Cancel");
                dispose();
            }
        });
    }

    private void saveInformation(String user, String server, String port) {
        Prefs.set("OMERO.TB.user.string", user);
        Prefs.set("OMERO.TB.server.string", server);
        Prefs.set("OMERO.TB.port.int", Integer.parseInt(port));
    }

    private void generateAndSaveKey() {
        /// generate key
        Encrypt encrypt = new Encrypt();
        char[] key = encrypt.generateRandomChars();
        //System.out.println("SAVE KEY PASSWORD");
        //for (int c = 0; c < key.length; c++) System.out.print(" " + key[c]);
        //System.out.println();
        // save key
        RandomAccessFile accessFile = null;
        try {
            File file = new File(System.getProperty("user.home") + File.separator + "OMEROKey");
            if (file.exists()) file.delete();
            file.createNewFile();
            accessFile = new RandomAccessFile(file, "rw");
            accessFile.seek(0);
            for (int c = 0; c < key.length; c++) {
                int idx = key.length - 1 - c;
                accessFile.writeChar(key[idx] + 1);
                key[idx] = 0;
            }
            accessFile.close();
        } catch (FileNotFoundException e) {
            IJ.log("save pb filenotfound: " + e.getMessage());
        } catch (IOException e) {
            IJ.log("save pb ioexception: " + e.getMessage());
        }
    }

}
