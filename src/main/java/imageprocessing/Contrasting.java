package imageprocessing;

import org.eclipse.swt.graphics.ImageData;

import java.sql.SQLOutput;

/**
 * Image inverter
 *
 * @author Christoph Stamm
 */
public class Contrasting implements IImageProcessor {

    @Override
    public boolean isEnabled(int imageType) {
        return true;
    }

    @Override
    public ImageData run(ImageData inData, int imageType) {
        ImageData outData = (ImageData) inData.clone();


        return contrast(outData);
    }

    public static ImageData contrast(ImageData outData) {
        int[] histogram = ImageProcessing.histogram(outData, 255);
        int wholeSize = outData.height * outData.width;
        int qLow = 0, qHigh = 255;

        int sizeTilNow = 0;
        for(int i = 0; i < histogram.length; i++){
            sizeTilNow += histogram[i];
            if(sizeTilNow >= wholeSize/100*2){
                qLow = i;
                break;
            }
        }
        sizeTilNow = 0;
        for(int i = histogram.length-1; i >= 0; i--){
            sizeTilNow += histogram[i];
            if(sizeTilNow >= wholeSize/100*2){
                qHigh = i;
                break;
            }
        }
        int[] lookupT = new int[256];
        for(int i = 0; i < 256; i++){
            if(i <= qLow){
                lookupT[i] = 0;
            }else if(i >= qHigh){
                lookupT[i] = 255;
            }else {
                lookupT[i] = (i - qLow) * (histogram.length-1) / (qHigh - qLow);
            }
        }
        return ImageProcessing.applyLUT(outData, lookupT);
    }
}
