package imageprocessing;

import main.Picsi;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.RGB;
import utils.Parallel;

import java.util.concurrent.atomic.AtomicInteger;

public class Dithering implements IImageProcessor {

    @Override
    public boolean isEnabled(int imageType) {
        return imageType == Picsi.IMAGE_TYPE_GRAY;
    }

    @Override
    public ImageData run(ImageData inData, int imageType) {
        return doDithering(inData);
    }

    public static ImageData doDithering(ImageData inData) {
        ImageData outData = ImageProcessing.createImage(inData.width, inData.height, Picsi.IMAGE_TYPE_BINARY);

        // Define the Stucki kernel as an array of {dx, dy, weight}
        int[][] kernel = {
                {1, 0, 8}, {2, 0, 4},
                {-2, 1, 2}, {-1, 1, 4}, {0, 1, 8}, {1, 1, 4}, {2, 1, 2},
                {-2, 2, 1}, {-1, 2, 2}, {0, 2, 4}, {1, 2, 2}, {2, 2, 1}
        };

        for (int v = 0; v < inData.height; v++) {
            for (int u = 0; u < inData.width; u++) {
                int grayness = inData.getPixel(u, v);
                int overload;

                // Thresholding
                if (grayness <= 128) {
                    outData.setPixel(u, v, 1);
                    overload = grayness;
                } else {
                    outData.setPixel(u, v, 0);
                    overload = grayness - 255;
                }

                // Normalize the error for distribution
                int normalizedError = overload / 42;

                // Distribute the error using the kernel
                for (int[] k : kernel) {
                    int dx = k[0];
                    int dy = k[1];
                    int weight = k[2];

                    int newX = u + dx;
                    int newY = v + dy;

                    // Check image boundaries
                    if (newX >= 0 && newX < inData.width && newY >= 0 && newY < inData.height) {
                        // Accumulate the weighted error on the target pixel
                        // Assuming inData.setPixel here is used to modify the pixel value.
                        // If you want to accumulate the error rather than overwrite,
                        // you might need to retrieve the current value first:
                        int currentValue = inData.getPixel(newX, newY);
                        inData.setPixel(newX, newY, currentValue + normalizedError * weight);
                    }
                }
            }
        }

        return outData;
    }

}
