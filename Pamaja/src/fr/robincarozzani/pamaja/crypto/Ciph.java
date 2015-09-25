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

/**
 * Object representation of ciphered data
 * @author Robin Carozzani
 */
public class Ciph {
	
	private byte[] _iv;
	private byte[] _enc;
	
	/**
	 * Constructs a Ciph object
	 * @param iv Initial Vector
	 * @param enc Encrypted data
	 */
	public Ciph(byte[] iv, byte[] enc) {
		_iv = iv;
		_enc = enc;
	}

	/**
	 * Gets the initial vector used when encrypting
	 * @return Initial vector
	 */
	public byte[] getIV() {
		return _iv;
	}

	/**
	 * Gets the result of encryption
	 * @return Encrypted data
	 */
	public byte[] getEnc() {
		return _enc;
	}

}
