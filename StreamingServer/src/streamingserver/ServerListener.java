
package streamingserver;

import java.io.*;
import java.util.*;
import java.net.*;

// Listener to get server commands from stdin at server's end
// No access modifier so only this package (streamingserver) can access
class ServerListener extends Thread {

	// Store a server reference to the server to close it
	private StreamingServer server;



	// Constructor
	public ServerListener(StreamingServer server){
		this.server = server;
	}


	// Start listener thread
	@Override
	public void run(){

		Scanner sc = new Scanner(System.in);

		// Listen to stdin commands
		while(true){
			
			// Shutdown server (cleanup here)
			if(sc.nextLine().equals("shutdown")){
				this.server.shutdown();
				break;
			} else if(sc.nextLine().equals("clear")){
				clear();
			}
		}
	}

	private static void clear(){
		
		final String ANSI_CLS = "\u001b[2J";
		final String ANSI_HOME = "\u001b[H";
		System.out.print(ANSI_CLS + ANSI_HOME);
		System.out.flush();
	}
}