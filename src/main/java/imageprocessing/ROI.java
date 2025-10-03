package imageprocessing;

import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.Rectangle;

/**
 * Region of Interest (ROI)
 *
 * @author Christoph Stamm
 *
 */
public class ROI {
    public ImageData m_imageData;
    public Rectangle m_rect;

    /**
     * Create new ROI
     * @param imageData image data
     * @param roi region of interest
     */
    public ROI(ImageData imageData, Rectangle roi) {
        this.m_imageData = imageData;
        this.m_rect = roi;
    }

    /**
     * @return width of ROI
     */
    public int getWidth() {
        return m_rect.width;
    }

    /**
     * @return height of ROI
     */
    public int getHeight() {
        return m_rect.height;
    }

    /**
     * Get pixel at position (x,y)
     * @param x x-coordinate in ROI coordinate system
     * @param y y-coordinate in ROI coordinate system
     * @return
     */
    public int getPixel(int x, int y) {
        for (int i = 0; i < m_imageData.height; i++) {
            for (int j = 0; j < m_imageData.width; j++) {
                if (i == m_rect.y + y && j == m_rect.x + x) {
                    return m_imageData.getPixel(j, i);
                }
            }
        }

        return -1;
    }

    /**
     * Set pixel at position (x,y)
     * @param x x-coordinate in ROI coordinate system
     * @param y y-coordinate in ROI coordinate system
     * @param val
     */
    public void setPixel(int x, int y, int val) {
        if (x < 0 || x >= m_rect.width || y < 0 || y >= m_rect.height) {
            throw new IllegalArgumentException("Coordinates out of bounds");
        }

        m_imageData.setPixel(m_rect.x + x, m_rect.y + y, val);
    }

    /**
     * Returns true if this ROI overlaps with r
     * @param r another ROI
     * @return
     */
    public boolean overlaps(ROI r) {

        for (int i = 0; i < this.m_rect.height; i++) {
            for (int j = 0; j < this.m_rect.width; j++) {
                if (getPixel(j, i) == r.getPixel(j + r.m_rect.x - this.m_rect.x, i + r.m_rect.y - this.m_rect.y)) {
                    return true;
                }
            }
        }
        return false;
    }

}
