package contest_2ima20.client.algorithms;

import contest_2ima20.client.schematrees.SchematicTreesAlgorithm;
import contest_2ima20.core.schematrees.*;

import java.util.*;

public class testAlgo20 extends SchematicTreesAlgorithm {

    @Override
    public Output doAlgorithm(Input input) {
        Output output = new Output(input);
        int width = input.width;
        int height = input.height;
        int radius = input.radius;

        Random random = new Random();
        int k = 10; // samples per node
        double initialTemp = 1.0;
        double lambda = 0.05;
        double alpha = 0.2;  // center bias

        Set<String> occupied = new HashSet<>();

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

            int step = 0;

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

                double temp = initialTemp * Math.exp(-lambda * step);
                int[] selectedPos = null;
                double bestScore = Double.MAX_VALUE;

                for (int i = 0; i < k && !candidates.isEmpty(); i++) {
                    int[] pos = candidates.remove(random.nextInt(candidates.size()));
                    double frontierDist = 0.0;
                    for (Position p : placedPositions) {
                        frontierDist += manhattan(pos[0], pos[1], p.x(), p.y());
                    }
                    if (!placedPositions.isEmpty()) {
                        frontierDist /= placedPositions.size();
                    }

                    double centerBias = alpha * manhattan(pos[0], pos[1], cx, cy);
                    double score = frontierDist + centerBias;

                    if (score < bestScore || random.nextDouble() < Math.exp((bestScore - score) / temp)) {
                        bestScore = score;
                        selectedPos = pos;
                    }
                }

                if (selectedPos != null) {
                    Position position = graph.getVertices().get(node.id);
                    position.set(selectedPos[0], selectedPos[1]);
                    nodeToPosition.put(node, position);
                    placedPositions.add(position);
                    occupied.add(selectedPos[0] + "," + selectedPos[1]);
                }

                step++;
            }

            connectWithMST(graph, nodeToPosition);
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
                    double dist = manhattan(positions.get(i), positions.get(j));
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
