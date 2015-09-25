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

package fr.robincarozzani.pamaja.utils;

import java.math.BigInteger;
import java.security.NoSuchAlgorithmException;

/**
 * Object handling hexadecimal conversions
 * @author Robin Carozzani
 */
public class HexHandler {
	
	/**
	 * Converts a byte array to its hexadecimal representation
	 * @param array Data to convert 
	 * @return The hexadecimal representation of the data
	 * @throws NoSuchAlgorithmException
	 */
	public static String toHex(byte[] array) throws NoSuchAlgorithmException {
		BigInteger bi = new BigInteger(1, array);
		String hex = bi.toString(16);
		int paddingLength = (array.length * 2) - hex.length();
		if (paddingLength > 0) {
			return String.format("%0"+paddingLength+"d", 0) + hex;
		} else {
			return hex;
		}
	}
	
	/**
	 * Converts a hexadecimal representation of data to its original representation
	 * @param hex Hexadecimal data to convert
	 * @return Original data
	 * @throws NoSuchAlgorithmException
	 */
	public static byte[] fromHex(String hex) throws NoSuchAlgorithmException {
		byte[] bytes = new byte[hex.length()/2];
		for(int i=0 ; i<bytes.length ; ++i) {
			bytes[i] = (byte)Integer.parseInt(hex.substring(2*i, 2*i+2), 16);
		}
		return bytes;
	}
}
