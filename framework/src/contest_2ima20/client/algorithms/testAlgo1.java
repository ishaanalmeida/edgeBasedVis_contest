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
public class testAlgo1 extends SchematicTreesAlgorithm {

    @Override
    public Output doAlgorithm(Input input) {
        Output output = new Output(input);

        for (Graph g : output.graphs) {
            int vertexCount = g.getVertices().size();
            if (vertexCount <= 1) continue;
            
            Position[] vertices = g.getVertices().toArray(new Position[0]);
            
            // Calculate optimal grid dimensions
            int cols = (int)Math.ceil(Math.sqrt(vertexCount));
            int rows = (int)Math.ceil((double)vertexCount / cols);
            
            // Calculate safe boundaries
            double margin = input.radius * 1.2;
            double maxX = input.width - margin;
            double maxY = input.width - margin;
            
            // Calculate step sizes to ensure even distribution
            double stepX = (maxX - margin) / Math.max(1, cols - 1);
            double stepY = (maxY - margin) / Math.max(1, rows - 1);
            
            // Use snake pattern to reduce edge lengths
            for (int i = 0; i < vertices.length; i++) {
                int row = i / cols;
                int col = i % cols;
                
                // Reverse direction for odd rows (snake pattern)
                if (row % 2 == 1) {
                    col = cols - 1 - col;
                }
                
                // Calculate position with proper spacing
                double x = margin + (col * stepX);
                double y = margin + (row * stepY);
                
                // Set position
                vertices[i].setX(x);
                vertices[i].setY(y);
                
                // Connect to previous vertex
                if (i > 0) {
                    g.addEdge(vertices[i-1], vertices[i]);
                }
            }
        }

        return output;
    }
}