package contest_2ima20.client.algorithms;

import contest_2ima20.client.schematrees.SchematicTreesAlgorithm;
import contest_2ima20.core.schematrees.*;

import java.util.*;

public class testAlgo24 extends SchematicTreesAlgorithm {

    @Override
    public Output doAlgorithm(Input input) {
        Output output = new Output(input);
        int width = input.width;
        int height = input.height;
        int radius = input.radius;

        Set<String> occupied = new HashSet<>();

        for (int setIndex = 0; setIndex < input.sets.size(); setIndex++) {
            NodeSet nodeSet = input.sets.get(setIndex);
            Graph graph = output.graphs.get(setIndex);

            Map<Node, Position> nodeToPosition = new HashMap<>();
            List<Position> placedPositions = new ArrayList<>();
            List<Node> nodeList = new ArrayList<>();
            for (Node node : nodeSet) nodeList.add(node);

            Node firstNode = nodeList.get(0);
            Position seed = graph.getVertices().get(firstNode.id);
            seed.set(firstNode.x(), firstNode.y());
            nodeToPosition.put(firstNode, seed);
            placedPositions.add(seed);
            occupied.add(firstNode.x() + "," + firstNode.y());

            for (int i = 1; i < nodeList.size(); i++) {
                Node current = nodeList.get(i);
                Position anchor = placedPositions.get(placedPositions.size() - 1);
                int bestX = -1, bestY = -1;
                double bestScore = Double.MAX_VALUE;

                // Try spiral growth
                for (int r = 1; r <= radius; r++) {
                    for (int dx = -r; dx <= r; dx++) {
                        int dy = r - Math.abs(dx);
                        for (int sign : new int[]{-1, 1}) {
                            int x = anchor.x() + dx;
                            int y = anchor.y() + dy * sign;
                            if (x < 0 || y < 0 || x > width || y > height) continue;
                            String key = x + "," + y;
                            if (occupied.contains(key)) continue;

                            // Check MST-validity (at least one node within radius)
                            boolean valid = false;
                            for (Position p : placedPositions) {
                                if (manhattan(x, y, p.x(), p.y()) <= radius) {
                                    valid = true;
                                    break;
                                }
                            }
                            if (!valid) continue;

                            double score = manhattan(x, y, anchor.x(), anchor.y());
                            if (score < bestScore) {
                                bestScore = score;
                                bestX = x;
                                bestY = y;
                            }
                        }
                    }
                }

                // Fallback greedy if spiral fails
                if (bestX == -1 || bestY == -1) {
                    for (int dx = -radius; dx <= radius; dx++) {
                        for (int dy = -radius; dy <= radius; dy++) {
                            if (Math.abs(dx) + Math.abs(dy) > radius) continue;
                            int x = current.x() + dx;
                            int y = current.y() + dy;
                            if (x < 0 || y < 0 || x > width || y > height) continue;
                            String key = x + "," + y;
                            if (occupied.contains(key)) continue;
                            double score = 0.0;
                            for (Position p : placedPositions) {
                                score += manhattan(x, y, p.x(), p.y());
                            }
                            score /= placedPositions.size();
                            if (score < bestScore) {
                                bestScore = score;
                                bestX = x;
                                bestY = y;
                            }
                        }
                    }
                }

                if (bestX != -1 && bestY != -1) {
                    Position pos = graph.getVertices().get(current.id);
                    pos.set(bestX, bestY);
                    nodeToPosition.put(current, pos);
                    placedPositions.add(pos);
                    occupied.add(bestX + "," + bestY);
                }
            }

            connectWithMST(graph, nodeToPosition);
        }

        output.sortToInput();
        return output;
    }

    private double manhattan(int x1, int y1, int x2, int y2) {
        return Math.abs(x1 - x2) + Math.abs(y1 - y2);
    }

    private void connectWithMST(Graph graph, Map<Node, Position> nodeToPosition) {
        List<Position> positions = new ArrayList<>(nodeToPosition.values());
        int n = positions.size();
        boolean[] connected = new boolean[n];
        connected[0] = true;
        int edgesAdded = 0;

        while (edgesAdded < n - 1) {
            double minDist = Double.MAX_VALUE;
            int from = -1, to = -1;

            for (int i = 0; i < n; i++) {
                if (!connected[i]) continue;
                for (int j = 0; j < n; j++) {
                    if (connected[j]) continue;
                    double dist = manhattan(positions.get(i).x(), positions.get(i).y(), positions.get(j).x(), positions.get(j).y());
                    if (dist < minDist) {
                        minDist = dist;
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
}
