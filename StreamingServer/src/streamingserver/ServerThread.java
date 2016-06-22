
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
	private PrintStream serverOut;	// debug?

	// Periodicaly sends message to client to check connection
	private Heartbeat hb;
	private boolean connection;
	private boolean hbHasBeenReceived;

	// Message delimiter
	public static final String DELIM = " ";



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

		this.hb = new Heartbeat(this, this.serverIn, this.serverOut);
		this.connection = true;		// Start assuming connection is OK
		this.hbHasBeenReceived = true;	// Start assuming connection is OK
	}



	// Setter/getter heartbeat flag
	public synchronized void setHeartbeat(boolean b) { this.hbHasBeenReceived = b; }
	public boolean getHeartbeat() { return this.hbHasBeenReceived; }

	public boolean isConnected() { return this.connection; }

	// Shutdown server thread
	public synchronized void shutdown() throws Exception {
		System.out.println("[Debug]: shutting down connection...");
		// TODO: Do cleanup (close streams, files, handler, hooks)

		this.hb.join();
		serverIn.close();
		serverOut.close();
		this.connection = false;
	}

	// Start server thread to handle this client
	@Override
	public void run(){

		// Received message
		String msg = null;
		String[] tokens = null;
		// Server response
		String response = null;

		// Start heartbeat checkup routine
		this.hb.start();

		// Get initial messages
		System.out.println("[Debug]: getting initial messages...\n");
		while(this.serverIn.hasNextLine()){

			msg = this.serverIn.nextLine();
			System.out.println(msg);
			if(msg.isEmpty()) break;
		}
		System.out.println("\n[Debug]: done!");

		System.out.println("[Debug]: starting heartbeat timer");
		// Start heartbeat scheduling - each 30s send heartbeat signal

		// Get & process requests
		while(this.connection){
			if(this.serverIn.hasNextLine()){

				// System.out.println("[Debug]: fetching request");
				msg = this.serverIn.nextLine();
				System.out.println("[Debug]: [Received]: \"" + msg + "\"");

				tokens = msg.split(DELIM);

				// Client requests
				switch(tokens[0]){
				case "List":		// List <int>
					break;
				case "Fetch":		// Fetch <path>
					break;
				case "Close":		// Close
					
					try{
						shutdown();
					} catch(Exception e){}

					break;
				case Heartbeat.HEARTBEAT:	// HEARTBEAT
					setHeartbeat(true);
					break;
				default:		// Request msg re-send
					break;
				}
			}
		}
	}

	public void listFiles(){

		File folder = new File("");
		File[] listOfFiles = folder.listFiles();

		for (int i = 0; i < listOfFiles.length; i++) {
			if (listOfFiles[i].isFile()) {
				System.out.println("File " + listOfFiles[i].getName());
			} else if (listOfFiles[i].isDirectory()) {
				System.out.println("Directory " + listOfFiles[i].getName());
			}
		}
	}
}