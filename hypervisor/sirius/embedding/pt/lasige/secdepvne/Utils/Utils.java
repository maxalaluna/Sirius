package net.floodlightcontroller.sirius.embedding.pt.lasige.secdepvne.Utils;

import java.io.File;
import java.io.IOException;

/**
 * Class with useful methods
 * @authors Luis Ferrolho, fc41914, Max Alaluna, fc47349, Faculdade de Ciencias da Universidade de Lisboa
 *
 */
public class Utils {

	/**
	 * Runs the model to embed a VN onto a SN.
	 * @param datFile Input file for the model with all the necessary info of the networks (parameters, sets, ...)
	 * @param modFile File with the model
	 * @param outputFile File with the result of the execution. The values of all variables are all here, as well as the cost of embedding and execution time
	 * @param timeout Period time during which the model is executed
	 * @return true is timeout was not reached (The execution of the model terminated); false otherwise
	 */
	public static boolean runGLPSOL(String datFile, String modFile, String outputFile, int timeout) {		
		
		try {
			ProcessBuilder builder = new ProcessBuilder("glpsol","--model", modFile, "--data", datFile);
			builder.redirectOutput(new File(outputFile));

			Process p = builder.start();

			long now = System.currentTimeMillis(); 
			long timeoutInMillis = 1000L * timeout; 
			long finish = now + timeoutInMillis; 

			// Check if the timeout was reached or not
			while(isAlive(p)){
				
				Thread.sleep(10);
				
				if (System.currentTimeMillis() > finish){
					System.out.println("[Error] Timeout excedeed!");
					p.destroy();
					return false;
				}
			}
			
		}catch (InterruptedException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}catch (Exception e) {
			e.printStackTrace();
		}

		return true;
	}
	
	/**
	 * Check if a process p has terminated
	 * @param p A process p
	 * @return true if p is executing, false otherwise.
	 */
	private static boolean isAlive(Process p) {  
		try{  
			p.exitValue();  
			return false;  
		}catch (IllegalThreadStateException e) {  
			return true;  
		}  
	}
	
	/**
	 * Formats the string to only return the name of the file whitout the extension
	 * @param file The full name of the file
	 * @return the name of the file without the extension
	 */
	public static String fileNameWithoutExt(String file) {
		int lastIndexDot = file.lastIndexOf('.');
		return file.substring(0, lastIndexDot); 
	}
	
}
