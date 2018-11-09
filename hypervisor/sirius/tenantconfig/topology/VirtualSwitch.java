package net.floodlightcontroller.sirius.tenantconfig.topology;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;
import net.floodlightcontroller.sirius.providerconfig.Cloud;
import net.floodlightcontroller.sirius.providerconfig.OpenVSwitch3;
import net.floodlightcontroller.sirius.providerconfig.Server3;
import net.floodlightcontroller.sirius.topology.FlowTable;
import net.floodlightcontroller.sirius.util.Enum.CloudType;
import net.floodlightcontroller.sirius.util.Enum.DependabilityLevelVirtualNode;
import net.floodlightcontroller.sirius.util.Enum.SecurityLevelVirtualNode;

public class VirtualSwitch {
	
	private int virtualSwitchId;
	private static final AtomicInteger count = new AtomicInteger(0);
	private int cpu;
	private int flowSize;
	private int MAX_FLOW_SIZE = 0;
	private OpenVSwitch3 openVSwitchWorking;
	private OpenVSwitch3 openVSwitchBackup;
	private Server3 workingSwitch;
	private Server3 backupSwitch;
	private ArrayList<FlowTable> flowTable = new ArrayList<FlowTable>();
	private SecurityLevelVirtualNode securityLevel;
	private DependabilityLevelVirtualNode dependabilityLevel;
	private CloudType cloudType;
	private Cloud cloud;
	private int tenantId;
	private String name;
	private int memory;	
	private int x;
	private int y;
	private boolean transit;

	
	public VirtualSwitch(int cpu, int flowSize, int mAX_FLOW_SIZE, OpenVSwitch3 openVSwitchWorking,
			ArrayList<FlowTable> flowTable, SecurityLevelVirtualNode securityLevel, 
			DependabilityLevelVirtualNode dependabilityLevel, Cloud cloud, CloudType cloudType) {
		super();
		this.virtualSwitchId = count.incrementAndGet();
		this.cpu = cpu;
		this.flowSize = flowSize;
		MAX_FLOW_SIZE = mAX_FLOW_SIZE;
		this.openVSwitchWorking = openVSwitchWorking;
		this.flowTable = flowTable;
		this.securityLevel = securityLevel;
		this.dependabilityLevel = dependabilityLevel;
		this.cloud = cloud;
		this.cloudType = cloudType;
	}

	public VirtualSwitch(int cpu, int flowSize, int mAX_FLOW_SIZE, CloudType cloudType) {
		super();
		this.virtualSwitchId = count.incrementAndGet();
		this.cpu = cpu;
		this.flowSize = flowSize;
		this.MAX_FLOW_SIZE = mAX_FLOW_SIZE;
		this.cloudType = cloudType;
	}
	
	public VirtualSwitch(int virtualSwitchId, int cpu, SecurityLevelVirtualNode securityLevel, CloudType cloudType, DependabilityLevelVirtualNode dependabilityLevel, int mAX_FLOW_SIZE, int tenantId) {
		super();
		this.virtualSwitchId = virtualSwitchId;
		this.cpu = cpu;
		this.securityLevel = securityLevel;
		this.cloudType = cloudType;
		this.dependabilityLevel = dependabilityLevel;
		this.MAX_FLOW_SIZE = mAX_FLOW_SIZE;
		this.tenantId = tenantId;
	}
	
	public VirtualSwitch(int virtualSwitchId, String name, int cpu, SecurityLevelVirtualNode securityLevel, CloudType cloudType, DependabilityLevelVirtualNode dependabilityLevel, int mAX_FLOW_SIZE, int tenantId) {
		super();
		this.virtualSwitchId = virtualSwitchId;
		this.cpu = cpu;
		this.securityLevel = securityLevel;
		this.cloudType = cloudType;
		this.dependabilityLevel = dependabilityLevel;
		this.MAX_FLOW_SIZE = mAX_FLOW_SIZE;
		this.tenantId = tenantId;
		this.name = name;
	}
	
	public VirtualSwitch(int virtualSwitchId, String name, int cpu, SecurityLevelVirtualNode securityLevel, CloudType cloudType, DependabilityLevelVirtualNode dependabilityLevel, 
			int mAX_FLOW_SIZE, int tenantId, int memory, boolean transit) {
		super();
		this.virtualSwitchId = virtualSwitchId;
		this.cpu = cpu;
		this.securityLevel = securityLevel;
		this.cloudType = cloudType;
		this.dependabilityLevel = dependabilityLevel;
		this.MAX_FLOW_SIZE = mAX_FLOW_SIZE;
		this.tenantId = tenantId;
		this.name = name;
		this.setMemory(memory);
		this.setTransit(transit);
	}

	
	public int getX() {
		return x;
	}

	public void setX(int x) {
		this.x = x;
	}

	public int getY() {
		return y;
	}

	public void setY(int y) {
		this.y = y;
	}

	public Server3 getWorkingSwitch() {
		return workingSwitch;
	}

	public void setWorkingSwitch(Server3 workingSwitch) {
		this.workingSwitch = workingSwitch;
	}

	public Server3 getBackupSwitch() {
		return backupSwitch;
	}


	public void setBackupSwitch(Server3 backupSwitch) {
		this.backupSwitch = backupSwitch;
	}


	public OpenVSwitch3 getOpenVSwitchWorking() {
		return openVSwitchWorking;
	}


	public void setOpenVSwitchWorking(OpenVSwitch3 openVSwitchWorking) {
		this.openVSwitchWorking = openVSwitchWorking;
	}

	public void setVirtualSwitchId(int virtualSwitchId) {
		this.virtualSwitchId = virtualSwitchId;
	}

	public int getVirtualSwitchId() {
		return virtualSwitchId;
	}
	
	public int getCpu() {
		return cpu;
	}

	public void setCpu(int cpu) {
		this.cpu = cpu;
	}

	public int getFlowSize() {
		return flowSize;
	}

	public void setFlowSize(int flowSize) {
		this.flowSize = flowSize;
	}

	public int getMAX_FLOW_SIZE() {
		return MAX_FLOW_SIZE;
	}


	public void setMAX_FLOW_SIZE(int mAX_FLOW_SIZE) {
		MAX_FLOW_SIZE = mAX_FLOW_SIZE;
	}

	public OpenVSwitch3 getOpenVSwitch() {
		return openVSwitchWorking;
	}

	public void setOpenVSwitch(OpenVSwitch3 openVSwitchWorking) {
		this.openVSwitchWorking = openVSwitchWorking;
	}

	public ArrayList<FlowTable> getFlowTable() {
		return flowTable;
	}

	public void setFlowTable(ArrayList<FlowTable> flowTable) {
		this.flowTable = flowTable;
	}

	public SecurityLevelVirtualNode getSecurityLevel() {
		return securityLevel;
	}

	public void setSecurityLevel(SecurityLevelVirtualNode securityLevel) {
		this.securityLevel = securityLevel;
	}

	public Cloud getCloud() {
		return cloud;
	}

	public void setCloud(Cloud cloud) {
		this.cloud = cloud;
	}

	public DependabilityLevelVirtualNode getDependabilityLevel() {
		return dependabilityLevel;
	}

	public void setDependabilityLevel(DependabilityLevelVirtualNode dependabilityLevel) {
		this.dependabilityLevel = dependabilityLevel;
	}
	
	@Override
	public int hashCode() {
		
		return this.openVSwitchWorking==null?1:this.openVSwitchWorking.getDatapathId().hashCode() ^ (this.flowTable==null?1:this.flowTable.hashCode()) ^
				(this.cloud==null?1:this.cloud.hashCode()) ^	this.flowSize ^ this.virtualSwitchId;
	}

	@Override
	public boolean equals(Object o) {

		return (o instanceof VirtualSwitch) && 
				(flowSize ==((VirtualSwitch) o).flowSize) &&
				(openVSwitchWorking==null?true:(openVSwitchWorking.getDatapathId().equals(((VirtualSwitch) o).getOpenVSwitch().getDatapathId()))) &&
				(flowTable==null?true:(flowTable.equals(((VirtualSwitch) o).flowTable))) &&
				(cloud==null?true:(cloud.equals(((VirtualSwitch) o).cloud))) &&
				(virtualSwitchId ==((VirtualSwitch) o).virtualSwitchId);

		
	}

	public CloudType getCloudType() {
		return cloudType;
	}

	public void setCloudType(CloudType cloudType) {
		this.cloudType = cloudType;
	}

	public OpenVSwitch3 getOpenVSwitchBackup() {
		return openVSwitchBackup;
	}

	public void setOpenVSwitchBackup(OpenVSwitch3 openVSwitchBackup) {
		this.openVSwitchBackup = openVSwitchBackup;
	}

	public int getTenantId() {
		return tenantId;
	}

	public void setTenantId(int tenantId) {
		this.tenantId = tenantId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getMemory() {
		return memory;
	}

	public void setMemory(int memory) {
		this.memory = memory;
	}

	public boolean isTransit() {
		return transit;
	}

	public void setTransit(boolean transit) {
		this.transit = transit;
	}
}
