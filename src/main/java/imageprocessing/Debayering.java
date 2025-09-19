package imageprocessing;

import main.Picsi;
import utils.Parallel;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.RGB;

import gui.OptionPane;
import imageprocessing.IImageProcessor;
import imageprocessing.ImageProcessing;

/**
 * Debayering
 * @author Christoph Stamm
 *
 */
public class Debayering implements IImageProcessor {
    static final int Bypp = 3;

    @Override
    public boolean isEnabled(int imageType) {
        return imageType == Picsi.IMAGE_TYPE_GRAY;
    }

    @Override
    public ImageData run(ImageData inData, int imageType) {
        Object[] outputTypes = { "Simple", "Good" };
        int ch = OptionPane.showOptionDialog("Debayering algorithms", SWT.ICON_QUESTION, outputTypes, 0);
        if (ch < 0) return null;

        // Debayering of raw input image
        if (ch == 0) return debayering1(inData);
        else return debayering2(inData);
    }

    /**
     * TODO: Simple Debayering
     * @param inData raw data
     * @return RGB image
     */
    private ImageData debayering1(ImageData inData) {
        ImageData outData = ImageProcessing.createImage(inData.width/2, inData.height/2, Picsi.IMAGE_TYPE_RGB);

        for(int y = 0; y < inData.height; y+=2) {
            for(int x = 0; x < inData.width; x+=2) {
                RGB b = inData.palette.getRGB(inData.getPixel(x,y));
                RGB g1 = inData.palette.getRGB(inData.getPixel(x+1,y));
                RGB g2 = inData.palette.getRGB(inData.getPixel(x,y+1));
                RGB r = inData.palette.getRGB(inData.getPixel(x+1,y+1));

                RGB out = new RGB(r.red, (g1.green + g2.green) /2, b.blue);
                int i = outData.palette.getPixel(out);

                outData.setPixel(x/2, y/2, i);
            }
        }
        return outData;
    }

    /**
     * Advanced Debayering
     * @param inData raw data
     * @return RGB image
     */
    private ImageData debayering2(ImageData inData) {
        ImageData outData = ImageProcessing.createImage(inData.width, inData.height, Picsi.IMAGE_TYPE_RGB);
        RGB rgb = new RGB(0, 0, 0);

        // interpolation of green channel
        Parallel.For(0, outData.height, v -> {
            for (int u=0; u < outData.width; u++) {
                outData.setPixel(u, v, outData.palette.getPixel(rgb));
            }
        });

        // interpolation of blue and red channels
        Parallel.For(0, outData.height, v -> {
            for (int u=0; u < outData.width; u++) {
                outData.setPixel(u, v, outData.palette.getPixel(rgb));
            }
        });
        return outData;
    }

}
