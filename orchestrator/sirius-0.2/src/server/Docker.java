package server;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import common.Console;
import common.Host;
import common.Image;
import common.ServerError;
import common.Vm;

public class Docker {
	
	public static String run(Host node, String cmd) throws ServerError {
		return node.getVm().runCmd("docker exec " + node.getName() + " " + cmd);
	}
	
	public static ArrayList<Container> getContainers(Vm vm) throws ServerError {
		String cmd = "docker ps --all --format {{.Names}}+{{.Image}}+{{.Command}}+{{.Status}}";
		String[] lines = vm.runCmd(cmd).split("\n");
		ArrayList<Container> tmp = new ArrayList<>();
		for (String line: lines) {
			String[] items = line.split("\\+");
			if (items.length == 4) {
				tmp.add(new Container(items[0], items[1], 
						items[2], items[3].contains("Up")));
			}
		}
		return tmp;
	}
		
	public static ArrayList<String> getImages(Vm vm) throws ServerError {
		String cmd = "docker images --format {{.Repository}}";
		String[] lines = vm.runCmd(cmd).split("\n");
		ArrayList<String> tmp = new ArrayList<>();
		for (String line: lines) tmp.add(line);
		return tmp;
	}
	
	public static void initConfiguration(Vm vm) throws ServerError {
		
	}
	
	public static void clearVolumes(Vm vm) throws ServerError {
		Console.info("Remove unused docker volunes");
		vm.runCmd("docker volume prune -f");
	}
	
	public static void restartService(Vm vm) throws ServerError {
		Console.info("Restarting docker service");
		vm.runCmd("sudo service docker restart");
	}
	
	public static void deployContainers(Server server, Vm vm,
			ArrayList<Host> nodes) throws ServerError {
		try {
			String name = vm.getName() + ".txt";
			String path = Console.getPath(name);
			long start = System.currentTimeMillis();
			Console.info("Creating data file '" + path + "'");
			FileOutputStream fos = new FileOutputStream(new File(path));
			BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(fos));
			for (Host node : nodes) {
				String data = node.getName() + " " + node.getPort() + " " 
						+ node.getIp() + " " + node.getMac() 
						+ " " + node.getImage();
				bw.write(data);
				bw.newLine();
			}
			bw.close();

			// Upload file
			vm.uploadFile(path, "sirius");
			
			// Create containers
			vm.runCmd("sirius/create.sh " + name);
			double time1 = (System.currentTimeMillis() - start) / 1000.0;
			Console.info("Creation time is " + time1 + "s in " + vm.getName());
			start = System.currentTimeMillis();

			// Configure containers
			vm.runCmd("sirius/configure.sh " + name);
			double time2 = (System.currentTimeMillis() - start) / 1000.0;
			Console.info("Configuration time is " + time2 + "s in " + vm.getName());
		} 
		catch (IOException ex) {
			throw new ServerError(ex.getMessage());
		}
	}
	
	public static void removeContainers(Server server, Vm vm, 
			ArrayList<Container> containers) throws ServerError {
		try {
			String prefix = server.getConfig().defaultDockerPrefix;
			String name = vm.getName() + ".txt";
			String path = Console.getPath(name);
			long start = System.currentTimeMillis();
			Console.info("Creating data file '" + path + "'");
			FileOutputStream fos = new FileOutputStream(new File(path));
			BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(fos));
			for (Container container: containers) {
				String data = container.getName();
				if (data.startsWith(prefix)) {
					bw.write(data);
					bw.newLine();
				}
			}
			bw.close();
			
			// Upload file
			vm.uploadFile(path, "sirius");
			
			// Remove containers
			vm.runCmd("sirius/remove.sh " + name);
			double time = (System.currentTimeMillis() - start) / 1000.0;
			Console.info("Removal time is " + time + "s in " + vm.getName());
		} 
		catch (IOException ex) {
			throw new ServerError(ex.getMessage());
		}
	}
	
	private static void uploadImageFiles(Vm vm, String name, String folder) throws ServerError {
		String path = System.getenv("SUPERCLOUD_DOCKER");
		if (path == null) 
			throw new ServerError("SUPERCLOUD_DOCKER variable not set");
		vm.runCmd("mkdir -p " + folder);
		vm.uploadFile(path + "/" + name, folder);
	}
	
	private static void removeImageFiles(Vm vm, String folder) throws ServerError {
		String path = System.getenv("SUPERCLOUD_DOCKER");
		if (path == null) 
			throw new ServerError("SUPERCLOUD_DOCKER variable not set");
		vm.runCmd("rm -r " + folder);
	}
	
	public static void deployImage(Server server, Vm vm, Image image) throws ServerError {
		Console.info("Deploying image '" + image.getFile() + "' in " + vm.getName());
		switch (image.getFile()) {
		case "kiwenlau/hadoop": {
			String cmd = "docker load -i docker-hadoop/hadoop.tar";
			uploadImageFiles(vm, "hadoop.tar", "docker-hadoop");
			vm.runCmd(cmd);
			removeImageFiles(vm,  "docker-hadoop");
			break;
		}
		default:
			throw new ServerError("Image '" + image.getFile() + "' not handled");
		}
	}
}
