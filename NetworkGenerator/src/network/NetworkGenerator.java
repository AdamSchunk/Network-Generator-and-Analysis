package network;

import java.util.Random;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class NetworkGenerator {
	double totalFollowers = 0;
	double numDone = 0;
	double clusteringFollowerDegeneration = 10;
	
	public int weighted_choice(double[] weights) {
		double total = 0;
		for (double w : weights) {
			total += w;
		}

		Random rand = new Random();
		double choice = rand.nextDouble() * (total);

		double upto = 0;
		for (int i = 0; i < weights.length; i++) {
			double weight = weights[i];
			if (upto + weight >= choice) {
				return i;
			}
			upto += weight;
		}
		System.out.println(upto);
		System.out.println(choice);
		System.out.println(Arrays.toString(weights));
		System.out.println("Should not see this");
		System.exit(0);
		return 0;
	}

	private double exponential(double x, double a, double b, double c) {
		return a*Math.exp(-b*x)+c;
	}
		
	public int[] nodeStatFunc() {
		int[] stats = { 0, 0 }; // [followers, following]
		Random rand = new Random();
		double[] x = { .0000, .00177, .31622, 10., 177.8, 562.3 };
		double[] y = { 100000., 10000., 1000., 100., 10., 1. };

		double r = rand.nextFloat()*100;
		
		double[] exp = {0.60428998, -0.07058309,  5.74510455};
		
		if(r <= 95) {
			stats[0] = (int)exponential(r, exp[0], exp[1], exp[2]);
		} else {
			stats[0] = 500 + (int)exponential(rand.nextDouble()*100, exp[0], exp[1], exp[2])*10;
		}

		if (stats[0] <= 1000) {
			stats[1] = (int) (stats[0] * (1.2 - (stats[0] / 5000)) + 3);
		}

		else {
			stats[1] = (int) (stats[0] - (stats[0] / 2) * ((stats[0] - 1000) / 100000));
		}
		return stats;
	}

	public ArrayList<Node> genNodes(int size) {
		System.out.println("generating Nodes");
		ArrayList<Node> nodes = new ArrayList<>();
		for (int i = 0; i < size; i++) {
			int[] stats = nodeStatFunc();
			Node n = new Node(stats[0], stats[1]);
			nodes.add(n);
		}
		System.out.println("Done");
		return nodes;
	}
	
	public double[] genEdgeWeights(Network net, int index, int clusteringWeight) {
		Random rand = new Random();
		Node baseNode = net.nodes.get(index);
		double expectedFollowing = baseNode.max_following;
		double expectedClustering = (1000-expectedFollowing)/1000*.25+.05;
		expectedClustering = Math.max(expectedClustering, .1);
		double r = rand.nextDouble();
		boolean inCluster = (r<expectedClustering&&baseNode.getFollowerIds().size()!=0);
		
		double[] weights = new double[net.size];
		if(inCluster) {
			int randBin = rand.nextInt(2);
			Node randomFriend = new Node(0, 0);
			if(randBin == 0) {
				randomFriend = net.nodes.get(
					baseNode.getFollowerIds().get(rand.nextInt(baseNode.getFollowerIds().size())));
			}
			if(randBin == 1) {
				randomFriend = net.nodes.get(
					baseNode.getFollowingIds().get(rand.nextInt(baseNode.getFollowingIds().size())));
			}
			Arrays.fill(weights, 0);
			for(int f : randomFriend.getFollowerIds()) {
				weights[f] = 10;
			}
			for(int f : randomFriend.getFollowingIds()) {
				weights[f] = 10;
			}
		} else {
			Arrays.fill(weights, 10);
		}
		weights[index] = 0;
		//baseNode.getClustering(net);
		for (int i = 0; i < net.size; i++) {
			if(weights[i] == 0)
				continue;
			
			Node currNode = net.nodes.get(i);
			
			// num_following/max_num_following
			double deduct = 10 * ((double) currNode.getCurrentNumFollowing() / (double) currNode.max_following);
			weights[i] = weights[i] - Math.min(deduct, 9); // minimum 10% chance of connecting
		}
		return weights;
	}

	public boolean genEdges(Network net, double rndmFillPercent, int clusteringWeight) {
		Boolean[] followersAvailable = new Boolean[net.size];
		Arrays.fill(followersAvailable, Boolean.TRUE);

		for(int i = 0; i < net.size; i++) {
			Node currNode = net.nodes.get(i);
			followersAvailable[i] = currNode.getCurrentNumFollowers() < currNode.max_followers;
		}
		
		double percentDone = 0;
		double timeSinceLast = System.currentTimeMillis();
		int level = 0;
		while (true) {
			
			boolean someNeedsFollowers = false;

			for (int i = 0; i < net.size; i++) {
				if (!followersAvailable[i])
					continue;
				else
					someNeedsFollowers = true;
				
				
				Node currNode = net.nodes.get(i);
				
				int newFollower = -1;
				double[] weights = new double[net.size];
				while(true) {				
					if(percentDone <= rndmFillPercent)
						 weights = genEdgeWeights(net, i, clusteringWeight);
					else
						Arrays.fill(weights, 1);
					
					newFollower = weighted_choice(weights);
					if(net.nodes.get(newFollower).follow(currNode)) {
						followersAvailable[i] = currNode.getCurrentNumFollowers() < currNode.max_followers;
						break;
					}
					//System.out.println("redo");
				}
				
				//if(percentDone <= rndmFillPercent|| currNode.num_followers > 2000) {
				//	updateIntersections(nodes, newFollower, i);
				//}
				
				numDone += 1;
				if (numDone % (int) (totalFollowers / 100) == 0) {
					percentDone = numDone / totalFollowers;
					double currTime = System.currentTimeMillis();
					System.out.println((int) (numDone / totalFollowers * 100) + "% ... " + (currTime - timeSinceLast)
							+ " ... " + level);
					timeSinceLast = currTime;
				}

			}
			level++;
			if(!someNeedsFollowers) {
				return true;
			}
		}
	}
	
	public boolean findClustering(Network net) {
		for (Node n : net.nodes) {
			n.clustering = net.getClustering(n);
		}
		return true;
	}
	
	public Network generateNetwork(double rndmFillPercent, int clusteringWeight, int size, String dir) throws IOException {
		Network net = new Network();
		
		net.nodes = genNodes(size);
		for (Node n : net.nodes) {
			totalFollowers += n.max_followers;
		}
		net.size = net.nodes.size();
		genEdges(net, rndmFillPercent, clusteringWeight);
		findClustering(net);
		net.saveNetwork(dir);
		return net;
	}

}
