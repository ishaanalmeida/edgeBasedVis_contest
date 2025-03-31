package contest_2ima20.client.algorithms;

import contest_2ima20.client.schematrees.SchematicTreesAlgorithm;
import contest_2ima20.core.schematrees.*;

import java.util.*;

/**
 * Baseline algorithm for valid point placement and MST construction.
 */
public class testAlgo3 extends SchematicTreesAlgorithm {

    @Override
    public Output doAlgorithm(Input input) {
        Output output = new Output(input);
        int width = input.width;
        int height = input.height;
        int radius = input.radius;

        Set<String> occupied = new HashSet<>(); // To ensure unique positions across all sets

        for (int setIndex = 0; setIndex < input.sets.size(); setIndex++) {
            NodeSet nodeSet = input.sets.get(setIndex);
            Graph graph = output.graphs.get(setIndex);

            Map<Node, Position> nodeToPosition = new HashMap<>();

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

                if (!candidates.isEmpty()) {
                    int[] pos = candidates.get(new Random().nextInt(candidates.size()));
                    Position position = graph.getVertices().get(node.id);
                    position.set(pos[0], pos[1]);
                    nodeToPosition.put(node, position);
                    occupied.add(pos[0] + "," + pos[1]);
                }
            }

            // Greedy MST construction using Manhattan distance
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
                } else {
                    break; // should not happen in valid input
                }
            }
        }

        output.sortToInput();
        return output;
    }

    private double manhattan(Position a, Position b) {
        return Math.abs(a.x() - b.x()) + Math.abs(a.y() - b.y());
    }
}
