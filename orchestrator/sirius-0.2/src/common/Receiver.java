package common;

public interface Receiver {

	public boolean receive(Packet packet) throws Exception;
}
