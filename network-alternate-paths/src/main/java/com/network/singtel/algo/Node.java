package com.network.singtel.algo;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

public class Node {

	private int linkId;
	private String name;
	// private String destRouter;
	private double peakUtil;
	private double peakBw;
	private int pktRate;

	private LinkedList<Node> shortestPath = new LinkedList<>();

	private Integer distance = Integer.MAX_VALUE;

	private Map<Node, Integer> adjacentNodes = new HashMap<>();

	public Node(String[] str) {
		this.linkId = Integer.parseInt(str[0]);
		this.name = str[1];
		// this.destRouter = str[2];

		// hardcoded for now
		// this.peakUtil = new Double(str[3]);
		// this.peakBw = new Double(str[4]);
		// this.pktRate = Integer.parseInt(str[5]);
		this.peakUtil = 7.5;
		this.peakBw = 20;
		this.pktRate = 100;
	}

	public Node(String str) {

		this.name = str;

	}

	public void addDestination(Node destination, int distance) {
		adjacentNodes.put(destination, distance);
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Map<Node, Integer> getAdjacentNodes() {
		return adjacentNodes;
	}

	public Integer getDistance() {
		return distance;
	}

	public void setDistance(Integer distance) {
		this.distance = distance;
	}

	public LinkedList<Node> getShortestPath() {
		return shortestPath;
	}

	public void setShortestPath(LinkedList<Node> shortestPath) {
		this.shortestPath = shortestPath;
	}

	// public String getDestRouter() {
	// return destRouter;
	// }
	//
	// public void setDestRouter(String destRouter) {
	// this.destRouter = destRouter;
	// }

	public int getLinkId() {
		return linkId;
	}

	public void setLinkId(int linkId) {
		this.linkId = linkId;
	}

	public double getPeakUtil() {
		return peakUtil;
	}

	public void setPeakUtil(double peakUtil) {
		this.peakUtil = peakUtil;
	}

	public double getPeakBw() {
		return peakBw;
	}

	public void setPeakBw(double peakBw) {
		this.peakBw = peakBw;
	}

	public int getPktRate() {
		return pktRate;
	}

	public void setPktRate(int pktRate) {
		this.pktRate = pktRate;
	}

	@Override
	public String toString() {
		return " + name + ";
	}

}