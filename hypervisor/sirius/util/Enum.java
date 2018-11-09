package net.floodlightcontroller.sirius.util;

import java.util.HashMap;
import java.util.Map;

public class Enum {

	public enum SecurityLevelVirtualNode{
		NORMALCONTAINER(1), NORMALVM(2), SECUREVM1(3), SECUREVM2(4), SECUREVM3(5), SECUREVM4(6);
		
		private int value;
	    private static Map map = new HashMap<>();

	    private SecurityLevelVirtualNode(int value) {
	        this.value = value;
	    }

	    static {
	        for (SecurityLevelVirtualNode pageType : SecurityLevelVirtualNode.values()) {
	            map.put(pageType.value, pageType);
	        }
	    }

	    public static SecurityLevelVirtualNode valueOf(int pageType) {
	        return (SecurityLevelVirtualNode) map.get(pageType);
	    }

	    public int getValue() {
	        return value;
	    }
	}
	
	public enum SecurityLevelPhysicalNode{
		NORMALCONTAINER(1), NORMALVM(2), SECUREVM1(3), SECUREVM2(4), SECUREVM3(5), SECUREVM4(6);
		
		private int value;
	    private static Map map = new HashMap<>();

	    private SecurityLevelPhysicalNode(int value) {
	        this.value = value;
	    }

	    static {
	        for (SecurityLevelPhysicalNode pageType : SecurityLevelPhysicalNode.values()) {
	            map.put(pageType.value, pageType);
	        }
	    }

	    public static SecurityLevelPhysicalNode valueOf(int pageType) {
	        return (SecurityLevelPhysicalNode) map.get(pageType);
	    }

	    public int getValue() {
	        return value;
	    }
	}
	
	public enum SecurityLevelLinks{
		REGULAR(1), INTERMEDIARY(2), HIGH1(3), HIGH2(4), HIGH3(5), HIGH4(6);
		
		private int value;
	    private static Map map = new HashMap<>();

	    private SecurityLevelLinks(int value) {
	        this.value = value;
	    }

	    static {
	        for (SecurityLevelLinks pageType : SecurityLevelLinks.values()) {
	            map.put(pageType.value, pageType);
	        }
	    }

	    public static SecurityLevelLinks valueOf(int pageType) {
	        return (SecurityLevelLinks) map.get(pageType);
	    }

	    public int getValue() {
	        return value;
	    }
	}
	

	public enum DependabilityLevelVirtualNode{
		
		NOREPLICATION(0), REPLICATIONOTHERCLOUD(1), REPLICATIONSAMECLOUD(2);
		
		private int value;
	    private static Map map = new HashMap<>();

	    private DependabilityLevelVirtualNode(int value) {
	        this.value = value;
	    }

	    static {
	        for (DependabilityLevelVirtualNode pageType : DependabilityLevelVirtualNode.values()) {
	            map.put(pageType.value, pageType);
	        }
	    }

	    public static DependabilityLevelVirtualNode valueOf(int pageType) {
	        return (DependabilityLevelVirtualNode) map.get(pageType);
	    }

	    public int getValue() {
	        return value;
	    }
	}

	public enum CloudType{
		PUBLIC(1), PUBLICTRUSTED(2), PRIVATEDATACENTER1(3), PRIVATEDATACENTER2(4), PRIVATEDATACENTER3(5), PRIVATEDATACENTER4(6);
		
	    private int value;
	    private static Map map = new HashMap<>();

	    private CloudType(int value) {
	        this.value = value;
	    }

	    static {
	        for (CloudType pageType : CloudType.values()) {
	            map.put(pageType.value, pageType);
	        }
	    }

	    public static CloudType valueOf(int pageType) {
	        return (CloudType) map.get(pageType);
	    }

	    public int getValue() {
	        return value;
	    }
	}

}
