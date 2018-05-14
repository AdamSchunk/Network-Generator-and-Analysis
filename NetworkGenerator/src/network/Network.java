package network;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.stream.Stream;

public class Network {
	
	ArrayList<Node> nodes;
	int size = 0;
	
	public Network() {
		this.nodes = new ArrayList<Node>();
	}
	
	public Network(String nodeCSVfileName, String edgeCSVfileName) throws IOException {
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
				getNodeById(fromIdx).followers.add(toIdx);
			}
        }
        this.size = nodes.size();
	}
	
	public void saveNetwork() throws IOException {
		System.out.println("saving data");
		FileWriter nodeWriter = new FileWriter("nodes.csv");
		for (Node n : nodes) {
			String output = n.id + "," + n.num_followers + "," + n.num_following + "\n";
			nodeWriter.write(output);
		}
		nodeWriter.close();

		// save edge data
		FileWriter edgeWriter = new FileWriter("edges.csv");
		String res = "from,to,weight\n";
		edgeWriter.write(res);
		for (Node node : nodes) {
			for (Integer follower : node.followers) {
				res = node.id + "," + follower + ",1\n";
				edgeWriter.write(res);
			}
		}
		edgeWriter.close();
	}
	
	
	public Node getNodeById(int id) {
		return nodes.get(id);
	}
	
	public static void main(String[] args) throws IOException{
		Network net = new Network("nodes.csv", "edges.csv");
	}

}
