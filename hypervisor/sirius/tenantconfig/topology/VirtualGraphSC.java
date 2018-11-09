package net.floodlightcontroller.sirius.tenantconfig.topology;

import java.util.List;

public class VirtualGraphSC {
  private final List<VirtualSwitch> vertexes;
  private final List<VirtualEdgeSC> edgeSCApps;

  public VirtualGraphSC(List<VirtualSwitch> vertexes, List<VirtualEdgeSC> edgeSCApps) {
    this.vertexes = vertexes;
    this.edgeSCApps = edgeSCApps;
  }

  public List<VirtualSwitch> getVertexes() {
    return vertexes;
  }

  public List<VirtualEdgeSC> getEdges() {
    return edgeSCApps;
  }
} 
