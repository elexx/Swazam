package swa.swazam.util.communication.api;

import java.util.concurrent.atomic.AtomicInteger;

class NetPacketFactory {

	private static final AtomicInteger currentId = new AtomicInteger();

	public static RequestWirePacket createRequestWirePacket(String methodName, Object... parameter) {
		return new RequestWirePacket(currentId.getAndIncrement(), methodName, parameter);
	}

	public static ResponseWirePacket createResponseWirePacket(RequestWirePacket request, Object returnValue) {
		return new ResponseWirePacket(request.getId(), returnValue);
	}
}
