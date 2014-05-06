/**
*  Map Meet Server
*  Milo Watanabe, April 2014
*  
*  Protocol: Client sends info about meeting, sent in as a JSON object.
*  Can send as many as they want, they will disconnect on their own.
*  Server then stores info in the database.
*  Client requesting info sends their name, queries db for info with
*  results having name in the list.
*  Server compiles a list of meetings and sends back a JSON list to the client.
*  
*  Server info -- name:cscilab.bc.edu, socket #:10003, password:eagle id #
*/

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;

import org.json.JSONObject;


public class MMServer {
	private static final int SERVER_SOCKET = 10003;
	public static void main(String args[]) {
		while (true ) { // this loop is for attempting to recover from errors and automatically restart
			ServerSocket ss = null;
			try{
				ss = new ServerSocket(SERVER_SOCKET);
				System.out.println("ss is the server socket.");
			} catch (IOException e) {
				System.out.println("Couldn't open server socket, will try again in a minute.");
				System.out.println(e);
				try { Thread.sleep(60*1000); } catch (InterruptedException e2) { /* ignore, shouldn't happen */ }
				continue;
			}
			while(true) {
				Socket socket = null;
				try {
					System.out.println("Listening...");
					socket = ss.accept();
					try {
						Thread.sleep(1000); // slow rate to one connection per second
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				} catch (IOException e) {
					System.out.println("Failed to accept a connection, will attempt server restart in a minute.");
					try { ss.close(); } catch (IOException e1) { /* tried */ }
					try { Thread.sleep(60*1000); } catch (InterruptedException e2) { /* ignore */ }
					break; // Try getting the socket again, attempt to recover
				}
				System.out.println("Got new client");
				(new Listener(socket)).start();
				System.out.println("Started listener");
			}
		}
	}
}

// Each instance of this class either stores JSON meeting object or serves JSON meeting list
class Listener extends Thread
{
    private MMDatabase db;
	private Socket socket;

    public Listener(Socket socket) {
        this.socket  = socket;
    }

    private void closeStreams(BufferedReader inStream, PrintStream outStream, Socket socket) {
    	if (inStream != null)
    		try { inStream.close(); } catch (IOException ex) { /* we tried */ }
    	if (outStream != null)
    		outStream.close(); // doesn't throw an exception
    	if (socket != null)
    		try { socket.close();   } catch (IOException ex) { /* we tried */ }
    }

    public void run() {  
    	BufferedReader inStream  = null;
    	PrintStream    outStream = null;
    	String requestType = null;
    	try {
        	inStream  = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"));
        	outStream = new PrintStream(socket.getOutputStream(), true);
        	
        	//open database
			db = new MMDatabase();
			System.out.println("database created.");
			while (true) {
				requestType = inStream.readLine(); // first step of protocol: new meeting or get meetings
				System.out.println("Client looking to request a " + requestType);
				
				if(requestType.equals("newmeeting")){
				//get meeting data, send it to database to be stored
					System.out.println("newmeeting");
					JSONObject json_mtg = null;
					try {
						json_mtg = new JSONObject();
						json_mtg.put("Title", inStream.readLine());
						json_mtg.put("Date", inStream.readLine());
						json_mtg.put("Time", inStream.readLine());
						json_mtg.put("Attendees", inStream.readLine());
						json_mtg.put("Location", inStream.readLine());
						System.out.println("json object created");
					} catch (Exception e) {
						System.out.println("Couldn't get json: " + e);
						closeStreams(inStream, outStream, socket);
						break;
					}
					if (db.addMeeting(json_mtg.toString())) outStream.println("yes");
					else outStream.println("no");
					System.out.println("Meeting added, listening again.");
					
				}
				
				else if(requestType.equals("mapview")){
				//get client name, query database for JSON list, send back to client
					String name = inStream.readLine();
					System.out.println(name);
					List<String> mtg_list = db.getMeetings(name);
					for (String mtg: mtg_list) outStream.println(mtg);
					outStream.println("no");
				}
				
				else {
					System.out.println("The request type is incorrect. This should not be possible. Shit.");
				}
			}
    	} catch (Exception e) {
    		System.out.println("Lost communication with client: " + e);
    		db.closeConnection();
    		closeStreams(inStream, outStream, socket);
    		return;  // give up on this client
    	}
    }
}