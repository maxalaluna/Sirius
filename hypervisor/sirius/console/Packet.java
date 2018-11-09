package net.floodlightcontroller.sirius.console;

public class Packet {

	public enum Type {
		
		NONE(0), PHYSICAL_REQUEST(1), PHYSICAL_SUCCESS(2),
		PHYSICAL_FAILURE(3), VIRTUAL_REQUEST(4), VIRTUAL_SUCCESS(5), 
		VIRTUAL_FAILURE(6), PHYSICAL_UPDATE(7);
		
		private final int value;
		
		private Type(int value) {
			this.value = value;
		}
		
		public int getValue() {
			return value;
		}
		
		public static Type fromValue(int value) {
			Type[] values = Type.values();
			for (int k = 0; k < values.length; k++)
				if (values[k].value == value)
					return values[k];
			return NONE;
		}
	};
	
	private Type type;
	private byte[] data;
	
	public Packet(Type type, byte[] data) {
		this.type = type;
		this.data = data;
	}

	public Type getType() {
		return type;
	}

	public byte[] getData() {
		return data;
	}
}
