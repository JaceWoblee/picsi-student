package imageprocessing.fourier;

import imageprocessing.IImageProcessor;
import imageprocessing.ImageProcessing;
import main.Picsi;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.RGB;
import utils.Parallel;

import java.util.concurrent.atomic.AtomicInteger;

public class Dithering implements IImageProcessor {
    int m_channel;

    @Override
    public boolean isEnabled(int imageType) {
        return imageType == Picsi.IMAGE_TYPE_RGBA || imageType == Picsi.IMAGE_TYPE_RGB || imageType == Picsi.IMAGE_TYPE_INDEXED;
    }

    @Override
    public ImageData run(ImageData inData, int imageType) {
        return getChannel(inData);
    }

    public static ImageData getChannel(ImageData inData) {
        ImageData outData = ImageProcessing.createImage(inData.width, inData.height, Picsi.IMAGE_TYPE_BINARY);
        final AtomicInteger[] overload = {new AtomicInteger()};
        // parallel image loop
        Parallel.For(0, inData.height, v -> {
            for (int u = 0; u < inData.width; u++) {
                RGB rgb = inData.palette.getRGB(inData.getPixel(u, v));
                int gray = (rgb.red * 20 + rgb.green * 70 + rgb.blue * 10) / 100;
                int pixel;
                if(overload[0].addAndGet(gray) <= 128){
                    pixel = 0;
                }else{
                    pixel = 1;
                    overload[0].getAndAdd(-255);
                }
                outData.setPixel(u, v, pixel);
            }
        });
        return outData;
    }
}
