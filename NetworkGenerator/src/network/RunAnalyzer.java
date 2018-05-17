package network;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Scanner;
import java.util.ArrayList;

public class RunAnalyzer {
	
	String runDirectory;
	Network net;
	
	public RunAnalyzer(String runDirecroty) throws IOException {
		this.runDirectory = runDirecroty;
		this.net = new Network(runDirecroty);
	}
	
	public ArrayList<ArrayList<Node>> loadTimeSteps(String dir) throws FileNotFoundException {
		ArrayList<ArrayList<Node>> timeSteps = new ArrayList<ArrayList<Node>>();
		try (Scanner scanner = new Scanner(new File(dir))) {
			while (scanner.hasNext()){
				ArrayList<Node> timeStep = new ArrayList<>();
				String line = scanner.next();
				String[] data = line.split(",");
				for(String d : data) {
					timeStep.add(net.getNodeById(Integer.parseInt(d)));
				}
				timeSteps.add(timeStep);
			}
        }
		return timeSteps;
	}
	
	public void countOccurances() throws FileNotFoundException {
		File[] runDirectories = new File(runDirectory).listFiles(File::isDirectory);
		int[] occurances = new int[net.size];
		int totalTweets = 0;
		for(File f : runDirectories) {
			ArrayList<ArrayList<Node>> timeSteps = loadTimeSteps(f.getPath() + "/timeSteps.csv");
			for(int i = 0; i < timeSteps.size(); i++) {
				for(Node n : timeSteps.get(i)) {
					occurances[n.id]++;
					totalTweets++;
				}
			}
		}
		int i = 0;
		for(int occurance : occurances) {
			if(occurance > 100 ) {
				System.out.println(i + ": " + occurance);	
			}
			i++;
		}
		//System.out.println(totalTweets);
	}
	
	public void findClusters() throws FileNotFoundException {
		File[] runDirectories = new File(runDirectory).listFiles(File::isDirectory);
		ArrayList<ArrayList<Node>> windows = new ArrayList<ArrayList<Node>>();
		
		for(Node n : net.nodes) { //for each node generate a window from each run
			if(n.num_followers < 100 || n.num_followers > 2000)
				continue;
			
			int[] window = new int[net.size];
			for(File f : runDirectories) { //for each run find the nodes that tweeted around our base node
				ArrayList<ArrayList<Node>> timeSteps = loadTimeSteps(f.getPath() + "/timeSteps.csv");
				for(int i = 0; i < timeSteps.size(); i++) { //for each timestep find the initial node
					if(!timeSteps.get(i).contains(n)) {
						continue;
					}
					
					int back = i-30;
					if(back < 0)
						back = 0;
					int front = i + 30;
					if(front > net.size-1)
						front = net.size-1;
					
					for(int j = back; j < front; j++) {
						ArrayList<Node> tsInWindow = timeSteps.get(j);
						for (Node nodeInWindow : tsInWindow) {
							if(n.num_followers < 100 || n.num_followers > 2000)
								continue;
							window[nodeInWindow.id]++;
						}
					}
					
				}
			}
			windows.add(stripUncommonNodesInWindow(window, net));
		}
	}
	
	public ArrayList<Node> stripUncommonNodesInWindow(int[] window, Network net) {
		ArrayList<Node> potentialCluster = new ArrayList<Node>();
		
		for(int i = 0; i < window.length; i++) {
			if (window[i] > 15) {
				potentialCluster.add(net.getNodeById(i));
			}
		}
		return potentialCluster;
	}
		
}
