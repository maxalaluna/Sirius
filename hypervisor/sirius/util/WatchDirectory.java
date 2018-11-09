package net.floodlightcontroller.sirius.util;

import static java.nio.file.LinkOption.NOFOLLOW_LINKS;
import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_DELETE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY;
import static java.nio.file.StandardWatchEventKinds.OVERFLOW;
import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.WatchEvent;
import java.nio.file.WatchEvent.Kind;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.Observable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WatchDirectory extends Observable implements Runnable{
	Path path;
	String fileChanged;
	long timeConfigFile;
	protected static Logger log = LoggerFactory.getLogger(WatchDirectory.class);
	
	public WatchDirectory(Path path) {
		super();
		this.path = path;
	}

	public long getTimeConfigFile() {
		return timeConfigFile;
	}

	public void setTimeConfigFile(long timeConfigFile) {
		this.timeConfigFile = timeConfigFile;
	}

	@SuppressWarnings("unchecked")
	public void run() {
		// Sanity check - Check if path is a folder
		try {
			File file = new File(path.toString());
			if (!file.exists()) {
				if (file.mkdirs()) {
					log.info("Directory "+path.toString()+" created!");
				} else {
					log.info("Failed to create directory!");
				}
			}
			Boolean isFolder = (Boolean) Files.getAttribute(path,
					"basic:isDirectory", NOFOLLOW_LINKS);
			if (!isFolder) {
				throw new IllegalArgumentException("Path: " + path
						+ " is not a folder");
			}
		} catch (IOException ioe) {
			// Folder does not exists
			ioe.printStackTrace();
		}

		log.info("Watching path: " + path);

		// We obtain the file system of the Path
		FileSystem fs = path.getFileSystem();

		// We create the new WatchService using the new try() block
		try (WatchService service = fs.newWatchService()) {

			// We register the path to the service
			// We watch for creation events
			path.register(service, ENTRY_CREATE, ENTRY_MODIFY, ENTRY_DELETE); 

			// Start the infinite polling loop
			WatchKey key = null;
			while (true) {
				key = service.take();

				// Dequeueing events
				Kind<?> kind = null;
				for (WatchEvent<?> watchEvent : key.pollEvents()) {
					// Get the type of the event
					kind = watchEvent.kind();
					if (OVERFLOW == kind) {
						continue; // loop
					} else if (ENTRY_CREATE == kind) {
						// A new Path was created
						Path newPath = ((WatchEvent<Path>) watchEvent)
								.context();
						// Output

						String fileString = path.toString()+"/";
						File file = new File(fileString.concat(newPath.toString()));
						String extension = Utils.getExtension(file);
						if (Utils.isConfigFile(extension)){
							this.setFileChanged(file.toString());
							log.info("Config file created: " + file);
							timeConfigFile = file.lastModified();
							setChanged(); // marca esse objeto observável como alterado  
					        notifyObservers(); 
						}
						log.info("New path created: " + newPath + "timeConfigFile: " +timeConfigFile);
						continue;

					} else if (ENTRY_MODIFY == kind) {
						// modified
						Path newPath = ((WatchEvent<Path>) watchEvent)
								.context();
						// Output
						
						String fileString = path.toString()+"/";
						File file = new File(fileString.concat(newPath.toString()));
						String extension = Utils.getExtension(file);
						if (Utils.isConfigFile(extension)){
							this.setFileChanged(file.toString());
							log.info("Config file modified: " + file);
							timeConfigFile = file.lastModified();
							setChanged(); // marca esse objeto observável como alterado  
					        notifyObservers(); 
						}
						log.info("New path modified: " + newPath);
						continue;
					}
				}

				if (!key.reset()) {
					break; // loop
				}
			}

			
		} catch (Exception ioe) {
			ioe.printStackTrace();
		}

	}

	public String getFileChanged() {
		return fileChanged;
	}

	public void setFileChanged(String fileChanged) {
		this.fileChanged = fileChanged;
	}

	/*
	public static void main(String[] args) throws IOException,
	InterruptedException {
		// Folder we are going to watch
		// Path folder =
		// Paths.get(System.getProperty("C:\\Users\\Isuru\\Downloads"));
		File dir = new File("/home/max/testeDir");

		watchDirectoryPath(dir.toPath());
	}
	 */
}