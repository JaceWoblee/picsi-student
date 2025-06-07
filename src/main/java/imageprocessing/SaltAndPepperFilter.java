package imageprocessing;

import org.eclipse.swt.graphics.ImageData;

/**
 * A utility class to remove "salt and pepper" noise from binary images
 * using morphological Opening and Closing operations.
 * <p>
 * SaltAndPepperFilter
 *
 * @author Yasha Lüscher
 */
public class SaltAndPepperFilter {

    public static ImageData applyOpening(ImageData inData) {
        boolean[][] struct = MorphologicFilter.s_circle3;
        int cx = 1;
        int cy = 1;

        System.out.println("Applying Opening to remove salt noise...");
        ImageData erodedData = MorphologicFilter.erosion(inData, struct, cx, cy);
        ImageData openedData = MorphologicFilter.dilation(erodedData, struct, cx, cy);
        return openedData;
    }

    public static ImageData applyClosing(ImageData inData) {
        boolean[][] struct = MorphologicFilter.s_circle3;
        int cx = 1;
        int cy = 1;

        System.out.println("Applying Closing to remove pepper noise...");
        ImageData dilatedData = MorphologicFilter.dilation(inData, struct, cx, cy);
        ImageData closedData = MorphologicFilter.erosion(dilatedData, struct, cx, cy);
        return closedData;
    }
}
