/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package contest_2ima20.client.algorithms;

import contest_2ima20.client.schematrees.SchematicTreesAlgorithm;

/**
 *
 * @author Wouter Meulemans (w.meulemans@tue.nl)
 */
public class AlgorithmList {

    public static SchematicTreesAlgorithm[] getAlgorithms() {
        return new SchematicTreesAlgorithm[]{
            new ArbitraryAlgorithm(),
            // new testAlgo1(),
            // new testAlgo2(),
            // new testAlgo3(),
            new testAlgo4(),
            // new testAlgo7(),
            // new testAlgo8(),
            new testAlgo9(),
            new testAlgo12(),
            new testAlgo14(),
        };
    }
}
