package imageprocessing.segmentation;

import java.util.Deque;
import java.util.LinkedList;
import java.util.Random;

import imageprocessing.Binarization;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.PaletteData;
import org.eclipse.swt.graphics.RGB;

import imageprocessing.IImageProcessor;
import imageprocessing.ImageProcessing;
import main.Picsi;
import org.w3c.dom.css.RGBColor;
import utils.Parallel;

/**
 * Flood Filling
 * @author Christoph Stamm
 *
 */
public class FloodFilling implements IImageProcessor {
	public static int s_background = 0; // white
	public static int s_foreground = 1; // black

	@Override
	public boolean isEnabled(int imageType) {
		return imageType == Picsi.IMAGE_TYPE_GRAY;
	}

	@Override
	public ImageData run(ImageData inData, int imageType) {
		final int threshold = Binarization.otsuThreshold(inData);
		ImageData grayData = inData;

		grayData = Binarization.binarization(grayData, threshold, false, false);

		int nLabels = floodFill(grayData);
		System.out.println("Anzahl Mnzen: " + nLabels);

		//return falseColor(grayData, nLabels + 2);
		return falseColor(grayData,nLabels + 2);
	}

	/**
	 * Labeling of a binarized grayscale image
	 * @param inData input: grayscale image with intensities 0 and 1 only, output: labeled foreground regions
	 * @return number of regions
	 */
	public static int floodFill(ImageData imageData) {
		assert ImageProcessing.determineImageType(imageData) == Picsi.IMAGE_TYPE_GRAY;
		int currentLabel = 2;
		Deque<int[]> queue = new LinkedList<>();

		for (int i = 0; i < imageData.height; i++) {
			for (int j = 0; j < imageData.width; j++) {
				if (imageData.getPixel(j, i) == s_foreground) {
					queue.add(new int[] {j, i});

					while (!queue.isEmpty()) {
						int[] currentPixel = queue.poll();
						int x = currentPixel[0];
						int y = currentPixel[1];

						if (imageData.getPixel(x, y) != s_foreground) {
							continue;
						}

						imageData.setPixel(x, y, currentLabel*10);
						if(y > 0 && imageData.getPixel(x, y - 1) == s_foreground) {
							queue.add(new int[] {x, y - 1});
						}
						if(x < imageData.width - 1 && imageData.getPixel(x + 1, y) == s_foreground) {
							queue.add(new int[] {x + 1, y});
						}
						if(y < imageData.height - 1 && imageData.getPixel(x, y + 1) == s_foreground) {
							queue.add(new int[] {x, y + 1});
						}
						if(x > 0 && imageData.getPixel(x - 1, y) == s_foreground) {
							queue.add(new int[] {x - 1, y});
						}
					}
					currentLabel++;
				}
			}
		}

		return currentLabel - 2;
	}

	/**
	 * False color presentation of labeled grayscale image
	 * @param inData labeled grayscale image
	 * @param n number of different false colors (<= 256)
	 * @return indexed color image
	 */
	public static ImageData falseColor(ImageData inData, int n) {
		assert ImageProcessing.determineImageType(inData) == Picsi.IMAGE_TYPE_GRAY;
		assert 0 < n && n <= 256;

		RGB[] palette = new RGB[n];

		palette[0] = new RGB(255,255,255);

		Random rand = new Random(42);
		for (int i = 1; i < n; i++){
			palette[i] = new RGB(
					rand.nextInt(256),
					rand.nextInt(256),
					rand.nextInt(256)
			);
		}
		inData.palette = new PaletteData(palette);

		for (int v = 0; v < inData.height; v++) {
			for (int u = 0; u < inData.width; u++) {
				int label = inData.getPixel(u, v);
				int colorIndex = (label == 0) ? 0 : (label/10 - 1);
				inData.setPixel(u, v, colorIndex);
			}
		}

		return inData;
	}
}