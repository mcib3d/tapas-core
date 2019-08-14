package mcib3d.tapas.plugins;

import ij.IJ;
import ij.ImagePlus;
import ij.Prefs;
import ij.WindowManager;
import ij.gui.GenericDialog;
import ij.io.FileSaver;
import ij.plugin.PlugIn;
import mcib3d.tapas.core.OmeroConnect;
import omero.gateway.model.ProjectData;
import java.io.File;

public class Omero_Save implements PlugIn {
    private String project = "project";
    private String dataset = "dataset";
    private String image = "image";
    private boolean strict = true;

    @Override
    public void run(String arg) {
        if (dialog()) {
            Prefs.set("OMEROSAVE.project", project);
            Prefs.set("OMEROSAVE.dataset", dataset);
            Prefs.set("OMEROSAVE.image", image);
            try {
                OmeroConnect connect = new OmeroConnect();
                connect.setLog(true);
                connect.connect();
                ProjectData projectData = connect.findProject(project, strict);
                if (projectData == null) {
                    IJ.log("Project not found : " + project);
                    return;
                }
                ImagePlus plus = WindowManager.getCurrentImage();
                FileSaver saver = new FileSaver(plus);
                if (plus.getNSlices() > 1)
                    saver.saveAsTiffStack(System.getProperty("user.home") + File.separator + image);
                else
                    saver.saveAsTiff(System.getProperty("user.home") + File.separator + image);
                IJ.log("Uploading to OMERO ...");
                connect.addImageToDataset(project, dataset, System.getProperty("user.home") + File.separator, image);
                connect.disconnect();
                IJ.log("Done.");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private boolean dialog() {
        project = Prefs.get("OMEROSAVE.project", "project");
        dataset = Prefs.get("OMEROSAVE.dataset", "dataset");
        image = Prefs.get("OMEROSAVE.image", "image");
        GenericDialog dialog = new GenericDialog("OMERO SAVE");
        dialog.addStringField("Project", project, 50);
        dialog.addStringField("Dataset", dataset, 50);
        dialog.addStringField("Image", image, 50);
        dialog.showDialog();
        project = dialog.getNextString();
        dataset = dialog.getNextString();
        image = dialog.getNextString();
        //strict = dialog.getNextBoolean();

        return dialog.wasOKed();
    }
}

