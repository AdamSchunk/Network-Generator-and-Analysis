package network;

import java.io.IOException;
import java.util.Random;

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
	
	public void run(Network net, int iterations) {
		Random rand = new Random();
		boolean[] tweeted = new boolean[net.size];
		int[] seen = new int[net.size];
		rand.nextInt(net.size);
		
	}
	
	public static void main(String[] args) throws IOException {
		NetworkRunner runner = new NetworkRunner();
		Network net = new Network("nodes.csv", "edges.csv");
		runner.run(net, 5);
	}
	
}
