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

import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SecureRandom;
import java.util.Random;

import javax.crypto.KeyGenerator;

/**
 * Generator of random data
 * @author Robin Carozzani
 */
public class Randgen {
	
	/**
	 * Generates a secret for AES encryption
	 * @return AES 128 bits secret key
	 * @throws NoSuchAlgorithmException
	 * @throws NoSuchProviderException
	 */
	public static Key generateKey() throws NoSuchAlgorithmException, NoSuchProviderException  {
		KeyGenerator generator = KeyGenerator.getInstance("AES", "BC");
		generator.init(128);
		return generator.generateKey();
	}
	
	/**
	 * Generates random salt for hashing
	 * @return Random salt
	 * @throws NoSuchAlgorithmException
	 */
	public static byte[] generateHashSalt() throws NoSuchAlgorithmException	{
		SecureRandom sr = SecureRandom.getInstance("SHA1PRNG");
		byte[] salt = new byte[16];
		sr.nextBytes(salt);
		return salt;
	}
	
	/**
	 * Generates random password, possibly containing numbers, letters (lower and upper case), special characters
	 * @param length Length of generated password
	 * @return Random clear text password
	 */
	public static String generateRandomPassword(int length) {
		final String[] symbols = {"0","1","2","3","4","5","6","7","8","9",
				"a","b","c","d","e","f","g","h","i","j","k","l","m","n","o","p","q","r","s","t","u","v","w","x","y","z",
				"A","B","C","D","E","F","G","H","I","J","K","L","M","N","O","P","Q","R","S","T","U","V","W","X","Y","Z",
				"@","&","$","#","%","*","?",":",";","!","§","-","_","=",".","/","<",">","(",")","¤"};
		String password = "";
		Random rand = new Random(System.currentTimeMillis());
		for (int i=0 ; i<length ; ++i) {
			int ind = rand.nextInt(symbols.length);
			password += symbols[ind];
		}
		return password;
	}
}
