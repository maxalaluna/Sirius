package net.floodlightcontroller.sirius.tenantconfig.topology;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class DijkstraAlgorithmVirtualNetwork {

	@SuppressWarnings("unused")
	private final List<VirtualSwitch> nodes;
	private final List<VirtualEdgeSC> edgeSCApps;
	private Set<VirtualSwitch> settledNodes;
	private Set<VirtualSwitch> unSettledNodes;
	private Map<VirtualSwitch, VirtualSwitch> predecessors;
	private Map<VirtualSwitch, Integer> distance;

	public DijkstraAlgorithmVirtualNetwork(VirtualGraphSC graphSCApp) {
		// create a copy of the array so that we can operate on this array
		this.nodes = new ArrayList<VirtualSwitch>(graphSCApp.getVertexes());
		this.edgeSCApps = new ArrayList<VirtualEdgeSC>(graphSCApp.getEdges());
	}

	public void execute(VirtualSwitch source) {
		settledNodes = new HashSet<VirtualSwitch>();
		unSettledNodes = new HashSet<VirtualSwitch>();
		distance = new HashMap<VirtualSwitch, Integer>();
		predecessors = new HashMap<VirtualSwitch, VirtualSwitch>();
		distance.put(source, 0);
		unSettledNodes.add(source);
		while (unSettledNodes.size() > 0) {
			VirtualSwitch node = getMinimum(unSettledNodes);
			settledNodes.add(node);
			unSettledNodes.remove(node);
			findMinimalDistances(node);
		}
	}

	private void findMinimalDistances(VirtualSwitch node) {
		List<VirtualSwitch> adjacentNodes = getNeighbors(node);
		for (VirtualSwitch target : adjacentNodes) {
			if (getShortestDistance(target) > getShortestDistance(node)
					+ getDistance(node, target)) {
				distance.put(target, getShortestDistance(node)
						+ getDistance(node, target));
				predecessors.put(target, node);
				unSettledNodes.add(target);
			}
		}
	}

	private int getDistance(VirtualSwitch node, VirtualSwitch target) {
		for (VirtualEdgeSC edgeSCApp : edgeSCApps) {
			if(edgeSCApp.isBetweenSwitches()){
				if (edgeSCApp.getSourceSwitch().equals(node)
						&& edgeSCApp.getDestinationSwitch().equals(target)) {
					return edgeSCApp.getWeight();
				}
			}
		}
		throw new RuntimeException("Should not happen");
	}

	private List<VirtualSwitch> getNeighbors(VirtualSwitch node) {
		List<VirtualSwitch> neighbors = new ArrayList<VirtualSwitch>();
		for (VirtualEdgeSC edgeSCApp : edgeSCApps) {
			if(edgeSCApp.isBetweenSwitches()){
				if (edgeSCApp.getSourceSwitch().equals(node)
						&& !isSettled(edgeSCApp.getDestinationSwitch())) {
					neighbors.add(edgeSCApp.getDestinationSwitch());
				}
			}
		}
		return neighbors;
	}

	private VirtualSwitch getMinimum(Set<VirtualSwitch> vertexes) {
		VirtualSwitch minimum = null;
		for (VirtualSwitch vertexSCApp : vertexes) {
			if (minimum == null) {
				minimum = vertexSCApp;
			} else {
				if (getShortestDistance(vertexSCApp) < getShortestDistance(minimum)) {
					minimum = vertexSCApp;
				}
			}
		}
		return minimum;
	}

	private boolean isSettled(VirtualSwitch vertexSCApp) {
		return settledNodes.contains(vertexSCApp);
	}

	private int getShortestDistance(VirtualSwitch destination) {
		Integer d = distance.get(destination);
		if (d == null) {
			return Integer.MAX_VALUE;
		} else {
			return d;
		}
	}

	/*
	 * This method returns the path from the source to the selected target and
	 * NULL if no path exists
	 */
	public LinkedList<VirtualSwitch> getPath(VirtualSwitch target) {
		LinkedList<VirtualSwitch> path = new LinkedList<VirtualSwitch>();
		VirtualSwitch step = target;
		// check if a path exists
		if (predecessors.get(step) == null) {
			return null;
		}
		path.add(step);
		while (predecessors.get(step) != null) {
			step = predecessors.get(step);
			path.add(step);
		}
		// Put it into the correct order
		Collections.reverse(path);
		return path;
	}
} 

