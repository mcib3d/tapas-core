package mcib3d.tapas.IJ;

import ij.ImagePlus;
import mcib3d.tapas.core.TapasProcessorAbstract;

public class TapasProcessorIJ extends TapasProcessorAbstract<ImagePlus> {

    public TapasProcessorIJ() {
        super();
        setNameProcessor("ImageJ Processor");
    }
}
