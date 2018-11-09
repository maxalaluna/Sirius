package net.floodlightcontroller.sirius.providerconfig.topology;

import java.util.List;

import net.floodlightcontroller.sirius.providerconfig.Server3;

public class GraphSC {
  private final List<Server3> vertexes;
  private final List<EdgeSC> edgeSCApps;

  public GraphSC(List<Server3> vertexes, List<EdgeSC> edgeSCApps) {
    this.vertexes = vertexes;
    this.edgeSCApps = edgeSCApps;
  }

  public List<Server3> getVertexes() {
    return vertexes;
  }

  public List<EdgeSC> getEdges() {
    return edgeSCApps;
  }
} 
