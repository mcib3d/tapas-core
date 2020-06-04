package mcib3d.tapas.core;

public interface TapasProcessingAbstract<Image> {
    boolean setParameter(String id, String value) ;

    Image execute(Image input);

    String getName() ;

    String[] getParameters();

    String getParameter(String id);

    void setCurrentImage(ImageInfo currentImage) ;
}
