package network;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

import javax.naming.InitialContext;

public class NetworkRunner {

	/*
	 * 
	 * def run_from_save(output_name, num_runs):
	#add in to do multiple runs
	nodes = load_network(output_name)
	for i in range(0,num_runs):
		print(i)
		ts = run_network(nodes)
		while np.count_nonzero(ts[-1] == True) <= 300: # if less than x people tweeted, redo the analysis
			ts = run_network(nodes)
		start_idx = ts[0].tolist().index(True)
		run_analysis(ts, nodes, output_name)
	 * 
	 */
	
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
	
	public ArrayList<ArrayList<Node>> run(Network net, int iterations) {
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
		double baseProb = 1;
		while(count <= 5) {
			ArrayList<Node> toTweet = new ArrayList<Node>();
			for(int i = 0; i < net.size; i++) {
				if(hasTweeted[i] || numSeen[i] == 0) 
					continue;
				
				double r = rand.nextDouble();
				double time_past = step - lastSeen[i];
				
				double prob = baseProb/(time_past*2);
				
				
				if (r < prob) {
					System.out.println("tweet");
					toTweet.add(net.getNodeById(i));
				}
				
			}
			tweet(toTweet, hasTweeted, numSeen, lastSeen, step);
			timeSteps.add(toTweet);
			count++;
		}
		
		return timeSteps;
	}
	
	public static void main(String[] args) throws IOException {
		NetworkRunner runner = new NetworkRunner();
		Network net = new Network("nodes.csv", "edges.csv");
		ArrayList<ArrayList<Node>> timeSteps = runner.run(net, 5);
		
		System.out.println(timeSteps.size());
		for(ArrayList<Node> nodes : timeSteps){
			System.out.println(nodes.size());
		}
	}
	
}
