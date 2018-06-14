package network;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
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
				getNodeById(toIdx).follow(getNodeById(fromIdx));
				
			}
        }
        this.size = nodes.size();
        System.out.println("Network Loaded");
	}
	
	public Network getSubgraphD(List<Double> ids) {
		List<Integer> idList = new ArrayList<Integer>();
		for(Double id : ids)
			idList.add(id.intValue());
		
		return getSubgraphI(idList);
	}
	
	public Network getSubgraphI(List<Integer> ids) {
		ListUtils listUtils = new ListUtils();
		Network subgraph = new Network();
		for(int id : ids) {
			Node baseNode = nodes.get(id);
			Node subGraphNode = new Node(baseNode.max_followers, baseNode.max_following, id);
			subgraph.nodes.add(subGraphNode);
		}
		
		for(Node subGraphNode : subgraph.nodes) {
			Node baseNode = nodes.get(subGraphNode.id);
			List<Integer> idsToFollow = listUtils.intersection(ids, baseNode.getFollowingIds());
			ArrayList<Node> nodesToFollow =  new ArrayList<Node>() {{ for (int i : idsToFollow) add(subgraph.getNodeById(i)); }};
			subGraphNode.follow(nodesToFollow);
		}
		
		return null;
	}
	
	public void saveNetwork(String dir) throws IOException {
		System.out.println("saving data");
		FileWriter nodeWriter = new FileWriter(dir + "nodes.csv");
		for (Node n : nodes) {
			String output = n.id + "," + n.max_followers + "," + n.max_following + "\n";
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
	}
	
	public Node getNodeById(int id) {
		return nodes.get(id);
	}

}
