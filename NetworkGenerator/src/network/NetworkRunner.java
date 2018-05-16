package network;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Random;
import java.util.UUID;

import javax.imageio.ImageIO;
import javax.naming.InitialContext;

import graphing.GraphUtils;;

public class NetworkRunner {
	
	private static int socialPressure = 0;
	private static int minRetweets = 10;
	
	public void tweet(Node user, boolean[] hasTweeted, int[] numSeen, int[] lastSeen, int ts) {
		hasTweeted[user.id] = true;
		
		for(int id : user.followers) {
			numSeen[id]++;
			lastSeen[id] = ts;
		}
	}
	
	public void tweet(ArrayList<Node> users, boolean[] hasTweeted, int[] numSeen, int[] lastSeen, int ts) {
		for (Node user : users) {
			tweet(user, hasTweeted, numSeen, lastSeen, ts);
		}
	}
	
	public ArrayList<ArrayList<Node>> run(Network net) {
		Random rand = new Random();
		boolean[] hasTweeted = new boolean[net.size];
		int[] numSeen = new int[net.size];
		int[] lastSeen = new int[net.size];
		int start = rand.nextInt(net.size);
		
		ArrayList<ArrayList<Node>> timeSteps = new ArrayList<ArrayList<Node>>();
		
		//System.out.println(net.getNodeById(start).id);
		Node startNode = net.getNodeById(start);
		tweet(startNode, hasTweeted, numSeen, lastSeen, 0);
		
		ArrayList<Node> initalTweet = new ArrayList<Node>();
		initalTweet.add(startNode);
		timeSteps.add(initalTweet);
		
		
		int step = 1;
		int count = 0;
		double baseProb = .02;
		while(count <= 5) {
			ArrayList<Node> toTweet = new ArrayList<Node>();
			for(int i = 0; i < net.size; i++) {
				if(hasTweeted[i] || numSeen[i] == 0) 
					continue;
				
				double r = rand.nextDouble();
				double time_past = step - lastSeen[i];
				
				double prob = baseProb/(time_past*2);

				//TODO: add social pressure influence
				
				if (r < prob) {
					toTweet.add(net.getNodeById(i));
				} 
			}
			tweet(toTweet, hasTweeted, numSeen, lastSeen, step);
			timeSteps.add(toTweet);
			
			if(toTweet.isEmpty()) 
				count++;
			else
				count = 0;
			step++;
		}
		
		return timeSteps;
	}

	public void saveRun(Network net, ArrayList<ArrayList<Node>> timeSteps, String outputDir) throws FileNotFoundException {
		System.out.println("Saving Run...");
		String runID = UUID.randomUUID().toString();
		String dir = outputDir + runID + "/";
		
		if (!new File(dir).exists()) {
			new File(dir).mkdirs();
		}
		
		//Cumulative graph data
		int[] data = new int[timeSteps.size()];
		int count = 0;
		for(int i = 0; i < timeSteps.size(); i++) {
			count += timeSteps.get(i).size();
			data[i] = count;
		}
		
		GraphUtils runDataGraph = new GraphUtils(data);
		runDataGraph.savePlot(dir + "cumulative.png");
		
		
		//Iterative graph data
		data = new int[timeSteps.size()];
		count = 0;
		for(int i = 0; i < timeSteps.size(); i++) {
			data[i] = timeSteps.get(i).size();
		}
		
		runDataGraph = new GraphUtils(data);
		runDataGraph.savePlot(dir + "stepByStep.png");
		
		//follower graph data
		data = new int[timeSteps.size()];
		count = 0;
		for(int i = 0; i < timeSteps.size(); i++) {
			data[i] = net.getNodeById(i).num_followers;
		}
		
		runDataGraph = new GraphUtils(data);
		runDataGraph.savePlot(dir + "followers.png");
		
		
		
		String timeStepsString = "";
		for (ArrayList<Node> ts : timeSteps) {
			for (Node n : ts) {
				timeStepsString += n.id + " ";
			}
			timeStepsString += "\n";
		}
		PrintWriter out = new PrintWriter(dir + "timeSteps.csv");
		out.write(timeStepsString);
		
		System.out.println("Done");
	}
	
	public void runMultiple(Network net, int iterations, String outputDir) throws FileNotFoundException {
		for (int i = 0; i < iterations; i++) {
			ArrayList<ArrayList<Node>> timeSteps = null;
			while(true) {
				timeSteps = run(net);
				if(timeSteps.size() > minRetweets)
					break;
			}
			System.out.println(timeSteps.size());
			
			saveRun(net, timeSteps, outputDir);
		}
	}
	
	public static void main(String[] args) throws IOException {
		String dir = "100000_lowClustering/";
		Network net = new Network(dir + "nodes.csv", dir + "edges.csv");
		
		NetworkRunner runner = new NetworkRunner();
		runner.runMultiple(net, 2, "100000_lowClustering/");
	}
	
}
