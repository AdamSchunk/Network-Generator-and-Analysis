package network;

import java.util.*;
import java.util.stream.Collectors;

public class Node {
	int max_followers = 0;
	int max_following = 0;
	int id = 0;
	static int numNodes = 0;
	private ArrayList<Integer> followers = new ArrayList<>();
	private ArrayList<Integer> following = new ArrayList<>();
	Map<Integer, Integer> intersection;
	
	public double getClustering(Network net) {
		double linksInSubgraph = 0;
		ArrayList<Integer> subgraph = new ArrayList<Integer>();
		subgraph.add(this.id);
		for(Integer followerId : this.getFollowerIds()) {
			subgraph.add(followerId);
		}
		
		for(Integer followingId : this.getFollowingIds()) {
			if (!subgraph.contains(followingId))
				subgraph.add(followingId);
		}
		
		double denom = subgraph.size() * (subgraph.size() -1);
		
		for(Integer nodeIdInSubgraph : subgraph) {
			Node nodeInSubgraph = net.getNodeById(nodeIdInSubgraph);
			List<Integer> intersectFollowers = subgraph.stream().filter(
					nodeInSubgraph.getFollowerIds()::contains).collect(Collectors.toList());
			linksInSubgraph += intersectFollowers.size();
		}
		return linksInSubgraph/denom;
	}
	
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
