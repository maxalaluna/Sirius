/**
 * Copyright 2010 Russ Weeks rweeks@newbrightidea.com
 * Licensed under the GNU LGPL
 * License details here: http://www.gnu.org/licenses/lgpl-3.0.txt
 */
package net.floodlightcontroller.sirius.embedding.pt.lasige.heuristic.Common.DisjointKShortestPath.graph;

public class NetworkInterface {

  private Double teMetric;

  private final Device device;

  NetworkInterface(Device device, Double teMetric) {
    this.device = device;
    this.teMetric = teMetric;
  }

  public Double getTeMetric() {
    return teMetric;
  }

  public void setTeMetric(Double teMetric) {
    this.teMetric = teMetric;
  }

  public Device getDevice() {
    return device;
  }

  @Override
public String toString()
  {
    return "nwIf: " + device.getName() + " teMetric: " + teMetric;
  }
}
