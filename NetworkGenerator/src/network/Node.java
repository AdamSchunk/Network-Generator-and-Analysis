package network;

import java.util.*;

public class Node {
	int max_followers = 0;
	int max_following = 0;
	int id = 0;
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
	
	public ArrayList<Integer> getFollowersIds() {
		return followers;
	}
	
	public ArrayList<Integer> getFollowingIds() {
		return following;
	}
	
	public boolean follow(int id) {
		if(following.contains(id)) {
			return false;
		}
		following.add(id);
		return true;
	}
	
	public boolean getFollowedBy(int id) {
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
