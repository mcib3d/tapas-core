package mcib3d.tapas.core;

import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.measure.Calibration;
import ij.process.ByteProcessor;
import ij.process.FloatProcessor;
import ij.process.ImageProcessor;
import ij.process.ShortProcessor;
import loci.common.services.DependencyException;
import loci.common.services.ServiceException;
import loci.common.services.ServiceFactory;
import loci.formats.ChannelSeparator;
import loci.formats.FormatException;
import loci.formats.ImageReader;
import loci.formats.meta.IMetadata;
import loci.formats.services.OMEXMLService;
import loci.plugins.util.ImageProcessorReader;
import loci.plugins.util.LociPrefs;
import mcib3d.image3d.ImageHandler;
import ome.units.quantity.Length;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.logging.Level;
import java.util.logging.Logger;

public class BioformatsReader {
    // only for one serie and 8 or 16-bis data
    ImageReader imageReader;
    IMetadata omeMeta;
    String fileName;
    String title;
    int serie = 0;
    int sx, sy, sz;
    int nbC, nbF, nbSerie;
    double resXY, resZ;
    String unit;
    boolean endian;
    int bits = 0;

    public BioformatsReader(String dir, String name) {
        this.title = name;
        this.fileName = dir + File.separator + name;
        IJ.log("Bioformats loading : " + fileName);
        imageReader = new ImageReader();
        try {
            omeMeta = loci.formats.MetadataTools.createOMEXMLMetadata();
            imageReader.setMetadataStore(omeMeta);
            imageReader.setId(this.fileName);
            imageReader.setSeries(serie);
            init();
        } catch (FormatException e) {
            IJ.log(e.getMessage());
            e.printStackTrace();
        } catch (IOException e) {
            IJ.log(e.getMessage());
            e.printStackTrace();
        }
    }

    public static ImageHandler OpenImage(String file, int channel, int time) {
        return OpenImage(new File(file), channel, 0, time);
    }

    public static ImagePlus OpenImagePlus(String file, int channel, int time) {
        return OpenImage(file, channel, time).getImagePlus();
    }


    /**
     * Main funstion to open images
     *
     * @param file         The file containing the image
     * @param channel      The channel to open, starts 0
     * @param seriesNumber The serie number, usually 0
     * @param timePoint    The time point, starts 0
     * @return the Image in ImageHandler format
     */
    public static ImageHandler OpenImage(File file, int channel, int seriesNumber, int timePoint) {
        ImageHandler res = null;

        ImageProcessorReader r = new ImageProcessorReader(new ChannelSeparator(LociPrefs.makeImageReader()));

        ServiceFactory factory;
        IMetadata meta = null;
        try {
            factory = new ServiceFactory();
            OMEXMLService service = factory.getInstance(OMEXMLService.class);
            try {
                meta = service.createOMEXMLMetadata();
                r.setMetadataStore(meta);
            } catch (ServiceException ex) {
                IJ.log("An error occurred while analysing image: " + file.getName() + " channel:" + channel + " t:" + timePoint + " s:" + seriesNumber + ex.getMessage());
            }
        } catch (DependencyException ex) {
            IJ.log("An error occurred while analysing image: " + file.getName() + " channel:" + channel + " t:" + timePoint + " s:" + seriesNumber + ex.getMessage());
        }

        try {
            IJ.log("Examining BioFormats file : " + file.getAbsolutePath());
            r.setId(file.getAbsolutePath());

            r.setSeries(seriesNumber);
            int width = r.getSizeX();
            int height = r.getSizeY();
            int sizeZ = r.getSizeZ();
            int sizeC = r.getSizeC(); // check channel
            int sizeT = r.getSizeT(); // check timepoint
            ImageStack stack = new ImageStack(width, height);
            for (int z = 0; z < sizeZ; z++) {
                ImageProcessor ip = r.openProcessors(r.getIndex(z, channel, timePoint))[0];
                stack.addSlice("" + (z + 1), ip);
            }
            res = ImageHandler.wrap(stack);
            res.setGraysLut();

            //MetadataRetrieve meta=(MetadataRetrieve)r.getMetadataStore();
            if (meta != null) {
                Length xy = meta.getPixelsPhysicalSizeX(0);
                Length z = meta.getPixelsPhysicalSizeZ(0);

                if (xy != null && z != null) {
                    //ij.IJ.log("calibration: xy"+ xy.value()+" z:"+z.value()+ "  units:"+xy.unit().getSymbol());
                    res.setScale((Double) xy.value(), (Double) z.value(), xy.unit().getSymbol());
                } else IJ.log("no calibration found");
            }
            r.close();


        } catch (Exception exc) {
            IJ.log("An error occurred while opening image: " + file.getName() + " channel:" + channel + " t:" + timePoint + " s:" + seriesNumber + exc.getMessage());
        }
        return res;
    }

    public void setSerie(int serie) {
        this.serie = serie;
    }

    private void init() {
        // calibration
        Length physSizeX = omeMeta.getPixelsPhysicalSizeX(0);
        Length physSizeY = omeMeta.getPixelsPhysicalSizeY(0);
        Length physSizeZ = omeMeta.getPixelsPhysicalSizeZ(0);
        resXY = 1;
        resZ = 1;
        unit = "pix";
        if (physSizeX != null) resXY = physSizeX.value().doubleValue();
        if (physSizeZ != null) resZ = physSizeZ.value().doubleValue();
        if (physSizeX != null) unit = physSizeX.unit().getSymbol();
        // info sizes
        sx = imageReader.getSizeX();
        sy = imageReader.getSizeY();
        sz = imageReader.getSizeZ();
        nbF = imageReader.getSizeT();
        nbC = imageReader.getSizeC();
        nbSerie = imageReader.getSeriesCount();
        endian = imageReader.isLittleEndian();
        bits = imageReader.getBitsPerPixel();
    }

    public ImagePlus getImagePlus(int c, int t) {
        ImageStack stack = readStack(t, c);
        if (stack == null) return null;
        ImagePlus plus = new ImagePlus(title, stack);
        plus.setCalibration(getCalibration());

        return plus;
    }

    public ImagePlus getImagePlusCropping(int c, int t, int startX, int startY, int startZ, int sizeX, int sizeY, int sizeZ) {
        ImageStack stack = readStackCropping(t, c, startX, startY, startZ, sizeX, sizeY, sizeZ);
        if (stack == null) return null;
        ImagePlus plus = new ImagePlus(title, stack);
        plus.setCalibration(getCalibration());

        return plus;
    }


    private ImageStack readStack(int t, int c) {
        ImageStack imageStack = new ImageStack(sx, sy);
        for (int s = 0; s < sz; s++) {
            ImageProcessor imageProcessor = readPlane(s, t, c);
            if (imageProcessor == null) return null;
            imageStack.addSlice(imageProcessor);
        }

        return imageStack;
    }

    private ImageStack readStackCropping(int t, int c, int startx, int starty, int startz, int sizex, int sizey, int sizez) {
        ImageStack imageStack = new ImageStack(sx, sy);
        int minZ = startz;
        int maxZ = startz + sizez;
        for (int s = minZ; s < maxZ; s++) {
            ImageProcessor imageProcessor = readPlane(s, t, c);
            if (imageProcessor == null) return null;
            imageStack.addSlice(imageProcessor);
        }

        return imageStack;
    }

    private ImageProcessor readPlane(int z, int t, int c) {
        ImageProcessor processor = null;
        if ((z >= sz) || (c >= nbC) || (t >= nbF)) return null;
        int idx = imageReader.getIndex(z, c, t);
        byte[] bytes;
        try {
            bytes = imageReader.openBytes(idx);
            if (bits == 16) {
                short[] shorts = convertShort(bytes, endian);
                processor = new ShortProcessor(sx, sy, shorts, null);
            } else if (bits == 8) {
                byte[] byt = convertByte(bytes);
                processor = new ByteProcessor(sx, sy, byt, null);
            } else if (bits == 32) { // not working
                float[] floats = convertFloat(bytes);
                processor = new FloatProcessor(sx, sy, floats, null);
            }
        } catch (FormatException e) {
            IJ.log(e.getMessage());
            e.printStackTrace();
            return null;
        } catch (IOException e) {
            IJ.log(e.getMessage());
            e.printStackTrace();
            return null;
        }

        return processor;
    }

    private ImageProcessor readPlaneCropping(int z, int t, int c, int startx, int starty, int sizex, int sizey) {
        ImageProcessor processor = null;
        if ((z >= sz) || (c >= nbC) || (t >= nbF)) return null;
        int idx = imageReader.getIndex(z, c, t);
        byte[] bytes;
        try {
            //bytes = imageReader.openBytes(idx);
            bytes = imageReader.openBytes(idx, startx, starty, sizex, sizey);
            if (bits == 16) {
                short[] shorts = convertShort(bytes, endian);
                processor = new ShortProcessor(sx, sy, shorts, null);
            } else if (bits == 8) {
                byte[] byt = convertByte(bytes);
                processor = new ByteProcessor(sx, sy, byt, null);
            } else if (bits == 32) { // not working
                float[] floats = convertFloat(bytes);
                processor = new FloatProcessor(sx, sy, floats, null);
            }
        } catch (FormatException e) {
            IJ.log(e.getMessage());
            e.printStackTrace();
            return null;
        } catch (IOException e) {
            IJ.log(e.getMessage());
            e.printStackTrace();
            return null;
        }

        return processor;
    }


    // FIXME
    private float[] convertFloat(byte[] bytes) {
        float[] floats = new float[sx * sy];
        for (int i = 0; i < floats.length; i++) {
            if (endian) {
                //ByteBuffer byteBuffer = ByteBuffer.allocate(4);
                //byteBuffer.put(new byte[]{bytes[4 * i + 0], bytes[4 * i + 1], bytes[4 * i + 2], bytes[4 * i + 3]});
                //floats[i] = byteBuffer.getFloat();
                //floats[i] = (float) (bytes[4 * i + 3] << 24 | bytes[4 * i + 2] << 16 | bytes[4 * i + 1] << 8 | bytes[4 * i + 0] & 0xFFFF);
                floats[i] = Float.intBitsToFloat(bytes[4 * i] ^ bytes[4 * i + 1] << 8 ^ bytes[4 * i + 2] << 16 ^ bytes[4 * i + 3] << 24);
            } else {
                //ByteBuffer byteBuffer = ByteBuffer.allocate(4);
                //byteBuffer.put(new byte[]{bytes[4 * i + 0], bytes[4 * i + 1], bytes[4 * i + 2], bytes[4 * i + 3]});
                //floats[i] = byteBuffer.getFloat();
                //floats[i] = (float) (bytes[4 * i + 0] << 24 | bytes[4 * i + 1] << 16 | bytes[4 * i + 2] << 8 | bytes[4 * i + 3] & 0xFFFF);
                floats[i] = Float.intBitsToFloat(bytes[4 * i + 3] ^ bytes[4 * i + 2] << 8 ^ bytes[4 * i + 1] << 16 ^ bytes[4 * i] << 24);
            }
        }
        return floats;
    }

    private byte[] convertByte(byte[] bytes) {
        byte[] byt = new byte[sx * sy];
        for (int i = 0; i < byt.length; i++) {
            byt[i] = (byte) (bytes[i] & 0xFF);
        }
        return byt;
    }


    private short[] convertShort(byte[] bytes, boolean endian) {
        short[] shorts = new short[sx * sy];
        for (int i = 0; i < shorts.length; i++) {
            if (endian)
                shorts[i] = (short) (bytes[2 * i + 1] << 8 | bytes[2 * i] & 0xFF);
            else shorts[i] = (short) (bytes[2 * i] << 8 | bytes[2 * i + 1] & 0xFF);
        }
        return shorts;
    }

    public Calibration getCalibration() {
        Calibration calibration = new Calibration();
        calibration.pixelWidth = resXY;
        calibration.pixelHeight = resXY;
        calibration.pixelDepth = resZ;
        calibration.setUnit(unit);

        return calibration;
    }

    public double[] getScale() {
        return new double[]{resXY, resXY, resZ};
    }

    public int[] getImageSizesXYZCT() {
        return new int[]{sx, sy, sz, nbC, nbF};
    }


}
