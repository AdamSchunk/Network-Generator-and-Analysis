package network;

import java.io.File;

public class CentralRunner {
	public static void main(String[] args) throws Exception {
		String outputDir = "50000_cluster5_fill70/";

		if (!new File(outputDir).exists()) {
			new File(outputDir).mkdirs();
		}
		
		int size = 50000;
		double rndmFillPercent = .7;
		int clusteringWeight = 5;
		NetworkGenerator ng = new NetworkGenerator();
		Network net = ng.generateNetwork(rndmFillPercent, clusteringWeight, size, outputDir);

		int numRuns = 1000;
		NetworkRunner runner = new NetworkRunner(0, 300, outputDir);
		runner.runMultiple(numRuns, outputDir);

		RunAnalyzer ra = new RunAnalyzer(outputDir);
		ra.countOccurances();

	}
}
