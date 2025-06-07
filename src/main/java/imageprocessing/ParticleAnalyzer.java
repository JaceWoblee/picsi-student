package imageprocessing;

import imageprocessing.segmentation.FloodFilling;
import org.eclipse.swt.graphics.ImageData;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import main.Picsi;

/**
 * Performs a complete particle analysis on a grayscale image.
 * This class orchestrates binarization, noise removal, segmentation,
 * and property calculation for each detected particle.
 *
 * @author Yasha Lüscher
 */
public class ParticleAnalyzer implements IImageProcessor {

    private static class Particle {
        final int label;
        int area = 0;
        int minX = Integer.MAX_VALUE;
        int minY = Integer.MAX_VALUE;
        int maxX = Integer.MIN_VALUE;
        int maxY = Integer.MIN_VALUE;

        List<Integer> xCoords = new ArrayList<>();
        List<Integer> yCoords = new ArrayList<>();

        double centroidX;
        double centroidY;
        double eccentricity;

        Particle(int label) {
            this.label = label;
        }

        void addPixel(int x, int y) {
            xCoords.add(x);
            yCoords.add(y);
            area++;
            if (x < minX) minX = x;
            if (x > maxX) maxX = x;
            if (y < minY) minY = y;
            if (y > maxY) maxY = y;
        }

        void calculateProperties() {
            if (area == 0) return;

            long sumX = 0;
            long sumY = 0;
            for (int x : xCoords) sumX += x;
            for (int y : yCoords) sumY += y;
            centroidX = (double) sumX / area;
            centroidY = (double) sumY / area;

            double mu_xx = 0, mu_yy = 0, mu_xy = 0;
            for (int i = 0; i < area; i++) {
                double dx = xCoords.get(i) - centroidX;
                double dy = yCoords.get(i) - centroidY;
                mu_xx += dx * dx;
                mu_yy += dy * dy;
                mu_xy += dx * dy;
            }

            double commonTerm = Math.sqrt(Math.pow(mu_xx - mu_yy, 2) + 4 * mu_xy * mu_xy);

            double lambda1 = (mu_xx + mu_yy + commonTerm) / 2.0;
            double lambda2 = (mu_xx + mu_yy - commonTerm) / 2.0;

            if (lambda1 <= 0 || lambda2 < 0) {
                eccentricity = 0;
            } else {
                eccentricity = Math.sqrt(1 - (lambda2 / lambda1));
            }
        }
    }


    @Override
    public boolean isEnabled(int imageType) {
        return imageType == Picsi.IMAGE_TYPE_GRAY;
    }

    @Override
    public ImageData run(ImageData inData, int imageType) {
        // Pre-processing and Segmentation
        ImageData processedData = Binarization.binarization(inData, Binarization.otsuThreshold(inData), false, false);
        processedData = SaltAndPepperFilter.applyClosing(processedData);
        processedData = SaltAndPepperFilter.applyOpening(processedData);

        ImageData labeledData = processedData;
        int numLabels = FloodFilling.floodFill(labeledData);

        // Gather Pixel Data for Each Particle
        Map<Integer, Particle> particles = new HashMap<>();
        for (int y = 0; y < labeledData.height; y++) {
            for (int x = 0; x < labeledData.width; x++) {
                int pixelValue = labeledData.getPixel(x, y);
                if (pixelValue != FloodFilling.s_background) {
                    particles.computeIfAbsent(pixelValue, Particle::new).addPixel(x, y);
                }
            }
        }

        // Calculate Properties and Print the Table
        System.out.println("--- Particle Analysis Results ---");
        System.out.println(String.format("%-10s | %-20s | %-10s | %-20s | %-15s", "Label", "Bounding-Box", "Area", "Centroid (x,y)", "Eccentricity"));
        System.out.println(new String(new char[85]).replace("\0", "-"));

        for (Particle p : particles.values()) {
            p.calculateProperties();

            String bbox = String.format("[%d,%d]-[%d,%d]", p.minX, p.minY, p.maxX, p.maxY);
            String centroid = String.format("%.2f, %.2f", p.centroidX, p.centroidY);

            System.out.println(String.format("%-10d | %-20s | %-10d | %-20s | %-15.4f", p.label / 10, bbox, p.area, centroid, p.eccentricity));
        }
        System.out.println("---------------------------------");

        return FloodFilling.falseColor(labeledData, numLabels + 2);
    }
}