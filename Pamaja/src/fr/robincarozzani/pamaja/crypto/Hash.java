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
 * Object representation of hashed data
 * @author Robin Carozzani
 */
public class Hash {
	
	private static final String SEPARATOR = ";";
	
	private byte[] _salt;
	private byte[] _hash;

	/**
	 * Constructs a Hash object
	 * @param salt Salt used when hashing
	 * @param hashedMessage Hashed data with the given salt
	 */
	public Hash(byte[] salt, byte[] hashedMessage) {
		_salt = salt;
		_hash = hashedMessage;
	}
	
	/**
	 * Gets the salt used when hashing
	 * @return Salt
	 */
	public byte[] getSalt() {
		return _salt;
	}
	
	/**
	 * Gets the hashed data
	 * @return Hashed data
	 */
	public byte[] getHashedMessage() {
		return _hash;
	}
	
	/**
	 * Gets the length of hashed data
	 * @return Length of hashed data
	 */
	public int length() {
		return _hash.length;
	}
	
	/**
	 * String representation of Hash, salt;hash
	 */
	@Override
	public String toString() {
		return (new String(getSalt()) + SEPARATOR + new String(getHashedMessage()));
	}
}
