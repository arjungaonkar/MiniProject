import java.io.*;
import java.net.InetAddress;


class Client extends Thread{
	String name;
	
	InputStream input;
	OutputStream output;
	InetAddress ipaddress;
	
	boolean play;
	String  opponentusername;
	
	boolean listen;
	public Client(String name,InputStream input,OutputStream output) {
		this.name=name;
		
		this.input=input;
		this.output=output;
		this.play=false;
		this.listen=true;
		start();
	}
	
    void setOpponent(String  opponentusername) {
		this.opponentusername=opponentusername;
	}
	
	@Override
	public void run() {
		
		while(listen)
		{   //listen to TCP connection
			try {
				
				
		 		byte[] lenBytes = new byte[4];
		 	     input.read(lenBytes, 0, 4);
		 	     int len = (((lenBytes[3] & 0xff) << 24) | ((lenBytes[2] & 0xff) << 16) |
		 	               ((lenBytes[1] & 0xff) << 8) | (lenBytes[0] & 0xff));
		 	     byte[] receivedBytes = new byte[len];
		 	     input.read(receivedBytes, 0, len);
		 	     String received = new String(receivedBytes, 0, len);
		 	     
			    execute.instance.actionsFunction(received, new Object[] {this});
	            		    
		} catch (Exception e) {
			
		}
			
			
			
		}
		
	}
	
	void stopClient() {
		listen=false;
		try {
			input.close();
			output.close();
		} catch (IOException e) {
			
			
		}
		
		interrupt();
	}
	
	
}
