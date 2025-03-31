package contest_2ima20.client.algorithms;

import contest_2ima20.client.schematrees.SchematicTreesAlgorithm;
import contest_2ima20.core.schematrees.*;

import java.util.*;

public class testAlgo5 extends SchematicTreesAlgorithm {

    @Override
    public Output doAlgorithm(Input input) {
        Output output = new Output(input);
        int width = input.width;
        int height = input.height;
        int radius = input.radius;

        Set<String> occupied = new HashSet<>();
        double epsilon = 0.75; // randomness factor
        int densityRadius = 3; // local density check distance

        for (int setIndex = 0; setIndex < input.sets.size(); setIndex++) {
            NodeSet nodeSet = input.sets.get(setIndex);
            Graph graph = output.graphs.get(setIndex);

            Map<Node, Position> nodeToPosition = new HashMap<>();

            // Compute center of mass
            double cx = 0, cy = 0;
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
                    double distToCenter = Math.abs(pos[0] - cx) + Math.abs(pos[1] - cy);
                    double localDensity = 0;

                    // Count occupied neighbors within densityRadius
                    for (int dx = -densityRadius; dx <= densityRadius; dx++) {
                        for (int dy = -densityRadius; dy <= densityRadius; dy++) {
                            if (Math.abs(dx) + Math.abs(dy) > densityRadius) continue;
                            int nx = pos[0] + dx;
                            int ny = pos[1] + dy;
                            String key = nx + "," + ny;
                            if (occupied.contains(key)) {
                                localDensity++;
                            }
                        }
                    }

                    double randomness = Math.random() * epsilon;
                    double score = distToCenter + 2.0 * localDensity + randomness;

                    if (score < bestScore) {
                        bestScore = score;
                        bestPos = pos;
                    }
                }

                if (bestPos != null) {
                    Position position = graph.getVertices().get(node.id);
                    position.set(bestPos[0], bestPos[1]);
                    nodeToPosition.put(node, position);
                    occupied.add(bestPos[0] + "," + bestPos[1]);
                }
            }

            // Greedy MST connection
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

        output.sortToInput();
        return output;
    }

    private double manhattan(Position a, Position b) {
        return Math.abs(a.x() - b.x()) + Math.abs(a.y() - b.y());
    }
}
