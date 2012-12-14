package swa.swazam.util.communication.api.intern.dto;

public class ResponseWirePacket extends WirePacket {
	private static final long serialVersionUID = -2843909496046085262L;

	private final Object returnValue;

	ResponseWirePacket(Integer id, Object returnValue) {
		super(id);
		this.returnValue = returnValue;
	}

	public Object getReturnValue() {
		return returnValue;
	}
}
