package contest_2ima20.client.algorithms;

import contest_2ima20.client.schematrees.SchematicTreesAlgorithm;
import contest_2ima20.core.schematrees.Graph;
import contest_2ima20.core.schematrees.Input;
import contest_2ima20.core.schematrees.Output;
import contest_2ima20.core.schematrees.Position;
import nl.tue.geometrycore.util.IntegerUtil;

import java.util.*;

public class PlanarityGuidedAlgorithm extends SchematicTreesAlgorithm {

    private static final int CANDIDATE_LIMIT = 20; // limit the number of position candidates

    @Override
    public Output doAlgorithm(Input input) {
        Output output = new Output(input);
        Set<String> usedPositions = new HashSet<>();

        // Step 1: Candidate Generation (with limit to prevent OOM)
        Map<Position, List<int[]>> candidateMap = new IdentityHashMap<>();
        for (Graph g : output.graphs) {
            for (Position p : g.getVertices()) {
                int cx = p.x();
                int cy = p.y();
                PriorityQueue<int[]> pq = new PriorityQueue<>(Comparator.comparingInt(v -> Math.abs(v[0] - cx) + Math.abs(v[1] - cy)));

                for (int dx = -input.radius; dx <= input.radius; dx++) {
                    for (int dy = -input.radius; dy <= input.radius; dy++) {
                        if (Math.abs(dx) + Math.abs(dy) <= input.radius) {
                            int x = cx + dx;
                            int y = cy + dy;
                            if (x >= 0 && x <= input.width && y >= 0 && y <= input.height) {
                                pq.offer(new int[]{x, y});
                            }
                        }
                    }
                }

                List<int[]> limitedCandidates = new ArrayList<>(CANDIDATE_LIMIT);
                for (int i = 0; i < CANDIDATE_LIMIT && !pq.isEmpty(); i++) {
                    limitedCandidates.add(pq.poll());
                }

                candidateMap.put(p, limitedCandidates);
            }
        }

        // Step 2: Conflict-Free Assignment (greedy)
        for (Graph g : output.graphs) {
            for (Position p : g.getVertices()) {
                List<int[]> candidates = candidateMap.get(p);
                for (int[] v : candidates) {
                    String key = v[0] + "," + v[1];
                    if (!usedPositions.contains(key)) {
                        p.setX(v[0]);
                        p.setY(v[1]);
                        usedPositions.add(key);
                        break;
                    }
                }
            }
        }

        // Step 3: MST Construction with Manhattan distances
        for (Graph g : output.graphs) {
            List<Position> vertices = new ArrayList<>(g.getVertices());
            boolean[] visited = new boolean[vertices.size()];
            visited[0] = true;
            for (int i = 1; i < vertices.size(); i++) {
                int minDist = Integer.MAX_VALUE;
                int from = -1, to = -1;
                for (int j = 0; j < vertices.size(); j++) {
                    if (!visited[j]) continue;
                    for (int k = 0; k < vertices.size(); k++) {
                        if (visited[k]) continue;
                        int dist = Math.abs(vertices.get(j).x() - vertices.get(k).x()) +
                                   Math.abs(vertices.get(j).y() - vertices.get(k).y());
                        if (dist < minDist) {
                            minDist = dist;
                            from = j;
                            to = k;
                        }
                    }
                }
                if (from != -1 && to != -1) {
                    g.addEdge(vertices.get(from), vertices.get(to));
                    visited[to] = true;
                }
            }
        }

        return output;
    }
}
