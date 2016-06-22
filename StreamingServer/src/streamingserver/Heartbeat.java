
package streamingserver;

import java.util.*;
import java.io.*;
import java.util.concurrent.TimeUnit;

public class Heartbeat extends Thread {

	// Server thread reference
	private ServerThread thread;

	// I/O handlers
	private Scanner serverIn;
	private PrintStream serverOut;

	public static final String HEARTBEAT = "heartbeat";	// Heartbeat message
	public static final int HB_TIME = 30;	// Time in seconds to send heartbeat message


	public Heartbeat(ServerThread thread, Scanner serverIn, PrintStream serverOut){
		this.thread = thread;
		this.serverIn = serverIn;
		this.serverOut = serverOut;
	}

	@Override
	public void run() {

		String msg = HEARTBEAT;

		while(this.thread.isConnected()){

			// Check if last heartbeat was received
			// If HEARTBEAT was not received, shutdown connection
			if(!this.thread.getHeartbeat()){
				try{
					this.thread.shutdown();
				} catch(Exception e){}
			}

			// Wait until next heartbeat must be sent
			try{
				TimeUnit.SECONDS.sleep(HB_TIME);
			} catch(Exception e){}

			// If connection was closed, stop this thread
			if(!this.thread.isConnected()) return;

			System.out.println("[Debug]: Sending " + HEARTBEAT);

			// Send heartbeat message
			this.serverOut.println(HEARTBEAT);
			this.thread.setHeartbeat(false);

			// Wait for response
			try{
				TimeUnit.SECONDS.sleep(HB_TIME);
			} catch(Exception e){}
		}
	}
}