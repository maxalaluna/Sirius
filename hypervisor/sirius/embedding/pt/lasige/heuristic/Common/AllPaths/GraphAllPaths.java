package net.floodlightcontroller.sirius.embedding.pt.lasige.heuristic.Common.AllPaths;

/**
 * 
 * @author Max Alaluna, fc47349, Faculdade de Ciencias da Universidade de Lisboa
 *
 */

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

public class GraphAllPaths {
    private Map<String, LinkedHashSet<String>> map = new HashMap<String, LinkedHashSet<String>>();
    private Map<Integer, LinkedHashSet<String>> routes = new HashMap<Integer, LinkedHashSet<String>>();
    private int indexRoutes = 0;

    public void addEdge(String node1, String node2) {
        LinkedHashSet<String> adjacent = map.get(node1);
        if(adjacent==null) {
            adjacent = new LinkedHashSet<String>();
            map.put(node1, adjacent);
        }
        adjacent.add(node2);
    }

    public void addTwoWayVertex(String node1, String node2) {
        addEdge(node1, node2);
        addEdge(node2, node1);
    }

    public boolean isConnected(String node1, String node2) {
        Set<?> adjacent = map.get(node1);
        if(adjacent==null) {
            return false;
        }
        return adjacent.contains(node2);
    }

    public LinkedList<String> adjacentNodes(String last) {
        LinkedHashSet<String> adjacent = map.get(last);
        if(adjacent==null) {
            return new LinkedList<String>();
        }
        return new LinkedList<String>(adjacent);
    }

	public Map<Integer, LinkedHashSet<String>> getRoutes() {
		return routes;
	}

	public void setRoutes(Map<Integer, LinkedHashSet<String>> routes) {
		this.routes = routes;
	}

	public int getIndexRoutes() {
		return indexRoutes;
	}

	public void setIndexRoutes(int indexRoutes) {
		this.indexRoutes = indexRoutes;
	}
}
