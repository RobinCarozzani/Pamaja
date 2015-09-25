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

package fr.robincarozzani.pamaja.db;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

import fr.robincarozzani.pamaja.Launcher;
import fr.robincarozzani.pamaja.crypto.Ciph;
import fr.robincarozzani.pamaja.crypto.Hash;
import fr.robincarozzani.pamaja.utils.Pair;

/**
 * Object handling database communications
 * @author Robin Carozzani
 */
public class DBHandler {
	
	private static final String DB_LOC = "data";
	private static final String DB_NAME = "PamajaDB.db";
	private static final String[] ACC_VERSIONS = {Launcher.PROG_VERSION, "0.1"};

	private Connection connection = null;

	private static DBHandler instance = null;

	private DBHandler() {
		try {
			Class.forName("org.sqlite.JDBC");
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Gets unique instance of DBHandler
	 * @return Instance of DBHandler
	 */
	public static DBHandler getInstance() {
		if (instance == null) {
			instance = new DBHandler();
		}
		return instance;
	}
	
	/**
	 * Establishes a connection to the database
	 */
	public void connect() {
		boolean newDB = (
				!(
						(new File(DB_LOC).exists()) &&
						(new File(DB_LOC).isDirectory()) &&
						(new File(DB_LOC+"/"+DB_NAME).exists())
				));
		new File(DB_LOC).mkdir();
		try {
			connection = DriverManager.getConnection("jdbc:sqlite:"+DB_LOC+"/"+DB_NAME);
			Statement stmt = connection.createStatement();
			stmt.executeUpdate("PRAGMA synchronous = OFF;");
			stmt.setQueryTimeout(30);
			stmt.close();
			if (newDB) {
				createDB();
			}
		} catch(SQLException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Closes connection to the database
	 */
	public void disconnect() {
		try {
			if (connection != null) {
				connection.close();
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	private void executeNoResult(String query) {
		try {
			Statement stmt = connection.createStatement();
			stmt.executeUpdate(query);
			stmt.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	private ResultSet executeWithResult(String query) {
		ResultSet rs = null;
		try {
			Statement stmt = connection.createStatement();
			rs = stmt.executeQuery(query);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return rs;
	}
	
	private boolean recordExistsInTable(String fieldName, String valueToFind, String table) {
		String sql = "SELECT COUNT(*) FROM " + table
				  + " WHERE " + fieldName + " = " + valueToFind +";";
		ResultSet rs = executeWithResult(sql);
		boolean res = false;
		try {
			if (rs.next()) {
				res = rs.getInt(1)!=0;
			}
			rs.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return res;
	}
	
	private void createDB() {
		String sql = "CREATE TABLE cida ("
				   + "	id INT PRIMARY KEY NOT NULL,"
				   + "	iv TEXT NOT NULL,"
				   + "	enc TEXT NOT NULL)";
		executeNoResult(sql);
		
		sql = "CREATE TABLE sercl ("
			+ "	id INT PRIMARY KEY NOT NULL,"
			+ "	ser TEXT NOT NULL)";
		executeNoResult(sql);
		
		sql = "CREATE TABLE locl ("
			+ "	id INT PRIMARY KEY NOT NULL,"
			+ " iv TEXT NOT NULL,"
			+ "	lo TEXT NOT NULL)";
		executeNoResult(sql);
		
		sql = "CREATE TABLE corda ("
			+ "	sid INT NOT NULL,"
			+ "	lid INT NOT NULL,"
			+ "	pid INT NOT NULL,"
			+ "	CONSTRAINT pk_corda PRIMARY KEY(sid, lid),"
			+ "	CONSTRAINT fk_corda1 FOREIGN KEY(sid) REFERENCES sercl(id),"
			+ "	CONSTRAINT fk_corda2 FOREIGN KEY(lid) REFERENCES locl(id),"
			+ "	CONSTRAINT fk_corda3 FOREIGN KEY(pid) REFERENCES cida(id))";
		executeNoResult(sql);
		
		sql = "CREATE TABLE cimd ("
		   + "	id INT PRIMARY KEY NOT NULL,"
		   + "	sa TEXT,"
		   + "	ivsa TEXT NOT NULL,"
		   + "	enc TEXT NOT NULL)";
		executeNoResult(sql);
		
		sql = "CREATE TABLE info ("
		   + " init INTEGER NOT NULL,"
		   + " version TEXT NOT NULL)";
		executeNoResult(sql);
		
		sql = "INSERT INTO info VALUES(0, '"+Launcher.PROG_VERSION+"')";
		executeNoResult(sql);
	}
	
	/**
	 * Checks if the database has been initialized
	 * @return <code>true</code> if the database has been initialized
	 */
	public boolean isInit() {
		String sql = "SELECT init FROM info";
		ResultSet rs = executeWithResult(sql);
		boolean init = false;
		try {
			if (rs.next()) {
				init = rs.getBoolean("init");
			}
			rs.close();
		} catch (SQLException e1) {
			e1.printStackTrace();
		}
		return init;
	}
	
	/**
	 * Checks if the version of the database corresponds to the version of the running program
	 * @return <code>true</code> if the database can properly be handled by the program
	 */
	public boolean dbVersionIsOk() {
		String sql = "SELECT version FROM info";
		ResultSet rs = executeWithResult(sql);
		String v = "";
		try {
			if (rs.next()) {
				v = rs.getString("version");
			}
			rs.close();
		} catch (SQLException e1) {
			e1.printStackTrace();
		}
		for (String s : ACC_VERSIONS) {
			if (s.equals(v)) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Set the database status to initialized
	 */
	public void setInit() {
		String sql = "UPDATE info SET init = 1";
		executeNoResult(sql);
	}
	
	/**
	 * Inserts the master password into the database
	 * @param password The double hashed password
	 * @param origSalt Salt of the first hash
	 */
	public void insertMasterPassword(Hash password, byte[] origSalt) {
		int id = 1;
		if (!recordExistsInTable("id", ""+id, "cimd")) {
			String salt = new String(password.getSalt());
			String enc = new String(password.getHashedMessage());
			String sql = "INSERT INTO cimd "
					   + "VALUES('"+id+"', '"+new String(origSalt)+"', '"+salt+"', '"+enc+"')";
			executeNoResult(sql);
		}
	}
	
	/**
	 * Gets the master password from the database and its salt from its first hash
	 * @return Double hashed master password
	 */
	public Pair<Byte[], Hash> getStoredPasswordAndSalt() {
		String sql = "SELECT * FROM cimd"
				  + " WHERE id = 1";
		ResultSet rs = executeWithResult(sql);
		Byte[] os = null;
		byte[] s = null;
		byte[] e = null;
		try {
			if (rs.next()) {
				byte[] tmpos = rs.getString("sa").getBytes();
				os = new Byte[tmpos.length];
				int i = 0;
				for (byte b : tmpos) {
					os[i++] = Byte.valueOf(b);
				}
				s = rs.getString("ivsa").getBytes();
				e = rs.getString("enc").getBytes();
			}
			rs.close();
		} catch (SQLException e1) {
			e1.printStackTrace();
		}
		if ((s != null) && (e != null)) {
			return new Pair<Byte[], Hash>(os, new Hash(s, e));
		}
		return null;
	}
	
	/**
	 * Inserts the secret encryption key into the database
	 * @param key The encrypted secret key
	 */
	public void insertMasterKey(Ciph key) {
		int id = 2;
		if (!recordExistsInTable("id", ""+id, "cimd")) {
			String iv = new String(key.getIV());
			String enc = new String(key.getEnc());
			String sql = "INSERT INTO cimd "
					   + "VALUES('"+id+"', '', '"+iv+"', '"+enc+"')";
			executeNoResult(sql);
		}
	}
	
	/**
	 * Gets the encryption key from the database
	 * @return The encrypted secret key
	 */
	public Ciph getStoredKey() {
		String sql = "SELECT * FROM cimd"
				  + " WHERE id = 2";
		ResultSet rs = executeWithResult(sql);
		byte[] i = null;
		byte[] e = null;
		try {
			if (rs.next()) {
				i = rs.getString("ivsa").getBytes();
				e = rs.getString("enc").getBytes();
			}
			rs.close();
		} catch (SQLException e1) {
			e1.printStackTrace();
		}
		if ((i != null) && (e != null)) {
			return new Ciph(i, e);
		}
		return null;
	}
	
	/**
	 * Inserts a new password in the database
	 * @param service Service associated to the password
	 * @param login Encrypted login corresponding to the password
	 * @param password Encrypted password to insert
	 */
	public void insertPassword(String service, Ciph login, Ciph password) {
		String sql = "SELECT MAX(id) FROM sercl;";
		ResultSet rs = executeWithResult(sql);
		int serviceId = 0;
		try {
			if (rs.next()) {
				serviceId = rs.getInt(1)+1;
			}
			rs.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		sql = "SELECT MAX(id) FROM locl;";
		rs = executeWithResult(sql);
		int loginId = 0;
		try {
			if (rs.next()) {
				loginId = rs.getInt(1)+1;
			}
			rs.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		sql = "SELECT MAX(id) FROM cida;";
		rs = executeWithResult(sql);
		int pwdId = 0;
		try {
			if (rs.next()) {
				pwdId = rs.getInt(1)+1;
			}
			rs.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		sql = "INSERT INTO sercl VALUES('"+serviceId+"', '"+service+"')";
		executeNoResult(sql);
		sql = "INSERT INTO locl VALUES('"+loginId+"', '"+new String(login.getIV())+"', '"+new String(login.getEnc())+"')";
		executeNoResult(sql);
		sql = "INSERT INTO cida VALUES('"+pwdId+"', '"+new String(password.getIV())+"', '"+new String(password.getEnc())+"')";
		executeNoResult(sql);
		sql = "INSERT INTO corda VALUES('"+serviceId+"', '"+loginId+"', '"+pwdId+"')";
		executeNoResult(sql);
	}
	
	/**
	 * Updates a password in the database
	 * @param serviceId ID of the service associated to the password
	 * @param loginId ID of the login corresponding to the password
	 * @param newPassword Encrypted password to insert
	 */
	public void updatePassword(int serviceId, int loginId, Ciph newPassword) {
		String sql = "UPDATE cida"
				  + " SET iv = '" + new String(newPassword.getIV()) + "', enc = '" + new String(newPassword.getEnc()) + "'"
				  + " WHERE id = (SELECT pid"
				  + "			  FROM corda"
				  + "			  WHERE sid = " + serviceId
				  + "			  AND lid = " + loginId + ");";
		executeNoResult(sql);
	}
	
	/**
	 * Gets a specific password from the database
	 * @param serviceId ID of the service associated to the password
	 * @param loginId ID of the login corresponding to the password
	 * @return Encrypted password
	 */
	public Ciph getPassword(int serviceId, int loginId) {
		String sql = "SELECT iv, enc"
				  + " FROM cida"
				  + " WHERE id = (SELECT pid"
				  + "			  FROM corda"
				  + "			  WHERE sid = " + serviceId
				  + "			  AND lid = " + loginId + ");";
		ResultSet rs = executeWithResult(sql);
		String iv = null;
		String enc = null;
		try {
			if (rs.next()) {
				iv = rs.getString("iv");
				enc = rs.getString("enc");
			}
			rs.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		if ((iv != null) && (enc != null)) {
			return new Ciph(iv.getBytes(), enc.getBytes());
		}
		return null;
	}
	
	/**
	 * Gets a list of registered services
	 * @return List of services (<code>ID => name</code>)
	 */
	public Map<Integer, String> getServices() {
		String sql = "SELECT * FROM sercl"
				  + " ORDER BY id;";
		ResultSet rs = executeWithResult(sql);
		Map<Integer, String> services = new HashMap<Integer, String>();
		try {
			while (rs.next()) {
				services.put(rs.getInt("id"), rs.getString("ser"));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return services;
	}
	
	/**
	 * Gets a list of logins for a given service
	 * @param serviceId ID of the service to get the logins from
	 * @return List of logins (<code>ID => name</code>)
	 */
	public Map<Integer, Ciph> getLogins(int serviceId) {
		String sql = "SELECT * FROM locl"
				  + " WHERE id = (SELECT lid FROM corda"
				  + "			  WHERE sid = "+serviceId+");";
		ResultSet rs = executeWithResult(sql);
		Map<Integer, Ciph> logins = new HashMap<Integer, Ciph>();
		try {
			while (rs.next()) {
				logins.put(rs.getInt("id"), new Ciph(rs.getString("iv").getBytes(), rs.getString("lo").getBytes()));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return logins;
	}
}
