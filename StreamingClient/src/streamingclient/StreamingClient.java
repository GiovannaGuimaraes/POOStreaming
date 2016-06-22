/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package streamingclient;

import java.util.*;
import java.io.*;
import java.net.*;

/**
 *
 * @author Rafael Augusto Monteiro - 9293095
 */
public class StreamingClient {
	
	// private final String name = "teste1.mp4";

	private static final String LOOPBACK = "127.0.0.1";
	private static final int PORT = 8080;

	// Heartbeat message
	private static final String HEARTBEAT = "heartbeat";

	// Message delimiter
	public static final String DELIM = " ";

	// Client I/O streams
	private static Scanner clientIn = null;
	private static PrintStream clientOut = null;
	
	// Connection flag
	private static boolean connected = false;

	public static synchronized void setConnected(boolean b){ connected = b; }

	/**
	 * @param args the command line arguments
	 */
	public static void main(String[] args) {
		
		// Server reference
		Socket server = null;

		// Stdin scanner
		Scanner sc = new Scanner(System.in);
		

		// Terminal commands for client
		ClientListener term = new ClientListener();

		String msg;
		String received;
		
		String[] tokens;

		// String ip = sc.nextLine();
		// int port = sc.nextInt();

		// Try to connect to host
		try{
			server = new Socket(LOOPBACK, PORT);
			connected = true;
		} catch(Exception e){
			System.err.println("Connection error " + e);
			System.exit(1);
		}

		// Try to open I/O streams
		try{
			clientIn = new Scanner(server.getInputStream());
			clientOut = new PrintStream(server.getOutputStream());
		} catch(Exception e){
			System.err.println("Error opening I/O streams " + e);
			System.exit(1);
		}

		term.start();

		// Send initial msg
		msg = "DEBUG FIRST MSG";
		sendMessage(msg);
		msg = "";	// Terminate intial message
		sendMessage(msg);

		while(connected){
			if(clientIn.hasNextLine() && connected){

				msg = clientIn.nextLine();

				switch(msg){
				case HEARTBEAT:
					sendMessage(HEARTBEAT);
					break;
				}
			}
		}
	}
	
	public static synchronized void sendMessage(String msg){
		System.out.println("[Debug]: sending \"" + msg + "\"");
		clientOut.println(msg);
	}
}
