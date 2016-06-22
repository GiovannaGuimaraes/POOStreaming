/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package streamingserver;

import java.io.*;
import java.net.*;
import java.util.*;

/**
 *
 * @author Rafael Augusto Monteiro - 9293095
 */
public class StreamingServer {

	// Socket handler to connect to the network
	private ServerSocket socket;

	// Server shutdown flag
	private boolean run;

	// List with reference to all connected clients
	private ArrayList<ServerThread> clients;



	// Constructor
	public StreamingServer(){
		
		this.clients = new ArrayList<ServerThread>();
	}



	public synchronized void shutdown(){
		// TODO: Create a clients handler to properly shutdown all connections
		// ArrayList<ServerThread> clients;
		this.run = false;
	}

	public void startServer(int port) throws IOException{

		System.out.println("[Debug]: starting server...");
		
		// Create a socket for this server's communications
		this.socket = new ServerSocket(port);
		
		// Start a listener to get inputs from stdin
		ServerListener listener = new ServerListener(this);


		System.out.println("[Debug]: preparing server listener...");
		listener.start();
		System.out.println("[Debug]: done!");
		

		// Start running server
		this.run = true;
		while(this.run){
			
			// Client handler
			Socket client = null;

			// Wait for client to connect
			try {
				System.out.println("Waiting for connection...");
				client = this.socket.accept();
				System.out.println("Client connected!");

			} catch(Exception e){
				System.out.println("Connection error: " + e);
				return;
			}

			System.out.println("[Debug]: launching new server thread...");

			// Launch a thread to handle the new client
			ServerThread st = new ServerThread(this, client);
			st.start();
		}
	}

	public static void main(String[] args) throws Exception{

		StreamingServer server = new StreamingServer();
		server.startServer(8080);
	}
}

