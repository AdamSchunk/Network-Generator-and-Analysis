package network;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Scanner;
import java.util.Set;
import java.util.stream.IntStream;

import arrayUtils.ArrayUtils;
import graphing.JfreeGraph;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import math.ExponentialMovingAverage;
import listUtils.ListUtils;

public class RunAnalyzer {
	
	String runDirectory;
	String netDirectory;
	Network net;
	
	public RunAnalyzer(String netDirectory, String runDirecroty) throws IOException {
		this.runDirectory = runDirecroty;
		this.netDirectory = netDirectory;
		this.net = new Network(netDirectory);
	}
	
	public void countOccurances(String runDir) throws Exception {
		File[] runDirectories = new File(runDir).listFiles(File::isDirectory);
		int[] occurances = new int[net.size];
		int totalTweets = 0;
		for(File f : runDirectories) {
			double[] runKey = loadArray(f.getPath() + "/runKey.csv");
			for(int i = 0; i < runKey.length; i++) {
				if(runKey[i] != -1) {
					occurances[i]++;
					totalTweets++;
				}
			}
		}
		
//		JfreeGraph graph = new JfreeGraph("occurances", occurances);
//		graph.saveGraph(runDir + "occurances.png");
		
		int i = 0;
		for(int occurance : occurances) {
			if(occurance > 200 ) {
				System.out.println(i + ": " + occurance + ", " + net.nodes.get(i).max_followers);	
			}
			i++;
		}
	}

	public void findClustersByRingingThresh() throws IOException {
		int windowSize = 10;
		File[] runDirectories = new File(runDirectory).listFiles(File::isDirectory);
		
		ArrayList<ArrayList<Double>> listOfClusters = new ArrayList<>();
		
		int numNodes = 0;
		for(File f : runDirectories) { //for each run find the nodes that tweeted around our base node
			boolean record = false;
			double[] smoothIterative = loadArray(f.getPath() + "/smoothIterative.csv");
			ArrayList<ArrayList<Node>> timeSteps = loadTimeSteps(f.getPath() + "/timeSteps.csv");
			
			ArrayList<ArrayList<Double>> clustersInFile = new ArrayList<>();
			ArrayList<Double> tmp = new ArrayList<>();
			int spikeWindow = 2;
			for(int i = spikeWindow +1; i < smoothIterative.length; i++) {
				double val = smoothIterative[i];
				double prevVal = smoothIterative[i-spikeWindow];
				if(!record && val > 12 && i > 50 && val - prevVal > 7) {
					tmp = new ArrayList<>();
					record = true;
					
					for(int j = i-windowSize; j < i; j++) {
						for(Node n : timeSteps.get(j)) {
							if(n != null) {
								numNodes++;
								tmp.add((double)n.id);
							}
						}
					}
					if(tmp.size() < 500)
						clustersInFile.add(tmp);
				} else if(record && val < 3) {
					record = false;
				}
			}
			if(clustersInFile.size() != 0)
				clustersInFile.remove(0);
			listOfClusters.addAll(clustersInFile);
		}
		
		System.out.println(numNodes + " total nodes");
		
		analyizeClusters(listOfClusters);
	}
			
	public void analyizeClusters(ArrayList<ArrayList<Double>> listOfClusters) throws IOException {
		
		String outStr = "";
		//finds the complete set of node ids (including non unique)
		//also finds the frequency at which each node is included
		int[] frequency = new int[net.size];
		for(int i = 0; i < listOfClusters.size(); i++) {
			ArrayList<Double> cluster = listOfClusters.get(i);
			for(Double nodeId : cluster) {
				frequency[nodeId.intValue()]++;
				outStr += nodeId.intValue() + " ";
			}
			outStr += "\n";
		}
		
		//filters the list of nodes by how frequently they appear
		int filteredNodeCount = 0;
		ArrayList<Double> filteredIds = new ArrayList<Double>();
		for(int i = 0; i < frequency.length; i++) {
			if (frequency[i] > 10) {
				Node n = net.nodes.get(i);
				if (n.getCurrentNumFollowers() < 4000){
				filteredIds.add(i*1.0);
				filteredNodeCount++;
				}
			}
		}
		
		Double[] nodeIdsD = new Double[filteredIds.size()];
		nodeIdsD = filteredIds.toArray(nodeIdsD);
		
		System.out.println(filteredNodeCount + " filtered unique nodes");
		
		//finds the degree distribution of the filtered set of nodes
		double[] degreesLog = new double[nodeIdsD.length];
		for(int i = 0; i < degreesLog.length; i++) {
			degreesLog[i] = Math.log10(net.nodes.get(nodeIdsD[i].intValue()).max_followers);
			
		}
		

		
		Arrays.sort(degreesLog);
		
	
		
		//finds the subgraph clustering of a set of nodes
		
		ArrayList<Double> subGraphClusterVals = new ArrayList<Double>();
		ArrayList<Double> subGraphFollowerVals = new ArrayList<Double>();
		for(int i = 0; i < listOfClusters.size(); i++) {
			ArrayList<Double> cluster = listOfClusters.get(i);
			Network subGraph = net.getSubgraphD(cluster);
			NetworkAnalyzer netAn = new NetworkAnalyzer();
			for(Node nodeInCluster : subGraph.nodes) {
				Node nInMain = subGraph.nodes.get(nodeInCluster.id);
				subGraphClusterVals.add(subGraph.getClustering(nodeInCluster));
				subGraphFollowerVals.add((double)nInMain.getCurrentNumFollowers());
			}
		}
		ArrayUtils arrayUtils = new ArrayUtils();
		
		double[] subGraphClusterValsArr = new double[subGraphClusterVals.size()];
		double[] subGraphFollowerValsArr = new double[subGraphFollowerVals.size()];
		for (int i = 0; i < subGraphClusterValsArr.length; i++) {
			subGraphClusterValsArr[i] = subGraphClusterVals.get(i).doubleValue();
			subGraphFollowerValsArr[i] = subGraphFollowerVals.get(i).doubleValue(); 
		}
		
		Object[] sortedSubgraphArrays = arrayUtils.sortAonB(subGraphClusterValsArr, subGraphFollowerValsArr);
		double[] sortedSubgraphClusterVals = (double[])sortedSubgraphArrays[0];
		double[] sortedSubgraphFollowerVals = (double[])sortedSubgraphArrays[1];
		
		double[] fullClusterVals = new double[nodeIdsD.length];
		double[] fullFollowerVals = new double[nodeIdsD.length];
		
		//finds the overall clustering of a set of nodes
		System.out.println("finding clustering vals");
		for(int i = 0; i < nodeIdsD.length; i++) {
			int id = nodeIdsD[i].intValue();
			Node n = net.nodes.get(id);
			fullClusterVals[i] = net.getClustering(n);
			fullFollowerVals[i] = n.getCurrentNumFollowers();
		}
		
		//sorts the cluster values based on the number of followers
		System.out.println("sorting clustering vals");
	
		Object[] sortedArrays = arrayUtils.sortAonB(fullClusterVals, fullFollowerVals);
		double[] sortedFullClusterVals = (double[])sortedArrays[0];
		double[] sortedFullFollowerVals = (double[])sortedArrays[1];
		
		
		System.out.println("evaluating logs");
		for(int i = 0; i < sortedFullFollowerVals.length; i++) {
			sortedFullFollowerVals[i] = Math.log10(sortedFullFollowerVals[i]);
		}
		
		JfreeGraph subGraphGrapher = new JfreeGraph("SubgraphClustering Log", sortedSubgraphFollowerVals, sortedSubgraphClusterVals);
		subGraphGrapher.saveGraph(netDirectory + "subgraph clustering.png");	
		
		JfreeGraph grapher = new JfreeGraph("Clustering Log", sortedFullFollowerVals, sortedFullClusterVals);
		grapher.saveGraph(netDirectory + "clusters clustering.png");	
		
		JfreeGraph runDataGraph = new JfreeGraph("log clustering degree" , degreesLog);
		runDataGraph.saveGraph(netDirectory+"cluster degree log.png");
		
		System.out.println("finishedMakingFile");
		
		PrintWriter out = new PrintWriter(netDirectory + "clusters.csv");
		out.write(outStr);
		out.close();
	}
	
	public void findClustersByWindow() throws Exception {
		File[] runDirectories = new File(runDirectory).listFiles(File::isDirectory);
		ArrayList<ArrayList<Node>> windows = new ArrayList<ArrayList<Node>>();
		
		for(Node n : net.nodes) { //for each node generate a window from all the runs
			if(n.max_followers > 500)
				continue;
			int[] window = new int[net.size];
			int numTimesSeen = 0;
			for(File f : runDirectories) { //for each run find the nodes that tweeted around our base node
				double[] runKey = loadArray(f.getPath() + "/runKey.csv");
				if(runKey[n.id] == -1) {
					continue;
				}
				numTimesSeen++;
				double tsIndex = runKey[n.id];
				
				for(int i = 0; i < runKey.length; i++) { 	// i is node id for each node in the run key
					double tsSeen = runKey[i];					// tsSeen is the timeStep that the node is seen
					if(Math.abs(tsSeen-tsIndex) < 30) {
						window[i]++;
					}
				}
			}
			//System.out.println((int)(numTimesSeen));
			ArrayList<Node> strippedWindow = stripUncommonNodesInWindow(window, net, (int)(numTimesSeen*.40));
			windows.add(strippedWindow);
			System.out.println(n.id + ": " + numTimesSeen);
			if(strippedWindow.size() > 3) {
				for(Node nInWin : strippedWindow) {
					System.out.println(nInWin.id);
				}
				System.out.println();
			}
		}
		findClustersFromWindows(windows);
	}
	
	private ArrayList<ArrayList<Node>> findClustersFromWindows(ArrayList<ArrayList<Node>> windows) {
		ArrayList<ArrayList<Node>> finalClusters = new ArrayList<ArrayList<Node>>();
		for(int i = 0; i < windows.size(); i++) {
			ArrayList<Node> clusterI = new ArrayList<Node>();
			boolean found = false;
			for(int j = i+1; j < windows.size(); j++) {
				ListUtils utils = new ListUtils();
				
				ArrayList<Node> overlap = new ArrayList(utils.intersection(windows.get(i), windows.get(j)));
				if(overlap.size() > 5) {
					if(!found) {
						clusterI = new ArrayList(utils.union(clusterI, windows.get(i)));
						found = true;
					}
					clusterI = new ArrayList(utils.union(clusterI, windows.get(j)));	
				}
			}
		}
		return finalClusters;
	}
	
	private ArrayList<ArrayList<Node>> loadTimeSteps(String dir) throws IOException {
		ArrayList<ArrayList<Node>> timeSteps = new ArrayList<ArrayList<Node>>();
		
		try (BufferedReader br = new BufferedReader(new FileReader(dir))) {
			String line;
			while ((line = br.readLine()) != null) {
				ArrayList<Node> timeStep = new ArrayList<>();
				String[] data = line.split(" ");
				for(String d : data) {
					if(!d.isEmpty())
						timeStep.add(net.nodes.get(Integer.parseInt(d)));
					else
						timeStep.add(null);
				}
				timeSteps.add(timeStep);
		    }
		}
		return timeSteps;
	}
	
	private double[] loadArray(String dir) throws IOException {
		double[] runKey = new double[net.size];
		
		try (BufferedReader br = new BufferedReader(new FileReader(dir))) {
			String line;
			while ((line = br.readLine()) != null) {
				line = line.substring(1, line.length()-1);
				runKey = Arrays.stream(line.split(", ")).mapToDouble(Double::parseDouble).toArray();  
		    }
		}
		return runKey;
	}

	
	/*
	 * removes any nodes that are seen less than a 
	 * number of times in the window
	 */
	private ArrayList<Node> stripUncommonNodesInWindow(int[] window, Network net, int thresh) {
		ArrayList<Node> potentialCluster = new ArrayList<Node>();
		
		for(int i = 0; i < window.length; i++) {
			if (window[i] > thresh) {
				potentialCluster.add(net.nodes.get(i));
			}
		}
		return potentialCluster;
	}
		
}
