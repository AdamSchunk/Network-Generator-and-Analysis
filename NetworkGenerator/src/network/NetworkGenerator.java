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

	public int[] nodeStatFunc() {
		int[] stats = { 0, 0 }; // [followers, following]
		Random rand = new Random();
		double[] x = { .0000, .00177, .31622, 10., 177.8, 562.3 };
		double[] y = { 100000., 10000., 1000., 100., 10., 1. };

		double r = rand.nextFloat() * 562;
		// System.out.println(r);
		for (int i = 0; i < x.length; i++) {
			double basket = x[i];
			if (r < basket) {
				double x_dis = (r - x[i - 1]) / (x[i] - x[i - 1]);
				int followers = (int) ((x_dis * (y[i - 1] - y[i]) + y[i])) + 1;
				stats[0] = followers;
				break;
			}
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

	public double[] genEdgeWeights(ArrayList<Node> nodes, int index, int clusteringWeight) {
		double[] weights = new double[nodes.size()];
		Arrays.fill(weights, 10);

		Node baseNode = nodes.get(index);

		for (int i = 0; i < nodes.size(); i++) {
			Node currNode = nodes.get(i);

			if (i == index || baseNode.followers.contains(i)) {
				weights[i] = 0;
				continue;
			}

			// num_following/max_num_following
			double deduct = 10 * ((double) currNode.following.size() / (double) currNode.num_following);
			weights[i] = weights[i] - Math.min(deduct, 9); // minimum 10% chance of connecting
			
			if(i/100 - index/100 == 0) {
				weights[i] = weights[i]*clusteringWeight;
			}

			//int intersection = 0;
			//if (currNode.intersection.containsKey(i)){
			//	intersection = currNode.intersection.get(i);
			//}
			//weights[i] = weights[i] * (1 + intersection) * clusteringWeight;
		}

		return weights;
	}

	public boolean genEdges(ArrayList<Node> nodes, double rndmFillPercent, int clusteringWeight) {
		Boolean[] followersAvailable = new Boolean[nodes.size()];
		Arrays.fill(followersAvailable, Boolean.TRUE);

		for(int i = 0; i < nodes.size(); i++) {
			Node currNode = nodes.get(i);
			followersAvailable[i] = currNode.followers.size() < currNode.num_followers;
		}
		
		double percentDone = 0;
		double timeSinceLast = System.currentTimeMillis();
		while (true) {
			
			boolean someNeedsFollowers = false;

			for (int i = 0; i < nodes.size(); i++) {
				if (!followersAvailable[i])
					continue;
				else
					someNeedsFollowers = true;
				
				
				Node currNode = nodes.get(i);
				
				double[] weights = new double[nodes.size()];
				if(percentDone <= rndmFillPercent)
					 weights = genEdgeWeights(nodes, i, clusteringWeight);
				else
					Arrays.fill(weights, 1);
				
				int newFollower = weighted_choice(weights);
				
				currNode.followers.add(newFollower);
				nodes.get(newFollower).following.add(currNode.id);
				followersAvailable[i] = currNode.followers.size() < currNode.num_followers;
				
				//if(percentDone <= rndmFillPercent|| currNode.num_followers > 2000) {
				//	updateIntersections(nodes, newFollower, i);
				//}
				
				numDone += 1;
				if (numDone % (int) (totalFollowers / 100) == 0) {
					percentDone = numDone / totalFollowers;
					double currTime = System.currentTimeMillis();
					System.out.println((int) (numDone / totalFollowers * 100) + "% ... " + (currTime - timeSinceLast));
					timeSinceLast = currTime;
				}

			}
			
			if(!someNeedsFollowers) {
				return true;
			}
		}
	}

	public void updateIntersections(ArrayList<Node> nodes, int fromIdx, int toIdx) {
		Node from = nodes.get(fromIdx);
		Node to = nodes.get(toIdx);
		
		for (Integer x : to.followers) {
			Node updateNode = nodes.get(x);
			if(updateNode.intersection.containsKey(fromIdx))
				updateNode.intersection.replace(fromIdx, updateNode.intersection.get(fromIdx)+1);
			else
				updateNode.intersection.put(fromIdx, 1);
		}
		for (Integer x : to.following) {
			Node updateNode = nodes.get(x);
			if(updateNode.intersection.containsKey(fromIdx))
				updateNode.intersection.replace(fromIdx, updateNode.intersection.get(fromIdx)+1);
			else
				updateNode.intersection.put(fromIdx, 1);
		}
		for (Integer x : from.followers) {
			Node updateNode = nodes.get(x);
			if(updateNode.intersection.containsKey(toIdx))
				updateNode.intersection.replace(toIdx, updateNode.intersection.get(toIdx)+1);
			else
				updateNode.intersection.put(toIdx, 1);
		}
		for (Integer x : from.following) {
			Node updateNode = nodes.get(x);
			if(updateNode.intersection.containsKey(toIdx))
				updateNode.intersection.replace(toIdx, updateNode.intersection.get(toIdx)+1);
			else
				updateNode.intersection.put(toIdx, 1);
		}
	}
	
	public Network generateNetwork(double rndmFillPercent, int clusteringWeight, int size, String dir) throws IOException {
		Network net = new Network();
		
		net.nodes = genNodes(size);
		for (Node n : net.nodes) {
			totalFollowers += n.num_followers;
		}
		net.size = net.nodes.size();
		genEdges(net.nodes, rndmFillPercent, clusteringWeight);
		net.saveNetwork(dir);
		return net;
	}

}
