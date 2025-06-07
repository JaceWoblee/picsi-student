package imageprocessing;

import org.eclipse.swt.graphics.ImageData;

import main.Picsi;
import utils.Parallel;

/**
 * Image segmentation (binarization) using Otsu's method
 * Image foreground = black
 * Palette: background, foreground
 * @author Yasha Lüscher
 *
 */
public class Binarization implements IImageProcessor {
	public static int s_background = 0; // white
	public static int s_foreground = 1; // black

	@Override
	public boolean isEnabled(int imageType) {
		return imageType == Picsi.IMAGE_TYPE_GRAY;
	}

	@Override
	public ImageData run(ImageData inData, int imageType) {
		final int threshold = otsuThreshold(inData);
		System.out.println(threshold);
		
		return binarization(inData, threshold, false, true);
	}

	/**
	 * Binarization of grayscale image
	 * @param inData grayscale image
	 * @param threshold 
	 * @param smallValuesAreForeground true: Image foreground <= threshold, false: Image foreground > threshold
	 * @param binary true: output is binary image, false: output is grayscale image
	 * @return binarized image
	 */
	public static ImageData binarization(ImageData inData, int threshold, boolean smallValuesAreForeground, boolean binary) {

		ImageData outData = ImageProcessing.createImage(inData.width, inData.height, (binary) ? Picsi.IMAGE_TYPE_BINARY : Picsi.IMAGE_TYPE_GRAY);
		final int fg = (smallValuesAreForeground) ? s_foreground : s_background;
		final int bg = (smallValuesAreForeground) ? s_background : s_foreground;

		Parallel.For(0, inData.height, v -> {
			for (int u=0; u < inData.width; u++) {
				outData.setPixel(u, v, (inData.getPixel(u,v) <= threshold) ? fg : bg);
			}
		});
		return outData;
	}

	/**
	 * Computes a global threshold for binarization using Otsu's method
	 * @param inData grayscale image
	 * @return threshold
	 */
	public static int otsuThreshold(ImageData inData) {
		int n = inData.width * inData.height;
		int[] hist = ImageProcessing.histogram(inData, 256);

		// Convert histogram to probabilities
		double[] p = new double[256];
		for (int i = 0; i < 256; i++) {
			p[i] = (double)hist[i] / n;
		}

		double maxInterVar = 0;
		int bestThreshold = 0;

		// Try each possible threshold value
		for (int t = 0; t < 255; t++) {
			double p0 = 0;  // P0(t)
			for (int i = 0; i <= t; i++) {
				p0 += p[i];
			}
			double p1 = 1 - p0;  // P1(t)

			double m0 = 0;  // μ0
			for (int i = 0; i <= t; i++) {
				m0 += i * p[i];
			}
			m0 = (p0 != 0) ? m0/p0 : 0;

			double m1 = 0;  // μ1
			for (int i = t + 1; i < 256; i++) {
				m1 += i * p[i];
			}
			m1 = (p1 != 0) ? m1/p1 : 0;

			double m = m0 * p0 + m1 * p1;

			double interVar = p0 * (m0 - m) * (m0 - m) + p1 * (m1 - m) * (m1 - m);

			if (interVar > maxInterVar) {
				maxInterVar = interVar;
				bestThreshold = t;
			}
		}
		System.out.println("Threshold: " + bestThreshold);
		return bestThreshold;
	}
}
