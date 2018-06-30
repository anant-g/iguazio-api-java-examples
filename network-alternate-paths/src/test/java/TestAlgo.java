import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import com.network.singtel.algo.Algo;
import com.network.singtel.algo.Graph;
import com.network.singtel.algo.Node;

public class TestAlgo {

	Map<String, Node> nodesMap;
	List<Node> shortestPathForNodeB;
	List<Node> shortestPathForNodeC;
	List<Node> shortestPathForNodeD;
	List<Node> shortestPathForNodeE;
	List<Node> shortestPathForNodeF;

	public Graph init() {
		Node nodeA = new Node("A");
		Node nodeB = new Node("B");
		Node nodeC = new Node("C");
		Node nodeD = new Node("D");
		Node nodeE = new Node("E");
		Node nodeF = new Node("F");
		nodesMap = new java.util.HashMap<>();

		nodeA.addDestination(nodeB, 10);
		nodeA.addDestination(nodeC, 15);

		nodeB.addDestination(nodeD, 12);
		nodeB.addDestination(nodeF, 15);

		nodeC.addDestination(nodeE, 10);

		nodeD.addDestination(nodeE, 2);
		nodeD.addDestination(nodeF, 1);

		nodeF.addDestination(nodeE, 5);

		Graph graph = new Graph();

		nodesMap.put(nodeB.getName(), nodeB);
		nodesMap.put(nodeC.getName(), nodeC);
		nodesMap.put(nodeD.getName(), nodeD);
		nodesMap.put(nodeA.getName(), nodeA);
		nodesMap.put(nodeE.getName(), nodeE);
		nodesMap.put(nodeF.getName(), nodeF);
		graph.addNode(nodeD);
		graph.addNode(nodeB);
		graph.addNode(nodeC);
		graph.addNode(nodeA);
		graph.addNode(nodeE);
		graph.addNode(nodeF);

		shortestPathForNodeB = Arrays.asList(nodeA);
		shortestPathForNodeC = Arrays.asList(nodeA);
		shortestPathForNodeD = Arrays.asList(nodeA, nodeB);
		shortestPathForNodeE = Arrays.asList(nodeA, nodeB, nodeD);
		shortestPathForNodeF = Arrays.asList(nodeA, nodeB, nodeD);

		return graph;
	}

	@Test
	public void whenSPPSolved_thenCorrect() {
		Graph graph = init();
		Algo.calculateShortestPathFromSource(nodesMap.get("A"));

		// to print full network graph
		// @Test
		// public void whenSPPSolved_thenCorrect() {
		// Graph graph = init();
		// for (String srcNode : nodesMap.keySet()) {
		// Set<Node> nodes = graph.getNodes();
		// Algo.calculateShortestPathFromSource(nodesMap.get(srcNode));
		//
		// System.out.println("source node***" + srcNode);
		// for (Node node : nodes) {
		// System.out.println("dest node name::" + node.getName());
		// System.out.println("cost::" + node.getDistance());
		//
		// for (Node n : node.getShortestPath()) {
		// System.out.println("node path::" + n.getName());
		//
		// }
		// }
		// graph = init();
		// }

		for (Node node : graph.getNodes()) {
			switch (node.getName()) {
			case "B":
				assertTrue(node.getShortestPath().equals(shortestPathForNodeB));
				break;
			case "C":
				assertTrue(node.getShortestPath().equals(shortestPathForNodeC));
				break;
			case "D":
				assertTrue(node.getShortestPath().equals(shortestPathForNodeD));
				break;
			case "E":
				assertTrue(node.getShortestPath().equals(shortestPathForNodeE));
				break;
			case "F":
				assertTrue(node.getShortestPath().equals(shortestPathForNodeF));
				break;
			}
		}
	}

}