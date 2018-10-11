package network;

import java.io.IOException;
import java.util.ArrayList;

public class Predictor {
	private Network net;
	
	public Predictor(Network net) {
		this.net = net;
	}
	
	public Predictor(String networkDirectory) throws IOException {
		this.net = new Network(networkDirectory);
	}
	
	public double makePredictionOnSubgraph(ArrayList<Node> subgraph) {
		
		
		return 0.2;
	}
	
	
}
