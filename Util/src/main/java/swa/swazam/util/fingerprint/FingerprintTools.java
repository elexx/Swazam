package swa.swazam.util.fingerprint;

import java.io.IOException;
import javax.sound.sampled.AudioInputStream;
import swa.swazam.util.exceptions.SwazamException;
import ac.at.tuwien.infosys.swa.audio.Fingerprint;
import ac.at.tuwien.infosys.swa.audio.FingerprintSystem;

public class FingerprintTools {
	
	/**
	 * Generates a fingerprint for the given song or song snippet.
	 * @param sample a song or song snippet
	 * @return the generated fingerprint
	 * @throws SwazamException in case the fingerprint generation failed
	 */
	public Fingerprint generate(AudioInputStream sample) throws SwazamException {
		try {
			return FingerprintSystem.fingerprint(sample);
		} catch (IOException e) {
			throw new SwazamException(e);
		}
	}

	/**
	 * Checks if the given snippet is part of the given song.
	 * @param song 
	 * @param snippet
	 * @return true, if the snippet is part of the given song, else false
	 */
	public boolean matches(Fingerprint song, Fingerprint snippet){
		return song.match(snippet) >= 0;
	}
}
