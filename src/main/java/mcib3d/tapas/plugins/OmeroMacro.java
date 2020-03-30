package mcib3d.tapas.plugins;

import ij.IJ;
import ij.ImagePlus;
import ij.WindowManager;
import ij.io.FileSaver;
import ij.macro.ExtensionDescriptor;
import ij.macro.Functions;
import ij.macro.MacroExtension;
import ij.measure.ResultsTable;
import ij.plugin.PlugIn;
import mcib3d.tapas.core.OmeroConnect;
import omero.gateway.model.DatasetData;
import omero.gateway.model.ImageData;
import omero.gateway.model.ProjectData;

import java.io.File;
import java.util.ArrayList;

public class OmeroMacro implements MacroExtension, PlugIn {
    private final static String OMERO_LOAD = "OMERO_LoadImage";
    private final static String OMERO_SAVE = "OMERO_SaveImage";
    private final static String OMERO_PROJECTS = "OMERO_ListProjects";
    private final static String OMERO_DATASETS = "OMERO_ListDatasets";
    private final static String OMERO_IMAGES = "OMERO_ListImages";
    private final static String OMERO_RESULTS = "OMERO_AttachResults";

    private static OmeroMacro omeroMacro = null;

    private static String delim = "\n";

    public OmeroMacro() {
        if (IJ.macroRunning()) Functions.registerExtensions(omeroMacro);
    }

    @Override
    public String handleExtension(String name, Object[] args) {
        if (name.equals(OMERO_LOAD)) {
            String[] argsOmero = extractArgsOmero(args);
            if (argsOmero != null) {
                ImagePlus plus = OmeroLoad(argsOmero[0], argsOmero[1], argsOmero[2]);
                WindowManager.setTempCurrentImage(plus);
                plus.show();
            }
        } else if (name.equals(OMERO_SAVE)) {
            String[] argsOmero = extractArgsOmero(args);
            if (argsOmero != null) {
                ImagePlus plus = WindowManager.getCurrentImage();
                if (plus != null) {
                    OmeroSave(plus, argsOmero[0], argsOmero[1], argsOmero[2]);
                }
            }
        } else if (name.equals(OMERO_RESULTS)) {
            String[] argsOmero = extractArgsOmeroName(args);
            if (argsOmero != null) {
                ResultsTable table = ResultsTable.getResultsTable();
                if (table != null) {
                    OmeroAttach(table, argsOmero[0], argsOmero[1], argsOmero[2], argsOmero[3]);
                }
            }
        } else if (name.equals(OMERO_PROJECTS)) {
            String projects = OmeroProjects();
            ((String[]) args[0])[0] = projects;
        } else if (name.equals(OMERO_DATASETS)) {
            String project = (String) args[0];
            String datasets = OmeroDatasets(project);
            ((String[]) args[1])[0] = datasets;
        } else if (name.equals(OMERO_IMAGES)) {
            String project = (String) args[0];
            String dataset = (String) args[1];
            String images = OmeroImages(project, dataset);
            ((String[]) args[2])[0] = images;
        }

        return null;
    }

    private ImagePlus OmeroLoad(String project, String dataset, String name) {
        OmeroConnect connect = new OmeroConnect();
        ImagePlus plus = null;
        try {
            connect.connect();
            ImageData imageData = connect.findOneImage(project, dataset, name, true);
            IJ.log("Loading from OMERO : " + project + "/" + dataset + "/" + name);
            plus = connect.getImage(imageData, 1, 1).getImagePlus();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            connect.disconnect();
        }
        return plus;
    }

    private void OmeroSave(ImagePlus plus, String project, String dataset, String name) {
        OmeroConnect connect = new OmeroConnect();
        try {
            //save tmp file
            String dirTmp = System.getProperty("java.io.tmpdir");
            String pathOmero = dirTmp + File.separator + name;
            if (!saveFileImage(plus, pathOmero)) IJ.log("Pb saving temp " + pathOmero);
            // upload to omero, tmp file wil lbe automatically deleted
            connect.connect();
            IJ.log("Saving to OMERO : " + project + "/" + dataset + "/" + name);
            connect.addImageToDataset(project, dataset, dirTmp + File.separator, name);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            connect.disconnect();
        }
    }

    private void OmeroAttach(ResultsTable table, String project, String dataset, String image, String name) {
        OmeroConnect connect = new OmeroConnect();
        try {
            File file = new File(System.getProperty("java.io.tmpdir") + File.separator + name);
            table.saveAs(file.getPath());
            connect.connect();
            IJ.log("Attaching to OMERO : " + project + "/" + dataset + "/" + image);
            connect.addFileAnnotation(connect.findOneImage(project, dataset, image, true), file);
            //file.delete();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            connect.disconnect();
        }
    }

    private String OmeroProjects() {
        OmeroConnect connect = new OmeroConnect();
        String s = "";
        try {
            connect.connect();
            ArrayList<ProjectData> projectsData = connect.findAllProjects();
            for (ProjectData data : projectsData) {
                s = s.concat(data.getName()).concat(delim);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            connect.disconnect();
        }

        return s;
    }

    private String OmeroDatasets(String project) {
        OmeroConnect connect = new OmeroConnect();
        String s = "";
        try {
            connect.connect();
            ArrayList<DatasetData> datasets = connect.findDatasets(connect.findProject(project, true));
            for (DatasetData data : datasets) {
                s = s.concat(data.getName()).concat(delim);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            connect.disconnect();
        }

        return s;
    }

    private String OmeroImages(String project, String dataset) {
        OmeroConnect connect = new OmeroConnect();
        String s = "";
        try {
            connect.connect();
            ArrayList<ImageData> images = connect.findAllImages(connect.findDataset(dataset, connect.findProject(project, true), true));
            for (ImageData data : images) {
                s = s.concat(data.getName()).concat(delim);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            connect.disconnect();
        }

        return s;
    }


    @Override
    public ExtensionDescriptor[] getExtensionFunctions() {
        int[] argOmero = {ARG_STRING, ARG_STRING, ARG_STRING};
        int[] argStringOutput = {ARG_STRING + ARG_OUTPUT};
        int[] argStringStringOutput = {ARG_STRING, ARG_STRING + ARG_OUTPUT};
        int[] argStringStringStringOutput = {ARG_STRING, ARG_STRING, ARG_STRING + ARG_OUTPUT};
        int[] argOmeroTable = {ARG_STRING, ARG_STRING, ARG_STRING, ARG_STRING};
        ExtensionDescriptor[] descriptors = new ExtensionDescriptor[]{
                new ExtensionDescriptor(OMERO_LOAD, argOmero, this),
                new ExtensionDescriptor(OMERO_SAVE, argOmero, this),
                new ExtensionDescriptor(OMERO_PROJECTS, argStringOutput, this),
                new ExtensionDescriptor(OMERO_DATASETS, argStringStringOutput, this),
                new ExtensionDescriptor(OMERO_IMAGES, argStringStringStringOutput, this),
                new ExtensionDescriptor(OMERO_RESULTS, argOmeroTable, this),
        };
        return descriptors;
    }

    private String[] extractArgsOmero(Object[] args) {
        if (args.length < 3) return null;
        return new String[]{(String) args[0], (String) args[1], (String) args[2]};
    }

    private String[] extractArgsOmeroName(Object[] args) {
        if (args.length < 4) return null;
        return new String[]{(String) args[0], (String) args[1], (String) args[2], (String) args[3]};
    }


    private boolean saveFileImage(ImagePlus input, String path) {
        FileSaver saver = new FileSaver(input);
        boolean saveOk;
        if (input.getNSlices() > 1) {
            saveOk = saver.saveAsTiffStack(path);
        } else {
            saveOk = saver.saveAsTiff(path);
        }

        return saveOk;
    }


    @Override
    public void run(String s) {
        if (omeroMacro == null) omeroMacro = new OmeroMacro();
    }
}
