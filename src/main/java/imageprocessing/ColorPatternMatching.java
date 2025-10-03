package imageprocessing;

import java.util.ArrayList;

import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Display;

import gui.RectTracker;
import imageprocessing.ROI;
import main.Picsi;
import utils.BoundedPQ;
import utils.Parallel;

/**
 * Color Pattern Matching based on color histogram
 * 
 * @author Christoph Stamm
 *
 */
public class ColorPatternMatching implements IImageProcessor {
	final static int Luma = 4;
	final static int Chroma = 8;
	final static int Saturation = 4;

	public static class PMResult implements Comparable<PMResult> {
		public ROI m_roi;
		public double m_corr;
		
		public PMResult(ROI roi, double correlation) {
			m_roi = roi; m_corr = correlation;
		}
		
		public int compareTo(PMResult pm) {
			return Double.compare(m_corr, pm.m_corr);
		}
		
		@Override
		public String toString() {
			return m_roi + ", corr = " + m_corr;
		}
	}
	
	@Override
	public boolean isEnabled(int imageType) {
		return imageType == Picsi.IMAGE_TYPE_RGB;
	}

	@Override
	public ImageData run(ImageData inData, int imageType) {
		final int nResults = 10000;	// search nResults best matches

		// let the user choose the ROI using a tracker
		RectTracker rt = new RectTracker();
		Rectangle pr = rt.track(inData.width/2, inData.height/2, 70, 50);
		
		// create color pattern
		ROI pattern = new ROI(inData, pr);
		
		// pattern matching
		BoundedPQ<PMResult> results = pm(inData, pattern, m == 0, nResults);
		
		// create output
		ImageData outData = ImageProcessing.createImage(inData.width, inData.height, Picsi.IMAGE_TYPE_RGB);
		
		Parallel.For(0, inData.height, v -> {
			for(int u = 0; u < inData.width; u++) {
				RGB rgb = inData.palette.getRGB(inData.getPixel(u,v));
				outData.setPixel(u, v, outData.palette.getPixel(rgb));
			}
		});
		
		return createOutput(outData, results, nResults);
	}

	/**
	 * Pattern matching based on color histogram matching
	 * @param inData
	 * @param pattern
	 * @param nResults number of best results
	 * @return results
	 */
	public static BoundedPQ<PMResult> pm(ImageData inData, ROI pattern, int nResults) {
		final int[] chP = colorHist(pattern);
		final int ph = pattern.getHeight();
		final int pw = pattern.getWidth();
		final double size = ph*pw*2; // for almost each pixel the color histogram is increased by 2
		BoundedPQ<PMResult> results = new BoundedPQ<>(nResults);
		
		Parallel.For(0, inData.height - ph + 1, 
			// creator
			() -> new BoundedPQ<PMResult>(nResults),
			// loop body
			(s, pq) -> {
				for (int r=0; r < inData.width - pw + 1; r++) {
					// compute color histogram of image
					final ROI roi = new ROI(inData, new Rectangle(r, s, pw, ph));
					final int[] chI = colorHist(roi);
					
					// compute histogram similarity
					double sum = 0;
					for(int i = 0; i < chP.length; i++) {
						sum += Math.pow((chI[i] - chP[i])/size, 2);
					}

					pq.add(new PMResult(roi, 1 - Math.sqrt(sum)));
				}
			},
			// reducer
			pq -> {
				for(PMResult r: pq) results.add(r);
			}
		);
		return results;
	}

	/**
	 * compute and return color histogram for given ROI
	 * @param roi
	 * @return
	 */
	public static int[] colorHist(ROI roi) {
		int[] colorHist = new int[Luma + Chroma*Saturation];
		
		for(int v = 0; v < roi.getHeight(); v++) {
			for(int u = 0; u < roi.getWidth(); u++) {
				// TODO
			}
		}
		return colorHist;
	}

	/**
	 * Show best matching results as rectangles in the input image
	 * @param outData output image
	 * @param pq
	 * @param nResults
	 * @return
	 */
	private ImageData createOutput(ImageData outData, BoundedPQ<PMResult> pq, int nResults) {
		ArrayList<PMResult> results = new ArrayList<>();
		
		// create image and write text into image
		Display display = Picsi.s_shell.getDisplay();
		Image output = new Image(display, outData);
		GC gc = new GC(output);

		// set font
		gc.setForeground(new Color(display, 255, 0, 0)); // red
		gc.setBackground(new Color(display, 255, 255, 255)); // white
		gc.setFont(new Font(display, "Segoe UI", 8, 0));
		
		for (int i=0; i < nResults; i++) {
			final PMResult pm = pq.removeMax();
			//System.out.println(pm);
			
			if (pm != null) {
				int j = 0;
				while(j < results.size() && !pm.m_roi.overlaps(results.get(j).m_roi)) j++;
				if (j == results.size()) {
					final Rectangle r = pm.m_roi.m_rect;

					results.add(pm);

					gc.drawRectangle(r);
					gc.drawText(String.format("%.2f", pm.m_corr), r.x, r.y + r.height, true);
				}
			}
		}
		
		gc.dispose();
		
		outData = output.getImageData();
		output.dispose();
		return outData;
	}
	
}
