package network;

import java.util.Random;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class NetworkGeneratorV7 {
	static double totalFollowers = 0;
	static double numDone = 0;

	class RunnableDemo implements Runnable {
		private Thread t;
		private ArrayList<Node> nodes;
		private NetworkGeneratorV7 ng;
		private ArrayList<Node> subset;
		private boolean sectionFinished;

		RunnableDemo(NetworkGeneratorV7 ng, ArrayList<Node> nodes) {
			this.nodes = nodes;
			this.ng = ng;
			this.t = new Thread(this);
			this.subset = new ArrayList<Node>();
			this.sectionFinished = false;
		}

		public void run() {
			this.sectionFinished = ng.genEdges(subset, true);
		}

		public void setSubset(List<Integer> indicies) {
			subset.clear();
			for (int idx : indicies) {
				subset.add(nodes.get(idx));
			}
		}

		public void start() {
			t.start();
		}
	}

	public int weighted_choice(double[] weights) {
		double total = 0;
		for (double w : weights) {
			total += w;
		}

		Random rand = new Random();
		int choice = (int) (rand.nextDouble() * (total));

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
		System.out.println("Should not see this");
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
		ArrayList<Node> nodes = new ArrayList<>();
		for (int i = 0; i < size; i++) {
			int[] stats = nodeStatFunc();
			Node n = new Node(stats[0], stats[1], size);
			nodes.add(n);
		}
		return nodes;
	}

	public double[] genEdgeWeights(ArrayList<Node> nodes, int index) {
		double[] weights = new double[nodes.size()];
		Arrays.fill(weights, 100);

		Node baseNode = nodes.get(index);

		for (int i = 0; i < nodes.size(); i++) {
			Node currNode = nodes.get(i);

			if (i == index || baseNode.followers.contains(i)) {
				weights[i] = 0;
				continue;
			}

			// num_following/max_num_following
			double deduct = 100 * ((double) currNode.following.size() / (double) currNode.num_following);
			weights[i] = weights[i] - Math.min(deduct, 90); // always 10% chance of connecting

			int intersection = 0;
			if (currNode.intersection.containsKey(i)){
				intersection = currNode.intersection.get(i);
			}
			weights[i] = weights[i] * (1 + intersection);
		}

		return weights;
	}

	public boolean genEdges(ArrayList<Node> nodes, boolean singleIter) {
		Boolean[] followersAvailable = new Boolean[nodes.size()];
		Arrays.fill(followersAvailable, Boolean.TRUE);

		for(int i = 0; i < nodes.size(); i++) {
			Node currNode = nodes.get(i);
			followersAvailable[i] = currNode.followers.size() < currNode.num_followers;
		}
		
		int iteration = 0;
		while (true) {
			int rndmFillPoint = 1000;
			boolean someNeedsFollowers = false;

			for (int i = 0; i < nodes.size(); i++) {
				if (!followersAvailable[i])
					continue;
				else
					someNeedsFollowers = true;
				
				
				Node currNode = nodes.get(i);
				
				double[] weights = new double[nodes.size()];
				if(iteration < rndmFillPoint)
					 weights = genEdgeWeights(nodes, i);
				else
					Arrays.fill(weights, 1);
				
				int newFollower = weighted_choice(weights);
				
				currNode.followers.add(newFollower);
				nodes.get(newFollower).following.add(currNode.id);
				followersAvailable[i] = currNode.followers.size() < currNode.num_followers;
				
				if(iteration < rndmFillPoint) {
					updateIntersections(nodes, newFollower, i);
				}
				
				numDone += 1;
				if (numDone % (int) (totalFollowers / 100) == 0) {
					System.out.println((int) (numDone / totalFollowers * 100) + "%");
					System.out.println(iteration);
				}

			}
			
			if(!someNeedsFollowers) {
				return true;
			}
			
			if (singleIter) {
				return false;
			}
			iteration++;
		}
	}

	public void updateIntersections(ArrayList<Node> nodes, int fromIdx, int toIdx) {
		Node from = nodes.get(fromIdx);
		Node to = nodes.get(toIdx);
		
		for (Integer x : to.followers) {
			if (from.followers.contains(x) || from.following.contains(x)) {
				Node updateNode = nodes.get(x);
				if(updateNode.intersection.containsKey(fromIdx))
					updateNode.intersection.replace(fromIdx, updateNode.intersection.get(fromIdx)+1);
				else
					updateNode.intersection.put(fromIdx, 1);
				if(updateNode.intersection.containsKey(toIdx))
					updateNode.intersection.replace(toIdx, updateNode.intersection.get(toIdx)+1);
				else
					updateNode.intersection.put(toIdx, 1);
			}
		}
		for (Integer x : to.following) {
			if (from.followers.contains(x) || from.following.contains(x)) {
				Node updateNode = nodes.get(x);
				if(updateNode.intersection.containsKey(fromIdx))
					updateNode.intersection.replace(fromIdx, updateNode.intersection.get(fromIdx)+1);
				else
					updateNode.intersection.put(fromIdx, 1);
				if(updateNode.intersection.containsKey(toIdx))
					updateNode.intersection.replace(toIdx, updateNode.intersection.get(toIdx)+1);
				else
					updateNode.intersection.put(toIdx, 1);
			}
		}
	}
	
	public void generateNetwork(int size) throws IOException {
		ArrayList<Node> nodes = genNodes(size);
		for (Node n : nodes) {
			totalFollowers += n.num_followers;
		}
		genEdges(nodes, false);

		saveNetwork(nodes);
	}

	public void generateNetworkMultithread(int size) throws Exception {
		ArrayList<Node> nodes = genNodes(size);
		for (Node n : nodes) {
			totalFollowers += n.num_followers;
		}

		ArrayList<Integer> indicies = new ArrayList<Integer>();

		for (int i = 0; i < size; i++) {
			indicies.add(i);
		}
		
		while(true) {
			Collections.shuffle(indicies);
			RunnableDemo r1 = new RunnableDemo(this, nodes);
			RunnableDemo r2 = new RunnableDemo(this, nodes);
			RunnableDemo r3 = new RunnableDemo(this, nodes);
			RunnableDemo r4 = new RunnableDemo(this, nodes);
			RunnableDemo r5 = new RunnableDemo(this, nodes);
			
			r1.setSubset(indicies.subList(0, indicies.size()/5));
			r2.setSubset(indicies.subList(indicies.size()/5, 2*indicies.size()/5));
			r3.setSubset(indicies.subList(2*indicies.size()/5, 3*indicies.size()/5));
			r4.setSubset(indicies.subList(3*indicies.size()/5, 4*indicies.size()/5));
			r5.setSubset(indicies.subList(4*indicies.size()/5, indicies.size()));
			
			
			r1.start();
			r2.start();
			r3.start();
			r4.start();
			r5.start();
			
			r1.t.join();
			r2.t.join();
			r3.t.join();
			r4.t.join();
			r5.t.join();
			if(r1.sectionFinished && r2.sectionFinished && r3.sectionFinished && r4.sectionFinished && r5.sectionFinished) {
				break;
			}
		}
		
		saveNetwork(nodes);
	}

	public void saveNetwork(ArrayList<Node> nodes) throws IOException {
		System.out.println("saving data");
		// save node data
		FileWriter nodeWriter = new FileWriter("nodes.csv");
		for (Node n : nodes) {
			String output = n.id + ", " + n.num_followers + ", " + n.num_following + "\n";
			nodeWriter.write(output);
		}
		nodeWriter.close();

		// save edge data
		FileWriter edgeWriter = new FileWriter("edges.csv");
		String res = "from,to,weight\n";
		edgeWriter.write(res);
		for (Node node : nodes) {
			for (Integer follower : node.followers) {
				res = node.id + ", " + follower + ", 1\n";
				edgeWriter.write(res);
			}
		}
		edgeWriter.close();
	}

	public static void main(String[] args) throws Exception {
		NetworkGeneratorV7 ng = new NetworkGeneratorV7();
		ng.generateNetwork(10000);
	}
}
