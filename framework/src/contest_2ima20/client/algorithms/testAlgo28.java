package contest_2ima20.client.algorithms;

import contest_2ima20.client.schematrees.SchematicTreesAlgorithm;
import contest_2ima20.core.schematrees.*;

import java.util.*;

public class testAlgo28 extends SchematicTreesAlgorithm {

    @Override
    public Output doAlgorithm(Input input) {
        Output output = new Output(input);
        int width = input.width;
        int height = input.height;
        int radius = input.radius;

        Set<String> occupied = new HashSet<>();
        Random random = new Random();
        double crowdingPenaltyWeight = 1.2;
        double centerBiasWeight = 0.8;

        for (int setIndex = 0; setIndex < input.sets.size(); setIndex++) {
            NodeSet nodeSet = input.sets.get(setIndex);
            Graph graph = output.graphs.get(setIndex);
            Map<Node, Position> nodeToPosition = new HashMap<>();
            List<Position> placedPositions = new ArrayList<>();

            List<Node> sortedNodes = new ArrayList<>(nodeSet);
            sortedNodes.sort(Comparator
                    .comparingInt(Node::x)
                    .thenComparingInt(Node::y));

            Position anchor = new Position();  // Fix for constructor error
            anchor.set(width / 2, height / 2);

            for (Node node : sortedNodes) {
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
                    double distToAnchor = manhattan(pos[0], pos[1], anchor.x(), anchor.y());
                    double crowdPenalty = 0.0;
                    for (Position other : placedPositions) {
                        double d = manhattan(pos[0], pos[1], other.x(), other.y());
                        if (d < 3) {
                            crowdPenalty += (3 - d);
                        }
                    }
                    double score = centerBiasWeight * distToAnchor + crowdingPenaltyWeight * crowdPenalty + random.nextDouble() * 0.2;
                    if (score < bestScore) {
                        bestScore = score;
                        bestPos = pos;
                    }
                }

                if (bestPos != null) {
                    Position p = graph.getVertices().get(node.id);
                    p.set(bestPos[0], bestPos[1]);
                    nodeToPosition.put(node, p);
                    placedPositions.add(p);
                    occupied.add(bestPos[0] + "," + bestPos[1]);
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
