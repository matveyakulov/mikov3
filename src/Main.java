import java.util.*;

public class Main {

    // n, r, k
    private static Map<Integer, Map<Double, Integer>> results = new HashMap<>();

    public static void main(String[] args) {
        List<Integer> nn = List.of(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);
        for (int i = 0; i < nn.size(); i++) {
            new Server(13_000, 20_000, 0.5, nn.get(i) * 50).run();
        }
//        new Server(10_000, 20_000, 1, 100).run();
//        new Server(10_000, 20_000, 1, 500).run();
//        new Server(10_000, 20_000, 1, 1000).run();
//        new Server(10_000, 20_000, 1, 5000).run();
//        new Server(10_000, 20_000, 1, 10000).run();
//        new Server(10_000, 20_000, 1, 15000).run();
//        new Server(10_000, 20_000, 1, 20000).run();
//        new Server(10_000, 20_000, 1, 25000).run();
//        new Server(10_000, 20_000, 1, 30000).run();
    }

    static class Server {

        public Server(double a, double b, double expectation, int n) {
            this.a = a;
            this.b = b;
            this.expectation = expectation;
            this.n = n;
            this.graphs = new boolean[n][n];
            results.put(n, new HashMap<>());
        }

        private final Random rand = new Random();
        private final int s = 10_000; // круг

        private final double a;
        private final double b;
        private final double expectation;
        private final int n;
        private final List<Point> points = new ArrayList<>();
        private final boolean[][] graphs;

        public void run() {
            for (int i = 0; i < n; i++) {
                double x = rand.nextDouble();
                points.add(new Point(x, getY(x), getR()));
            }
            for (int i = 0; i < n; i++) {
                for (int j = 0; j < n; j++) {
                    graphs[i][j] = checkRadius(points.get(i), points.get(j));
                }
            }

            for (int i = 0; i < n; i++) {
                for (int j = 0; j < n; j++) {
                    if (graphs[i][j]) {
                        points.get(i).n++;
                    }
                }
            }

            Map<Double, Integer> map = results.get(n);
            for (Point point : points) {
                map.put(point.r, point.n);
                //System.out.printf("n = %d, r = %f, k = %d%n", n, point.r, point.n);
            }

            System.out.println("K = " + new EdmondsKarp(points, graphs).getMaxFlow());
            System.out.println("===========================================");
        }

        private boolean checkRadius(Point p1, Point p2) {
            return (Math.pow(p1.x - p2.x, 2) + Math.pow(p1.y - p2.y, 2)) <= Math.pow(p1.r, 2);
        }

        private double getR() { // R[a,b]
            return Math.abs((a + rand.nextDouble()) / (b - a));
        }

        private double getY(double x) { // M
            return Math.abs(Math.log(x) / -expectation);
        }

    }

    static class Point {
        public double x;
        public double y;
        public double r;
        public int n = 0;

        public Point(double x, double y, double r) {
            this.x = x;
            this.y = y;
            this.r = r;
        }
    }

    static class EdmondsKarp {

        private final long[][] flow; //max flow beetween i and j verticles
        private final long[][] capacity; // edge capacity
        private final int[] parent; //parent
        private final boolean[] visited; //just for checking if visited
        private final int numOfVerticles;
        private final List<Point> points;
        private final boolean[][] graphs;

        public EdmondsKarp(List<Point> points, boolean[][] graphs) {
            this.numOfVerticles = points.size();
            this.points = points;
            this.flow = new long[this.numOfVerticles][this.numOfVerticles];
            this.capacity = new long[this.numOfVerticles][this.numOfVerticles];
            this.parent = new int[this.numOfVerticles];
            this.visited = new boolean[this.numOfVerticles];
            this.graphs = graphs;
            fillEdges();
        }

        public void fillEdges() {
            for (int i = 0; i < numOfVerticles; i++) {
                for (int j = 0; j < numOfVerticles; j++) {
                    this.capacity[i][j] = graphs[i][j] ? 1 : 0;
                }
            }
        }

        public long getMaxFlow() {
            long minFlow = Long.MAX_VALUE;
            for (int s = 0; s < numOfVerticles; s++) {
                for (int t = 0; t < numOfVerticles; t++) {
                    if (t == s) continue;
                    while (true) {
                        final Queue<Integer> Q = new ArrayDeque<>();
                        Q.add(s);

                        for (int i = 0; i < this.numOfVerticles; ++i) {
                            if (i != s) {
                                visited[i] = false;
                            }
                        }
                        visited[s] = true;

                        boolean check = false;
                        int current;
                        while (!Q.isEmpty()) {
                            current = Q.peek();
                            if (current == t) {
                                check = true;
                                break;
                            }
                            Q.remove();
                            for (int i = 0; i < numOfVerticles; ++i) {
                                if (!visited[i] && capacity[current][i] > flow[current][i]) {
                                    visited[i] = true;
                                    Q.add(i);
                                    parent[i] = current;
                                }
                            }
                        }
                        if (!check)
                            break;

                        long temp = capacity[parent[t]][t] - flow[parent[t]][t];
                        for (int i = t; i != s; i = parent[i])
                            temp = Math.min(temp, (capacity[parent[i]][i] - flow[parent[i]][i]));

                        for (int i = t; i != s; i = parent[i]) {
                            flow[parent[i]][i] += temp;
                            flow[i][parent[i]] -= temp;
                        }
                    }
                }
                long result = 0;
                for (int i = 0; i < numOfVerticles; ++i)
                    result += flow[s][i];
                minFlow = Math.min(minFlow, result);
            }
            return minFlow;
        }
    }
}
