package net.floodlightcontroller.sirius.routing;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.floodlightcontroller.sirius.providerconfig.Server3;
import net.floodlightcontroller.sirius.providerconfig.topology.EdgeSC;
import net.floodlightcontroller.sirius.providerconfig.topology.GraphSC;

public class DijkstraAlgorithmSCApp {

  @SuppressWarnings("unused")
private final List<Server3> nodes;
  private final List<EdgeSC> edgeSCApps;
  private Set<Server3> settledNodes;
  private Set<Server3> unSettledNodes;
  private Map<Server3, Server3> predecessors;
  private Map<Server3, Integer> distance;

  public DijkstraAlgorithmSCApp(GraphSC graphSCApp) {
    // create a copy of the array so that we can operate on this array
    this.nodes = new ArrayList<Server3>(graphSCApp.getVertexes());
    this.edgeSCApps = new ArrayList<EdgeSC>(graphSCApp.getEdges());
  }

  public void execute(Server3 source) {
    settledNodes = new HashSet<Server3>();
    unSettledNodes = new HashSet<Server3>();
    distance = new HashMap<Server3, Integer>();
    predecessors = new HashMap<Server3, Server3>();
    distance.put(source, 0);
    unSettledNodes.add(source);
    while (unSettledNodes.size() > 0) {
    	Server3 node = getMinimum(unSettledNodes);
      settledNodes.add(node);
      unSettledNodes.remove(node);
      findMinimalDistances(node);
    }
  }

  private void findMinimalDistances(Server3 node) {
    List<Server3> adjacentNodes = getNeighbors(node);
    for (Server3 target : adjacentNodes) {
      if (getShortestDistance(target) > getShortestDistance(node)
          + getDistance(node, target)) {
        distance.put(target, getShortestDistance(node)
            + getDistance(node, target));
        predecessors.put(target, node);
        unSettledNodes.add(target);
      }
    }
  }

  private int getDistance(Server3 node, Server3 target) {
    for (EdgeSC edgeSCApp : edgeSCApps) {
      if (edgeSCApp.getSource().equals(node)
          && edgeSCApp.getDestination().equals(target)) {
        return edgeSCApp.getWeight();
      }
    }
    throw new RuntimeException("Should not happen");
  }

  private List<Server3> getNeighbors(Server3 node) {
    List<Server3> neighbors = new ArrayList<Server3>();
    for (EdgeSC edgeSCApp : edgeSCApps) {
      if (edgeSCApp.getSource().equals(node)
          && !isSettled(edgeSCApp.getDestination())) {
        neighbors.add(edgeSCApp.getDestination());
      }
    }
    return neighbors;
  }

  private Server3 getMinimum(Set<Server3> vertexes) {
	  Server3 minimum = null;
    for (Server3 vertexSCApp : vertexes) {
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

  private boolean isSettled(Server3 vertexSCApp) {
    return settledNodes.contains(vertexSCApp);
  }

  private int getShortestDistance(Server3 destination) {
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
  public LinkedList<Server3> getPath(Server3 target) {
    LinkedList<Server3> path = new LinkedList<Server3>();
    Server3 step = target;
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

