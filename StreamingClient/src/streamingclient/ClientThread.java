// checked
package streamingclient;

import java.io.File;
import java.net.Socket;

public class ClientThread extends Thread {

	private StreamingClient client;
	private boolean isFetching = false;


	// Constructor
	public ClientThread(StreamingClient client) {
		this.client = client;
	} 


	@Override
	public void run() {

		// Server-client messages
		String msg = null;
		String[] tokens = null;

		// Data transfer handlers
		PacketManager pm = null;
		Socket dataSocket = null;

		System.out.println("[Debug]: client thread successfully started!");

		// Receive server responses
		while (this.client.isConnected()) {
			if (client.clientIn.hasNextLine() && this.client.isConnected()) {

				msg = client.clientIn.nextLine();
				if(!msg.equals(StreamingClient.HEARTBEAT))
					System.out.println("[Debug]: [Received]: \"" + msg + "\"");

				tokens = msg.split(StreamingClient.DELIM);

				// Server responses requests
				switch(tokens[0]){
				case "ListRes":		// ListRes

					System.out.println("[Response]: files list");
					for(int i = 1; i < tokens.length; i++)
						System.out.println(tokens[i]);

					break;
				
				case "FetchRes":	// FetchRes <status>
							//		- start
							//		- end
							//		- dropped

							// data stream responses: 
							// byte[] = "ok"
							// byte[] = "fail"
					
					// Transfer begin
					if(tokens[1].equals("start") && !isFetching){
								
						System.out.println("[Debug]: start received, starting fetch");
						try{
							dataSocket = new Socket(StreamingClient.ip,
										StreamingClient.port+1);
							
							System.out.println("[Debug]: [Fetch]: connected to " + 
										StreamingClient.ip + ":" +
										(StreamingClient.port+1));

							pm = new PacketManager(dataSocket,
										this.client.command);
						} catch(Exception e){
							System.err.println(
								"[Debug]: error opening data socket streams");
							isFetching = false;
							break;
						}

						isFetching = true;
						pm.start();

					// Problem ocurred during transfer
					} else if(tokens[1].equals("dropped")){
						
						pm.interrupt();
						try{
							dataSocket.close();
						} catch(Exception e){}
						
						isFetching = false;

					// Transfer end
					} else if(tokens[1].equals("end")){
						
						System.out.println("[Debug]: fetch has ended, setting isFetching false");
						try{

							System.out.println("[Debug]: waiting for pm to join");
							pm.join();
							dataSocket.close();

						} catch(Exception e){}

						System.out.println("[Debug]: joined");
						isFetching = false;
					}
					break;
				
				case "Close":		// Close
					this.client.shutdown();
					return;

				case "Error":
					System.out.println("[Response]: " + tokens[1]);
					break;
				
				case StreamingClient.HEARTBEAT:	// HEARTBEAT
					client.sendMessage(StreamingClient.HEARTBEAT);
					break;
				
				default:		// Request msg re-send
					System.out.println("[Response]: unhandled response\n");
					break;
				}
			}
		}
	}

	// List local media files
	public String listFiles(){

		File folder = new File("media/");
		File[] listOfFiles = folder.listFiles();
		String msg = "";

		// List files in page
		for(int i = 0; i < listOfFiles.length; i++)
			msg += listOfFiles[i].getName() + StreamingClient.DELIM;

		return msg;
	}
}
