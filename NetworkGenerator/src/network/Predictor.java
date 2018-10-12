package network;

import java.io.IOException;
import java.util.ArrayList;

public class Predictor {
	private Network net;
	private ArrayList<Double> heatMap;
	
	private Predictor() {
	}
	
	public Predictor(Network net) {
		this.net = net;
		this.generateNetHeatMap();
	}
	
	public Predictor(String networkDirectory) throws IOException {
		this.net = new Network(networkDirectory);
		this.generateNetHeatMap();
	}
	
	
	public ArrayList<Double> generateNetHeatMap(){
		heatMap = new ArrayList<Double>();
		for(Node n : net.nodes) {
			double nodeImportance = 0;
			double clustering = n.clustering;
			double clusteringSum = 0;
			double followers = n.getCurrentNumFollowers();
			double followees = n.getCurrentNumFollowing();
			for(int follower : n.getFollowerIds()) {
				clusteringSum += Math.pow(net.nodes.get(follower).clustering,2);
			}
			for(int followee : n.getFollowingIds()) {
				clusteringSum += Math.pow(net.nodes.get(followee).clustering,2);
			}
			
			heatMap.add(clusteringSum/(followers + followees));
		}
		return heatMap;
	}
	
	
	public ArrayList<Double> makePredictionOnRun(ArrayList<ArrayList<Node>> timeSteps) {
		ArrayList<Double> prediction = new ArrayList<Double>();
		ArrayList<Node> retweeters = new ArrayList<Node>();
		double windowHeat = 0;
		for(ArrayList<Node> ts : timeSteps) {
			for(Node n : ts) {
				retweeters.add(n);
				windowHeat += heatMap.get(n.id);
				if(retweeters.size() > 50) {
					windowHeat -= heatMap.get(retweeters.get(retweeters.size()-50).id);
				}
				prediction.add(windowHeat);
			}
		}
		return prediction;
	}
}
