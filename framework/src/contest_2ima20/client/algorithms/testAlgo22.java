package contest_2ima20.client.algorithms;

import contest_2ima20.client.schematrees.SchematicTreesAlgorithm;
import contest_2ima20.core.schematrees.*;

import java.util.*;

public class testAlgo22 extends SchematicTreesAlgorithm {

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

            // Smart seeding
            Iterator<Node> iterator = nodeSet.iterator();
            Node firstNode = iterator.next();
            int[] bestSeed = null;
            double bestSeedScore = Double.MAX_VALUE;

            for (int dx = -radius; dx <= radius; dx++) {
                for (int dy = -radius; dy <= radius; dy++) {
                    if (Math.abs(dx) + Math.abs(dy) > radius) continue;
                    int newX = firstNode.x() + dx;
                    int newY = firstNode.y() + dy;
                    if (newX < 0 || newY < 0 || newX > width || newY > height) continue;

                    double dist = manhattan(newX, newY, firstNode.x(), firstNode.y());
                    if (dist < bestSeedScore) {
                        bestSeedScore = dist;
                        bestSeed = new int[]{newX, newY};
                    }
                }
            }

            if (bestSeed != null) {
                Position pos = graph.getVertices().get(firstNode.id);
                pos.set(bestSeed[0], bestSeed[1]);
                nodeToPosition.put(firstNode, pos);
                placedPositions.add(pos);
                occupied.add(bestSeed[0] + "," + bestSeed[1]);
            }

            // MST-aware greedy expansion
            while (iterator.hasNext()) {
                Node node = iterator.next();
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
                    double distSum = 0.0;
                    for (Position p : placedPositions) {
                        distSum += manhattan(pos, p);
                    }
                    double avgDist = distSum / placedPositions.size();
                    if (avgDist < bestScore) {
                        bestScore = avgDist;
                        bestPos = pos;
                    }
                }

                if (bestPos != null) {
                    Position pos = graph.getVertices().get(node.id);
                    pos.set(bestPos[0], bestPos[1]);
                    nodeToPosition.put(node, pos);
                    placedPositions.add(pos);
                    occupied.add(bestPos[0] + "," + bestPos[1]);
                }
            }

            connectWithMST(graph, nodeToPosition);
        }

        output.sortToInput();
        return output;
    }

    private double manhattan(int[] a, Position b) {
        return Math.abs(a[0] - b.x()) + Math.abs(a[1] - b.y());
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
