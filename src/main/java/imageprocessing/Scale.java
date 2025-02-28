package imageprocessing;

import gui.OptionPane;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.ImageData;
import utils.Matrix;
import utils.Parallel;

public class Scale implements IImageProcessor{
    @Override
    public boolean isEnabled(int imageType) {
        return true;
    }

    @Override
    public ImageData run(ImageData inData, int imageType) {
        Object[] methods = { "Nearest Neighbor", "Bilinear" };
        int m = OptionPane.showOptionDialog("Options", SWT.ICON_INFORMATION, methods, 0);
        double a = OptionPane.showDoubleDialog("Scaling", 2);
        double newWidth = (double) inData.width * a;
        double newHeight = (double) inData.height * a;
        ImageData outData = ImageProcessing.createImage((int) newWidth, (int) newHeight, imageType);
        scale(inData, outData, a, methods[m]);
        return outData;
    }

    public static void scale(ImageData inData, ImageData outData, double scale, Object m) {
        Matrix translationMatrix1 = Matrix.translation((double) -inData.width / 2, (double) -inData.height / 2);
        Matrix scalingMatrix = Matrix.scaling(scale,scale);
        Matrix translationMatrix2 = Matrix.translation((double) outData.width / 2, (double) outData.height / 2);
        Matrix transformationMatrix = translationMatrix2.multiply(scalingMatrix.multiply(translationMatrix1));
        Matrix inverseTransformationMatrix = transformationMatrix.inverse();
        Parallel.For(0, outData.width, i -> {
            for (int j = 0; j < outData.height; j++) {
                Matrix point = new Matrix(new double[][]{{i}, {j}, {1}});
                Matrix newPoint = inverseTransformationMatrix.multiply(point);
                int x = (int) newPoint.el(0, 0);
                int y = (int) newPoint.el(1, 0);
                if (x >= 0 && x < inData.width && y >= 0 && y < inData.height) {
                    outData.setPixel(i, j, inData.getPixel(x, y));
                }
            }
        });
    }
}
