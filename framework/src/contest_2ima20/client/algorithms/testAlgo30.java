package contest_2ima20.client.algorithms;

import contest_2ima20.client.schematrees.SchematicTreesAlgorithm;
import contest_2ima20.core.schematrees.*;

import java.util.*;

public class testAlgo30 extends SchematicTreesAlgorithm {

    @Override
    public Output doAlgorithm(Input input) {
        Output output = new Output(input);
        int width = input.width;
        int height = input.height;
        int radius = input.radius;

        Set<String> occupied = new HashSet<>();
        Random random = new Random();
        double randomWeight = 0.5;
        double decay = 1.25;
        double alpha = 0.2;
        double gamma = 0.1;
        double delta = 2.0;

        for (int setIndex = 0; setIndex < input.sets.size(); setIndex++) {
            NodeSet nodeSet = input.sets.get(setIndex);
            Graph graph = output.graphs.get(setIndex);

            Map<Node, Position> nodeToPosition = new HashMap<>();
            List<Position> placedPositions = new ArrayList<>();

            double cx = 0.0, cy = 0.0;
            for (Node node : nodeSet) {
                cx += node.x();
                cy += node.y();
            }
            cx /= nodeSet.size();
            cy /= nodeSet.size();

            for (Node node : nodeSet) {
                List<int[]> candidates = new ArrayList<>();

                for (int dx = -radius; dx <= radius; dx++) {
                    for (int dy = -radius; dy <= radius; dy++) {
                        if (Math.abs(dx) + Math.abs(dy) > radius) continue;

                        int newX = node.x() + dx;
                        int newY = node.y() + dy;

                        if (newX < 0 || newY < 0 || newX > width || newY > height) continue;

                        String key = newX + "," + newY;
                        if (!occupied.contains(key)) {
                            candidates.add(new int[]{newX, newY});
                        }
                    }
                }

                int[] bestPos = null;
                double bestScore = Double.MAX_VALUE;

                for (int[] pos : candidates) {
                    double weightedDist = 0.0;
                    int size = placedPositions.size();

                    for (int i = 0; i < size; i++) {
                        Position placed = placedPositions.get(i);
                        double weight = Math.pow(size - i, decay);
                        weightedDist += weight * manhattan(pos[0], pos[1], placed.x(), placed.y());
                    }

                    double beta = 1.0 / (placedPositions.size() + 1);
                    double centerAttract = alpha * manhattan(pos[0], pos[1], cx, cy);
                    double mstAnchor = (size > 0) ? beta * manhattan(pos[0], pos[1], placedPositions.get(0).x(), placedPositions.get(0).y()) : 0.0;
                    double densityPenalty = gamma * countNearbyPlacedNodes(pos, placedPositions, 2) / (double)(placedPositions.size() + 1);
                    double crossingPenalty = delta * estimateEdgeCrossings(pos, placedPositions, graph.getEdges());
                    double randomness = random.nextDouble() * randomWeight;
                    double score = weightedDist + centerAttract + mstAnchor + densityPenalty + crossingPenalty + randomness;

                    if (score < bestScore) {
                        bestScore = score;
                        bestPos = pos;
                    }
                }

                if (bestPos != null) {
                    Position position = graph.getVertices().get(node.id);
                    position.set(bestPos[0], bestPos[1]);
                    nodeToPosition.put(node, position);
                    placedPositions.add(position);
                    occupied.add(bestPos[0] + "," + bestPos[1]);
                }
            }

            connectWithCrossingAwareMST(graph, nodeToPosition, delta);
        }

        output.sortToInput();
        return output;
    }

    private double manhattan(int x1, int y1, double x2, double y2) {
        return Math.abs(x1 - x2) + Math.abs(y1 - y2);
    }

    private double manhattan(Position a, Position b) {
        return manhattan(a.x(), a.y(), b.x(), b.y());
    }

    private int countNearbyPlacedNodes(int[] pos, List<Position> placed, int threshold) {
        int count = 0;
        for (Position p : placed) {
            if (manhattan(pos[0], pos[1], p.x(), p.y()) <= threshold) {
                count++;
            }
        }
        return count;
    }

    private int estimateEdgeCrossings(int[] newPos, List<Position> placed, List<Edge> existingEdges) {
        int crossings = 0;
        for (Position p : placed) {
            for (Edge e : existingEdges) {
                if (segmentsCross(newPos[0], newPos[1], p.x(), p.y(),
                                  e.getStart().x(), e.getStart().y(),
                                  e.getEnd().x(), e.getEnd().y())) {
                    crossings++;
                }
            }
        }
        return crossings;
    }

    private void connectWithCrossingAwareMST(Graph graph, Map<Node, Position> nodeToPosition, double delta) {
        List<Position> positions = new ArrayList<>(nodeToPosition.values());
        int n = positions.size();
        boolean[] connected = new boolean[n];
        connected[0] = true;
        int edgesAdded = 0;

        while (edgesAdded < n - 1) {
            double minScore = Double.MAX_VALUE;
            int from = -1, to = -1;

            for (int i = 0; i < n; i++) {
                if (!connected[i]) continue;
                for (int j = 0; j < n; j++) {
                    if (connected[j]) continue;
                    double dist = manhattan(positions.get(i), positions.get(j));
                    int crossings = 0;
                    for (Edge e : graph.getEdges()) {
                        if (segmentsCross(positions.get(i).x(), positions.get(i).y(),
                                          positions.get(j).x(), positions.get(j).y(),
                                          e.getStart().x(), e.getStart().y(),
                                          e.getEnd().x(), e.getEnd().y())) {
                            crossings++;
                        }
                    }
                    double score = dist + delta * crossings;
                    if (score < minScore) {
                        minScore = score;
                        from = i;
                        to = j;
                    }
                }
            }

            if (from != -1 && to != -1) {
                graph.addEdge(positions.get(from), positions.get(to));
                connected[to] = true;
                edgesAdded++;
            }
        }
    }

    private boolean segmentsCross(int ax, int ay, int bx, int by, int cx, int cy, int dx, int dy) {
        return ccw(ax, ay, cx, cy, dx, dy) != ccw(bx, by, cx, cy, dx, dy) &&
               ccw(ax, ay, bx, by, cx, cy) != ccw(ax, ay, bx, by, dx, dy);
    }

    private boolean ccw(int ax, int ay, int bx, int by, int cx, int cy) {
        return (cy - ay) * (bx - ax) > (by - ay) * (cx - ax);
    }
}
