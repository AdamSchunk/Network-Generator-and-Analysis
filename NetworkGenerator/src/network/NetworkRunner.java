package network;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Random;
import java.util.UUID;

import graphing.JfreeGraph;;

public class NetworkRunner {
	
	private double socialPressure = 0;
	private int minRetweets = 30;
	private Network net;
	
	public NetworkRunner(double socialPressure, int minRetweets, Network net) {
		this.socialPressure = socialPressure;
		this.minRetweets = minRetweets;
		this.net = net;
	}
	
	public NetworkRunner(double socialPressure, int minRetweets, String networkDirectory) throws IOException {
		this.socialPressure = socialPressure;
		this.minRetweets = minRetweets;
		this.net = new Network(networkDirectory);
	}
	
	public void tweet(Node user, boolean[] hasTweeted, int[] numSeen, int[] lastSeen, int ts) {
		hasTweeted[user.id] = true;
		
		for(int id : user.getFollowersIds()) {
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
		double baseProb = .01;
		if(socialPressure >= 2)
			baseProb = baseProb/socialPressure + baseProb/2;
		int totalTweets = 0;
		while(count <= 5) {
			ArrayList<Node> toTweet = new ArrayList<Node>();
			for(int i = 0; i < net.size; i++) {
				if(hasTweeted[i] || numSeen[i] == 0) 
					continue;
				
				double r = rand.nextDouble();
				double time_past = step - lastSeen[i];
				
				double prob = baseProb/(time_past*3);

				if(socialPressure >= 2) {
					double pressure = Math.min(numSeen[i]*socialPressure/10+1, socialPressure);
					prob = prob*pressure;
				}
				
				
				if (r < prob) {
					toTweet.add(net.getNodeById(i));
					totalTweets++;
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

	public void saveRun(ArrayList<ArrayList<Node>> timeSteps, String outputDir) throws Exception {
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
		
		JfreeGraph runDataGraph = new JfreeGraph(runID, data);
		runDataGraph.saveGraph(dir+"cumulative.png");
		
		//Iterative graph data
		data = new int[timeSteps.size()];
		count = 0;
		for(int i = 0; i < timeSteps.size(); i++) {
			data[i] = timeSteps.get(i).size();
		}
		
		runDataGraph = new JfreeGraph(runID, data);
		runDataGraph.saveGraph(dir+"iterative.png");
		
		//follower graph data
		data = new int[timeSteps.size()];
		count = 0;
		for(int i = 0; i < timeSteps.size(); i++) {
			for(Node n : timeSteps.get(i)) {
				data[i] =+ n.max_followers;
			}
		}
		
		runDataGraph = new JfreeGraph(runID, data);
		runDataGraph.saveGraph(dir+"followers.png");
		
		
		
		String timeStepsString = "";
		for (ArrayList<Node> ts : timeSteps) {
			for (Node n : ts) {
				timeStepsString += n.id + " ";
			}
			timeStepsString += "\n";
		}
		PrintWriter out = new PrintWriter(dir + "timeSteps.csv");
		out.write(timeStepsString);
		out.close();
	}

	public void runMultiple(int iterations, String outputDir) throws Exception {
		for (int i = 0; i < iterations; i++) {
			System.out.println(i);
			ArrayList<ArrayList<Node>> timeSteps = null;
			while(true) {
				//System.out.println("running");
				timeSteps = run(net);
				if(timeSteps.size() > minRetweets) {
					break;
				}
				//System.out.println("bad run");
			}
			saveRun(timeSteps, outputDir);
		}
	}
	
}
