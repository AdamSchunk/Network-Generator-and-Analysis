package network;

import java.io.File;
import java.util.ArrayList;

public class CentralRunner {
	public static void main(String[] args) throws Exception {
		String networkDir = "100000_cluster1000/";
		String outputDir = "100000_cluster1000/socialPressure10/";

		if (!new File(networkDir).exists()) {
			new File(networkDir).mkdirs();
		}
		
		if (!new File(outputDir).exists()) {
			new File(outputDir).mkdirs();
		}
		
//		int size = 100000;
//		double rndmFillAtPercent = 1;
//		int clusteringWeight = 1000;
//		NetworkGenerator ng = new NetworkGenerator();
//		Network net = ng.generateNetwork(rndmFillAtPercent, clusteringWeight, size, networkDir);
		
//		NetworkAnalyzer netAn = new NetworkAnalyzer();
//		Network net = new Network(networkDir);
//		netAn.saveClustering(net, networkDir, "network log clustering");
//		netAn.saveNodeDistribution(net, networkDir);
//		netAn.saveClusteringById(net, networkDir);
		
		//social pressure indicates how much more liekly someone is to tweet after their friend tweets
		// social pressure should be 
		int numRuns = 20;
		double socialPressure = 10;
		int minRetweets = 1000;
		NetworkRunner runner = new NetworkRunner(socialPressure, minRetweets, networkDir);
		runner.runMultiple(numRuns, outputDir);
		

//		RunAnalyzer ra = new RunAnalyzer(networkDir, outputDir);
//		ra.countOccurances(outputDir);
//		ra.findClustersByRingingThresh();
//		ra.findClustersByWindow();
//		
	}
}
