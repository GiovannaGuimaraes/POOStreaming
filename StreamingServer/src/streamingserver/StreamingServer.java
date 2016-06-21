/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package streamingserver;

import java.io.*;
import java.net.*;

/**
 *
 * @author Rafael Augusto Monteiro - 9293095
 */
public class StreamingServer {

	// Socket handler to connect to the network
	private ServerSocket socket;

	// Server shutdown flag
	private boolean run;



	// Constructor
	public StreamingServer(){}



	public synchronized void shutdown(){ this.run = false; }
	public void startServer(int port) throws IOException{
		
		// Create a socket for this server's communications
		this.socket = new ServerSocket(port);
		
		// Start a listener to get inputs from stdin
		ServerListener listener = new ServerListener(this);
		listener.start();
		

		// Start running server
		this.run = true;
		while(this.run){
			
			// Client handler
			Socket client = null;

			// Wait for client to connect
			try {
				client = this.socket.accept();
			} catch(Exception e){
				System.out.println("Connection error: " + e);
				return;
			}

			// Launch a thread to handle the new client
			ServerThread st = new ServerThread(this, client);
			st.start();
		}
	}

	public static void main(String[] args) throws Exception{

	}
}

