package com.network.singtel.drivers;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Encoders;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.RowFactory;
import org.apache.spark.sql.SQLContext;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.sql.types.DataTypes;
import org.apache.spark.sql.types.StructField;
import org.apache.spark.sql.types.StructType;

import com.network.singtel.algo.Algo;
import com.network.singtel.algo.Graph;
import com.network.singtel.algo.MonitorFunction;
import com.network.singtel.algo.Node;

public class NetworkAlternatePathsDriver2 implements Serializable {

	private SparkSession session;
	private Map<String, Node> nodesMap;
	private Map<String, String> currentNetworkMap;
	private List<String> unhealthyPathEndPoints;
	// private List<List<Node>> finalList;

	public NetworkAlternatePathsDriver2() {

		init();
	}

	public void init() {

		this.session = SparkSession.builder().master("local[*]").appName("SingTel-Alternate-Paths-Driver")
				.getOrCreate();

		this.session.sparkContext().setLogLevel("ERROR");

		this.currentNetworkMap = new HashMap<>();

		this.unhealthyPathEndPoints = new ArrayList<>();
		// this.finalList = new ArrayList<>();

	}

	public static void main(String[] args) {
		NetworkAlternatePathsDriver2 driver = new NetworkAlternatePathsDriver2();
		driver.run();

	}

	public void run() {

		SQLContext sqlCtx = new SQLContext(session);
		// AlternateLinks graph = new AlternateLinks();

		Dataset<Row> df = sqlCtx.read().format("io.iguaz.v3io.spark.sql.kv").load("/network_monitor_score");
		// caching the df for multiple actions on same df
		df.cache();
		df.createOrReplaceTempView("network_monitor_score");

		// // query all excluding down links
		Dataset<Row> currNetworkState = sqlCtx.sql("select CONCAT(t1.linkid,'::',t1.pe_name,'::',t2.pe_name) "
				+ "as routers from network_monitor_score t1,network_monitor_score t2 "
				+ "where t1.linkid=t2.linkid and t1.pe_name!=t2.pe_name and t1.link_status_score!=15");

		// // query all unhealthy links
		Dataset<Row> unhealthNetworkLinks = sqlCtx
				.sql("select CONCAT(t1.linkid,'::',t1.pe_name,'::',t2.pe_name) as unhealthyRouters "
						+ "from network_monitor_score t1,network_monitor_score t2 "
						+ "where t1.linkid=t2.linkid and t1.pe_name!=t2.pe_name and t1.link_status_score!=15 and t1.total_score>10");
		List<String> originalList = currNetworkState.map(new MonitorFunction(), Encoders.STRING()).collectAsList();
		List<String> unhealthyList = unhealthNetworkLinks.map(new MonitorFunction(), Encoders.STRING()).collectAsList();

		// List<String> originalList = new ArrayList<>();
		// originalList.add("1::A::C");
		// originalList.add("2::A::D");
		// originalList.add("3::C::D");
		// originalList.add("4::C::E");
		// originalList.add("5::D::E");
		// originalList.add("6::B::D");
		// originalList.add("7::B::F");
		// originalList.add("8::F::E");
		//
		// List<String> unhealthyList = new ArrayList<>();
		// unhealthyList.add("4::C::E");

		LinkedList<String> originalLinksList = new LinkedList<String>();
		for (String s : originalList)
			originalLinksList.add(s);

		LinkedList<String> unhealthyLinksList = new LinkedList<String>();
		for (String s : unhealthyList)
			unhealthyLinksList.add(s);

		Graph graph = initGraph(originalLinksList);

		List<Row> origRows = new LinkedList<Row>();

		StructType structType = DataTypes
				.createStructType(new StructField[] { DataTypes.createStructField("id", DataTypes.StringType, false),
						DataTypes.createStructField("date", DataTypes.StringType, false),
						DataTypes.createStructField("sourceRouter", DataTypes.StringType, false),
						DataTypes.createStructField("destRouter", DataTypes.StringType, false),
						DataTypes.createStructField("path", DataTypes.StringType, false) });

		// calculate existing network state
		for (String srcNode : nodesMap.keySet()) {
			Set<Node> destNodes = graph.getNodes();
			Node node = nodesMap.get(srcNode);
			Algo.calculateShortestPathFromSource(node);

			for (Node destNode : destNodes) {

				// for debugging
				// finalList.add(destNode.getShortestPath());
				List<String> path = new ArrayList<>();
				String[] rowArr = new String[5];
				rowArr[0] = srcNode + "_" + destNode.getName();// id
				rowArr[1] = String.valueOf(System.currentTimeMillis()); // timestamp
				rowArr[2] = srcNode;
				rowArr[3] = destNode.getName();

				boolean isPathExist = false;
				for (Node pathNode : destNode.getShortestPath()) {
					path.add(pathNode.getName());
					isPathExist = true;
				}

				if (isPathExist) {
					path.add(destNode.getName());
					currentNetworkMap.put(path.toString(), srcNode);
					rowArr[4] = StringUtils.join(path, "::");
					origRows.add(RowFactory.create(rowArr[0], rowArr[1], rowArr[2], rowArr[3], rowArr[4]));
				}

			}
			graph = initGraph(originalLinksList);

		}
		Dataset<Row> currentDf = sqlCtx.createDataFrame(origRows, structType).cache();
		currentDf.write().format("io.iguaz.v3io.spark.sql.kv").mode("append").option("key", "id")
				.save("v3io://bigdata/network_monitor_current_paths");
		// currentDf.show(5);
		currentDf.createOrReplaceTempView("currentNetworkState");
		// System.out.println("****" + currentNetworkMap.keySet().toString());
		// System.out.println("total no of paths original" + finalList.size());
		// System.out.println("total map size" + currentNetworkMap.size());
		// finalList.clear();

		List<Row> alternateRows = new LinkedList<Row>();

		StructType structTypeAlternate = DataTypes
				.createStructType(new StructField[] { DataTypes.createStructField("id", DataTypes.StringType, false),
						DataTypes.createStructField("date", DataTypes.StringType, false),
						DataTypes.createStructField("sourceRouter", DataTypes.StringType, false),
						DataTypes.createStructField("destRouter", DataTypes.StringType, false),
						DataTypes.createStructField("originalPath", DataTypes.StringType, false),
						DataTypes.createStructField("alternatePath", DataTypes.StringType, false),
						DataTypes.createStructField("latencyCriteria", DataTypes.StringType, false),
						DataTypes.createStructField("bwCriteria", DataTypes.StringType, false),
						DataTypes.createStructField("pktRateCriteria", DataTypes.StringType, false),
						DataTypes.createStructField("isFeasible", DataTypes.StringType, false), });

		// calculate alternate network state ( only for unhealthy links)

		Map<String, String> srcDestNodesMap = new HashMap<>();

		for (String row : unhealthyLinksList) {
			String[] str = row.split("::");
			String pathLike = str[1] + "::" + str[2];
			List<String> results = sqlCtx.sql(
					"SELECT CONCAT(sourceRouter,'--',destRouter,'--',path) from currentNetworkState where path LIKE CONCAT('%','"
							+ pathLike + "', '%')")
					.map(new MonitorFunction(), Encoders.STRING()).collectAsList();
			for (String result : results) {
				String[] split = result.split("--");
				srcDestNodesMap.put(split[0], split[1] + "," + split[2]);

			}

			for (Map.Entry<String, String> entry : srcDestNodesMap.entrySet()) {
				originalLinksList.remove(row);
				graph = initGraph(originalLinksList);
				Set<Node> destNodes = graph.getNodes();
				Node srcNode = nodesMap.get(entry.getKey());
				Algo.calculateShortestPathFromSource(srcNode);

				for (Node destNode : destNodes) {
					String[] split = entry.getValue().split(",");
					if (destNode.getName().equalsIgnoreCase(split[0])) {
						// for debugging

						// StringJoiner path = new StringJoiner("::");
						List<String> path = new ArrayList<>();
						String[] rowArr = new String[10];
						rowArr[0] = str[0] + "_" + entry.getKey() + "_" + split[0];// id
						rowArr[1] = String.valueOf(System.currentTimeMillis()); // timestamp
						rowArr[2] = entry.getKey();// source name
						rowArr[3] = split[0]; // destination name
						rowArr[4] = split[1]; // original path
						rowArr[6] = "True";
						rowArr[7] = "True";
						rowArr[8] = "True";
						rowArr[9] = "True";

						boolean isPathExist = false;
						for (Node pathNode : destNode.getShortestPath()) {
							path.add(pathNode.getName());
							isPathExist = true;
						}

						if (isPathExist) {
							path.add(destNode.getName());
							// currentNetworkMap.put(path.toString(), srcNode);
							rowArr[5] = StringUtils.join(path, "::");// alternate path
							alternateRows.add(RowFactory.create(rowArr[0], rowArr[1], rowArr[2], rowArr[3], rowArr[4],
									rowArr[5], rowArr[6], rowArr[7], rowArr[8], rowArr[9]));
						}

					}

				}
				originalLinksList.add(row);
				graph = initGraph(originalLinksList);
			}

		}
		Dataset<Row> alternateDf = sqlCtx.createDataFrame(alternateRows, structTypeAlternate).cache();

		alternateDf.write().format("io.iguaz.v3io.spark.sql.kv").mode("append").option("key", "id")
				.save("v3io://bigdata/network_monitor_alternate_paths");
		// alternateDf.show();
		// origlist.remove(row);
		// Graph graphAlternate = initGraph(origlist);
		//
		//
		//
		// // calculate existing network state
		// // for each unhealthy link , instead of nodesMap.keySet() , we will run this
		// // algo only for those source routers , where unhealthy link appears in the
		// path
		// for (String srcNode : nodesMap.keySet()) {
		// Set<Node> currentNodes = graphAlternate.getNodes();
		// Algo.calculateShortestPathFromSource(nodesMap.get(srcNode));
		//
		// // System.out.println("source node***" + srcNode);
		// for (Node node : currentNodes) {
		// // System.out.println("dest node name::" + node.getName());
		// // System.out.println("cost::" + node.getDistance());
		// finalList.add(node.getShortestPath());
		//
		// }
		// origlist.add(row);
		// graphAlternate = initGraph(origlist);
		//
		// }
		//
		// // String[] str = row.split("::");
		// // for (String path : currentNetworkMap.values()) {
		// // if (path.contains(str[1] + "::" + str[2])) {
		// // Graph alternateGraph = deepClone(currentGraph);
		// //
		// // String[] links = path.split("::");
		// // unhealthyPathEndPoints.add(links[0] + "::" + links[links.length - 1]);
		// // removeNodesFromGraph(str[1], str[2], alternateGraph);
		// // alternateGraph = Algo.calculateShortestPathFromSource(alternateGraph,
		// // nodes.get(links[0]));
		// //
		// // Set<Node> nodes = alternateGraph.getNodes();
		// // String shortestPath = "";
		// // for (Node node : nodes) {
		// // // System.out.println("node name-->" + node.getName());
		// // // System.out.println("node distance-->" + node.getDistance());
		// // if (node.getName().equalsIgnoreCase(links[links.length - 1])) {
		// // List<Node> routers = node.getShortestPath();
		// // for (Node link : routers) {
		// // shortestPath = shortestPath + "-" + link.getName();
		// // finalList.add(shortestPath);
		// // }
		// // // System.out.println("source:dest pair::" + links[0] + links[links.length
		// -
		// // // 1]);
		// // // System.out.println(shortestPath);
		// // shortestPath = "";
		// // }
		// // }
		// //
		// // }
		// // }
		//
		// }
		// System.out.println("total no of paths alternate" + finalList.size());
		session.close();

	}

	public Graph initGraph(List<String> list) {
		Graph graph = new Graph();
		this.nodesMap = new HashMap<>();
		for (String row : list) {

			String[] nodeArr = row.split("::");
			String sourceRouter = nodeArr[1];
			String destRouter = nodeArr[2];
			// int cost = Integer.parseInt(str[3]);
			Random rand = new Random();
			int cost = rand.nextInt(50) + 1;

			if (nodesMap.containsKey(sourceRouter)) {
				if (nodesMap.containsKey(destRouter)) {
					nodesMap.get(sourceRouter).addDestination(nodesMap.get(destRouter), cost);
				} else {
					Node destNode = new Node(destRouter);
					nodesMap.put(destRouter, destNode);
					nodesMap.get(sourceRouter).addDestination(nodesMap.get(destRouter), cost);
				}
			} else {
				Node sourceNode = new Node(sourceRouter);
				nodesMap.put(sourceRouter, sourceNode);
				if (nodesMap.containsKey(destRouter)) {
					nodesMap.get(sourceRouter).addDestination(nodesMap.get(destRouter), cost);
				} else {
					Node destNode = new Node(destRouter);
					nodesMap.put(destRouter, destNode);
					nodesMap.get(sourceRouter).addDestination(nodesMap.get(destRouter), cost);
				}
			}
			graph.addNode(nodesMap.get(sourceRouter));
			graph.addNode(nodesMap.get(destRouter));
		}
		return graph;
	}

	/**
	 * This method makes a "deep clone" of any Java object it is given.
	 * 
	 * @param <T>
	 */
	public static <T> T deepClone(T object) {
		try {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			ObjectOutputStream oos = new ObjectOutputStream(baos);
			oos.writeObject(object);
			ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
			ObjectInputStream ois = new ObjectInputStream(bais);
			return (T) ois.readObject();
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	// public Map<String, String> processPaths(Node srcNode, Set<Node> destNodes) {
	// for (Node destNode : destNodes) {
	// String shortestPath = srcNode.getName();
	// // boolean isFeasible = false;
	// for (Node link : destNode.getShortestPath()) {
	// // isFeasible = true;
	// shortestPath = (link.getName().equals(srcNode.getName())) ? (shortestPath +
	// "::" + link.getName())
	// : shortestPath;
	// }
	// // if (isFeasible)
	// currentNetworkMap.put(srcNode.getName() + "::" + destNode.getName(),
	// shortestPath + "::" + destNode.getName());
	// shortestPath = "";
	// }
	// return currentNetworkMap;
	// }
}
