package network;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import graphing.GraphUtils;
import graphing.JfreeGraph;

public class NetworkAnalyzer {
	
	String netDirectory;
	Network net;
	
	public NetworkAnalyzer(String runDirecroty) throws IOException {
		this.netDirectory = runDirecroty;
		this.net = new Network(runDirecroty);
	}
		
	public void saveClustering() throws IOException {
		double[] clusterVals = new double[net.nodes.size()];
		double[] followerVals = new double[net.nodes.size()];
		for(Node n : net.nodes) {
			followerVals[n.id] = n.max_followers;
			if(n.id%1000 == 0) {
				System.out.println(n.id);
			}
			
			clusterVals[n.id] = n.getClustering(net);
		}
		double[] sortedClusterVals = new double[clusterVals.length];
		double[] sortedFollowerVals = new double[followerVals.length];
		for(int i = 0; i < clusterVals.length; i++) {
			int minFollowers = Integer.MAX_VALUE;
			double clusterVal = 0;
			double followerVal = 0;
			int index = 0;
			for(int j = 0; j < clusterVals.length; j++) {
				if(clusterVals[j] == 0) {
					continue;
				}
				if(net.getNodeById(j).max_followers <= minFollowers) {
					minFollowers = net.getNodeById(j).max_followers;
					clusterVal = clusterVals[j];
					followerVal = followerVals[j];
					index = j;
				}
			}
			clusterVals[index] = 0;
			sortedClusterVals[i] = clusterVal;
			sortedFollowerVals[i] = followerVal;
		}
		for(int i = 0; i < sortedFollowerVals.length; i++) {
			sortedFollowerVals[i] = Math.log10(sortedFollowerVals[i]);
		}
		JfreeGraph grapher = new JfreeGraph("Clustering Log", sortedFollowerVals, sortedClusterVals);
		grapher.saveGraph(netDirectory + "clustering.png");	
		
	}
}
