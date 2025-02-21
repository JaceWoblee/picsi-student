package imageprocessing;

import main.Picsi;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.PaletteData;
import org.eclipse.swt.graphics.RGB;
import utils.Parallel;

public class GrayScale implements IImageProcessor {
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
        ImageData outData = ImageProcessing.createImage(inData.width, inData.height, Picsi.IMAGE_TYPE_GRAY);

        // parallel image loop
        Parallel.For(0, inData.height, v -> {
            for (int u = 0; u < inData.width; u++) {
                RGB rgb = inData.palette.getRGB(inData.getPixel(u, v));
                int gray = (rgb.red * 20 + rgb.green * 70 + rgb.blue * 10) / 100;

                outData.setPixel(u, v, gray);
            }
        });
        return outData;
    }
}
