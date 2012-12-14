package swa.swazam.util.communication.api;

import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import swa.swazam.util.communication.Client2Server;
import swa.swazam.util.communication.api.intern.dto.NetPacketFactory;
import swa.swazam.util.communication.api.intern.dto.RequestWirePacket;
import swa.swazam.util.communication.api.intern.dto.ResponseWirePacket;
import swa.swazam.util.dto.CredentialsDTO;
import swa.swazam.util.dto.MessageDTO;
import swa.swazam.util.exceptions.SwazamException;

class Client2ServerStub implements Client2Server, Notifiable {

	private final Map<Integer, ResponseWirePacket> responses = new HashMap<>();

	private final Lock lock = new ReentrantLock();
	private final Map<Integer, Condition> locks = new ConcurrentHashMap<>();
	private final WriteChannel channel;

	public Client2ServerStub(WriteChannel channel) {
		this.channel = channel;
	}

	@Override
	public boolean verifyCredentials(CredentialsDTO user) throws SwazamException {
		RequestWirePacket packet = NetPacketFactory.createRequestWirePacket("verifyCredentials", user);
		return callRemoteMethode(packet);
	}

	@Override
	public List<InetSocketAddress> getPeerList() throws SwazamException {
		RequestWirePacket packet = NetPacketFactory.createRequestWirePacket("getPeerList");
		return callRemoteMethode(packet);
	}

	@Override
	public void logRequest(CredentialsDTO user, MessageDTO message) throws SwazamException {
		RequestWirePacket packet = NetPacketFactory.createRequestWirePacket("logRequest", user, message);
		callRemoteMethode(packet);
	}

	@Override
	public boolean hasCoins(CredentialsDTO user) throws SwazamException {
		RequestWirePacket packet = NetPacketFactory.createRequestWirePacket("hasCoins", user);
		return callRemoteMethode(packet);
	}

	@SuppressWarnings("unchecked")
	private <T> T callRemoteMethode(RequestWirePacket packet) {
		Integer id = packet.getId();

		Condition condition = lock.newCondition();
		locks.put(id, condition);
		try {
			lock.lock();

			responses.put(id, null);

			channel.write(packet);
			do {
				condition.awaitUninterruptibly();
			} while (responses.get(id) == null);

		} finally {
			lock.unlock();
		}

		return (T) responses.get(id).getReturnValue();
	}

	@Override
	public void notifyId(int id, ResponseWirePacket packet) {
		lock.lock();
		try {
			responses.put(id, packet);
			locks.get(id).signal();
		} finally {
			lock.unlock();
		}
	}

}
