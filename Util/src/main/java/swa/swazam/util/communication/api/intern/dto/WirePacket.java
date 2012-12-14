package swa.swazam.util.communication.api.intern.dto;

import java.io.Serializable;

abstract class WirePacket implements Serializable {
	private static final long serialVersionUID = -8181197702007335647L;

	private final Integer id;

	public WirePacket(Integer id) {
		this.id = id;
	}

	public Integer getId() {
		return id;
	}
}
