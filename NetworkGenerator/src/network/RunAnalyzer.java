package network;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Scanner;
import java.util.stream.IntStream;

import graphing.JfreeGraph;

import java.util.ArrayList;
import java.util.Arrays;

import math.ExponentialMovingAverage;

public class RunAnalyzer {
	
	String runDirectory;
	Network net;
	
	public RunAnalyzer(String netDirectory, String runDirecroty) throws IOException {
		this.runDirectory = runDirecroty;
		this.net = new Network(netDirectory);
	}
	
	public void countOccurances(String runDir) throws Exception {
		File[] runDirectories = new File(runDir).listFiles(File::isDirectory);
		int[] occurances = new int[net.size];
		int totalTweets = 0;
		for(File f : runDirectories) {
			int[] runKey = loadRunKey(f.getPath() + "/runKey.csv");
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
				System.out.println(i + ": " + occurance + ", " + net.getNodeById(i).max_followers);	
			}
			i++;
		}
	}
	
	public void findClustersByRingingThresh() {
		File[] runDirectories = new File(runDirectory).listFiles(File::isDirectory);
		ArrayList<ArrayList<Node>> windows = new ArrayList<ArrayList<Node>>();
		
		for(Node n : net.nodes) { //for each node generate a window from all the runs
			if(n.max_followers > 500)
				continue;
			int[] window = new int[net.size];
			int numTimesSeen = 0;
			for(File f : runDirectories) { //for each run find the nodes that tweeted around our base node
				
			}
		}
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
				int[] runKey = loadRunKey(f.getPath() + "/runKey.csv");
				if(runKey[n.id] == -1) {
					continue;
				}
				numTimesSeen++;
				int tsIndex = runKey[n.id];
				
				for(int i = 0; i < runKey.length; i++) { 	// i is node id for each node in the run key
					int tsSeen = runKey[i];					// tsSeen is the timeStep that the node is seen
					if(Math.abs(tsSeen-tsIndex) < 30) {
						window[i]++;
					}
				}
			}
			ArrayList<Node> strippedWindow = stripUncommonNodesInWindow(window, net, (int)(numTimesSeen*.50));
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
			for(int j = i; j < windows.size(); j++) {
				if(j==i)
					continue;
				
				if(overlap(windows.get(i), windows.get(j)) > 5) {
					if(!found) {
						clusterI.addAll(windows.get(i));
						found = true;
					}
						clusterI.addAll(windows.get(j));	
				}
			}
		}
		return finalClusters;
	}
	
	private int overlap(ArrayList<Node> w1, ArrayList<Node> w2) {
		int overlap = 0;
		return overlap;
	}
	
	private ArrayList<ArrayList<Node>> loadTimeSteps(String dir) throws IOException {
		ArrayList<ArrayList<Node>> timeSteps = new ArrayList<ArrayList<Node>>();
		
		try (BufferedReader br = new BufferedReader(new FileReader(dir))) {
			ArrayList<Node> timeStep = new ArrayList<>();
			String line;
			while ((line = br.readLine()) != null) {
				String[] data = line.split(" ");
				for(String d : data) {
					if(!d.isEmpty())
						timeStep.add(net.getNodeById(Integer.parseInt(d)));
					else
						timeStep.add(null);
				}
				timeSteps.add(timeStep);
		    }
		}
		return timeSteps;
	}
	
	private int[] loadRunKey(String dir) throws IOException {
		int[] runKey = new int[net.size];
		
		try (BufferedReader br = new BufferedReader(new FileReader(dir))) {
			String line;
			while ((line = br.readLine()) != null) {
				line = line.substring(1, line.length()-1);
				runKey = Arrays.stream(line.split(", ")).mapToInt(Integer::parseInt).toArray();  
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
				potentialCluster.add(net.getNodeById(i));
			}
		}
		return potentialCluster;
	}
		
}
