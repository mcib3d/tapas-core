package mcib3d.tapas.core;

import ij.IJ;
import mcib3d.tapas.TapasProcessing;
import mcib3d.tapas.TapasProcessingAbstract;
import mcib3d.utils.Logger.AbstractLog;
import mcib3d.utils.Logger.IJLog;

import java.time.Instant;
import java.util.ArrayList;

public abstract class TapasProcessorAbstract<Image> {
    private ArrayList<TapasProcessingAbstract<Image>> processings;
    private ArrayList<ImageInfo> allImages;
    AbstractLog log = new IJLog();
    private String nameProcessor="Unknown Processor";

    public TapasProcessorAbstract() {
    }

    public void init(ArrayList<TapasProcessingAbstract<Image>> processings, ArrayList<ImageInfo> allImages) {
        this.processings = processings;
        this.allImages = allImages;
    }

    public void init(ArrayList<TapasProcessingAbstract<Image>> processings) {
        this.processings = processings;
    }

    public String getNameProcessor() {
        return nameProcessor;
    }

    public void setNameProcessor(String nameProcessor) {
        this.nameProcessor = nameProcessor;
    }

    public AbstractLog getLog() {
        return log;
    }

    public void setLog(AbstractLog log) {
        this.log = log;
    }

    public boolean processAllImages() {
        log.log("PROCESSING " + allImages.size() + " images");
        int c = 1;

        for (ImageInfo imageInfo : allImages) {
            log.log(" ");
            log.log("---------- Processing " + c + "/" + allImages.size() + " ----------");
            Image image = processOneImage(imageInfo);
            if (image == null) {
                log.log("***************** Pb after processing " + imageInfo.getImage());
                return false;
            }
            c++;
        }
        log.log("********** PROCESSING DONE **********");

        return true;
    }

    public Image processOneImage(ImageInfo info) {
        Image img = null;
        log.log("PROCESSING " + info);
        Instant start = Instant.now();
        for (TapasProcessingAbstract processing : processings) {
            processing.setCurrentImage(info);
            log.log("* " + processing.getName());
            img = (Image) processing.execute(img);
            if (img == null) {
                log.log("Processing stopped.");
                return null;
            }
            System.gc();
        }
        Instant end = Instant.now();
        log.log("Processing took " + java.time.Duration.between(start, end));

        return img;
    }
}
