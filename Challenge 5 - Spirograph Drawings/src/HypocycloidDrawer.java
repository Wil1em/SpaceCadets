import javax.swing.*;
import java.awt.*;
import java.awt.geom.Path2D;
import java.util.List;
import java.util.ArrayList;

class Hypocycloid {
    private double R;
    private double r;
    private double O;
    private Color color;
    private int offsetX;
    private int offsetY;
    private double tStep;
    private double k;
    private int formula;

    public Hypocycloid(double r, double k, Color color, int offsetX, int offsetY, double tStep) {
        if (r <= 0 || k <= 1 || tStep <= 0){
            throw new IllegalArgumentException("Invalid arguments provided.");
        }
        this.formula = 1;
        this.r = r;
        this.k = k;
        this.color = color;
        this.offsetX = offsetX;
        this.offsetY = offsetY;
        this.tStep = tStep;
    }

    public Hypocycloid(double R, double r, double O, Color color, int offsetX, int offsetY, double tStep) {
        if (R <= r || R <= 0 || r <= 0 || O < 0 || tStep <= 0) {
            throw new IllegalArgumentException("Invalid arguments provided.");
        }
        this.formula = 0;
        this.R = R;
        this.r = r;
        this.O = O;
        this.color = color;
        this.offsetX = offsetX;
        this.offsetY = offsetY;
        this.tStep = tStep;
    }

    public void paint(Graphics2D g2) {
        g2.setColor(color);
        Path2D path = new Path2D.Double();

        double tMax = 2 * Math.PI * r;
//        double tMax = 4 * Math.PI * r;

        boolean isFirst = true;
        for (double t = 0; t < tMax; t += tStep) {
            double x, y;
            if (formula == 1) {
                x = r * (k - 1) * Math.cos(t) + r * Math.cos((k - 1) * t);
                y = r * (k - 1) * Math.sin(t) - r * Math.sin((k - 1) * t);
            }else {
                x = (R - r) * Math.cos(t) + O * Math.cos(((R - r) / r) * t);
                y = (R - r) * Math.sin(t) - O * Math.sin(((R - r) / r) * t);
            }
            if (isFirst) {
                path.moveTo(x + offsetX, y + offsetY);
                isFirst = false;
            }else {
                path.lineTo(x + offsetX, y + offsetY);
            }
        }
        path.closePath();
        g2.draw(path);
    }
}

class MultiHypocycloid {
    private final List<Hypocycloid> hypocycloids = new ArrayList<>();;

    public void addHypocycloid(Hypocycloid h) {
        hypocycloids.add(h);
    }

    public void paint(Graphics2D g2) {
        for (Hypocycloid hypocycloid : hypocycloids) {
            hypocycloid.paint(g2);
        }
    }

}

public class HypocycloidDrawer extends JPanel {
    private final MultiHypocycloid hypocycloids;

    public HypocycloidDrawer() {
        this.hypocycloids = new MultiHypocycloid();
        hypocycloids.addHypocycloid(new Hypocycloid(100, 2.1, Color.PINK, 250, 250, 0.1));
        // an astroid
        hypocycloids.addHypocycloid(new Hypocycloid(25, 4, Color.red, 250, 250, 0.1));
        // a deltoid
        hypocycloids.addHypocycloid(new Hypocycloid(90, 30, 30, Color.ORANGE, 250, 250, 0.01));
        hypocycloids.addHypocycloid(new Hypocycloid(100, 25, 50, Color.BLUE, 250, 250, 0.01));

    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        hypocycloids.paint(g2);
    }

    public static void main(String[] args) {
        JFrame frame = new JFrame("Hypocycloid Drawer");
        frame.setSize(500, 500);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.add(new HypocycloidDrawer());
        // centred the window
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}