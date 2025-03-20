package imageprocessing;

import org.eclipse.swt.graphics.ImageData;

/**
 * Image inverter
 *
 * @author Christoph Stamm
 */
public class LinContrasting implements IImageProcessor {

    @Override
    public boolean isEnabled(int imageType) {
        return true;
    }

    @Override
    public ImageData run(ImageData inData, int imageType) {
        ImageData outData = (ImageData) inData.clone();


        return linContrast(outData);
    }

    public static ImageData linContrast(ImageData outData) {
        int[] histogram = ImageProcessing.histogram(outData, 255);
        int wholeSize = outData.height * outData.width;
        int count = 0;
        for (int i = 0; i < histogram.length; i++) {
            count += histogram[i];
            histogram[i] = count;
        }
        int[] newHist = new int[256];
        for (int i = 0; i < histogram.length; i++) {
            newHist[i] = histogram[i] * (histogram.length-1) / wholeSize;
        }

        return ImageProcessing.applyLUT(outData, newHist);
    }
}
