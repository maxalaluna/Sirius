package net.floodlightcontroller.sirius.console;

public interface Receiver {

	public boolean receive(Packet packet) throws Exception;
}
