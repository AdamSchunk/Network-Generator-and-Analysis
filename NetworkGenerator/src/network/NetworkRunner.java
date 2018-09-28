package network;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;
import java.util.UUID;

import graphing.JfreeGraph;
import math.ExponentialMovingAverage;

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
		
		for(int follower : user.getFollowerIds()) {
			if(!hasTweeted[follower]) {
				numSeen[follower]++;
				lastSeen[follower] = ts;
			}
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
		Node startNode = net.nodes.get(start);
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
				//reset decay factor if seen recently
				double time_past = step - lastSeen[i];
				
				double prob = baseProb/(time_past*3);

				if(socialPressure >= 2) {
					double pressure = Math.min(numSeen[i]*socialPressure/20+1, socialPressure);
					prob = prob*pressure;
				}
				
				
				if (r < prob) {
					toTweet.add(net.nodes.get(i));
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
		
		int[] cumulativeData = new int[timeSteps.size()];
		int[] iterativeData = new int[timeSteps.size()];
		int[] numSeenData = new int[timeSteps.size()];
		int[] averageFollowerTimestep = new int[timeSteps.size()];
		int[] runKey = new int[net.size];
		int[] numSeen = new int[net.size];
		Arrays.fill(runKey, -1);
		String timeStepsString = "";
		int count = 0;
		for(int i = 0; i < timeSteps.size(); i++) {
			ArrayList<Node> ts = timeSteps.get(i);
			count += ts.size();
			cumulativeData[i] = count;
			iterativeData[i] = ts.size();
			for(Node n : ts) {
				averageFollowerTimestep[i] =+ n.max_followers/ts.size();
				timeStepsString += n.id + " ";
				runKey[n.id] = i;
				if(numSeenData[i] < numSeen[n.id])
					numSeenData[i] = numSeen[n.id];
			}
			for(Node n : ts) {
				for(int follower : n.getFollowerIds()) {
					numSeen[follower]++;
				}
				
			}
			if(i > 5) {
				ArrayList<Node> prevTs = timeSteps.get(i-5);
				for(Node n : prevTs) {
					for(int follower : n.getFollowerIds()) {
						numSeen[follower]--;
					}
					
				}
			}
			timeStepsString += "\n";  
		}
		
		ExponentialMovingAverage ema = new ExponentialMovingAverage(.25);
		double[] smoothIterative = ema.average(Arrays.stream(iterativeData).asDoubleStream().toArray());
		double[] smoothNumSeenData = ema.average(Arrays.stream(numSeenData).asDoubleStream().toArray());
		
		JfreeGraph runDataGraph = new JfreeGraph(runID, cumulativeData);
		runDataGraph.saveGraph(dir+"cumulative.png");
		
		runDataGraph = new JfreeGraph(runID, iterativeData);
		runDataGraph.saveGraph(dir+"iterative.png");
		
		runDataGraph = new JfreeGraph(runID, smoothIterative);
		runDataGraph.saveGraph(dir+"smoothIterative.png");
		
		runDataGraph = new JfreeGraph(runID, averageFollowerTimestep);
		runDataGraph.saveGraph(dir+"followersInTsAverage.png");
		
		runDataGraph = new JfreeGraph(runID, smoothNumSeenData);
		runDataGraph.saveGraph(dir+"maxNumSeen.png");
		
		PrintWriter out = new PrintWriter(dir + "timeSteps.csv");
		out.write(timeStepsString);
		out.close();
		
		out = new PrintWriter(dir + "runKey.csv");
		out.write(Arrays.toString(runKey));
		out.close();
		
		out = new PrintWriter(dir + "smoothIterative.csv");
		out.write(Arrays.toString(smoothIterative));
		out.close();
		
		out = new PrintWriter(dir + "numSeenData.csv");
		out.write(Arrays.toString(smoothNumSeenData));
		out.close();
		
		
		
	}

	public void runMultiple(int iterations, String outputDir) throws Exception {
		for (int i = 0; i < iterations; i++) {
			System.out.println(i);
			ArrayList<ArrayList<Node>> timeSteps = null;
			while(true) {
				//System.out.println("running");
				timeSteps = run(net);
				int retweets = 0;
				for(ArrayList<Node>  ts : timeSteps) {
					for(Node node : ts) {
						retweets++;
					}
				}
				if(retweets > minRetweets) {
					break;
				}
				//System.out.println("bad run");
			}
			saveRun(timeSteps, outputDir);
		}
	}
	
}
