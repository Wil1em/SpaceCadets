import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class CircleDetection {
    public static void main(String[] args) {
        JFrame frame = new JFrame("Circle Detection");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(500, 500);

        try {
            String file_name = "1.png";
            BufferedImage colorImage = ImageIO.read(new File("images/" + file_name));
            BufferedImage grayImage = new BufferedImage(colorImage.getWidth(), colorImage.getHeight(), BufferedImage.TYPE_BYTE_GRAY);
            // Draw the colorImage onto the grayImage, Java would automatically convert it to grayscale cause "TYPE_BYTE_GRAY"
            grayImage.getGraphics().drawImage(colorImage, 0, 0, null);

            // Do edge detection (Sobel operator)
            BufferedImage edgeImage = edgeDetection(grayImage);

            // Examples of circles:
            List<int[]> circles = detectCircles(edgeImage, 40, 60, 250, 130);
//            List<int[]> circles = detectCircles(edgeImage, 20, 40, 200, 50);
//            List<int[]> circles = detectCircles(edgeImage, 15, 30, 200,150);

            BufferedImage resultImage = new BufferedImage(edgeImage.getWidth(), edgeImage.getHeight(), BufferedImage.TYPE_INT_RGB);

            Graphics2D g2d = resultImage.createGraphics();
            g2d.drawImage(edgeImage, 0, 0, null);
            g2d.setColor(Color.green);
            g2d.setStroke(new BasicStroke(5));
            int num=0;
            for (int[] circle : circles) {
                num++;
                int x = circle[0];
                int y = circle[1];
                int r = circle[2];
                g2d.drawOval(x-r, y-r, 2*r, 2*r);
            }
            g2d.dispose();

            System.out.println("number of circles printed: " + num);

            ImageIO.write(resultImage, "png", new File("images/out/" + file_name));

            ImageIcon resultIcon = new ImageIcon(resultImage);
            JLabel label = new JLabel(resultIcon);
            frame.add(label);

            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static BufferedImage edgeDetection(BufferedImage grayImage) {
        int width = grayImage.getWidth();
        int height = grayImage.getHeight();
        BufferedImage edgeImage = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY);

        int[][] sobelX = {
                {-1, 0, 1},
                {-2, 0, 2},
                {-1, 0, 1}
        };
        int[][] sobelY = {
                {-1, -2, -1},
                { 0,  0,  0},
                { 1,  2,  1}
        };

        for (int x = 1; x < width - 1; x++) {
            for (int y = 1; y < height - 1; y++) {
                int gx = 0; int gy = 0;
                // Traverse the 3×3 area around (x, y) pixel point
                for (int i = -1; i <= 1; i++) {
                    for (int j = -1; j <= 1; j++) {
                        int pixel = grayImage.getRGB(x + i, y + j) & 0xFF;
                        gx += pixel * sobelX[i+1][j+1];
                        gy += pixel * sobelY[i+1][j+1];
                    }
                }

                int g = (int) Math.sqrt(gx * gx + gy * gy);
                g = Math.min(255, Math.max(0, g));

                edgeImage.setRGB(x, y, (g << 16) | (g << 8) | g);
            }
        }
        return edgeImage;
    }
    private static List<int[]> detectCircles(BufferedImage edgeImage, int minR, int maxR, int threshold, int brightness) {
        int width = edgeImage.getWidth();
        int height = edgeImage.getHeight();

        int[][][] count = new int[width][height][maxR - minR + 1];

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                int pixel = edgeImage.getRGB(x, y) & 0xFF;
                // filter bright edge pixels
                if (pixel >= brightness) {
                    // Traverse the possible radius in range for each bright edge pixel
                    for (int r = minR; r <= maxR; r++){
                        for (int theta = 0; theta < 360; theta++) {
                            // formula: a = x - r*cos\theta; b = y - r*cos\theta
                            int a = (int) (x - r * Math.cos(Math.toRadians(theta)));
                            int b = (int) (y - r * Math.sin(Math.toRadians(theta)));

                            if (a >=0 && a < width && b>=0 && b <height) {
                                // (r - minR) would move index to right number
                                count[a][b][r-minR]++;
                            }
                        }
                    }
                }
            }
        }

        List<int[]> circles = new ArrayList<>();
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                for (int r = 0; r <= maxR - minR; r++) {
                    if (count[x][y][r] > threshold) {
                        circles.add(new int[]{x, y, r+minR});
                    }
                }
            }
        }

        double averageR = (double) (maxR - minR) / 2;
        int minDistance = (int) (averageR * 0.9);
        int minRadDiff = (int) (averageR * 1);
        // remove some duplicated circles by filterCircles
        return filterCircles(circles, minDistance, minRadDiff);
    }

    private static List<int[]> filterCircles(List<int[]> circles, int minDistance, int minRadDiff) {
        List<int[]> uniqCircles = new ArrayList<>();

        for (int[] circle : circles) {
            boolean isDuplicate = false;
            for (int[] uniqueCircle : uniqCircles) {
                int dx = uniqueCircle[0] - circle[0];
                int dy = uniqueCircle[1] - circle[1];
                int dr = uniqueCircle[2] - circle[2];

                // Check for duplicate circles:
                // If center distance < minDistance and radius difference < minRadiusDifference,
                // consider the circles duplicates due to close center and radius
                if (Math.sqrt(dx * dx + dy * dy) < minDistance && Math.abs(dr) < minRadDiff) {
                    isDuplicate = true;
                    break;
                }
            }
            if (!isDuplicate)
                uniqCircles.add(circle);
        }
        return uniqCircles;
    }
}