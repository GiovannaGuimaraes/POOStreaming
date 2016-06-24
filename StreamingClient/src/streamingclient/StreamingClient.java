// checked
/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package streamingclient;

import java.io.PrintStream;
import java.util.Scanner;
import java.net.Socket;

/**
 *
 * @author Rafael Augusto Monteiro - 9293095
 */
public class StreamingClient {

	//========
	//  Constants
	//========

	// Debug
	static final String LOOPBACK = "127.0.0.1";
	static final int PORT = 8080;
	
	// Message delimiter
	public static final String DELIM = " ";
	// Heartbeat message
	public static final String HEARTBEAT = "heartbeat";
	// Media directory
	static final String MEDIA_DIR = "media/";

	//========
	//  Attributes
	//========

	// Server ip & connection port
	static String ip;
	static int port;
	
	// Server reference
	private Socket server = null;

	// Server-client I/O streams
	Scanner clientIn = null;
	PrintStream clientOut = null;

	// Terminal handler for client -- debug only!
	private ClientListener term;
	// Client thread -- debug only!
	private ClientThread clientThread;
	
	// Connection flag
	private boolean connected = false;
	
	// Sent requests
	public String command;


	// Constructor
	public StreamingClient(){}
	public StreamingClient(String serverIp, int serverPort){
		ip = serverIp;
		port = serverPort;
	}


	// Setters
	public void setIp(String serverIp){ ip = serverIp; }
	public void setPort(int serverPort){ port = serverPort; }
	public synchronized void setConnected(boolean b){ connected = b; }

	public boolean isConnected(){ return connected; }


	// Cleanup & close client
	public void shutdown(){

		System.out.println("Closing client");
		
		// Send close message to client
		sendMessage("Close");
		setConnected(false);
		
		// Stop threads
		try{
			this.term.interrupt();
			this.clientThread.interrupt();
			this.server.close();
		} catch(Exception e){}
		System.exit(0);
	}


	public boolean startClient(String initialMsg){

		// Client-server messages
		String msg;
		String received;
		String[] tokens;

		System.out.println("Starting up client");
		System.out.println("Connecting...");

		try{			
			server = new Socket(ip, PORT);
			connected = true;
		} catch(Exception e){
			connected = false;
			System.err.println("[Debug @ startClient()]: Connection error " + e);
			return false;
		}

		System.out.println("Connected! Opening I/O streams...");

		// Try to open I/O streams
		try{
			clientIn = new Scanner(server.getInputStream());
			clientOut = new PrintStream(server.getOutputStream());
		} catch(Exception e){
			return false;
		}

		System.out.println("Done! Starting terminal listener & GUI");
		
		
		// Terminal handlers -- debug only!
		this.term = new ClientListener(this);
		this.clientThread = new ClientThread(this);
		// Start terminal listener thread
		term.start();
		clientThread.start();

		System.out.println("\nSending initial server messages...");

		// Send initial msg
		sendMessage(initialMsg);
		msg = "";	// Terminate intial message
		sendMessage(msg);

		System.out.println("");

		return true;
	}
	
	public synchronized void sendMessage(String msg){
		System.out.println("[Debug]: sending \"" + msg + "\"");
		clientOut.println(msg);
	}

	// Terminal debug main
	public static void main(String[] args) {

		StreamingClient client = new StreamingClient("127.0.0.1", 8080);
		client.startClient("Init [Client] \n\tip: 127.0.0.1\n\tport: 8080\n\t---END---");
		try{
			client.term.join();
			System.out.println("[Debug]: term joined!");
		} catch(Exception e){}
	}
}
