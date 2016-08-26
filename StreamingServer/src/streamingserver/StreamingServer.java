// checked
/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package streamingserver;

import java.io.IOException;

import java.util.Scanner;
import java.util.ArrayList;

import java.net.Socket;
import java.net.ServerSocket;
import java.net.SocketException;


/**
 *
 * @author Rafael Augusto Monteiro - 9293095
 */
public class StreamingServer {

	// Media directory
	static final String MEDIA_DIR = "media/";


	// Socket handler to connect to the network
	private ServerSocket socket;
	// Port to connect
	int port;

	// Server flag
	private boolean run;
	private boolean connected;

	// List with reference to all connected clients
	private ArrayList<ServerThread> clients;


	// Constructor
	public StreamingServer(int port){
		this.clients = new ArrayList<>();
		this.port = port;
	}


	// Force server shutdown
	public synchronized void shutdown() {
		
		System.out.println("[Debug]: server shutting down...");
		this.run = false;

		// Disconnect all clients
		for(ServerThread st : this.clients){
			try{
				st.shutdown();
			} catch(Exception e){}
		}

		// Close server socket
		try{
			this.socket.close();
		} catch(Exception e){
			System.err.println("[Debug @ StreamingServer.shutdown()]: error closing server socket");
			System.exit(1);
		}		
	}

	public void startServer() throws IOException {

		System.out.println("[Debug]: starting server...");
		
		// Create a socket for this server's communications
		this.socket = new ServerSocket(port);
		
		// Start a listener to get inputs from stdin
		ServerListener listener = new ServerListener(this);


		System.out.println("[Debug]: preparing server listener...");
		listener.start();
		System.out.println("[Debug]: done!");

		ServerListener.help();		

		// Start running server
		this.run = true;
		while(this.run){
			
			// Client handler
			Socket client = null;

			// Wait for client to connect
			try {
				System.out.println("Waiting for connection...");
				client = this.socket.accept();
				System.out.println("[Debug]: Client connected!");
				connected = true;
			} catch(SocketException e){	
				System.out.println("[Debug]: Socket successfuly closed");
				connected = false;
			} catch(Exception e){
				System.err.println("[Debug @ startServer()]: connection error: " + e);
				connected = false;
			}
			if(connected){
				System.out.println("[Debug]: launching new server thread...");

				// Launch a thread to handle the new client
				ServerThread st = new ServerThread(this, client);
				st.start();
				clients.add(st);
			}
		}
	}

	public static void main(String[] args) throws Exception{
		StreamingServer server = new StreamingServer(8080);
		server.startServer();
		return;
	}
}

