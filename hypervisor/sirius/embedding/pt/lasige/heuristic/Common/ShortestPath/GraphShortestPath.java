package net.floodlightcontroller.sirius.embedding.pt.lasige.heuristic.Common.ShortestPath;

import java.util.List;

public class GraphShortestPath {
  private final List<Vertex> vertexes;
  private final List<Edge> edges;

  public GraphShortestPath(List<Vertex> vertexes, List<Edge> edges){
    this.vertexes = vertexes;
    this.edges = edges;
  }

  public List<Vertex> getVertexes() {
    return vertexes;
  }

  public List<Edge> getEdges() {
    return edges;
  }
} 