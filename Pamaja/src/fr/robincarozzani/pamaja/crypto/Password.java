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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

/**
 * Password object representation
 * @author Robin Carozzani
 * @TODO Forbid empty alphabet
 * @TODO Possibility to exclude specific characters
 */
public class Password {
	
	private static final String[] UPPERCHARS = {"A","B","C","D","E","F","G","H","I","J","K","L","M","N","O","P","Q","R","S","T","U","V","W","X","Y","Z"};
	private static final String[] LOWERCHARS = {"a","b","c","d","e","f","g","h","i","j","k","l","m","n","o","p","q","r","s","t","u","v","w","x","y","z"};
	private static final String[] NUMERIC = {"0","1","2","3","4","5","6","7","8","9"};
	private static final String[] SPECIAL = {"@","&","$","#","%","*","?",":",";","!","§","-","_","=",".","/","<",">","(",")","¤"};
	
	private int _minLength;
	private int _maxLength;
	private int _length;
	
	private boolean _withUpperChars;
	private boolean _withLowerChars;
	private boolean _withNumeric;
	private boolean _withSpecials;
	
	private String _pwd;
	
	/**
	 * Creates an instance of Password object
	 * @param minLength Minimum number of symbols in the password
	 * @param maxLength Maximum number of symbols in the password
	 * @param useUpperLetter Whether the password can contain uppercase letters
	 * @param useLowerLetter Whether the password can contain lowercase letter
	 * @param useNumbers Whether the password can contain numbers
	 * @param useSpecialChars Whether the password can special characters
	 */
	public Password(int minLength, int maxLength, boolean useUpperLetter, boolean useLowerLetter, boolean useNumbers, boolean useSpecialChars) {
		_minLength = minLength;
		_maxLength = maxLength;
		computeLength();
		_withUpperChars = useUpperLetter;
		_withLowerChars = useLowerLetter;
		_withNumeric = useNumbers;
		_withSpecials = useSpecialChars;
		_pwd = null;
	}

	private void computeLength() {
		Random random = new Random(System.currentTimeMillis());
		if (_maxLength == _minLength) {
			_length = _maxLength;
		} else {
			_length = random.nextInt(_maxLength - _minLength) + _minLength;
		}
	}
	
	private void createPwd() {
		List<String> alphabet = new ArrayList<>();
		if (_withUpperChars) {
			alphabet.addAll(Arrays.asList(UPPERCHARS));
		}
		if (_withLowerChars) {
			alphabet.addAll(Arrays.asList(LOWERCHARS));
		}
		if (_withNumeric) {
			alphabet.addAll(Arrays.asList(NUMERIC));
		}
		if (_withSpecials) {
			alphabet.addAll(Arrays.asList(SPECIAL));
		}
		_pwd = Randgen.generateRandomString(_length, alphabet.toArray(new String[alphabet.size()]));
	}
	
	/**
	 * Gives the randomly-generated password
	 * @return Clear-text random password
	 */
	public String getPwd() {
		if (_pwd == null) {
			createPwd();
		}
		return _pwd;
	}
}