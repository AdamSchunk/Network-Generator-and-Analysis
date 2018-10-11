package network;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import listUtils.ListUtils;

public class Network {
	
	ArrayList<Node> nodes;
	int size = 0;
	
	public Network() {
		this.nodes = new ArrayList<Node>();
	}
	
	public Network(String directory) throws IOException { //generates a network from a folder containing a nodes.csv and edges.csv
		this(directory+"nodes.csv", directory+"edges.csv");
	}
	
	public Network(String nodeCSVfileName, String edgeCSVfileName) throws IOException {
		System.out.println("loading network...");
        this.nodes = new ArrayList<Node>();
        try (Scanner scanner = new Scanner(new File(nodeCSVfileName))) {
			while (scanner.hasNext()){
				String line = scanner.next();
				String[] data = line.split(",");
				Node n = new Node(Integer.parseInt(data[1]), 
						Integer.parseInt(data[2]), Integer.parseInt(data[0]));
				this.nodes.add(n);
			}
        }
        
        try (Scanner scanner = new Scanner(new File(edgeCSVfileName))) {
        	scanner.next();
			while (scanner.hasNext()){
				String line = scanner.next();
				String[] data = line.split(",");
				int fromIdx = Integer.parseInt(data[0]);
				int toIdx = Integer.parseInt(data[1]);
				nodes.get(toIdx).follow(nodes.get(fromIdx));
				
			}
        }
        this.size = nodes.size();
        System.out.println("Network Loaded");
	}
	
	public boolean addNode(Node n) {
		this.nodes.add(n);
		this.size++;
		return true;
	}
	
	public Network getSubgraphD(List<Double> ids) {
		List<Integer> idList = new ArrayList<Integer>();
		for(Double id : ids)
			idList.add(id.intValue());
		
		return getSubgraphI(idList);
	}
	
	//output has ids from 0 to n
	public Network getSubgraphI(List<Integer> ids) {
		ListUtils listUtils = new ListUtils();
		Network subgraph = new Network();
		
		HashMap<Integer, Integer> hmap = new HashMap<Integer, Integer>();
		int count = 0;
		for(int id : ids) { //adds all the true Ids of the subgraph nodes
			Node baseNode = nodes.get(id);
			hmap.put(id, count);
			Node subGraphNode = new Node(baseNode.max_followers, baseNode.max_following, hmap.get(id), id);
			subgraph.addNode(subGraphNode);
			count ++;
		}
		
		for(Node subGraphNode : subgraph.nodes) {
			Node baseNode = nodes.get(subGraphNode.refId);
			List<Integer> idsToFollow = listUtils.intersection(ids, baseNode.getFollowingIds());
			ArrayList<Node> nodesToFollow =  new ArrayList<Node>() {{ for (int i : idsToFollow) add(subgraph.getNodeById(hmap.get(i))); }};
			subGraphNode.follow(nodesToFollow);
		}
		
		return subgraph;
	}
	
	public double getClustering(Node node) {
		double linksInSubgraph = 0;
		ArrayList<Integer> subgraph = new ArrayList<Integer>();
		subgraph.add(node.id);
		for(Integer followerId : node.getFollowerIds()) {
			subgraph.add(followerId);
		}
		
		for(Integer followingId : node.getFollowingIds()) {
			if (!subgraph.contains(followingId))
				subgraph.add(followingId);
		}
		
		if(subgraph.contains(node.id)) {
			subgraph.remove(subgraph.indexOf(node.id));
		}
		
		double denom = subgraph.size() * (subgraph.size() -1);
		
		for(Integer nodeIdInSubgraph : subgraph) {
			Node nodeInSubgraph = this.nodes.get(nodeIdInSubgraph);
			List<Integer> intersectFollowers = subgraph.stream().filter(
					nodeInSubgraph.getFollowerIds()::contains).collect(Collectors.toList());
			linksInSubgraph += intersectFollowers.size();
			if(intersectFollowers.size() > nodeInSubgraph.getCurrentNumFollowers()) {
				System.out.println(nodeIdInSubgraph);
				System.out.println(nodeInSubgraph.getFollowerIds().toString());
				System.out.println(intersectFollowers.toString());
			}
		}
		
		if(linksInSubgraph > denom) {
			System.out.println("not like this");
		}
		
		return linksInSubgraph/denom;
	}
	
	public void saveNetwork(String dir) throws IOException {
		System.out.println("saving data");
		FileWriter nodeWriter = new FileWriter(dir + "nodes.csv");
		for (Node n : nodes) {
			String output = n.id + "," + n.max_followers + "," + n.max_following + "," + n.clustering + "\n";
			nodeWriter.write(output);
		}
		nodeWriter.close();

		// save edge data
		FileWriter edgeWriter = new FileWriter(dir +"edges.csv");
		String res = "from,to,weight\n";
		edgeWriter.write(res);
		for (Node node : nodes) {
			for (Integer follower : node.getFollowerIds()) {
				res = node.id + "," + follower + ",1\n";
				edgeWriter.write(res);
			}
		}
		edgeWriter.close();
		
		FileWriter clusteringWriter = new FileWriter(dir + "nodeClustering.csv");
		for (Node n : nodes) {
			String output = this.getClustering(n) + "\n";
			clusteringWriter.write(output);
		}
		clusteringWriter.close();
	}
	
	public Node getNodeById(int id) {
		for(Node n : this.nodes) {
			if(n.id == id)
				return n;

		}
		return null;
	}

}
