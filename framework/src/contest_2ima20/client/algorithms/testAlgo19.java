package contest_2ima20.client.algorithms;

import contest_2ima20.client.schematrees.SchematicTreesAlgorithm;
import contest_2ima20.core.schematrees.*;

import java.util.*;

public class testAlgo19 extends SchematicTreesAlgorithm {

    @Override
    public Output doAlgorithm(Input input) {
        Output output = new Output(input);
        int width = input.width;
        int height = input.height;
        int radius = input.radius;

        Set<String> occupied = new HashSet<>();
        Random random = new Random();
        double randomWeight = 0.3;
        double alpha = 0.2;

        for (int setIndex = 0; setIndex < input.sets.size(); setIndex++) {
            NodeSet nodeSet = input.sets.get(setIndex);
            Graph graph = output.graphs.get(setIndex);

            Map<Node, Position> nodeToPosition = new HashMap<>();
            List<Position> placedPositions = new ArrayList<>();
            List<Position> mstFrontier = new ArrayList<>();

            double cx = 0.0, cy = 0.0;
            for (Node node : nodeSet) {
                cx += node.x();
                cy += node.y();
            }
            cx /= nodeSet.size();
            cy /= nodeSet.size();

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

                    double distToCenter = manhattan(newX, newY, cx, cy);
                    double seedScore = distToCenter + random.nextDouble();
                    if (seedScore < bestSeedScore) {
                        bestSeedScore = seedScore;
                        bestSeed = new int[]{newX, newY};
                    }
                }
            }

            if (bestSeed != null) {
                Position seedPos = graph.getVertices().get(firstNode.id);
                seedPos.set(bestSeed[0], bestSeed[1]);
                nodeToPosition.put(firstNode, seedPos);
                placedPositions.add(seedPos);
                mstFrontier.add(seedPos);
                occupied.add(bestSeed[0] + "," + bestSeed[1]);
            }

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
                    double frontierDist = 0.0;
                    for (Position p : mstFrontier) {
                        frontierDist += manhattan(pos[0], pos[1], p.x(), p.y());
                    }
                    if (!mstFrontier.isEmpty()) {
                        frontierDist /= mstFrontier.size();
                    }

                    double centerAttract = alpha * manhattan(pos[0], pos[1], cx, cy);
                    double randomness = random.nextDouble() * randomWeight;
                    double score = frontierDist + centerAttract + randomness;

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
                    mstFrontier.add(position);
                    occupied.add(bestPos[0] + "," + bestPos[1]);
                }
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
