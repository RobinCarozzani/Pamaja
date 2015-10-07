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

package fr.robincarozzani.pamaja;

import java.io.Console;
import java.security.Security;
import java.util.Map;

import org.bouncycastle.jce.provider.BouncyCastleProvider;

import fr.robincarozzani.pamaja.crypto.Ciph;
import fr.robincarozzani.pamaja.crypto.Ciphor;
import fr.robincarozzani.pamaja.crypto.Hash;
import fr.robincarozzani.pamaja.crypto.Hashor;
import fr.robincarozzani.pamaja.crypto.Randgen;
import fr.robincarozzani.pamaja.db.DBHandler;
import fr.robincarozzani.pamaja.utils.Pair;

/**
 * Main class of Pamaja
 * @author Robin Carozzani
 */
public class Launcher {
	
	/**
	 * Program version
	 */
	public static final String PROG_VERSION = "0.1.1";
	
	private static byte[] masterKey;
	private static Hash storedPwd;
	private static byte[] origSalt;
	
	private static Console cons;
	
	private static final int PWDLEN = 17;

	public static void main(String[] args) {
		
		try {			
			init();
			mainJob();
			quit();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private static void init() throws Exception {
		printProgramTitle(30);
		System.out.print("\nInitialize instances... ");
		Security.addProvider(new BouncyCastleProvider());
		Hashor h = Hashor.getInstance();
		Ciphor c = Ciphor.getInstance();
		DBHandler dbh = DBHandler.getInstance();
		cons = System.console();
		System.out.println("done");
		
		System.out.print("Opening DB... ");
		dbh.connect();
		System.out.println("done");
		
		System.out.print("Checking DB... ");
		boolean dbOk = dbh.dbVersionIsOk();
		System.out.println("done");
		if (!dbOk) {
			System.out.println("Wrong version of database");
			quit();
		}
		
		if (!dbh.isInit()) {
			System.out.println("No master password is set");
			System.out.println("Type a master password (and remember it) ");
			String clearMasterPassword = cons.readLine();
			
			System.out.print("Hashing password... ");
			Hash hashedPassword = h.generatePasswordHash(clearMasterPassword);
			origSalt = hashedPassword.getSalt();
			storedPwd = h.generatePasswordHash(hashedPassword.toString());
			System.out.println("done");
			
			System.out.print("Generating encryption key... ");
			masterKey = Randgen.generateKey().getEncoded();
			Ciph storedKey = c.encrypt(masterKey, hashedPassword.getHashedMessage());
			System.out.println("done");
			
			System.out.print("Updating DB... ");
			dbh.insertMasterPassword(storedPwd, origSalt);
			dbh.insertMasterKey(storedKey);
			dbh.setInit();
			System.out.println("done");
			
		} else {
			System.out.print("Checking stored password... ");
			Pair<Byte[], Hash> storedPwdAndSalt = dbh.getStoredPasswordAndSalt();
			storedPwd = storedPwdAndSalt.second();
			Byte[] oSalt = storedPwdAndSalt.first();
			origSalt = new byte[oSalt.length];
			int i = 0;
			for (Byte b : oSalt) {
				origSalt[i++] = b.byteValue();
			}
			System.out.println("done");
			System.out.println("Type your master password");
			String clearInPassword = new String(cons.readPassword());
			System.out.print("Checking master password... ");
			Hash hashedIn = h.hash(clearInPassword, origSalt, 64);
			clearInPassword = null;
			boolean matched = h.checkHash(hashedIn, storedPwd);
			System.out.println("done");
			if (!matched) {
				System.out.println("Wrong master password");
				quit();
			}
			System.out.println("Master password ok");
			System.out.print("Getting encryption key... ");
			Ciph storedKey = dbh.getStoredKey();
			if (storedKey != null) {
				masterKey = c.decrypt(storedKey, hashedIn.getHashedMessage());
				System.out.println("done");
			} else {
				System.out.println("failed");
				quit();
			}
		}
	}
	
	private static void printProgramTitle(int width) {
		for (int i=0 ; i<width+4 ; ++i) {
			System.out.print("*");
		}
		System.out.print("\n**");
		String txt = "PAMAJA";
		int nbSpaces = (width-txt.length())/2;
		for (int i=0 ; i<nbSpaces ; ++i) {
			System.out.print(" ");
		}
		System.out.print(txt);
		for (int i=0 ; i<width-txt.length()-nbSpaces ; ++i) {
			System.out.print(" ");
		}
		System.out.println("**");
		System.out.print("**");
		txt = "v"+PROG_VERSION;
		nbSpaces = (width-txt.length())/2;
		for (int i=0 ; i<nbSpaces ; ++i) {
			System.out.print(" ");
		}
		System.out.print(txt);
		for (int i=0 ; i<width-txt.length()-nbSpaces ; ++i) {
			System.out.print(" ");
		}
		System.out.println("**");
		for (int i=0 ; i<width+4 ; ++i) {
			System.out.print("*");
		}
		System.out.println();
	}
	
	private static void mainJob() throws Exception {
		char choice = ' ';
		do {
			System.out.println("\nMenu:");
			System.out.println("\t (A) Generate and save a new password");
			System.out.println("\t (B) Get a password");
			System.out.println("\t (C) Change a password");
			System.out.println("\t (Q) Quit");
			System.out.println("Type the letter corresponding to your choice");
			String check = cons.readLine();
			if (check.equals("")) {
				choice = ' ';
			} else {
				choice = check.toCharArray()[0];
			}
			System.out.println();
			switch (Character.toUpperCase(choice)) {
			case 'A':
				System.out.println("Type the name of the service (website, game...) (empty to cancel)");
				String service = cons.readLine();
				if (!service.equals("")) {
					System.out.println("Type your login (empty to cancel)");
					String login = cons.readLine();
					if (!login.equals("")) {
						System.out.print("\nEncrypting login... ");
						Ciph ciphLoginA = Ciphor.getInstance().encrypt(login.getBytes(), masterKey);
						System.out.println("done");
						System.out.print("Generating a password... ");
						String pwd = Randgen.generateRandomPassword(PWDLEN);
						Ciph ciphPwdA = Ciphor.getInstance().encrypt(pwd.getBytes(), masterKey);
						System.out.println("done");
						System.out.print("Updating DB... ");
						DBHandler.getInstance().insertPassword(service, ciphLoginA, ciphPwdA);
						System.out.println("done");
						System.out.println("\nPassword for "+service+" is:");
						System.out.println("\tlogin: "+login);
						System.out.println("\tpassword: "+pwd);
					}
				}
				break;
			case 'B':
				Pair<Integer, String> serviceB = scanService();
				if (serviceB != null) {
					Pair<Integer, String> loginB = scanLogin(serviceB.first().intValue());
					if (loginB != null) {
						System.out.print("Getting password... ");
						Ciph ciphPwdB = DBHandler.getInstance().getPassword(serviceB.first().intValue(), loginB.first().intValue());
						String clPwdB = new String(Ciphor.getInstance().decrypt(ciphPwdB, masterKey));
						System.out.println("done");
						System.out.println("\nPassword for "+serviceB.second()+" is:");
						System.out.println("\tlogin: "+loginB.second());
						System.out.println("\tpassword: "+clPwdB);
					}
				}
				break;
			case 'C':
				Pair<Integer, String> serviceC = scanService();
				if (serviceC != null) {
					Pair<Integer, String> loginC = scanLogin(serviceC.first().intValue());
					if (loginC != null) {
						System.out.print("Generating a password... ");
						String pwdc = Randgen.generateRandomPassword(PWDLEN);
						Ciph ciphPwdC = Ciphor.getInstance().encrypt(pwdc.getBytes(), masterKey);
						System.out.println("done");
						System.out.print("Updating DB... ");
						DBHandler.getInstance().updatePassword(serviceC.first().intValue(), loginC.first().intValue(), ciphPwdC);
						System.out.println("done");
						System.out.println("\nNew password for "+serviceC.second()+" is:");
						System.out.println("\tlogin: "+loginC.second());
						System.out.println("\tpassword: "+pwdc);
					}
				}
				break;
			case 'Q':
				System.out.println("Quit");
				break;
			default:
				System.out.println("Unknown function");
				break;
			}
		} while (Character.toUpperCase(choice) != 'Q');
	}
	
	private static Pair<Integer, String> scanService() {
		int serviceId = -2;
		Map<Integer, String> services = DBHandler.getInstance().getServices();
		do {
			System.out.println("Select a service");
			for (int id : services.keySet()) {
				System.out.println("\t("+id+") "+services.get(id));
			}
			System.out.println("Type the number of the wanted service (-1 to cancel)");
			try {
				serviceId = Integer.parseInt(cons.readLine());
			} catch (NumberFormatException e) {
				serviceId = -2;
			}
			System.out.println();
		} while ((serviceId != -1) && (!services.containsKey(serviceId)));
		if (serviceId != -1) {
			return new Pair<Integer, String>(Integer.valueOf(serviceId), services.get(serviceId));
		}
		return null;
	}
	
	private static Pair<Integer, String> scanLogin(int serviceId) throws Exception {
		int loginId = -2;
		Map<Integer, Ciph> logins = DBHandler.getInstance().getLogins(serviceId);
		do {
			System.out.println("Select a login");
			for (int id : logins.keySet()) {
				System.out.println("\t("+id+") "+new String(Ciphor.getInstance().decrypt(logins.get(id), masterKey)));
			}
			System.out.println("Type the number of the wanted login (-1 to cancel)");
			try {
				loginId = Integer.parseInt(cons.readLine());
			} catch (NumberFormatException e) {
				loginId = -2;
			}
			System.out.println();
		} while ((loginId != -1) && (!logins.containsKey(loginId)));
		if (loginId != -1) {
			return new Pair<Integer, String>(Integer.valueOf(loginId), new String(Ciphor.getInstance().decrypt(logins.get(loginId), masterKey)));
		}
		return null;
	}
	
	private static void quit() {
		System.out.print("Closing DB... ");
		DBHandler.getInstance().disconnect();
		System.out.println("done");
		System.out.print("Releasing instances... ");
		for (int i=0 ; i<masterKey.length ; ++i) {
			masterKey[i] = 0;
		}
		for (int i=0 ; i<origSalt.length ; ++i) {
			origSalt[i] = 0;
		}
		storedPwd.clear();
		System.out.println("done");
		System.out.println("\nGoodbye");
		System.exit(0);
	}
}
