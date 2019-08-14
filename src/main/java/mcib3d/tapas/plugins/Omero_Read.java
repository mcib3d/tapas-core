package mcib3d.tapas.plugins;

import ij.IJ;
import ij.ImagePlus;
import ij.Prefs;
import ij.gui.GenericDialog;
import ij.plugin.Concatenator;
import ij.plugin.HyperStackConverter;
import mcib3d.image3d.ImageByte;
import mcib3d.image3d.ImageHandler;
import mcib3d.tapas.core.OmeroConnect;
import omero.gateway.model.ImageData;

public class Omero_Read implements ij.plugin.PlugIn {
    private String project = "project";
    private String dataset = "dataset";
    private String image = "image";
    private int c0 = 1;
    private int c1 = 1;
    private int t0 = 1;
    private int t1 = 1;
    private int binningXY = 1;
    private int binningZ = 1;
    private boolean strict = true;

    @Override
    public void run(String s) {
        if (dialog()) {
            Prefs.set("OMEROREAD.project", project);
            Prefs.set("OMEROREAD.dataset", dataset);
            Prefs.set("OMEROREAD.image", image);
            Prefs.set("OMEROREAD.frame0.int", t0);
            Prefs.set("OMEROREAD.frame1.int", t1);
            Prefs.set("OMEROREAD.channel0.int", c0);
            Prefs.set("OMEROREAD.channel1.int", c1);
            Prefs.set("OMEROREAD.binXY.int", binningXY);
            Prefs.set("OMEROREAD.binZ.int", binningZ);
            try {
                OmeroConnect connect = new OmeroConnect();
                connect.setLog(true);
                connect.connect();
                ImageData imageData = connect.findOneImage(project, dataset, image, strict);
                if (imageData == null) {
                    IJ.log("Image not found ");
                    return;
                }

                //Runtime rt = Runtime.getRuntime();
                IJ.log("Found image " + imageData.getName());
                // read multiple frames
                // first frame
                IJ.log("Reading first image t" + t0 + " c" + c0);
                ImageHandler handler = connect.getImageBin(imageData, t0, c0, binningXY, binningZ);
                //IJ.log("Memory used " + ((rt.totalMemory() - rt.freeMemory()) / 1024 / 1024) + " mb");
                ImagePlus stack = handler.getImagePlus();
                // next channels
                for (int c = c0 + 1; c <= c1; c++) {
                    IJ.log("Reading image t" + t0 + " c" + c);
                    ImageHandler handler2 = connect.getImageBin(imageData, t0, c, binningXY, binningZ);
                    stack = Concatenator.run(stack, handler2.getImagePlus());
                }

                for (int t = t0 + 1; t <= t1; t++) {
                    for (int c = c0; c <= c1; c++) {
                        IJ.log("Reading image t" + t + " c" + c);
                        ImageHandler handler2 = connect.getImageBin(imageData, t, c, binningXY, binningZ);
                        stack = Concatenator.run(stack, handler2.getImagePlus());
                    }
                }
                connect.disconnect();
                int nbStacks = (t1 - t0 + 1) * (c1 - c0 + 1);
                if (nbStacks > 1)
                    stack = HyperStackConverter.toHyperStack(stack, c1 - c0 + 1, handler.sizeZ, t1 - t0 + 1, "xyzct", "composite");
                stack.show();
                IJ.log("Done");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        ImageHandler handler = new ImageByte("test", 10, 10, 1);
        handler.getBinaryData();
    }

    private boolean dialog() {
        // read Prefs
        project = Prefs.get("OMEROREAD.project", "project");
        dataset = Prefs.get("OMEROREAD.dataset", "dataset");
        image = Prefs.get("OMEROREAD.image", "image");
        c0 = Prefs.getInt("OMEROREAD.channel0.int", 1);
        c1 = Prefs.getInt("OMEROREAD.channel0.int", 1);
        t0 = Prefs.getInt("OMEROREAD.frame0.int", 1);
        t1 = Prefs.getInt("OMEROREAD.frame1.int", 1);
        binningXY = Prefs.getInt("OMEROREAD.binXY.int", 1);
        binningZ = Prefs.getInt("OMEROREAD.binZ.int", 1);
        GenericDialog dialog = new GenericDialog("OMERO LOAD");
        dialog.addStringField("Project", project, 50);
        dialog.addStringField("Dataset", dataset, 50);
        dialog.addStringField("Image", image, 50);
        dialog.addMessage("Binning");
        dialog.addNumericField("BinningXY", binningXY, 0);
        dialog.addNumericField("BinningZ", binningZ, 0);
        dialog.addMessage("Channels");
        dialog.addNumericField("Channel0", c0, 0);
        dialog.addNumericField("Channel1", c1, 0);
        dialog.addMessage("Frames");
        dialog.addNumericField("Frame0", t0, 0);
        dialog.addNumericField("Frame1", t1, 0);
        //dialog.addCheckbox("Strict name", strict);
        dialog.showDialog();
        project = dialog.getNextString();
        dataset = dialog.getNextString();
        image = dialog.getNextString();
        binningXY = (int) dialog.getNextNumber();
        binningZ = (int) dialog.getNextNumber();
        c0 = (int) dialog.getNextNumber();
        c1 = (int) dialog.getNextNumber();
        t0 = (int) dialog.getNextNumber();
        t1 = (int) dialog.getNextNumber();

        //strict = dialog.getNextBoolean();

        return dialog.wasOKed();
    }
}
