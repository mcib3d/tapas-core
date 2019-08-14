package mcib3d.tapas;


import ij.ImagePlus;
import mcib3d.tapas.core.ImageInfo;

public interface TapasProcessing {

    boolean setParameter(String id, String value) ;

    ImagePlus execute(ImagePlus input);

    String getName() ;

    String[] getParameters();

    String getParameter(String id);

    void setCurrentImage(ImageInfo currentImage) ;


}