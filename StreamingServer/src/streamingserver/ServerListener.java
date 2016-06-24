
package streamingserver;

import java.util.Scanner;

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
		String command;

		// Listen to stdin commands
		while(true){
			if(sc.hasNextLine()){

				command = sc.nextLine();				

				switch(command){
				case "help":
					System.out.println("Commands: ");
					System.out.println("\thelp - display this message");
					System.out.println("\tshutdown - close server");
					System.out.println("\tclear - clear console");
					break;
				case "shutdown":
					this.server.shutdown();
					return;
				case "clear":
					clear();
				}
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