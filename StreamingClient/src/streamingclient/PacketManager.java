// checked
package streamingclient;

import java.io.*;
import java.lang.IllegalArgumentException;
import java.net.Socket;

class PacketManager extends Thread {

	// Packet constants
	public static final int KB = 1024;
	public static final int MB = 1024*KB;
	public static final int GB = 1024*MB;

	public static final int HEADER_SIZE = 4+4+1; // int + int + byte
	public static final int PACKET_SIZE = 32*KB;
	public static final int CONTENT_SIZE = PACKET_SIZE - HEADER_SIZE;

	private Socket socket;
	private DataInputStream dataIn;
	private PrintStream dataOut;
	private String packetName;
	
	// Packet header vars
	private int pktNb = 0;
	private int contentLength;
	private boolean done = false;

	// Packet content
	private byte[] content;

	public PacketManager(Socket dataSocket, String name) throws IOException {

		this.socket = dataSocket;
		this.dataIn = new DataInputStream(dataSocket.getInputStream());
		this.dataOut = new PrintStream(dataSocket.getOutputStream());
		this.packetName = name.split(StreamingClient.DELIM)[1];
		System.out.println("[Debug]: packet name: \"" + this.packetName + "\"");
	}

	public synchronized byte[] getContent() { return this.content; }

	@Override
	public void run(){

		// File handlers
		File newFile = null;
		FileOutputStream fileStream = null;

		// Control
		int lastPktNb = -1;

		try{
			newFile = new File(StreamingClient.MEDIA_DIR + this.packetName);
			fileStream= new FileOutputStream(newFile);
		} catch(Exception e){
			System.err.println("[Debug @ PacketManager.run()]: failed to open file...");
			return;
		}

		// Packet content
		byte[] pktContent = new byte[PACKET_SIZE];

		// Bytes read
		int read = 0;
		// Send status diagnosis to server
		String status;

		while(!done){

			// Read header first
			try{
				this.dataIn.read(pktContent, 0, HEADER_SIZE);
				readHeader(pktContent);
			} catch(IllegalArgumentException e){
				
				System.err.println("[Debug @ PacketManager.run()]: error reading file");
				try{
					this.socket.close();
					fileStream.close();
				} catch(Exception ex){}
				
				return;

			} catch(Exception e){}

			// Wait for packet
			try{
				read = this.dataIn.read(pktContent, 0, CONTENT_SIZE);
			} catch(Exception e){}

			// Check if last read was valid
			if(read == -1) this.dataOut.println("fail");
			else if(read > 0 && lastPktNb == this.pktNb-1){
				
				// Unpack
				try{
					readContent(pktContent);
				} catch(IllegalArgumentException e){
					
					System.err.println("[Debug @ PacketManager.run()]: error reading file");
					try{
						this.socket.close();
						fileStream.close();
					} catch(Exception ex){}
					
					return;

				} catch(Exception e){} 
				
				// Write packet
				try{
					System.out.println("[Debug]: writing to file");
					fileStream.write(this.content, 0, this.contentLength);
					status = "ok";
				} catch(IOException e){
					status = "fail";
				}
				System.out.println("[Debug]: sending status \"" + status + "\"");
				this.dataOut.println(status);
				lastPktNb = pktNb;
			}
		}
		
		// Close file
		try{
			fileStream.close();
		} catch(IOException e){
			System.err.println("[Debug @ PacketManager.run()]: failed to close file");
		}

		System.out.println("[Debug]: pm thread returning");
	}

	private void readPacket(byte[] packet) throws IllegalArgumentException {
		readHeader(packet);
		readContent(packet);
	}

	private void readHeader(byte[] packet) throws IllegalArgumentException {

		// Read header
		this.pktNb = ( 	( ((int) packet[0] & 0xFF) <<  0)   |
				( ((int) packet[1] & 0xFF) <<  8)   |
				( ((int) packet[2] & 0xFF) << 16)  |
				( ((int) packet[3] & 0xFF) << 24)    );

		this.contentLength = (	( ((int) packet[4] & 0xFF) <<  0)   |
					( ((int) packet[5] & 0xFF) <<  8)   |
					( ((int) packet[6] & 0xFF) << 16)  |
					( ((int) packet[7] & 0xFF) << 24)    );
		
		this.done = (packet[8] == 1);

		// Check header integrity
		if((this.pktNb < 0) ||
		   (this.contentLength <= 0) ||
		   (this.contentLength > CONTENT_SIZE)){
			System.out.println("[Debug]: [Fetch]: [ReadHeader Throw]: " +
				"pktNb " + this.pktNb);
			System.out.println("[Debug]: [Fetch]: [ReadHeader Throw]: " +
				"contentLength " + this.contentLength);
			throw new IllegalArgumentException();
		}
	}
	
	private void readContent(byte[] packet){
		
		this.content = new byte[this.contentLength];

		for (int i = 0; i < this.contentLength; i++)
			this.content[i] = packet[i];
	}
}