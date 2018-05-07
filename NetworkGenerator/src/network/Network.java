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
	
	public Network() {
		this.nodes = new ArrayList<Node>();
	}
	
	public Network(String nodeCSVfileName, String edgeCSVfileName) throws IOException {
        this.nodes = new ArrayList<Node>();
        try (Scanner scanner = new Scanner(new File(nodeCSVfileName))) {
			while (scanner.hasNext()){
				String line = scanner.next();
				String[] data = line.split(",");
				System.out.println(data[0] + "," + data[1]);
				Node n = new Node(Integer.parseInt(data[0]), 
						Integer.parseInt(data[1]), Integer.parseInt(data[2]));
				this.nodes.add(n);
			}
        }
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
		for (Node n : nodes) {
			if (n.id == id) {
				return n;
			}
		}
		return null;
	}
	
	public static void main(String[] args) throws IOException{
		Network net = new Network("nodes.csv", "edges.csv");
	}

}
