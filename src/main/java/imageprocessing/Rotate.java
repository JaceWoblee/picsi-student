package imageprocessing;

import gui.OptionPane;
import org.eclipse.swt.graphics.ImageData;
import utils.Matrix;
import utils.Parallel;

public class Rotate implements IImageProcessor {


    @Override
    public boolean isEnabled(int imageType) {
        return true;
    }

    @Override
    public ImageData run(ImageData inData, int imageType) {
        double a = OptionPane.showDoubleDialog("Rotation", 45);
        double newWidth = (double) inData.width  * Math.abs(Math.cos(Math.toRadians(a))) + (double) inData.height * Math.abs(Math.sin(Math.toRadians(a)));
        double newHeight = (double) inData.width  * Math.abs(Math.sin(Math.toRadians(a))) + (double) inData.height * Math.abs(Math.cos(Math.toRadians(a)));

        ImageData newOutData = ImageProcessing.createImage((int) newWidth, (int) newHeight, imageType);
        rotate(inData, a, newOutData);

        return newOutData;
    }

    public static void rotate(ImageData inData, double rotation, ImageData outData) {
        Matrix translationMatrix1 = Matrix.translation((double) -inData.width / 2, (double) -inData.height / 2);
        Matrix rotationMatrix = Matrix.rotation(Math.toRadians(rotation));
        Matrix translationMatrix2 = Matrix.translation((double) outData.width / 2, (double) outData.height / 2);
        Matrix transformationMatrix = translationMatrix2.multiply(rotationMatrix.multiply(translationMatrix1));
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
