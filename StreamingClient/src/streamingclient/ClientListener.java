
package streamingclient;

import java.io.*;
import java.util.*;
import java.net.*;

// Listener to get client commands from stdin at client's end
// No access modifier so only this package (StreamingClient) can access
class ClientListener extends Thread {

	// Store a client reference to the client to close it
	private StreamingClient client;



	// Constructor
	public ClientListener(){}



	// Start listener thread
	@Override
	public void run(){

		Scanner sc = new Scanner(System.in);
		String command;
		String[] tokens;

		// Listen to stdin commands
		while(true){
			command = sc.nextLine();
			System.out.println("[Shell Debug]: command: \"" + command + "\"");
			tokens = command.split(StreamingClient.DELIM);

			switch(tokens[0]){
			case "help":
				System.out.println("Commands: ");
				System.out.println("\thelp - display this message");
				System.out.println("\trequest <arg1> <arg2> ... (whitespace separated) - send a request to the server");
				System.out.println("\tclose - send a signal to close connection");
				System.out.println("\tclear - clear console");
				break;
			case "request":	// Sends a request message
				break;
			case "close":
				StreamingClient.sendMessage("Close");
				StreamingClient.setConnected(false);
				return;
			case "clear":
				clear();
				break;
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