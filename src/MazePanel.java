import javax.swing.*;
import java.awt.*;
import java.awt.geom.Path2D;
import java.util.*;
import java.util.List;
import javax.swing.Timer;

public class MazePanel extends JPanel {
    private MazeApp app; // Referensi ke aplikasi utama untuk akses data
    private List<Cell> path = new ArrayList<>();
    private Set<Cell> visitedAnimation = new HashSet<>();
    private Timer animationTimer;

    public MazePanel(MazeApp app) {
        this.app = app;
        setPreferredSize(new Dimension(MazeApp.COLS * MazeApp.CELL_SIZE, MazeApp.ROWS * MazeApp.CELL_SIZE));
        setBackground(new Color(100, 140, 50));
    }

    public void clearPath() {
        if (animationTimer != null && animationTimer.isRunning()) animationTimer.stop();
        path.clear();
        visitedAnimation.clear();
        repaint();
    }

    public void animateSearch(List<Cell> visitOrder, Map<Cell, Cell> parentMap, boolean found) {
        visitedAnimation.clear();
        animationTimer = new Timer(15, null);
        Iterator<Cell> iter = visitOrder.iterator();

        animationTimer.addActionListener(e -> {
            if (iter.hasNext()) {
                Cell c = iter.next();
                if (c != app.startNode && c != app.endNode) visitedAnimation.add(c);
                repaint();
            } else {
                animationTimer.stop();
                if (found) {
                    reconstructPath(parentMap);
                    SoundEngine.playVictory();
                }
            }
        });
        animationTimer.start();
    }

    private void reconstructPath(Map<Cell, Cell> parentMap) {
        path.clear();
        Cell curr = app.endNode;
        int totalCost = 0;
        while (curr != null) {
            path.add(curr);
            totalCost += (curr.type == MazeApp.WALL ? 0 : curr.type);
            curr = parentMap.get(curr);
        }
        Collections.reverse(path);
        app.statusLabel.setText("Found! Cost: " + totalCost);
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        for (int r = 0; r < MazeApp.ROWS; r++) {
            for (int c = 0; c < MazeApp.COLS; c++) {
                Cell cell = app.grid[r][c];
                int x = c * MazeApp.CELL_SIZE;
                int y = r * MazeApp.CELL_SIZE;
                
                if (cell.type == MazeApp.WALL) drawHouseRoof(g2d, x, y);
                else drawRoadTile(g2d, x, y, cell.type);
                
                if (visitedAnimation.contains(cell)) {
                    g2d.setColor(new Color(255, 255, 150, 150));
                    g2d.fillOval(x + 5, y + 5, MazeApp.CELL_SIZE - 10, MazeApp.CELL_SIZE - 10);
                }
            }
        }
        
        if (!path.isEmpty()) {
            g2d.setColor(new Color(255, 0, 0, 200));
            g2d.setStroke(new BasicStroke(5, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            Path2D pathShape = new Path2D.Float();
            pathShape.moveTo(path.get(0).c * MazeApp.CELL_SIZE + MazeApp.CELL_SIZE / 2.0, path.get(0).r * MazeApp.CELL_SIZE + MazeApp.CELL_SIZE / 2.0);
            for (int i = 1; i < path.size(); i++)
                pathShape.lineTo(path.get(i).c * MazeApp.CELL_SIZE + MazeApp.CELL_SIZE / 2.0, path.get(i).r * MazeApp.CELL_SIZE + MazeApp.CELL_SIZE / 2.0);
            g2d.draw(pathShape);
        }
        
        if (app.startNode != null && app.endNode != null) {
            drawStartMarker(g2d, app.startNode.c * MazeApp.CELL_SIZE, app.startNode.r * MazeApp.CELL_SIZE);
            drawEndMarker(g2d, app.endNode.c * MazeApp.CELL_SIZE, app.endNode.r * MazeApp.CELL_SIZE);
        }
    }

    private void drawHouseRoof(Graphics2D g2d, int x, int y) {
        g2d.setColor(new Color(160, 60, 30));
        g2d.fillRect(x, y, MazeApp.CELL_SIZE, MazeApp.CELL_SIZE);
        g2d.setColor(new Color(220, 100, 60));
        Polygon roof = new Polygon();
        roof.addPoint(x, y + MazeApp.CELL_SIZE);
        roof.addPoint(x + MazeApp.CELL_SIZE, y + MazeApp.CELL_SIZE);
        roof.addPoint(x + MazeApp.CELL_SIZE / 2, y);
        g2d.fillPolygon(roof);
    }

    private void drawRoadTile(Graphics2D g2d, int x, int y, int type) {
        Color baseColor, detailColor1, detailColor2;
        if (type == MazeApp.GRASS) {
            baseColor = new Color(200, 200, 200);
            detailColor1 = new Color(180, 180, 180);
            detailColor2 = new Color(220, 220, 220);
            g2d.setColor(baseColor);
            g2d.fillRect(x + 1, y + 1, MazeApp.CELL_SIZE - 2, MazeApp.CELL_SIZE - 2);
            g2d.setColor(detailColor1);
            g2d.drawRect(x + 5, y + 5, MazeApp.CELL_SIZE - 10, MazeApp.CELL_SIZE - 10);
            g2d.drawLine(x + MazeApp.CELL_SIZE / 2, y + 5, x + MazeApp.CELL_SIZE / 2, y + MazeApp.CELL_SIZE - 5);
            g2d.drawLine(x + 5, y + MazeApp.CELL_SIZE / 2, x + MazeApp.CELL_SIZE - 5, y + MazeApp.CELL_SIZE / 2);
            g2d.setColor(detailColor2);
            g2d.fillOval(x + 4, y + 4, 4, 4);
            g2d.fillOval(x + MazeApp.CELL_SIZE - 8, y + 4, 4, 4);
            g2d.fillOval(x + 4, y + MazeApp.CELL_SIZE - 8, 4, 4);
            g2d.fillOval(x + MazeApp.CELL_SIZE - 8, y + MazeApp.CELL_SIZE - 8, 4, 4);
        } else if (type == MazeApp.MUD) {
            baseColor = new Color(139, 105, 20);
            detailColor1 = new Color(120, 85, 10);
            detailColor2 = new Color(160, 125, 30);
            GradientPaint mudGradient = new GradientPaint(x, y, baseColor, x + MazeApp.CELL_SIZE, y + MazeApp.CELL_SIZE, detailColor2);
            g2d.setPaint(mudGradient);
            g2d.fillRect(x + 1, y + 1, MazeApp.CELL_SIZE - 2, MazeApp.CELL_SIZE - 2);
            g2d.setColor(detailColor1);
            Random rand = new Random((x * 31 + y) * 17);
            for (int i = 0; i < 5; i++) {
                int bx = x + 5 + rand.nextInt(MazeApp.CELL_SIZE - 10);
                int by = y + 5 + rand.nextInt(MazeApp.CELL_SIZE - 10);
                int size = 2 + rand.nextInt(4);
                g2d.fillOval(bx, by, size, size);
            }
            g2d.setColor(new Color(160, 125, 30, 100));
            g2d.fillOval(x + MazeApp.CELL_SIZE / 3, y + MazeApp.CELL_SIZE / 3, MazeApp.CELL_SIZE / 3, MazeApp.CELL_SIZE / 3);
        } else { // WATER
            baseColor = new Color(70, 130, 220);
            detailColor1 = new Color(100, 160, 240);
            detailColor2 = new Color(50, 110, 200);
            GradientPaint waterGradient = new GradientPaint(x, y, detailColor1, x, y + MazeApp.CELL_SIZE, detailColor2);
            g2d.setPaint(waterGradient);
            g2d.fillRect(x + 1, y + 1, MazeApp.CELL_SIZE - 2, MazeApp.CELL_SIZE - 2);
            g2d.setColor(new Color(255, 255, 255, 80));
            Stroke originalStroke = g2d.getStroke();
            g2d.setStroke(new BasicStroke(1.5f));
            for (int i = 0; i < 3; i++) {
                int waveY = y + 5 + i * 8;
                g2d.drawLine(x + 3, waveY, x + MazeApp.CELL_SIZE - 3, waveY);
            }
            g2d.setColor(new Color(255, 255, 255, 120));
            g2d.fillOval(x + 8, y + 8, 6, 6);
            g2d.fillOval(x + MazeApp.CELL_SIZE - 14, y + MazeApp.CELL_SIZE - 14, 4, 4);
            g2d.setStroke(originalStroke);
        }
        g2d.setColor(new Color(0, 0, 0, 50));
        g2d.drawRect(x + 1, y + 1, MazeApp.CELL_SIZE - 2, MazeApp.CELL_SIZE - 2);
    }

    private void drawStartMarker(Graphics2D g2d, int x, int y) {
        g2d.setColor(new Color(0, 200, 0));
        g2d.fillOval(x + 4, y + 4, MazeApp.CELL_SIZE - 8, MazeApp.CELL_SIZE - 8);
        g2d.setColor(Color.WHITE);
        Polygon arrow = new Polygon();
        arrow.addPoint(x + MazeApp.CELL_SIZE / 2, y + 8);
        arrow.addPoint(x + 8, y + MazeApp.CELL_SIZE - 8);
        arrow.addPoint(x + MazeApp.CELL_SIZE - 8, y + MazeApp.CELL_SIZE - 8);
        g2d.fill(arrow);
    }

    private void drawEndMarker(Graphics2D g2d, int x, int y) {
        g2d.setColor(Color.RED);
        g2d.fillRect(x + 6, y + 6, MazeApp.CELL_SIZE - 12, MazeApp.CELL_SIZE - 12);
        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Arial", Font.BOLD, 12));
        FontMetrics fm = g2d.getFontMetrics();
        String text = "E";
        int textX = x + (MazeApp.CELL_SIZE - fm.stringWidth(text)) / 2;
        int textY = y + (MazeApp.CELL_SIZE - fm.getHeight()) / 2 + fm.getAscent();
        g2d.drawString(text, textX, textY);
    }
}