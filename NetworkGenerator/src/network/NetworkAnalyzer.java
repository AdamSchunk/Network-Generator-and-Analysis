package network;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import graphing.GraphUtils;
import graphing.JfreeGraph;

public class NetworkAnalyzer {
	
	public void saveNodeDistribution(Network net, String netDirectory) throws IOException{
		double[] degrees = new double[net.size];
		for(int i = 0; i < net.size; i++) {
			Node n = net.nodes.get(i);
			degrees[i] = Math.log10(n.max_followers);
		}
		Arrays.sort(degrees);
		
		JfreeGraph runDataGraph = new JfreeGraph("log network degree" , degrees);
		runDataGraph.saveGraph(netDirectory+"log network degree.png");
	}
	
	public double[] getClustering(Network net) throws IOException {
		double[] clusterVals = new double[net.nodes.size()];
		double[] followerVals = new double[net.nodes.size()];
		for(Node n : net.nodes) {
			followerVals[n.id] = n.max_followers;
			if(n.id%1000 == 0 && net.size > 10000) {
				System.out.println(n.id);
			}
			double clusterVal = net.getClustering(n);
			System.out.println(clusterVal);
			clusterVals[n.id] = clusterVal;
		}
		return clusterVals;
		
	}
	
	public void saveClustering(Network net, String netDirectory, String fileName) throws IOException {
		double[] clusterVals = getClustering(net);
		double[] sortedClusterVals = new double[clusterVals.length];
		double[] sortedFollowerVals = new double[net.nodes.size()];

		
		for(int i = 0; i < clusterVals.length; i++) {
			int minFollowers = Integer.MAX_VALUE;
			double clusterVal = 0;
			int index = 0;
			for(int j = 0; j < clusterVals.length; j++) {
				if(clusterVals[j] == 0) {
					continue;
				}
				if(net.nodes.get(j).max_followers <= minFollowers) {
					minFollowers = net.nodes.get(j).max_followers;
					clusterVal = clusterVals[j];
					index = j;
				}
			}
			clusterVals[index] = 0;
			sortedClusterVals[i] = clusterVal;
			sortedFollowerVals[i] = minFollowers;
		}
		for(int i = 0; i < sortedFollowerVals.length; i++) {
			sortedFollowerVals[i] = Math.log10(sortedFollowerVals[i]);
		}
		JfreeGraph grapher = new JfreeGraph("Log Node Clustering by Followers", sortedFollowerVals, sortedClusterVals);
		grapher.saveGraph(netDirectory + fileName + ".png");	
		
	}
}
