// checked
package streamingclient;

import java.util.Scanner;
import java.net.Socket;

// Listener to get client commands from stdin at client's end
// No access modifier so only this package (StreamingClient) can access
class ClientListener extends Thread {

	// Store a client reference to the client to close it
	private StreamingClient client;


	// Constructor
	public ClientListener(StreamingClient client){
		this.client = client;
	}


	// Start listener thread
	@Override
	public void run(){

		Scanner sc = new Scanner(System.in);
		String command;
		String[] tokens;
		String msg;

		// Listen to stdin commands
		while(true){
			command = sc.nextLine();
			System.out.println("[Shell Debug]: command: \"" + command + "\"");
			tokens = command.split(StreamingClient.DELIM);

			switch(tokens[0]){
			case "help":
				System.out.println("Commands: ");
				System.out.println("\thelp - display this message");
				System.out.println("\tclose - send a signal to close connection");
				System.out.println("\tclear - clear console");
				System.out.println("\n\tAnything else - ");
				break;

			case "close":
				this.client.shutdown();
				return;

			case "clear":
				clear();
				break;

			default: 
				this.client.sendMessage(command);
				this.client.command = command;
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