package swa.swazam.util.hash;

public class HashGenerator {

	/**
	 * are use for Blowfish's salt generation
	 */
	static final int ROUNDS = 10;

	/**
	 * Generates the hash value of the given plain text
	 * 
	 * @param password the text that should be hashed
	 * @return the hashed text
	 */
	public static String hash(String password) {
		return BCrypt.hashpw(password, BCrypt.gensalt(ROUNDS));
	}

	/**
	 * Checks whether the paintext matches the given hashed password or not
	 * 
	 * @param plaintext
	 * @param hashed
	 * @return
	 */
	public static boolean checkPassword(String plaintext, String hashed) {
		return BCrypt.checkpw(plaintext, hashed);
	}
}
