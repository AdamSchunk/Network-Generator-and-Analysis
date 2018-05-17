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
		for(Node n : net.nodes) {
			int[] window = new int[net.size];
			for(File f : runDirectories) {
				ArrayList<ArrayList<Node>> timeSteps = loadTimeSteps(f.getPath() + "/timeSteps.csv");
				for(int i = 0; i < timeSteps.size(); i++) {
					if(!timeSteps.get(i).contains(n)) {
						continue;
					}
				}
			}
		}
	}
	
	public static void main(String[] args) throws IOException {
		String dir = "100000_rndm/";
		RunAnalyzer ra = new RunAnalyzer(dir);
		ra.countOccurances();
	}	
}
