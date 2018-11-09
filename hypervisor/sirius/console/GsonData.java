package net.floodlightcontroller.sirius.console;

import java.util.ArrayList;

public class GsonData {

	public ArrayList<VM> vms;
	public ArrayList<Cloud> clouds;
	public ArrayList<Node> nodes;
	public ArrayList<Link> links;

	public GsonData() {
		vms = new ArrayList<>();
		clouds = new ArrayList<>();
		nodes = new ArrayList<>();
		links = new ArrayList<>();
	}
	
	public class Cloud {
		
		public int id;
		public String name;
		public int rate;
		
		public Cloud(int id, String name, int rate) {
			this.id = id;
			this.name = name;
			this.rate = rate;
		}
	}
	
	public class VM {
	
		public int id;
		public int state;
		public String name;
		
		public VM(int id, int state, String name) {
			this.id = id;
			this.state = state;
			this.name = name;
		}
	}
	
	public class Node {
		
		public int id;
		public int vid;
		public int cid;
		public String label;
		public String title;
		public String image;
		public boolean selected;
		
		public Node(int id, int vid, int cid, String label, 
				String title, String image, boolean selected) {
			this.id = id;
			this.vid = vid;
			this.cid = cid;
			this.label = label;
			this.title = title;
			this.image = image;
			this.selected = selected;
		}
	}

	public class Link {

		public int id;
		public String title;
		public int from, to;
		public boolean selected;
		
		public Link(int id, String title, int from, 
				int to, boolean selected) {
			this.id = id;
			this.title = title;
			this.from = from;
			this.to = to;
			this.selected = selected;
		}
	}
	
	public void addCloud(int id, String name, int rate) {
		clouds.add(new Cloud(id, name, rate));
	}
	
	public void addVM(int id, int state, String name) {
		vms.add(new VM(id, state, name));
	}
	
	public void addNode(int id, int vid, int cid, 
			String label, String title, String image, boolean selected) {
		nodes.add(new Node(id, vid, cid, label, title, image, selected));
	}
	
	public void addLink(int id, String title, int from, int to, boolean selected) {
		links.add(new Link(id, title, from, to, selected));
	}
}
