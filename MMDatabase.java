/* Map Meet Database
*  Milo Watanabe, April 2014
 * 
 * Database info -- server:localhost, username:watanami, password:mapmeet
 */

import java.sql.*;
import java.util.List;
import java.util.LinkedList;
import org.json.*;

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
	
	public boolean addMeeting(String mtgdata) {
		JSONTokener tokener = new JSONTokener(mtgdata);
		JSONObject mtgjson = new JSONObject(tokener);
		
		String title = mtgjson.getString("Title");
		String date = mtgjson.getString("Date");
		String time = mtgjson.getString("Time");
		String attlst = mtgjson.getString("Attendees");
		double lat = mtgjson.getDouble("Location_Lat");
		double lon = mtgjson.getDouble("Location_Long");
		
		try {
			String query = "insert into meetings (Title, Date, Time, Attendees, Location_Lat, Location_Long) values (" +title+", "+date+", "+time+", "+attlst+", "+lat+", "+lon+")";
			Statement statement = conn.createStatement();
			statement.executeUpdate(query);
			statement.close();
		} catch (SQLException e) { die("getMeetings failed: " + e); }
		return true;
	}
	
	public List<String> getMeetings(String name) {
		//query for meetings that include name, returns list of JSON meeting objects
		List<String> mtg_list = new LinkedList<String>();
		ResultSet results = null;
		String query = "select * from meetings where " + name + " like '%" + name + "%';";
		try {
			Statement statement = conn.createStatement();
			results = statement.executeQuery(query);
			while (results.next()) {
				JSONObject json_mtg = queryToJson(results);
				mtg_list.add(json_mtg.toString());
			}
			results.close();
			statement.close();
		} catch (SQLException e) { die("getMeetings failed: " + e); }
		return mtg_list;
	}

	private JSONObject queryToJson(ResultSet result) {
		JSONObject json_meeting = new JSONObject();
		try {
			json_meeting.put("Title", result.getString("Title"));
			json_meeting.put("Date", result.getDate("Date").toString());
			json_meeting.put("Time",result.getTime("Time").toString());
			json_meeting.put("Att_list", result.getString("Attendees"));
			json_meeting.put("Latitude", result.getDouble("Location_Lat"));
			json_meeting.put("Longitude", result.getDouble("Location_Long"));
		} catch (SQLException e) { die("queryToJson failed: " + e); }
		
		return json_meeting;
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