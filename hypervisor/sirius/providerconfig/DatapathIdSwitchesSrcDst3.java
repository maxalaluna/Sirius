package net.floodlightcontroller.sirius.providerconfig;

import org.projectfloodlight.openflow.types.DatapathId;

public class DatapathIdSwitchesSrcDst3 {
	
	protected DatapathId datapathIdSwitchSrc;
	protected DatapathId datapathIdSwitchDst;
	
	public DatapathIdSwitchesSrcDst3(DatapathId datapathIdSwitchSrc,
			DatapathId datapathIdSwitchDst) {
		super();
		this.datapathIdSwitchSrc = datapathIdSwitchSrc;
		this.datapathIdSwitchDst = datapathIdSwitchDst;

	}

	public DatapathId getDatapathIdSwitchSrc() {
		return datapathIdSwitchSrc;
	}

	public void setDatapathIdSwitchSrc(DatapathId datapathIdSwitchSrc) {
		this.datapathIdSwitchSrc = datapathIdSwitchSrc;
	}


	public DatapathId getDatapathIdSwitchDst() {
		return datapathIdSwitchDst;
	}


	public void setDatapathIdSwitchDst(DatapathId datapathIdSwitchDst) {
		this.datapathIdSwitchDst = datapathIdSwitchDst;
	}

	public boolean equals(Object o) {
		return (o instanceof DatapathIdSwitchesSrcDst3) && 
				(datapathIdSwitchDst.equals(((DatapathIdSwitchesSrcDst3) o).datapathIdSwitchDst)) && 
				(datapathIdSwitchSrc.equals(((DatapathIdSwitchesSrcDst3) o).datapathIdSwitchSrc));
	}

	public int hashCode() {
		return datapathIdSwitchSrc.hashCode() ^ datapathIdSwitchDst.hashCode();
	}
}
