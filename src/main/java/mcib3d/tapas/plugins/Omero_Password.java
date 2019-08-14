package mcib3d.tapas.plugins;

import ij.plugin.PlugIn;

public class Omero_Password implements PlugIn {
    @Override
    public void run(String arg) {
        new OmeroPassword("OMERO connection");
    }
}