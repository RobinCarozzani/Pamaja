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

/**
 * Object containing two objects
 * @author Robin Carozzani
 */
public class Pair<K, V> {
	
	private K _first;
	private V _second;
	
	/**
	 * Constructs a Pair
	 * @param first The first object to be contained in the Pair
	 * @param second The second object to be contained in the Pair
	 */
	public Pair(K first, V second) {
		_first = first;
		_second = second;
	}
	
	/**
	 * Gets the first object of the Pair
	 * @return First object of the Pair
	 */
	public K first() {
		return _first;
	}
	
	/**
	 * Gets the second object of the Pair
	 * @return Second object of the Pair
	 */
	public V second() {
		return _second;
	}
}
