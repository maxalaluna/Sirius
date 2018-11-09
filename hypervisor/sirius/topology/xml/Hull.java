package net.floodlightcontroller.sirius.topology.xml;

import java.awt.Point;
import java.util.ArrayList;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class Hull {

	private ArrayList<Point> points;
	private int id;
	
	public Hull(int id, ArrayList<Point> points) {
		this.points = points;
		this.id = id;
	}

	public ArrayList<Point> getPoints() {
		return points;
	}

	public void setPoints(ArrayList<Point> points) {
		this.points = points;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	/**
	 * Write hull's attributes into XML document
	 * @param doc
	 * @param config
	 * @return XML element
	 */
	
	public Element toXML(Document doc, Config config) {
		Element elem = doc.createElement("hull");
		elem.setAttribute("id", Integer.toString(id));
		for (Point point : points) {
			Element aux = doc.createElement("point");
			Config.writeXMLInteger(doc, aux, "x", (int)point.getX());
			Config.writeXMLInteger(doc, aux, "y", (int)point.getY());
			elem.appendChild(aux);
		}
		return elem;
	}
}
