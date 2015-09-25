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

import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.ShortBufferException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import fr.robincarozzani.pamaja.utils.HexHandler;

/**
 * Object handling encryption and decryption processes
 * @author Robin Carozzani
 */
public class Ciphor {
	
	private static final String ALGO = "AES";
	private static final String MODE = "CTR";
	private static final String PADD = "NoPadding";
	private static final String TRANSFO = ALGO+"/"+MODE+"/"+PADD;

	private static Ciphor instance = null;
	
	private Ciphor() {
	}

	/**
	 * Gets the unique instance of the Ciphor
	 * @return Instance of Ciphor
	 */
	public static Ciphor getInstance() {
		if (instance == null) {
			instance = new Ciphor();
		}
		return instance;
	}
	
	private Cipher getCipher() throws NoSuchAlgorithmException, NoSuchProviderException, NoSuchPaddingException  {
		return Cipher.getInstance(TRANSFO, "BC");
	}
	
	/**
	 * Encrypts a message with a secret key using the defined algorithm (AES/CTR/NoPadding)
	 * @param message The message to encrypt
	 * @param key The secret key to encrypt the message with
	 * @return The encrypted message
	 * @throws NoSuchPaddingException 
	 * @throws NoSuchProviderException 
	 * @throws NoSuchAlgorithmException 
	 * @throws InvalidKeyException 
	 * @throws ShortBufferException 
	 * @throws BadPaddingException 
	 * @throws IllegalBlockSizeException 
	 */
	public Ciph encrypt(byte[] message, byte[] key)
			throws NoSuchAlgorithmException, NoSuchProviderException, NoSuchPaddingException, InvalidKeyException,
				ShortBufferException, IllegalBlockSizeException, BadPaddingException  {
		Cipher cipher = getCipher();
		Key k = new SecretKeySpec(key, ALGO);
		cipher.init(Cipher.ENCRYPT_MODE, k);
		byte[] cipherMessage = new byte[cipher.getOutputSize(message.length)];
		int len = cipher.update(message, 0, message.length, cipherMessage, 0);
		cipher.doFinal(cipherMessage, len);
		return new Ciph(HexHandler.toHex(cipher.getIV()).getBytes(), HexHandler.toHex(cipherMessage).getBytes());
	}

	/**
	 * Decrypts a message with a secret key using the defined algorithm (AES/CTR/NoPadding)
	 * @param cipherMessage The encrypted message
	 * @param key The secret key to decrypt the message with
	 * @return The clear message
	 * @throws NoSuchAlgorithmException
	 * @throws NoSuchProviderException
	 * @throws NoSuchPaddingException
	 * @throws InvalidKeyException
	 * @throws InvalidAlgorithmParameterException
	 * @throws ShortBufferException
	 * @throws IllegalBlockSizeException
	 * @throws BadPaddingException
	 */
	public byte[] decrypt(Ciph cipherMessage, byte[] key)
			throws NoSuchAlgorithmException, NoSuchProviderException, NoSuchPaddingException, InvalidKeyException,
				InvalidAlgorithmParameterException, ShortBufferException, IllegalBlockSizeException, BadPaddingException {
		byte[] cipherMsg = HexHandler.fromHex(new String(cipherMessage.getEnc()));
		Cipher cipher = getCipher();
		Key k = new SecretKeySpec(key, ALGO);
		cipher.init(Cipher.DECRYPT_MODE, k, new IvParameterSpec(HexHandler.fromHex(new String(cipherMessage.getIV()))));
		byte[] plainMessage = new byte[cipher.getOutputSize(cipherMsg.length)];
		int len = cipher.update(cipherMsg, 0, cipherMsg.length, plainMessage, 0);
		cipher.doFinal(plainMessage, len);
		return plainMessage;
	}
}
