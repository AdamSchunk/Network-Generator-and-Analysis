package network;

import java.io.File;

public class CentralRunner {
	public static void main(String[] args) throws Exception {
		String networkDir = "10000_test/";
		String outputDir = "50000_rndm/socialPressure3/";

		if (!new File(networkDir).exists()) {
			new File(networkDir).mkdirs();
		}
		
		if (!new File(outputDir).exists()) {
			new File(outputDir).mkdirs();
		}
		
//		int size = 10000;
//		double rndmFillPercent = .7;
//		int clusteringWeight = 5;
//		NetworkGenerator ng = new NetworkGenerator();
//		Network net = ng.generateNetwork(rndmFillPercent, clusteringWeight, size, networkDir);
		
		
		Network net = new Network(networkDir);
		
		for(Node n : net.nodes) {
			System.out.println(n.max_followers + " ... " +  n.getCurrentNumFollowers());
		}
		
		//social pressure of 0 or 1 have no effect, 
		//higher numbers mean lower base prob of tweet but more effect from seen.
//		int numRuns = 900;
//		double socialPressure = 3;
//		int minRetweets = 400;
//		NetworkRunner runner = new NetworkRunner(socialPressure, minRetweets, networkDir);
//		runner.runMultiple(numRuns, outputDir);

//		RunAnalyzer ra = new RunAnalyzer(outputDir);
//		ra.findClusters();
	}
}
