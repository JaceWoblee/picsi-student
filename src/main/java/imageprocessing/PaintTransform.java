package imageprocessing;

import main.Picsi;
import org.eclipse.swt.graphics.ImageData;

public class PaintTransform implements IImageProcessor {

    private static final double WR = 0.299;
    private static final double WG = 0.299;
    private static final double WB = 0.299;

    @Override
    public boolean isEnabled(int imageType) {
        return imageType == Picsi.IMAGE_TYPE_GRAY;
    }

    @Override
    public ImageData run(ImageData inData, int imageType) {
        ImageData outdata = (ImageData) inData.clone();
        paintTransform(outdata);
        return outdata;
    }

    public static void paintTransform(ImageData inData) {
        
    }

}
