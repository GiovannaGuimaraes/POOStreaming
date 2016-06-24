// checked
package streamingserver;

import java.io.*;

import java.util.Scanner;

import java.net.Socket;
import java.net.ServerSocket;


// Client handler in separate thread (non-blockable)
// No access modifier so only this package (streamingserver) can access
class ServerThread extends Thread {

	//========
	//  Constants
	//========

	// Message delimiter
	public static final String DELIM = " ";
	// Maximum number of iterations until fetch is dropped
	public static final int MAX_ITERATIONS = 20000;
	public static final int HB_TIMEOUT = 10;


	//========
	//  Attributes
	//========

	// Server reference for communication
	private StreamingServer server;
	// Client to handle
	private Socket client;

	// I/O handlers
	private Scanner serverIn;
	private PrintStream serverOut;	// debug?
	// Data I/O handlers
	private Scanner dataRes;
	private DataOutputStream dataOut;

	// Data transfer
	private ServerSocket dataSocket = null;
	private FileInputStream fileStream;

	// Periodicaly sends message to client to check connection
	private Heartbeat hb;
	private boolean connection;
	private boolean hbHasBeenReceived;


	// Constructor
	public ServerThread(StreamingServer server, Socket client){
		
		this.server = server;
		this.client = client;

		// Open communication streams
		try{
			this.dataSocket = new ServerSocket(this.server.port + 1);

			this.serverIn = new Scanner(this.client.getInputStream());
			this.serverOut = new PrintStream(this.client.getOutputStream());
		} catch(IOException e){
			System.err.println("[Debug @ ServerThread constructor] Could not open I/O communications, " + e);
		}

		this.hb = new Heartbeat(this, this.serverIn, this.serverOut, HB_TIMEOUT);
		this.connection = true;		// Start assuming connection is OK
		this.hbHasBeenReceived = true;	// Start assuming connection is OK
	}



	// Setter/getter heartbeat flag
	public synchronized void setHeartbeat(boolean b) { this.hbHasBeenReceived = b; }
	public boolean getHeartbeat() { return this.hbHasBeenReceived; }

	public boolean isConnected() { return this.connection; }

	// Shutdown server thread
	public void shutdown() {

		System.out.println("[Debug]: shutting down connection...");

		this.hb.interrupt();
		try{
			this.serverOut.println("Close");
			this.serverIn.close();
			this.serverOut.close();
		// Scanner is already closed
		} catch(IllegalStateException e){}

		this.connection = false;
	}

	// Start server thread to handle this client
	@Override
	public void run(){

		// Received message
		String msg = null;
		String[] tokens = null;
		// Server response
		String response = null;
		boolean requestIsReady;

		// Start heartbeat checkup routine
		this.hb.start();

		// Get initial messages
		System.out.println("[Debug]: getting initial messages...\n");
		while(this.serverIn.hasNextLine()){

			msg = this.serverIn.nextLine();
			System.out.println(msg);
			if(msg.isEmpty()) break;
		}
		System.out.println("[Debug]: done!");
		System.out.println("[Debug]: starting heartbeat timer");

		// Get & process requests
		while(this.connection){
			try{
				requestIsReady = this.serverIn.hasNextLine();
			} catch(IllegalStateException e){
				System.err.println("[Debug @ ServerThread.run()]: client I/O closed, shutting thread down");
				requestIsReady = false;
			}
			if(requestIsReady){

				// Waiting for requests
				msg = this.serverIn.nextLine();
				if(!msg.equals(Heartbeat.HEARTBEAT))
					System.out.println("[Debug]: [Received]: \"" + msg + "\"");

				tokens = msg.split(DELIM);

				// Client requests
				switch(tokens[0]){
				case "List":		// List
					sendMessage("ListRes " + listFiles());
					break;
				
				case "Fetch":		// Fetch <file name>
					
					if(tokens.length == 1){
						sendMessage("Error NO_FILE_REQUESTED");
						break;
					}
					
					try{
						fetch(tokens[1]);
					} catch(FileNotFoundException e){
						sendMessage("Error FILE_NOT_FOUND");
					} catch(Exception e){}
					break;
				
				case "Close":		// Close
					
					try{
						this.fileStream.close();
						shutdown();
					} catch(Exception e){
						return;
					}

					break;
				
				case Heartbeat.HEARTBEAT:	// HEARTBEAT
					setHeartbeat(true);
					break;
				
				default:		// Request msg re-send
					sendMessage("Error INVALID_REQUEST");
					break;
				}
			}
		}
	}

	public void sendMessage(String msg){
		this.serverOut.println(msg);
		if(!msg.equals(Heartbeat.HEARTBEAT))
			System.out.println("[Debug]: [Sending]: \"" + msg + "\"");
	}

	public String listFiles(){

		File folder = new File(StreamingServer.MEDIA_DIR);
		File[] listOfFiles = folder.listFiles();
		String msg = "";

		// List files in MEDIA_DIR 
		for(int i = 0; i < listOfFiles.length; i++)
			msg += listOfFiles[i].getName() + DELIM;

		return msg;
	}

	public void fetch(String fileName) throws FileNotFoundException, IOException {

		// File vars
		String path = "media/" + fileName;
		System.out.println("[Debug]: start fetching " + path);
		this.fileStream = new FileInputStream(new File(path));
		System.out.println("[Debug]: fileStream opened");
		
		// Packet vars
		int pktNb = 0;
		int length;
		MediaPacket mp = new MediaPacket();
		byte[] content = new byte[MediaPacket.CONTENT_SIZE];
		byte[] nextContent = new byte[MediaPacket.CONTENT_SIZE];

		System.out.println("[Debug]: packets created");

		// Communication vars
		Socket s = null;
		byte[] response = null;

		// Send start message
		sendMessage("FetchRes start");

		// Prepare data socket
		
		System.out.println("[Debug]: [Fetch]: port: " + (this.server.port + 1));

		System.out.println("[Debug]: waiting for data socket to open");
		System.out.flush();
		
		try{

			System.out.println("[Debug]: opening data socket");
			s = this.dataSocket.accept();
			System.out.println("[Debug]: data socket open");

			dataRes = new Scanner(s.getInputStream());
			dataOut = new DataOutputStream(s.getOutputStream());

		} catch(Exception e){
			
			System.err.println("[Debug @ fetch()]: failed to open data I/O stream, dropping fetch request...");
			sendMessage("FetchRes dropped");
			cleanupFetch(s, fileStream);
			return;
		}

		System.out.println("[Debug]: connected!");
		System.out.println("[Debug]: start sending packets");

		// Control
		int iteration;

		// Send file loop
		fileStream.read(content);
		while((length = fileStream.read(nextContent)) != -1){
		
			// Try to clean nextContent buffer if it's size varied
			if(length < nextContent.length){

				byte[] tmp = new byte[length];

				for (int i = 0; i < length; i++)
					tmp[i] = nextContent[i];
				nextContent = tmp;
			}

			// Prepare packet
			mp.setHeader(pktNb, content.length, (byte) 0);
			mp.setContent(content);

			// Send packet
			mp.sendPacket(dataOut);

			// Reset counter
			iteration = 0;

			// Wait for response
			while(true){
				if(dataRes.hasNextLine()){

					String msg = dataRes.nextLine();
					
					System.out.println("[Debug]: received \"" + msg + "\"");
					
					if(msg.equals("ok")) break;
					// If receive a fail message, re-send the packet
					else if(msg.equals("fail")){
						mp.sendPacket(dataOut);
						iteration = 0;
					}
				}
				// If got no response in MAX_ITERATIONS, stop fetching
				if(iteration++ > MAX_ITERATIONS){
					sendMessage("FetchRes dropped");
					cleanupFetch(s, fileStream);
					return;
				}
			}
			content = nextContent.clone();
			pktNb++;
		}

		// Send last packet
		System.out.println("[Debug]: sending last packet...");

		// Prepare packet
		mp.setHeader(pktNb, nextContent.length, (byte) 1);
		mp.setContent(nextContent);

		// Send packet
		mp.sendPacket(dataOut);

		// Reset counter
		iteration = 0;

		// Wait for response
		while(true){
			if(dataRes.hasNextLine()){

				String msg = dataRes.nextLine();
				System.out.println("[Debug]: received \"" + msg + "\"");
				
				if(msg.equals("ok")) break;
				// If receive a fail message, re-send the packet
				else if(msg.equals("fail")){
					mp.sendPacket(dataOut);
					iteration = 0;
				}
			}
			// If got no response in MAX_ITERATIONS, stop fetching
			if(iteration++ > MAX_ITERATIONS){
				sendMessage("FetchRes dropped");
				cleanupFetch(s, fileStream);
				return;
			}
		}

		// End fetch
		sendMessage("FetchRes end");
		System.out.println("[Debug]: file successfully uploaded");

		// Close streams & socket
		cleanupFetch(s, fileStream);
	}

	private void cleanupFetch(Socket s, FileInputStream fs){
		try{
			s.close();
			fs.close();
		} catch(IOException e){
			System.out.println("[Debug @ cleanupFetch()]: error closing socket");
		}
	}
}