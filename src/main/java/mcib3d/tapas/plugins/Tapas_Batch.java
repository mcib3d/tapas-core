package mcib3d.tapas.plugins;

import ij.IJ;
import ij.Prefs;
import ij.gui.GenericDialog;
import ij.plugin.PlugIn;
import mcib3d.tapas.utils.CheckInstall;
import mcib3d.tapas.core.TapasBatchProcess;

import java.io.File;

public class Tapas_Batch implements PlugIn {
    private String root = "OMERO";
    private String project = "";
    private String dataset = "";
    private String image = "";
    private String processFile = "";
    private int cmin = 1;
    private int cmax = 2;
    private int tmin = 0;
    private int tmax = 0;

    public static void main(String[] args) {
    }

    @Override
    public void run(String s) {
        IJ.log("TAPAS BATCH");
        if (!CheckInstall.installComplete()) IJ.log("Please check installation.");

        // read Prefs
        root = Prefs.get("OMEROBATCH.root", root);
        project = Prefs.get("OMEROBATCH.project", "Project");
        dataset = Prefs.get("OMEROBATCH.dataset", "Dataset");
        image = Prefs.get("OMEROBATCH.image", "*");
        processFile = Prefs.get("OMEROBATCH.process", "");
        cmin = (int) Prefs.get("OMEROBATCH.cmin", 0);
        cmax = (int) Prefs.get("OMEROBATCH.cmax", 0);
        tmin = (int) Prefs.get("OMEROBATCH.tmin", 0);
        tmax = (int) Prefs.get("OMEROBATCH.tmax", 0);

        File tapasFile = new File(IJ.getDirectory("imagej") + File.separator + "tapas.txt");
        IJ.log("Checking tapas file " + tapasFile.getAbsolutePath());
        if (!tapasFile.exists()) {
            IJ.log("No tapas found");
        }

        if (dialog()) {
            TapasBatchProcess batchProcess = new TapasBatchProcess();
            if (!batchProcess.init(processFile, tapasFile.getAbsolutePath())) {
                IJ.log("Aborting");
                return;
            }
            if (IJ.isMacro()) {
                IJ.log("Macro mode ON for image " + image);
            }

            // init to find images
            if (root.equalsIgnoreCase("OMERO"))
                batchProcess.initBatchOmero(project, dataset, image, cmin, cmax, tmin, tmax);
            else batchProcess.initBatchFiles(root, project, dataset, image, cmin, cmax, tmin, tmax);
            // batch process
            batchProcess.processAllImages();

            // save prefs
            Prefs.set("OMEROBATCH.root", root);
            Prefs.set("OMEROBATCH.project", project);
            Prefs.set("OMEROBATCH.dataset", dataset);
            Prefs.set("OMEROBATCH.image", image);
            Prefs.set("OMEROBATCH.process", processFile);
            Prefs.set("OMEROBATCH.cmin", cmin);
            Prefs.set("OMEROBATCH.cmax", cmax);
            Prefs.set("OMEROBATCH.tmin", tmin);
            Prefs.set("OMEROBATCH.tmax", tmax);
        }

        return;
    }

    private boolean dialog() {
        GenericDialog dialog = new GenericDialog("TAPAS BATCH");
        dialog.addStringField("Root", root, 50);
        dialog.addStringField("Project", project, 50);
        dialog.addStringField("Dataset", dataset, 50);
        dialog.addStringField("Image", image, 50);
        //dialog.addStringField("exclude", exclude, 100);
        dialog.addStringField("Channel", "" + cmin + "-" + cmax, 50);
        dialog.addStringField("Frame", "" + tmin + "-" + tmax, 50);
        dialog.addStringField("Processing", processFile, 100);
        dialog.showDialog();
        root = dialog.getNextString();
        project = dialog.getNextString().trim();
        dataset = dialog.getNextString().trim();
        image = dialog.getNextString().trim();
        int[] vals = processTextForTimeChannel(dialog.getNextString());
        cmin = vals[0];
        cmax = vals[1];
        vals = processTextForTimeChannel(dialog.getNextString());
        tmin = vals[0];
        tmax = vals[1];
        processFile = dialog.getNextString();

        return dialog.wasOKed();
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
