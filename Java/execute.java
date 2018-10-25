
import java.io.InputStream;
import java.io.OutputStream;
import java.net.DatagramPacket;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;


public class execute {
	public static execute instance;
	
	JDBC database;
	ConcurrentHashMap <String, Client> CLientList;//helpful in removing clients nd udp connection
	
	//called from tcp or udp connection
	void  actionsFunction(String data,Object[] args)
	{
		String [] splitdata=data.split("/");
		
		switch(splitdata[0]) {
		
		case "login":
			
			
			loginORregister(splitdata, "login", args);
			
		    
			break;
			
		case "register":
			if(database.query(String.format("insert into PLAYER(Username,Password) values(%s,%s)","'"+splitdata[1]+"'","'"+splitdata[2]+"'")))
			{ 
				database.query(String.format("insert into SKINPURCHASED(Username) values(%s)", "'"+splitdata[1]+"'"));
				loginORregister(splitdata, "register", args);
				
			    
			}
			else {
				TcpServer.instance.sendData("registerunsuccessful", (OutputStream)args[1]);
			}
			break;
			
		case "IPforUDP":
			  //set up udp connection for client
			  DatagramPacket dp=(DatagramPacket)args[0];
		      Client cli= CLientList.get(splitdata[1]);
		      cli.ipaddress =dp.getAddress();
		     
		      TcpServer.instance.sendData("IPforUDPsuccessful", cli.output);
			break;
			
		case "logout":
			Client c=(Client)args[0];
			c.stopClient();
			
			if(c.opponentusername!=null)
			{
				CLientList.get(c.name).opponentusername=null;
			}
			
			CLientList.remove(c.name);
			newline();
			System.out.println(c.name+" logged out");
			System.out.println("Total Clients:"+CLientList.size());
			break;
			
			
		case "deleteaccount":
			if(database.query(String.format("delete from PLAYER where Username=%s","'"+splitdata[1]+"'")))
			{   Client cl=(Client)args[0];
			
			TcpServer.instance.sendData("deleteaccountsuccessful",cl.output);
				newline();
				System.out.println(cl.name+" account deleted");
				cl.stopClient();
				CLientList.remove(cl.name);
			}
			break;
		
		case "selectskin":
			if(database.query(String.format("update PLAYER set SkinIDuse=%s where Username=%s and exists (select SkinID from SKINPURCHASED where SkinID=%s)",splitdata[2],"'"+splitdata[1]+"'",splitdata[2])))
			{
				lobbyUpdate(splitdata, args);
			}
			else {
				Client cl=(Client)args[0];
				
				TcpServer.instance.sendData("selectskinunsuccessful",cl.output);
			}
				break;
		
				
		case "buyskin":
			
			
			if(database.query(String.format("update PLAYER set credit=credit-(select price from SKIN where SkinID=%s) where username='%s' and credit-(select price from skin where skinid=%s)>=0", splitdata[2],splitdata[1],splitdata[2])))
			{//credit aviable
				Client cl=(Client)args[0];
				if(database.query(String.format("insert into SKINPURCHASED values('%s',%s)",splitdata[1],splitdata[2])))
				{
					lobbyUpdate(splitdata, args);
				}
				else {
					database.query(String.format("update PLAYER set credit=credit+(select price from SKIN where SkinID=%s) where username='%s'", splitdata[2],splitdata[1]));
					TcpServer.instance.sendData("buyskinunsuccesful", cl.output);
				}
			}
			else {
				Client cl=(Client)args[0];
				TcpServer.instance.sendData("buyskinunsuccesful", cl.output);
			}
				break;
			
		case "play":
			Client cl=(Client)args[0];
			cl.play=true;
			
		     	TcpServer.instance.sendData("playsuccessful",cl.output);
				newline();
				System.out.println(cl.name+" ready to play");
				
			
			break;
		
		case "cancelmatchmaking":
			Client clin=(Client)args[0];
			clin.play=false;
			clin.opponentusername=null;
			newline();
			System.out.println(clin.name+" not ready to play");
			lobbyUpdate(splitdata, args);
			break;
			
			
		case "transform":
			//player position and rotation is obtained nd send to its opponent
			DatagramPacket d=(DatagramPacket)args[0];
			Client clie= CLientList.get(splitdata[1]);
			
			UdpServer.instance.sendData(new String(d.getData(),0,d.getLength()), CLientList.get(clie.opponentusername).ipaddress);
			
			break;
		
		case "bullethit":
			//when player gets hit by opponents bullet
			Client cl1=(Client)args[0];
			Client cl2=CLientList.get(cl1.opponentusername);
			TcpServer.instance.sendData("bullethit",cl2.output);
			break;
			
		case "idied":
			//when player dies
			Client cl3=(Client)args[0];
			Client cl4=CLientList.get(cl3.opponentusername);
			TcpServer.instance.sendData("opponentdied",cl4.output);
			newline();
			System.out.println(cl4.name+" won");
			//add points 
			database.query(String.format("update PLAYER set Score=Score+100,Credit=Credit+10 where Username='%s'",cl4.name));
			//remove them
			
			lobbyUpdate(new String[]{" ",cl3.name},new Object[] {cl3}  );
			lobbyUpdate(new String[]{" ",cl4.name},new Object[] {cl4}  );
			newline();
			System.out.println(cl3.name+" not ready to play");
			System.out.println(cl4.name+" not ready to play");
			
			cl3.opponentusername=null;
			cl4.opponentusername=null;
			cl3.play=false;
			cl4.play=false;
			break;
			
		 default:
			newline();
			System.out.println("Invalid command:"+splitdata[0]);
			Client clien=(Client)args[0];
			clien.stopClient();
			try {
				CLientList.get(clien.opponentusername).opponentusername=null;
			}
			catch (Exception e) {
				
			}
			
			
			CLientList.remove(clien.name);
			System.out.println(clien.name+" removed");
			System.out.println("Total Clients:"+CLientList.size()); 
			 break;
		}
	}
	
	void loginORregister(String[] splitdata,String msg,Object [] args) {
		if(CLientList.containsKey(splitdata[1]))
		{   //user already logged in
			TcpServer.instance.sendData(msg+"unsuccessful/piu", (OutputStream)args[1]);
			return;
		}
		
		//checking if user is valid,get score,credit,skiniduse
		ResultSet r=database.select(String.format("select Score,Credit,SkinIDuse from PLAYER where Username=%s and Password=%s","'"+splitdata[1]+"'","'"+splitdata[2]+"'" ));
		int score=0;
		int credit=0;
		int skin_in_use=0;
		
		int count=0;
		try {
			
			 
			
			while(r.next())
			{  score =r.getInt(1);
			   credit=r.getInt(2);
			   skin_in_use=r.getInt(3);
				count++;
			}
			
			if(count!=1)
			{   
				TcpServer.instance.sendData(msg+"unsuccessful", (OutputStream)args[1]);
				  return;  
			}
			
				
			
		} catch (Exception e) {
			e.printStackTrace();
			TcpServer.instance.sendData(msg+"unsuccessful", (OutputStream)args[1]);
		}
		//
		
		//valid add to online list
		CLientList.put(splitdata[1], new Client(splitdata[1],(InputStream)args[0],(OutputStream)args[1]));
		
        //get avialable skinids
	    String avialable_skins="";
	    r=database.select("select SkinID,Price from SKIN");
		try {
			 while(r.next())
			   {
				 avialable_skins+=r.getInt(1)+":"+r.getInt(2)+",";
				   
			   }
		} catch (Exception e) {
			
		}
		
		
		//get client skinids
				String client_skins="";
			    r=database.select(String.format("select SkinID from SKINPURCHASED where Username=%s","'"+splitdata[1]+"'"));
				try {
					 while(r.next())
					   {
						 client_skins+=r.getInt(1)+",";
						   
					   }
				} catch (Exception e) {
					
				}
				
		  
		
		
		
				String top_players="";
	    r=database.select("select username from PLAYER order by score desc limit 15");
		try {
			 while(r.next())
			   {
				 top_players+=r.getString(1)+",";
			   }
		} catch (Exception e) {
			
		}
		
		
		
		TcpServer.instance.sendData(msg+"successful"+"/"+score+"/"+credit+"/"+avialable_skins+"/"+skin_in_use+"/"+client_skins+"/"+top_players, (OutputStream)args[1]);
	    newline();
	    System.out.println(splitdata[1]+" logged in");
	    System.out.println("Total Clients:"+CLientList.size());
	} 

	void lobbyUpdate(String[] splitdata,Object [] args) {
		//sends player infomartion 
		//checking if user is valid,get score,credit,skiniduse
		
		ResultSet r=database.select(String.format("select Score,Credit,SkinIDuse from PLAYER where Username=%s ","'"+splitdata[1]+"'" ));
		int score=0;
		int credit=0;
		int skin_in_use=0;
		
		int count=0;
		try {
			
			 
			
			while(r.next())
			{  score =r.getInt(1);
			   credit=r.getInt(2);
			   skin_in_use=r.getInt(3);
				count++;
			}
			
			if(count!=1)
			{   
				
				  return;  
			}
			
				
			
		} catch (Exception e) {
			e.printStackTrace();
			
		}
		//
		
		//valid add to online list
		
		
        //get avialable skinids
	    String avialable_skins="";
	    r=database.select("select SkinID,price from SKIN");
		try {
			 while(r.next())
			   {
				 avialable_skins+=r.getInt(1)+":"+r.getInt(2)+",";
				 
			   }
		} catch (Exception e) {
			
		}
		
		
		//get client skinids
				String client_skins="";
			    r=database.select(String.format("select SkinID from SKINPURCHASED where Username=%s","'"+splitdata[1]+"'"));
				try {
					 while(r.next())
					   {
						 client_skins+=r.getInt(1)+",";
						   
					   }
				} catch (Exception e) {
					
				}
				
		  
		
		
		
				String top_players="";
	    r=database.select("select username from PLAYER order by score desc limit 15");
		try {
			 while(r.next())
			   {
				 top_players+=r.getString(1)+",";
			   }
		} catch (Exception e) {
			
		}
		
		
		
		TcpServer.instance.sendData("lobbyupdate"+"/"+score+"/"+credit+"/"+avialable_skins+"/"+skin_in_use+"/"+client_skins+"/"+top_players, ((Client)args[0]).output);
	}

	void newline() {
		System.out.println("*****************************************************");
	}
	
	public static void main(String[] args) {
		
		execute e=new execute();
		if(instance==null)
		{
			instance=e;
		}
		////////////////////////////
		instance.database=new JDBC("jdbc:mysql://localhost/MiniProject?allowPublicKeyRetrieval=true&useSSL=false", "root", "root");
        
		new TcpServer(3000);new UdpServer(3000, 5000);
		///////////////////////////
		
		//loop the online clients
		while(true)
		{  
			List<Client> clientlistcollection=new ArrayList<Client>(e.CLientList.values());
			
			Collections.shuffle(clientlistcollection);
			Object[] clientlistarray=clientlistcollection.toArray();
			//shuffle nd pair two clients if play==true and opponent ==null
			
			//matchmaking code
			for(int i=0;i<clientlistarray.length;i++)
			{   Client ci=(Client)clientlistarray[i];
			
			     if(ci.play && ci.opponentusername==null)
			     {   int j;
			         
			    	 for(j=i+1;j<clientlistarray.length ;j++)
					{ 
			    		 Client cj=(Client)clientlistarray[j];
			    		 
						if(cj.play && cj.opponentusername==null)
					     {
			    			 //set
							ci.setOpponent(cj.name);
							cj.setOpponent(ci.name);
							System.out.println(ci.name+" matched with "+cj.name);
							j++;
							//send msg to both...usernames,skinused
							String msg="letsplay";
							ResultSet r=e.database.select(String.format("select Username,SkinIDuse from PLAYER where Username='%s' or Username='%s'", ci.name,cj.name));
							
							try {
								while(r.next())
								{
									msg+="/"+r.getString(1)+","+r.getInt(2);
								}
							} catch (SQLException e1) {
								
							}
							TcpServer.instance.sendData(msg, ci.output);
							TcpServer.instance.sendData(msg, cj.output);
			    		     break;	 
					     }
						
					} 
			    	 
			    	 i=j;
			     }
				
			}
			//matchmaking code end
			
			
		}
		//loop the online clients ends
	}

}
