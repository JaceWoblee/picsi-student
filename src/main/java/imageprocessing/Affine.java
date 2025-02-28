package imageprocessing;

import gui.OptionPane;
import org.eclipse.swt.graphics.ImageData;
import utils.Matrix;
import utils.Parallel;

public class Affine implements IImageProcessor{
    @Override
    public boolean isEnabled(int imageType) {
        return true;
    }

    @Override
    public ImageData run(ImageData inData, int imageType) {
        ImageData outData = (ImageData)inData.clone();
        affine(inData, outData);
        return outData;
    }

    public static void affine(ImageData inData, ImageData outData) {
        Matrix translationMatrix1 = Matrix.translation((double) -inData.width / 2, (double) -inData.height / 2);
        Matrix rotationMatrix = Matrix.rotation(Math.toRadians(45));
        Matrix scalingMatrix = Matrix.scaling(2,2);
        Matrix rotateAndScalingMatrix = rotationMatrix.multiply(scalingMatrix);
        Matrix translationMatrix2 = Matrix.translation((double) outData.width / 2, (double) outData.height / 2);
        Matrix transformationMatrix = translationMatrix2.multiply(rotateAndScalingMatrix.multiply(translationMatrix1));
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
