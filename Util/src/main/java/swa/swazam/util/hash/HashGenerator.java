package swa.swazam.util.hash;

public class HashGenerator {

	/**
	 * are use for Blowfish's salt generation
	 */
	static final int ROUNDS = 10;

	/**
	 * Constant salt for design-failure workaround (see non-javadoc comment)
	 */
	static final String CONST_SALT = "$2a$10$PQFZE0XQ9JqXDGgjvNub4u";

	/*
	 * It was a design error to use variable salting in an environment where a
	 * comparison between a hash and a hash is needed, since no two hashes will
	 * be the same (with very high probability, at least). A quick-and-dirty
	 * fix for this is using a constant salt. This is dirty, a better solution
	 * would be to use no salt at all. Of course, this is very unsecure, since
	 * knowing the hash (which is transmitted in clear-text) means accessing the
	 * system. A better approach would be to use a key agreement algorithm such
	 * as Diffie-Hellman or similar, however, this would probably be an overkill
	 * for a course where security is not an issue.
	 * 
	 * The current solution is exactly as secure as using a non-salt-hashing,
	 * which in turn is practically exactly as secure as transmitting the password
	 * in plain text. 
	 */

	/**
	 * Generates the hash value of the given plain text
	 * 
	 * @param password
	 *            the text that should be hashed
	 * @return the hashed text
	 */
	public static String hash(String password) {
		return BCrypt.hashpw(password, CONST_SALT);
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
