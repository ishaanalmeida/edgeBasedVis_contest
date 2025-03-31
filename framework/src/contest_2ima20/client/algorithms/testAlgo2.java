package contest_2ima20.client.algorithms;

import contest_2ima20.client.schematrees.SchematicTreesAlgorithm;
import contest_2ima20.core.schematrees.Graph;
import contest_2ima20.core.schematrees.Input;
import contest_2ima20.core.schematrees.Output;
import contest_2ima20.core.schematrees.Position;
import nl.tue.geometrycore.util.IntegerUtil;

/**
 *
 * @author Wouter Meulemans (w.meulemans@tue.nl)
 */
public class testAlgo2 extends SchematicTreesAlgorithm {

    @Override
    public Output doAlgorithm(Input input) {
        Output output = new Output(input);

        for (Graph g : output.graphs) {
            int vertexCount = g.getVertices().size();
            if (vertexCount <= 1) continue;
            
            Position[] vertices = g.getVertices().toArray(new Position[0]);
            
            // Use a circular layout for better edge distribution
            double centerX = input.width / 2.0;
            double centerY = input.width / 2.0;
            double radius = (input.width / 2.0) - (input.radius * 2.0);
            
            for (int i = 0; i < vertices.length; i++) {
                // Calculate position on circle
                double angle = 2 * Math.PI * i / vertexCount;
                double x = centerX + radius * Math.cos(angle);
                double y = centerY + radius * Math.sin(angle);
                
                // Set position
                vertices[i].setX(x);
                vertices[i].setY(y);
                
                // Connect to previous vertex
                if (i > 0) {
                    g.addEdge(vertices[i-1], vertices[i]);
                }
            }
            
            // Connect last to first if more than 2 vertices
            if (vertexCount > 2) {
                g.addEdge(vertices[vertexCount-1], vertices[0]);
            }
        }

        return output;
    }
}
