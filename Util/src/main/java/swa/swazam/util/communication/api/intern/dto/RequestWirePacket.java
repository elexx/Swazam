package swa.swazam.util.communication.api.intern.dto;

public class RequestWirePacket extends WirePacket {
	private static final long serialVersionUID = 7415171648414577107L;

	private final String methodName;
	private final Object[] parameterList;

	RequestWirePacket(Integer id, String methodName, Object[] parameterList) {
		super(id);
		this.methodName = methodName;
		this.parameterList = parameterList;
	}

	public String getMethodName() {
		return methodName;
	}

	public Object[] getParameterList() {
		return parameterList;
	}
}
