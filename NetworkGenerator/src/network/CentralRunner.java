package network;

import java.io.File;
import java.util.ArrayList;

public class CentralRunner {
	public static void main(String[] args) throws Exception {
		String networkDir = "100000_cluster1000/";
		String outputDir = "100000_cluster1000/socialPressure0/";

		if (!new File(networkDir).exists()) {
			new File(networkDir).mkdirs();
		}
		
		if (!new File(outputDir).exists()) {
			new File(outputDir).mkdirs();
		}
		
//		int size = 100000;
//		double rndmFillPercent = 1;
//		int clusteringWeight = 10000;
//		NetworkGenerator ng = new NetworkGenerator();
//		Network net = ng.generateNetwork(rndmFillPercent, clusteringWeight, size, networkDir);
//		
//		NetworkAnalyzer netAn = new NetworkAnalyzer(networkDir);
//		netAn.saveClustering();
		
		//social pressure of 0 or 1 have no effect, 
		//higher numbers mean lower base prob of tweet but more effect from seen.
//		int numRuns = 500;
//		double socialPressure = 0;
//		int minRetweets = 400;
//		NetworkRunner runner = new NetworkRunner(socialPressure, minRetweets, networkDir);
//		runner.runMultiple(numRuns, outputDir);
		

		RunAnalyzer ra = new RunAnalyzer(networkDir, outputDir);
//		ra.smoothIterative();
//		ra.countOccurances(outputDir);
		ra.findClusters();
		
	}
}
