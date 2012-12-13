package swa.swazam.util.communication.api;

class ResponseWirePacket extends WirePacket {
	private static final long serialVersionUID = -2843909496046085262L;

	private final Object returnValue;

	public ResponseWirePacket(Integer id, Object returnValue) {
		super(id);
		this.returnValue = returnValue;
	}

	public Object getReturnValue() {
		return returnValue;
	}
}
