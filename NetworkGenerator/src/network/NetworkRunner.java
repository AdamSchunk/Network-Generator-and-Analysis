package network;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
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
	private Predictor predictor;
	
	private NetworkRunner(double socialPressure, int minRetweets) {
		this.socialPressure = socialPressure;
		this.minRetweets = minRetweets;
	}
	
	public NetworkRunner(double socialPressure, int minRetweets, Network net) {
		this(socialPressure, minRetweets);
		this.net = net;
		this.predictor = new Predictor(net);
	}
	
	public NetworkRunner(double socialPressure, int minRetweets, String networkDirectory) throws IOException {
		this(socialPressure, minRetweets);
		this.net = new Network(networkDirectory);
		this.predictor = new Predictor(net);
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
		//double baseProb = .00005;
		double baseProb = .002;
		int totalTweets = 0;
		while(count <= 5) {
			ArrayList<Node> toTweet = new ArrayList<Node>();
			for(int i = 0; i < net.size; i++) {
				if(hasTweeted[i] || numSeen[i] == 0) 
					continue;
				
				double r = rand.nextDouble();
				//reset decay factor if seen recently
				double timePast = step - lastSeen[i];
				
				double prob = 0;
				
				if (socialPressure != 0) {
					prob = baseProb/(timePast*3)*(numSeen[i]/500+1);
//					double socialPressureFactor = (socialPressure*(numSeen[i]-1)/10)+1;
//							//Math.pow(socialPressure,numSeen[i]-1);
//					prob = baseProb*socialPressureFactor;
//					prob = prob/((step+50)/50);
				} else {
					//small decay factor over time
					prob = baseProb/((step+50)/50);
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

	public void saveRun(ArrayList<ArrayList<Node>> timeSteps, String outputDir, String runId) throws Exception {
		String dir = outputDir + runId + "/";
		
		ArrayList<Double> nodeClustering = new ArrayList<Double>();
		try (BufferedReader br = new BufferedReader(new FileReader(outputDir + "/../nodeClustering.csv"))) {
		    String line;
		    while ((line = br.readLine()) != null) {
		       nodeClustering.add(Double.parseDouble(line));
		    }
		}
		
		if (!new File(dir).exists()) {
			new File(dir).mkdirs();
		}
		
		int[] cumulativeData = new int[timeSteps.size()];
		int[] iterativeData = new int[timeSteps.size()];
		int[] maxNumSeenPerTs = new int[timeSteps.size()];
		int[] averageFollowerTimestep = new int[timeSteps.size()];
		int[] runKey = new int[net.size];
		int[] numSeen = new int[net.size];
		double[] maxClusteringPerTs = new double[timeSteps.size()];
		double[] outDegWindow = new double[timeSteps.size()];
		double[] outDegTs = new double[timeSteps.size()];
		double[] outDegNodeRatio = new double[timeSteps.size()];
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
				outDegTs[i] += n.max_followers;
				double clustering = nodeClustering.get(n.id).doubleValue();
				if(clustering > maxClusteringPerTs[i])
					maxClusteringPerTs[i] = clustering;
				if(numSeen[n.id] > maxNumSeenPerTs[i])
					maxNumSeenPerTs[i] = numSeen[n.id];
			}
			for(Node n : ts) {
				for(int follower : n.getFollowerIds()) {
					numSeen[follower]++;
				}
			}
			if(i > 5) {
				ArrayList<Node> prevTs = timeSteps.get(i-5);
				double nodesInWindow = 0;
				for(int j = 0; j < 4; j++) {
					outDegWindow[i] += outDegTs[i-j-1];
					nodesInWindow += timeSteps.get(i-j-1).size();
				}
				outDegNodeRatio[i] = outDegWindow[i]/nodesInWindow;
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
		double[] smoothNumSeenData = ema.average(Arrays.stream(maxNumSeenPerTs).asDoubleStream().toArray());
		double[] smoothClustering = ema.average(Arrays.stream(maxClusteringPerTs).toArray());
		double[] smoothOutDegree = ema.average(Arrays.stream(outDegWindow).toArray());
		double[] smoothOutDegNodeRatio = ema.average(Arrays.stream(outDegNodeRatio).toArray());
		
		JfreeGraph runDataGraph = new JfreeGraph(runId, cumulativeData);
		runDataGraph.saveGraph(dir+"cumulative.png");
		
		runDataGraph = new JfreeGraph(runId, iterativeData);
		runDataGraph.saveGraph(dir+"iterative.png");
		
		runDataGraph = new JfreeGraph(runId, outDegNodeRatio);
		runDataGraph.saveGraph(dir+"Out Degree to Nodes Ratio in Window.png");
		
		runDataGraph = new JfreeGraph(runId, smoothIterative);
		runDataGraph.saveGraph(dir+"smoothIterative.png");
		
		runDataGraph = new JfreeGraph(runId, averageFollowerTimestep);
		runDataGraph.saveGraph(dir+"followersInTsAverage.png");
		
		runDataGraph = new JfreeGraph(runId, smoothNumSeenData);
		runDataGraph.saveGraph(dir+"maxNumSeen.png");
		
		runDataGraph = new JfreeGraph(runId, smoothOutDegree);
		runDataGraph.saveGraph(dir+"out degree window.png");
		
		runDataGraph = new JfreeGraph(runId, smoothClustering);
		runDataGraph.saveGraph(dir+"Max Clustering Per Ts.png");
		
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
		
		out = new PrintWriter(dir + "OutDegreeToNodesInWindowRatio.csv");
		out.write(Arrays.toString(outDegNodeRatio));
		out.close();
		
	}
	
	public void savePediction(ArrayList<ArrayList<Node>> timeSteps, String outputDir, String runId) throws IOException {
		String dir = outputDir + runId + "/";
		ArrayList<Double> prediction = predictor.makePredictionOnRun(timeSteps);
		double[] predictionArray = new double[prediction.size()];
		for(int i = 0; i < prediction.size(); i++) {
			predictionArray[i] = prediction.get(i);
		}
		
		ExponentialMovingAverage ema = new ExponentialMovingAverage(.25);
		double[] smoothPrediction = ema.average(predictionArray);
		
		double[] timeSeenInRun = new double[prediction.size()];
		
		for(int i = 0; i < timeSteps.size(); i++) {
			ArrayList<Node> ts = timeSteps.get(i);
			for(int j = 0; j < ts.size(); j++) {
				timeSeenInRun[i+j] = i+(j/ts.size());
			}
		}
		
		JfreeGraph runDataGraph = new JfreeGraph(runId, timeSeenInRun, smoothPrediction);
		runDataGraph.saveGraph(dir+"prediction.png");
		
	}

	public void runMultiple(int iterations, String outputDir) throws Exception {
		for (int i = 0; i < iterations; i++) {
			System.out.println("Beginning run: " +  i);
			ArrayList<ArrayList<Node>> timeSteps = null;
			while(true) {
				//System.out.println("running");
				timeSteps = run(net);
				int retweets = 0;
				for(ArrayList<Node>  ts : timeSteps) {
					retweets+= ts.size();
					for(Node node : ts) {
						//retweets++;
					}
				}
				if(retweets > minRetweets) {
					break;
				}
				//System.out.println("bad run");
			}
			System.out.println("Saving Run: " + i);
			String runId = Integer.toString(i);
			saveRun(timeSteps, outputDir, runId);
			savePediction(timeSteps, outputDir, runId);
		}
	}
	
}
