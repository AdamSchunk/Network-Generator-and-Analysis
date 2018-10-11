package network;

import java.util.*;
import java.util.stream.Collectors;

public class Node {
	int max_followers = 0;
	int max_following = 0;
	double clustering = 0;
	int id = 0;
	int refId = -1;
	static int numNodes = 0;
	private ArrayList<Integer> followers = new ArrayList<>();
	private ArrayList<Integer> following = new ArrayList<>();
	Map<Integer, Integer> intersection;
	

	
	public Node(int followers, int following) {
		this(followers, following, numNodes);
		this.id = numNodes;
		numNodes++;
	}

	
	public Node(int followers, int following, int id) {
		this.max_following = following;
		this.max_followers = followers;
		this.id = id;
		this.intersection = new HashMap<Integer, Integer>();
	}
	
	public Node(int followers, int following, int id, double clustering) {
		this(followers, following, id);
		this.clustering = clustering;
	}
	
	//ONLY used for generating subgraphs
	public Node(int followers, int following, int id, int refId) {
		this.max_following = following;
		this.max_followers = followers;
		this.id = id;
		this.refId = refId;
		this.intersection = new HashMap<Integer, Integer>();
	}
	
	public ArrayList<Integer> getFollowerIds() {
		return followers;
	}
	
	public ArrayList<Integer> getFollowingIds() {
		return following;
	}
	
	public boolean follow(Node nodeToFollow) {
		int id = nodeToFollow.id;
		if(following.contains(id)) {
			return false;
		}
		following.add(id);
		nodeToFollow.getFollowedBy(this);
		return true;
	}
	
	public boolean follow(List<Node> nodes) {
		for(Node n : nodes)
			follow(n);
		return true;
	}
	
	private boolean getFollowedBy(Node followedBy) {
		int id = followedBy.id;
		if(followers.contains(id)) {
			return false;
		}
		followers.add(id);
		return true;
	}
	
	public int getCurrentNumFollowers() {
		return this.followers.size();
	}
	
	public int getCurrentNumFollowing() {
		return this.following.size();
	}
}
