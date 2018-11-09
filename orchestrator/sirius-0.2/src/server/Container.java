package server;

import java.util.ArrayList;

public class Container {
	
	private String name;
	private String image;
	private String command;
	private boolean running;

	public Container(String name, String image, String command, boolean running) {
		this.name = name;
		this.image = image;
		this.command = command;
		this.running = running;
	}

	public String getName() {
		return name;
	}

	public String getImage() {
		return image;
	}

	public String getCommand() {
		return command;
	}
	
	public boolean isRunning() {
		return running;
	}

	public static Container findByName(ArrayList<Container> infos, String name) {
		for (Container info: infos)
			if (info.getName().equals(name))
				return info;
		return null;
	}
}
