package contest_2ima20.client.algorithms;

import contest_2ima20.client.schematrees.SchematicTreesAlgorithm;
import contest_2ima20.core.schematrees.Graph;
import contest_2ima20.core.schematrees.Input;
import contest_2ima20.core.schematrees.Output;
import contest_2ima20.core.schematrees.Position;
import contest_2ima20.core.schematrees.Edge;

import java.util.*;

public class PlanarityGuidedAlgorithmV5 extends SchematicTreesAlgorithm {

    private static final int CANDIDATE_LIMIT = 10;

    @Override
    public Output doAlgorithm(Input input) {
        Output output = new Output(input);
        Set<String> usedPositions = new HashSet<>();

        for (Graph g : output.graphs) {
            for (Position p : g.getVertices()) {
                int cx = p.x(), cy = p.y();
                List<int[]> candidates = generateCandidates(cx, cy, input.width, input.height, input.radius);

                boolean assigned = false;
                for (int[] c : candidates) {
                    String key = c[0] + "," + c[1];
                    if (usedPositions.add(key)) {
                        // System.out.printf("Assigning vertex from (%d, %d) → (%d, %d)%n", cx, cy, c[0], c[1]);
                        p.setX(c[0]);
                        p.setY(c[1]);
                        assigned = true;
                        break;
                    }
                }

                if (!assigned) {
                    System.out.printf("Fallback to original position (%d, %d)%n", cx, cy);
                }
            }
        }

        for (Graph g : output.graphs) {
            buildCrossingAwareMST(g, output);
        }

        // System.out.println("Total edge crossings: " + countTotalCrossings(output));
        return output;
    }

    private List<int[]> generateCandidates(int cx, int cy, int width, int height, int radius) {
        PriorityQueue<int[]> pq = new PriorityQueue<>(Comparator.comparingInt(v -> Math.abs(v[0] - cx) + Math.abs(v[1] - cy)));
        for (int dx = -radius; dx <= radius; dx++) {
            for (int dy = -radius; dy <= radius; dy++) {
                if (Math.abs(dx) + Math.abs(dy) <= radius) {
                    int x = cx + dx, y = cy + dy;
                    if (x >= 0 && x <= width && y >= 0 && y <= height) {
                        pq.offer(new int[]{x, y});
                    }
                }
            }
        }

        List<int[]> limited = new ArrayList<>();
        for (int i = 0; i < CANDIDATE_LIMIT && !pq.isEmpty(); i++) {
            limited.add(pq.poll());
        }
        return limited;
    }

    private void buildCrossingAwareMST(Graph g, Output output) {
        List<Position> vertices = new ArrayList<>(g.getVertices());
        boolean[] visited = new boolean[vertices.size()];
        visited[0] = true;

        for (int i = 1; i < vertices.size(); i++) {
            int minScore = Integer.MAX_VALUE;
            int from = -1, to = -1;
            for (int j = 0; j < vertices.size(); j++) {
                if (!visited[j]) continue;
                for (int k = 0; k < vertices.size(); k++) {
                    if (visited[k]) continue;
                    Position a = vertices.get(j);
                    Position b = vertices.get(k);
                    int score = Math.abs(a.x() - b.x()) + Math.abs(a.y() - b.y());
                    score += countEdgeCrossings(a, b, output);
                    if (score < minScore) {
                        minScore = score;
                        from = j;
                        to = k;
                    }
                }
            }
            if (from != -1 && to != -1) {
                g.addEdge(vertices.get(from), vertices.get(to));
                visited[to] = true;
            }
        }
    }

    private int countEdgeCrossings(Position a, Position b, Output output) {
        int penalty = 0;
        for (Graph g : output.graphs) {
            List<Edge> edges = new ArrayList<>(g.getEdges());
            for (Edge edge : edges) {
                if (edgesIntersect(a, b, edge.getStart(), edge.getEnd())) {
                    penalty += 10;
                }
            }
        }
        return penalty;
    }

    private int countTotalCrossings(Output output) {
        int crossings = 0;
        List<Edge> allEdges = new ArrayList<>();
        for (Graph g : output.graphs) {
            allEdges.addAll(g.getEdges());
        }

        for (int i = 0; i < allEdges.size(); i++) {
            for (int j = i + 1; j < allEdges.size(); j++) {
                Edge e1 = allEdges.get(i);
                Edge e2 = allEdges.get(j);
                if (edgesIntersect(e1.getStart(), e1.getEnd(), e2.getStart(), e2.getEnd())) {
                    crossings++;
                }
            }
        }
        return crossings;
    }

    private boolean edgesIntersect(Position a, Position b, Position c, Position d) {
        return linesIntersect(a.x(), a.y(), b.x(), b.y(), c.x(), c.y(), d.x(), d.y());
    }

    private boolean linesIntersect(int x1, int y1, int x2, int y2,
                                   int x3, int y3, int x4, int y4) {
        int d1 = direction(x3, y3, x4, y4, x1, y1);
        int d2 = direction(x3, y3, x4, y4, x2, y2);
        int d3 = direction(x1, y1, x2, y2, x3, y3);
        int d4 = direction(x1, y1, x2, y2, x4, y4);

        return ((d1 > 0 && d2 < 0) || (d1 < 0 && d2 > 0)) &&
               ((d3 > 0 && d4 < 0) || (d3 < 0 && d4 > 0));
    }

    private int direction(int xi, int yi, int xj, int yj, int xk, int yk) {
        return (xk - xi) * (yj - yi) - (xj - xi) * (yk - yi);
    }
}
