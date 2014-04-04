/* Map Meet Database
*  Milo Watanabe, April 2014
 * 
 * Database info -- server:localhost, username:watanami, password:mapmeet
 */

import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.text.*;
import javax.swing.*;

public class MMDatabase {
	private static final String
		SERVER = "localhost",
		DB = "watanami",
		ID = "watanami",
		PW = "mapmeet",
		DRIVER = "com.mysql.jdbc.Driver";
	private Connection conn = null;
	
	public MMDatabase() {
		try {
			Class.forName(DRIVER).newInstance();
		} catch (Exception e) { die("Obtaining class " + DRIVER + " failed: " + e); }
		try {
			conn = DriverManager.getConnection("jdbc:mysql://"+SERVER+"/"+DB, ID, PW);
		} catch (SQLException e) { die("getConnection failed: " + e); }
	}
	
	public boolean addMeeting(JSONObject mtgdata) {
		//unpack JSON meeting data, put into the database, return true/false
	}
	
	public JSONObject getMeetings(String name) {
		//query for meetings that include name, pack into JSON list and return
	}
	public void closeConnection() {
		if (conn != null){
			try {
				conn.close();
			} catch (SQLException e) {
				System.err.println("Unable to close db connection: " + e);
				return;
			}
		}
	}
	
	private static void die(String msg) {
		System.err.println(msg);
		System.exit(1);
	}
}