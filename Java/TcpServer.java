import java.io.*;
import java.net.*;
import java.util.concurrent.ConcurrentHashMap;


public class TcpServer extends Thread {
    static TcpServer instance;
  
	int port;
	ServerSocket s;
	
	

	
public TcpServer(int port) {
	if(instance==null)
	{
		instance=this;
	}
	
	this.port=port;
	try {
		s=new ServerSocket(this.port);
		System.out.println("--------------------TCP SERVER RUNNING-------------------");
		execute.instance.CLientList=new ConcurrentHashMap<String, Client>();
		//start the listener loop
		start();
	} catch (Exception e) {
		execute.instance.newline();
		e.printStackTrace();
	}
	
}


@Override
public void run() {
	while(true)
	{
		try {
			Socket link=s.accept();
			
			InputStream input = link.getInputStream();
	        OutputStream output = link.getOutputStream();
		
	        execute.instance.actionsFunction(reciveData(input),new Object[] {input,output});
	        
		} catch (IOException e) {
			
		}
	}
	
}


String reciveData(InputStream input)  {
	 	try {
		
		
	 		byte[] lenBytes = new byte[4];
	 	     input.read(lenBytes, 0, 4);
	 	     int len = (((lenBytes[3] & 0xff) << 24) | ((lenBytes[2] & 0xff) << 16) |
	 	               ((lenBytes[1] & 0xff) << 8) | (lenBytes[0] & 0xff));
	 	     byte[] receivedBytes = new byte[len];
	 	     input.read(receivedBytes, 0, len);
	 	     String received = new String(receivedBytes, 0, len);

	 	     

		 
		return received;
	} catch (Exception e) {
		execute.instance.newline();
		e.printStackTrace();
	}
	return "";
}

void sendData(String msg,OutputStream output)  {
	try {
		    String toSend = msg;
	        byte[] toSendBytes = toSend.getBytes();
	        int toSendLen = toSendBytes.length;
	        byte[] toSendLenBytes = new byte[4];
	        toSendLenBytes[0] = (byte)(toSendLen & 0xff);
	        toSendLenBytes[1] = (byte)((toSendLen >> 8) & 0xff);
	        toSendLenBytes[2] = (byte)((toSendLen >> 16) & 0xff);
	        toSendLenBytes[3] = (byte)((toSendLen >> 24) & 0xff);
	        output.write(toSendLenBytes);
	        output.write(toSendBytes);
	} catch (Exception e) {
		e.printStackTrace();
	}
	
}




}

