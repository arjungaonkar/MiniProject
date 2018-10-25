import java.io.IOException;
import java.net.*;

public class UdpServer extends Thread {
	public static UdpServer instance;
	
	int port;
	int bytelength;
	DatagramSocket s;
    DatagramPacket p;
    DatagramSocket f;
    
public UdpServer(int port,int bytelength)  {
	if(instance==null)
	{
		instance=this;
	}
	this.port=port;
	this.bytelength=bytelength;
	
	try {
		s=new DatagramSocket(this.port+1);
		f=new DatagramSocket();
		System.out.println("--------------------UDP SERVER RUNNING-------------------");
		
		//start the listener loop
		start();
	} catch (SocketException e) {
		execute.instance.newline();
		e.printStackTrace();
	}
	}

@Override
public void run() {
while(true)
{try {
	
	byte[] b=new byte[bytelength];
	p=new DatagramPacket(b, b.length);
	try {
		
		s.receive(p);
		//data
		String msg=new String(p.getData(),0,p.getLength());
		execute.instance.actionsFunction(msg, new Object[] {p});
		
		
		
	} catch (IOException e) {
		
		
	}
	
	
} catch (Exception e) {
	
}
	}
	
}

void sendData(String msg,InetAddress client) {
	
	DatagramPacket pt=new DatagramPacket(msg.getBytes(),msg.length(),client,port+2);
	try {
		
		f.send(pt);
		
	} catch (IOException e) {
		
		e.printStackTrace();
	}

}

}
