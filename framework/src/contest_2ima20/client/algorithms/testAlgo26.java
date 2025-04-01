package contest_2ima20.client.algorithms;

import contest_2ima20.client.schematrees.SchematicTreesAlgorithm;
import contest_2ima20.core.schematrees.*;

import java.util.*;

public class testAlgo26 extends SchematicTreesAlgorithm {

    @Override
    public Output doAlgorithm(Input input) {
        Output output = new Output(input);
        int width = input.width;
        int height = input.height;
        int radius = input.radius;

        Set<String> occupied = new HashSet<>();
        Random random = new Random();

        for (int setIndex = 0; setIndex < input.sets.size(); setIndex++) {
            NodeSet nodeSet = input.sets.get(setIndex);
            Graph graph = output.graphs.get(setIndex);

            Map<Node, Position> nodeToPosition = new HashMap<>();
            List<Node> nodeList = new ArrayList<>(nodeSet);
            List<Position> placed = new ArrayList<>();

            for (Node node : nodeList) {
                List<int[]> candidates = new ArrayList<>();

                for (int dx = -radius; dx <= radius; dx++) {
                    for (int dy = -radius; dy <= radius; dy++) {
                        if (Math.abs(dx) + Math.abs(dy) > radius) continue;
                        int x = node.x() + dx;
                        int y = node.y() + dy;
                        if (x < 0 || y < 0 || x > width || y > height) continue;

                        String key = x + "," + y;
                        if (!occupied.contains(key)) {
                            candidates.add(new int[]{x, y});
                        }
                    }
                }

                int[] best = null;
                double bestScore = Double.MAX_VALUE;

                for (int[] pos : candidates) {
                    double score = 0;
                    for (Position p : placed) {
                        score += Math.abs(pos[0] - p.x()) + Math.abs(pos[1] - p.y());
                    }
                    if (score < bestScore) {
                        bestScore = score;
                        best = pos;
                    }
                }

                if (best != null) {
                    Position position = graph.getVertices().get(node.id);
                    position.set(best[0], best[1]);
                    nodeToPosition.put(node, position);
                    placed.add(position);
                    occupied.add(best[0] + "," + best[1]);
                }
            }

            connectWithMST(graph, nodeToPosition);
        }

        output.sortToInput();
        return output;
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
                    double dist = Math.abs(positions.get(i).x() - positions.get(j).x()) +
                                   Math.abs(positions.get(i).y() - positions.get(j).y());
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
