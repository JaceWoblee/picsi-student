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

/**
 * Flood Filling
 * @author Yasha Lüscher
 *
 */
public class FloodFilling implements IImageProcessor {
	public static int s_background = 1;
	public static int s_foreground = 0;

	@Override
	public boolean isEnabled(int imageType) {
		return imageType == Picsi.IMAGE_TYPE_GRAY;
	}

	@Override
	public ImageData run(ImageData inData, int imageType) {
		final int threshold = Binarization.otsuThreshold(inData);
		ImageData processedData = inData;

		processedData = Binarization.binarization(processedData, threshold, false, false);

		processedData = imageprocessing.SaltAndPepperFilter.applyClosing(processedData);
		processedData = imageprocessing.SaltAndPepperFilter.applyOpening(processedData);

		int nLabels = floodFill(processedData);
		System.out.println("Anzahl Münzen: " + nLabels);

		return falseColor(processedData, nLabels + 2);
	}

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

						imageData.setPixel(x, y, currentLabel * 10);

						if (y > 0 && imageData.getPixel(x, y - 1) == s_foreground) {
							queue.add(new int[] {x, y - 1});
						}
						if (x < imageData.width - 1 && imageData.getPixel(x + 1, y) == s_foreground) {
							queue.add(new int[] {x + 1, y});
						}
						if (y < imageData.height - 1 && imageData.getPixel(x, y + 1) == s_foreground) {
							queue.add(new int[] {x, y + 1});
						}
						if (x > 0 && imageData.getPixel(x - 1, y) == s_foreground) {
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
	 * Creates a new, safe false-color presentation of a labeled grayscale image.
	 * @param inData labeled grayscale image
	 * @param n number of different false colors (<= 256)
	 * @return A new, valid indexed-color image
	 */
	public static ImageData falseColor(ImageData inData, int n) {
		assert ImageProcessing.determineImageType(inData) == Picsi.IMAGE_TYPE_GRAY;
		assert 0 < n && n <= 256;

		RGB[] colors = new RGB[n];

		colors[0] = new RGB(0, 0, 0);

		Random rand = new Random(42);
		for (int i = 1; i < n; i++){
			colors[i] = new RGB(
					rand.nextInt(256),
					rand.nextInt(256),
					rand.nextInt(256)
			);
		}
		PaletteData newPalette = new PaletteData(colors);

		ImageData outData = new ImageData(inData.width, inData.height, 8, newPalette);

		for (int v = 0; v < outData.height; v++) {
			for (int u = 0; u < outData.width; u++) {
				int label = inData.getPixel(u, v);
				int colorIndex = (label == s_background) ? 0 : (label / 10 - 1);
				outData.setPixel(u, v, colorIndex);
			}
		}
		return outData;
	}
}