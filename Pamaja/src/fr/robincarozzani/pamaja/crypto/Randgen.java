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
	 * Generates random String, containing any symbol from the given list
	 * @param length Length (number of symbols) of generated String
	 * @param symbols Characters to construct the String from
	 * @return Random String
	 */
	public static String generateRandomString(int length, String[] symbols) {
		String randString = "";
		Random rand = new Random(System.currentTimeMillis());
		for (int i=0 ; i<length ; ++i) {
			int ind = rand.nextInt(symbols.length);
			randString += symbols[ind];
		}
		return randString;
	}
}
