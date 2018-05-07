package network;

import java.util.*;

public class Node {
	int num_followers = 0;
	int num_following = 0;
	int id = 0;
	static int numNodes = 0;
	ArrayList<Integer> followers = new ArrayList<>();
	ArrayList<Integer> following = new ArrayList<>();
	Map<Integer, Integer> intersection;
	
	
	public Node(int followers, int following, int maxNodes) {
		this.num_following = following;
		this.num_followers = followers;
		this.id = numNodes;
		numNodes++;
		this.intersection = new HashMap<Integer, Integer>();
	}
}
