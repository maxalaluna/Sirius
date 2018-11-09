package common;

public class ServerError extends Exception {

	private static final long serialVersionUID = 1L;
	
	public ServerError(String message) {
		super(message);
	}
}
