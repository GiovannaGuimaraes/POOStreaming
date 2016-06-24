// checked
package streamingserver;

import java.io.DataOutputStream;
import java.io.IOException;

public class MediaPacket {


	// Packet constants
	public static final int KB = 1024;
	public static final int MB = 1024*KB;
	public static final int GB = 1024*MB;

	public static final int HEADER_SIZE = 4+4+1; // int + int + byte
	public static final int PACKET_SIZE = 32*KB;
	public static final int CONTENT_SIZE = PACKET_SIZE - HEADER_SIZE;

	private byte[] content;


	public MediaPacket(){
		this.content = new byte[PACKET_SIZE];
	}


	public void setHeader(int id, int contentSize, byte end){

		// Prepare prepare header
		// Packet number
		this.content[0] = (byte) (id >> 0);
		this.content[1] = (byte) (id >> 8);
		this.content[2] = (byte) (id >> 16);
		this.content[3] = (byte) (id >> 24);

		// Content size
		this.content[4] = (byte) (contentSize >> 0);
		this.content[5] = (byte) (contentSize >> 8);
		this.content[6] = (byte) (contentSize >> 16);
		this.content[7] = (byte) (contentSize >> 24);

		// Last packet flag
		this.content[8] = end;
	}

	public void setContent(byte[] content){
		for (int i = HEADER_SIZE; i < content.length+HEADER_SIZE; i++)
			this.content[i] = content[i - HEADER_SIZE];
	}

	public void sendPacket(DataOutputStream dataOut) throws IOException {
		dataOut.write(this.content);
		dataOut.flush();
	}

	@Override
	public String toString(){
		String s = "";
		for(int i = 8; i < this.content.length; i++){
			s += (char) this.content[i] + " ";
		}
		return s;
	}
}