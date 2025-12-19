import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.Queue;

public class MazeApp extends JFrame {

    // Grid Configuration - Public agar bisa diakses MazePanel
    public static final int ROWS = 21;
    public static final int COLS = 25;
    public static final int CELL_SIZE = 30;

    // Cell Types & Costs
    public static final int WALL = -1;
    public static final int GRASS = 1;
    public static final int MUD = 5;
    public static final int WATER = 10;

    private MazePanel mazePanel;
    private JButton btnGenPrim, btnBFS, btnDFS, btnDijkstra;
    public JLabel statusLabel; // Public agar MazePanel bisa update text
    public Cell[][] grid;      // Public agar MazePanel bisa baca grid
    public Cell startNode, endNode;

    public MazeApp() {
        setTitle("Village Maze - No Scroll Version");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        grid = new Cell[ROWS][COLS];
        initGrid();

        // Pass 'this' ke MazePanel
        mazePanel = new MazePanel(this);
        add(mazePanel, BorderLayout.CENTER);

        JPanel controlPanel = new JPanel();
        controlPanel.setBackground(new Color(240, 230, 200));

        btnGenPrim = new JButton("Build Village");
        btnBFS = new JButton("BFS");
        btnDFS = new JButton("DFS");
        btnDijkstra = new JButton("Dijkstra");

        statusLabel = new JLabel("Ready to build...");
        statusLabel.setFont(new Font("SansSerif", Font.BOLD, 13));

        controlPanel.add(btnGenPrim);
        controlPanel.add(btnBFS);
        controlPanel.add(btnDFS);
        controlPanel.add(btnDijkstra);

        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.add(controlPanel, BorderLayout.CENTER);
        bottomPanel.add(statusLabel, BorderLayout.SOUTH);
        add(bottomPanel, BorderLayout.SOUTH);

        btnGenPrim.addActionListener(e -> { SoundEngine.playClick(); generateMazePrims(); });
        btnBFS.addActionListener(e -> { SoundEngine.playClick(); solve("BFS"); });
        btnDFS.addActionListener(e -> { SoundEngine.playClick(); solve("DFS"); });
        btnDijkstra.addActionListener(e -> { SoundEngine.playClick(); solve("Dijkstra"); });

        pack();
        setLocationRelativeTo(null);
        setResizable(false);
    }

    private void initGrid() {
        for (int r = 0; r < ROWS; r++) {
            for (int c = 0; c < COLS; c++) {
                grid[r][c] = new Cell(r, c, WALL);
            }
        }
    }

    private void generateMazePrims() {
        mazePanel.clearPath();
        initGrid();
        LinkedList<Cell> frontiers = new LinkedList<>();
        Random rand = new Random();
        int startR = 1, startC = 1;
        grid[startR][startC].type = GRASS;
        addFrontiers(startR, startC, frontiers);
        while (!frontiers.isEmpty()) {
            Cell frontier = frontiers.remove(rand.nextInt(frontiers.size()));
            List<Cell> neighbors = getVisitedNeighborsDist2(frontier.r, frontier.c);
            if (!neighbors.isEmpty()) {
                Cell neighbor = neighbors.get(rand.nextInt(neighbors.size()));
                int wallR = (frontier.r + neighbor.r) / 2;
                int wallC = (frontier.c + neighbor.c) / 2;
                grid[frontier.r][frontier.c].type = GRASS;
                grid[wallR][wallC].type = GRASS;
                addFrontiers(frontier.r, frontier.c, frontiers);
            }
        }
        assignWeights();
        startNode = grid[1][1];
        endNode = grid[ROWS - 2][COLS - 2];
        startNode.type = GRASS;
        endNode.type = GRASS;
        statusLabel.setText("Village Built!");
        mazePanel.repaint();
    }

    private void addFrontiers(int r, int c, LinkedList<Cell> frontiers) {
        int[] dr = {-2, 2, 0, 0};
        int[] dc = {0, 0, -2, 2};
        for (int i = 0; i < 4; i++) {
            int nr = r + dr[i];
            int nc = c + dc[i];
            if (isValid(nr, nc) && grid[nr][nc].type == WALL) {
                frontiers.add(grid[nr][nc]);
                grid[nr][nc].type = WALL;
            }
        }
    }

    private List<Cell> getVisitedNeighborsDist2(int r, int c) {
        List<Cell> list = new ArrayList<>();
        int[] dr = {-2, 2, 0, 0};
        int[] dc = {0, 0, -2, 2};
        for (int i = 0; i < 4; i++) {
            int nr = r + dr[i];
            int nc = c + dc[i];
            if (isValid(nr, nc) && grid[nr][nc].type != WALL) list.add(grid[nr][nc]);
        }
        return list;
    }

    private void assignWeights() {
        Random rand = new Random();
        for (int r = 0; r < ROWS; r++) {
            for (int c = 0; c < COLS; c++) {
                if (grid[r][c].type != WALL) {
                    double chance = rand.nextDouble();
                    if (chance < 0.6) grid[r][c].type = GRASS;
                    else if (chance < 0.85) grid[r][c].type = MUD;
                    else grid[r][c].type = WATER;
                }
            }
        }
    }

    private void solve(String algo) {
        if (startNode == null || endNode == null) return;
        mazePanel.clearPath();
        new Thread(() -> {
            List<Cell> visitedOrder = new ArrayList<>();
            Map<Cell, Cell> parentMap = new HashMap<>();
            boolean found = false;
            if (algo.equals("BFS")) found = runBFS(visitedOrder, parentMap);
            else if (algo.equals("DFS")) found = runDFS(visitedOrder, parentMap);
            else if (algo.equals("Dijkstra")) found = runDijkstra(visitedOrder, parentMap);
            mazePanel.animateSearch(visitedOrder, parentMap, found);
        }).start();
    }

    private boolean runBFS(List<Cell> visitedOrder, Map<Cell, Cell> parentMap) {
        Queue<Cell> queue = new LinkedList<>();
        Set<Cell> visited = new HashSet<>();
        queue.add(startNode);
        visited.add(startNode);
        while (!queue.isEmpty()) {
            Cell current = queue.poll();
            visitedOrder.add(current);
            if (current == endNode) return true;
            for (Cell neighbor : getNeighbors(current)) {
                if (!visited.contains(neighbor)) {
                    visited.add(neighbor);
                    parentMap.put(neighbor, current);
                    queue.add(neighbor);
                }
            }
        }
        return false;
    }

    private boolean runDFS(List<Cell> visitedOrder, Map<Cell, Cell> parentMap) {
        Stack<Cell> stack = new Stack<>();
        Set<Cell> visited = new HashSet<>();
        stack.push(startNode);
        visited.add(startNode);
        while (!stack.isEmpty()) {
            Cell current = stack.pop();
            if (!visitedOrder.contains(current)) visitedOrder.add(current);
            if (current == endNode) return true;
            for (Cell neighbor : getNeighbors(current)) {
                if (!visited.contains(neighbor)) {
                    visited.add(neighbor);
                    parentMap.put(neighbor, current);
                    stack.push(neighbor);
                }
            }
        }
        return false;
    }

    private boolean runDijkstra(List<Cell> visitedOrder, Map<Cell, Cell> parentMap) {
        PriorityQueue<Cell> pq = new PriorityQueue<>(Comparator.comparingInt(c -> c.dist));
        for (int r = 0; r < ROWS; r++) {
            for (int c = 0; c < COLS; c++) grid[r][c].dist = Integer.MAX_VALUE;
        }
        startNode.dist = 0;
        pq.add(startNode);
        while (!pq.isEmpty()) {
            Cell current = pq.poll();
            if (visitedOrder.contains(current)) continue;
            visitedOrder.add(current);
            if (current == endNode) return true;
            for (Cell neighbor : getNeighbors(current)) {
                int newDist = current.dist + neighbor.type;
                if (newDist < neighbor.dist) {
                    neighbor.dist = newDist;
                    parentMap.put(neighbor, current);
                    pq.add(neighbor);
                }
            }
        }
        return false;
    }

    private List<Cell> getNeighbors(Cell c) {
        List<Cell> list = new ArrayList<>();
        int[] dr = {-1, 1, 0, 0};
        int[] dc = {0, 0, -1, 1};
        for (int i = 0; i < 4; i++) {
            int nr = c.r + dr[i];
            int nc = c.c + dc[i];
            if (isValid(nr, nc) && grid[nr][nc].type != WALL) list.add(grid[nr][nc]);
        }
        return list;
    }

    private boolean isValid(int r, int c) {
        return r >= 0 && r < ROWS && c >= 0 && c < COLS;
    }

    public static void main(String[] args) {
        try {
            // Mengatur gaya tampilan agar mirip sistem operasi (Windows/Mac)
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {
        }
        
        // Menjalankan aplikasi
        SwingUtilities.invokeLater(() -> new MazeApp().setVisible(true));
    }
}