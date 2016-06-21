
package streamingserver;

import java.io.*;
import java.util.*;
import java.net.*;

// Client handler in separate thread (non-blockable)
// No access modifier so only this package (streamingserver) can access
class ServerThread extends Thread {

	// Server reference for communication
	private StreamingServer server;
	// Client to handle
	private Socket client;

	// I/O handlers
	private Scanner serverIn;
	private PrintStream serverOut;

	// Media file reference
	private Media requestedMedia;



	// Constructor
	public ServerThread(StreamingServer server, Socket client){
		
		this.server = server;
		this.client = client;

		// Open communication streams
		try{
			this.serverIn = new Scanner(client.getInputStream());
			this.serverOut = new PrintStream(client.getOutputStream());
		} catch(IOException e){
			System.err.println("Could not open I/O communications, " + e);
		}
	}


	// Start listener thread
	@Override
	public void run(){

		// Received message
		String msg = null;
		// Server response
		String response = null;




	}
}