package contest_2ima20.client.algorithms;

import contest_2ima20.client.schematrees.SchematicTreesAlgorithm;
import contest_2ima20.core.schematrees.*;

import java.util.*;

public class testAlgo7 extends SchematicTreesAlgorithm {

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

            // Center of mass
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
                    double score = Math.abs(pos[0] - cx) + Math.abs(pos[1] - cy);
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

            connectWithMST(graph, nodeToPosition);
        }

        // Local jittering phase
        double bestQuality = output.computeQuality();
        boolean improved;

        do {
            improved = false;

            for (int setIndex = 0; setIndex < input.sets.size(); setIndex++) {
                NodeSet nodeSet = input.sets.get(setIndex);
                Graph graph = output.graphs.get(setIndex);

                for (Node node : nodeSet) {
                    Position current = graph.getVertices().get(node.id);
                    int origX = node.x(), origY = node.y();
                    int currX = current.x(), currY = current.y();

                    int[] bestPos = new int[]{currX, currY};
                    double localBest = bestQuality;

                    for (int dx = -radius; dx <= radius; dx++) {
                        for (int dy = -radius; dy <= radius; dy++) {
                            if (Math.abs(dx) + Math.abs(dy) > radius) continue;

                            int newX = origX + dx;
                            int newY = origY + dy;
                            String key = newX + "," + newY;

                            if (newX < 0 || newY < 0 || newX > width || newY > height) continue;
                            if (newX == currX && newY == currY) continue;
                            if (isOccupiedExcept(occupied, newX, newY, currX, currY)) continue;

                            current.set(newX, newY);

                            Graph tempGraph = new Graph(nodeSet);
                            for (Position p : graph.getVertices()) {
                                tempGraph.addVertex(p.node).set(p.x(), p.y());
                            }

                            // FIX: Rebuild local nodeToPosition
                            Map<Node, Position> tempNodeToPosition = new HashMap<>();
                            for (Position p : tempGraph.getVertices()) {
                                tempNodeToPosition.put(p.node, p);
                            }

                            connectWithMST(tempGraph, tempNodeToPosition);
                            Output tempOutput = new Output(input);
                            tempOutput.graphs.set(setIndex, tempGraph);
                            double newQuality = tempOutput.computeQuality();

                            if (newQuality < localBest) {
                                localBest = newQuality;
                                bestPos = new int[]{newX, newY};
                            }

                            current.set(currX, currY); // revert
                        }
                    }

                    if (bestPos[0] != currX || bestPos[1] != currY) {
                        occupied.remove(currX + "," + currY);
                        current.set(bestPos[0], bestPos[1]);
                        occupied.add(bestPos[0] + "," + bestPos[1]);
                        improved = true;
                        bestQuality = localBest;
                    }
                }
            }

        } while (improved);

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

    private boolean isOccupiedExcept(Set<String> occupied, int x, int y, int currX, int currY) {
        String key = x + "," + y;
        String currentKey = currX + "," + currY;
        return !key.equals(currentKey) && occupied.contains(key);
    }

    private double manhattan(Position a, Position b) {
        return Math.abs(a.x() - b.x()) + Math.abs(a.y() - b.y());
    }
}
