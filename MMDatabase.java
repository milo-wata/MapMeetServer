/* Map Meet Database
*  Milo Watanabe, April 2014
 * 
 * Database info -- server:localhost, username:watanami, password:mapmeet
 */

import java.sql.*;
import java.util.List;
import java.util.LinkedList;

import org.json.JSONException;
import org.json.JSONObject;


public class MMDatabase {
	private static final String
		SERVER = "localhost",
		DB = "watanami",
		ID = "watanami",
		PW = "password",
		DRIVER = "com.mysql.jdbc.Driver";
	private Connection conn = null;
	
	public MMDatabase() {
		try {
			Class.forName(DRIVER).newInstance();
		} catch (Exception e) { die("Obtaining class " + DRIVER + " failed: " + e); }
		try {
			conn = DriverManager.getConnection("jdbc:mysql://"+SERVER+"/"+DB, ID, PW);
		} catch (SQLException e) { e.printStackTrace(); die("getConnection failed: " + e); }
		catch (Exception e) { System.out.println("some error"); e.printStackTrace(); }
	}
	
	public boolean addMeeting(String mtgdata) throws JSONException {
		System.out.println("db addmeeting");
		JSONObject jmeeting = new JSONObject(mtgdata);
		
		String title = (String) jmeeting.get("Title");
		String date = (String) jmeeting.get("Date");
		String time = (String) jmeeting.get("Time");
		String attlist = (String) jmeeting.get("Attendees");
		String location = (String) jmeeting.get("Location");
		try {
			System.out.println("addmeeting try");
			String query = "insert into meetings (Title, Date, Time, Attendees, Location) values ('" +title+"', '"+date+"', '"+time+"', '"+attlist+"', '"+location+"')";
			System.out.println(query);
			Statement statement = conn.createStatement();
			statement.executeUpdate(query);
			statement.close();
		} catch (SQLException e) { die("addMeetings failed: " + e); }
		return true;
	}
	
	public List<String> getMeetings(String name) {
		//query for meetings that include name, returns list of JSON meeting objects
		List<String> mtg_list = new LinkedList<String>();
		ResultSet results = null;
		String query = "select * from meetings where Attendees like '%" + name + "%';";
		try {
			Statement statement = conn.createStatement();
			results = statement.executeQuery(query);
			while (results.next()) {
				JSONObject json_mtg = queryToJson(results);
				System.out.println(json_mtg.toString());
				mtg_list.add(json_mtg.toString());
			}
			results.close();
			statement.close();
		} catch (SQLException e) { die("getMeetings failed: " + e); }
		catch (JSONException e1) { die("queryToJSON failed: " + e1); }
		return mtg_list;
	}

	private JSONObject queryToJson(ResultSet result) throws JSONException {
		JSONObject json_meeting = new JSONObject();
		try {
			json_meeting.put("Title", result.getString("Title"));
			json_meeting.put("Date", result.getString("Date").toString());
			json_meeting.put("Time",result.getString("Time").toString());
			json_meeting.put("Attendees", result.getString("Attendees"));
			json_meeting.put("Location", result.getString("Location"));
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
		System.out.println(msg);
		System.exit(1);
	}
}