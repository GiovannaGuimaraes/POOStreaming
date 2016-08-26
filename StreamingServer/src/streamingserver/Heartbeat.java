// checked
package streamingserver;

import java.util.Scanner;
import java.io.PrintStream;
import java.util.concurrent.TimeUnit;

// Heartbeat protocol to check connection health
// No access modifier so only this package (streamingserver) can access
class Heartbeat extends Thread {

	//========
	//  Constants
	//========

	public final int HB_TIME;		// Time in seconds to send heartbeat message
	public static final String HEARTBEAT = "heartbeat";	// Heartbeat message


	//========
	//  Attributes
	//========

	// Server thread reference
	private ServerThread thread;

	// I/O handlers
	private Scanner serverIn;
	private PrintStream serverOut;


	// Constructor
	public Heartbeat(ServerThread thread, Scanner serverIn,
			PrintStream serverOut, int timerInSeconds){
		this.thread = thread;
		this.serverIn = serverIn;
		this.serverOut = serverOut;
		this.HB_TIME = timerInSeconds;
	}


	// Heartbeat subroutine
	@Override
	public void run() {

		String msg = HEARTBEAT;
		int counter = 0;

		while(this.thread.isConnected()){

			// Check if last heartbeat was received
			// If HEARTBEAT was not received 3 times, shutdown connection
			if(!this.thread.getHeartbeat()){
				if(counter++ >= 2){
					try{
						this.thread.shutdown();
					} catch(Exception e){}
				}
			} else counter = 0;

			// Wait until next heartbeat must be sent
			try{
				TimeUnit.SECONDS.sleep(this.HB_TIME);
			} catch(Exception e){}

			// If connection was closed, stop this thread
			if(!this.thread.isConnected()) return;

			// Send heartbeat message
			this.thread.sendMessage(HEARTBEAT);
			this.thread.setHeartbeat(false);

			// Wait for response
			try{
				TimeUnit.SECONDS.sleep(this.HB_TIME);
			} catch(Exception e){}
		}
	}
}