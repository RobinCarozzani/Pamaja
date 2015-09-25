/*  This file is part of Pamaja.
*
*  Pamaja is free software: you can redistribute it and/or modify
*  it under the terms of the GNU General Public License as published by
*  the Free Software Foundation, either version 3 of the License, or
*  (at your option) any later version.
*
*  Pamaja is distributed in the hope that it will be useful,
*  but WITHOUT ANY WARRANTY; without even the implied warranty of
*  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
*  GNU General Public License for more details.
*
*  You should have received a copy of the GNU General Public License
*  along with Pamaja.  If not, see <http://www.gnu.org/licenses/>.
*/

package fr.robincarozzani.pamaja.crypto;

import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

import fr.robincarozzani.pamaja.utils.HexHandler;

/**
 * Object handling the hashing process
 * @author Robin Carozzani
 */
public class Hashor {
	
	private static final String ALGO = "PBKDF2WithHmacSHA1";
	private static final int ITER = 1000;
	
	private static Hashor instance = null;
	
	private Hashor() {
	}
	
	/**
	 * Gets the unique instance of Hashor
	 * @return Instance of Hashor
	 */
	public static Hashor getInstance() {
		if (instance == null) {
			instance = new Hashor();
		}
		return instance;
	}
	
	private byte[] doHash(char[] message, byte[] salt, int iterations, int keyLength)
			throws NoSuchAlgorithmException, InvalidKeySpecException {
		PBEKeySpec spec = new PBEKeySpec(message, salt, ITER, keyLength);
		SecretKeyFactory skf = SecretKeyFactory.getInstance(ALGO);
		return skf.generateSecret(spec).getEncoded();
	}
	
	/**
	 * Hashes data with a given salt and a fixed key length
	 * @param message Data to hash
	 * @param salt Salt
	 * @param length Key length
	 * @return Hashed data
	 * @throws NoSuchAlgorithmException
	 * @throws InvalidKeySpecException
	 */
	public Hash hash(String message, byte[] salt, int length)
			throws NoSuchAlgorithmException, InvalidKeySpecException {
		byte[] hashed = doHash(message.toCharArray(), HexHandler.fromHex(new String(salt)), ITER, length);
		return new Hash(salt, HexHandler.toHex(hashed).getBytes());
	}
	
	/**
	 * Checks if (single)hashed data is equal to (double)hashed data, data to test is hashed once more before checking
	 * @param toTest Data to test (single hash)
	 * @param storedPassword Reference data (double hash)
	 * @return <code>true</code> if tested data is equal to reference data
	 * @throws NoSuchAlgorithmException
	 * @throws InvalidKeySpecException
	 */
	public boolean checkHash(Hash toTest, Hash storedPassword)
			throws NoSuchAlgorithmException, InvalidKeySpecException {
		byte[] salt = storedPassword.getSalt();
		byte[] hash = storedPassword.getHashedMessage();
		String originalPassword = toTest.toString();
		byte[] testHash = HexHandler.toHex(doHash(originalPassword.toCharArray(), HexHandler.fromHex(new String(salt)), ITER, hash.length * 4)).getBytes();
		int diff = hash.length ^ testHash.length;
		for (int i=0 ; i<hash.length && i<testHash.length ; ++i) {
			if ((diff |= hash[i] ^ testHash[i]) != 0) {
				return false;
			}
		}
		return true;
	}
	
	/**
	 * Hashes a password, salt is random, key length is 64
	 * @param password The password to hash
	 * @return Hashed password
	 * @throws NoSuchAlgorithmException
	 * @throws InvalidKeySpecException
	 */
	public Hash generatePasswordHash(String password) throws NoSuchAlgorithmException, InvalidKeySpecException {
		byte[] salt = Randgen.generateHashSalt();
		byte[] hashed = doHash(password.toCharArray(), salt, ITER, 64);
		return new Hash(HexHandler.toHex(salt).getBytes(), HexHandler.toHex(hashed).getBytes());		
	}
}
