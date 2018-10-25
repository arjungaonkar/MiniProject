# MiniProject
java and rdbms miniproject
There are five classes
1.TcpServer
2.UdpServer
3.Client
4.JDBC
5.execute

1.TcpServer:Runs as separate thread which listens for new clients.Once new clients are connected, CLient class object is created(only if               username and password sent is valid or new registration) which runs as separated threads.

2.UdpServer:Runs as separate thread which is used to transfer postion and rotation of one player to another.

3.Client:It holds Tcp socket object,client Ip address which is used for UDP,username,play(boolean,true->ready to play),opponent username.
         Its object is created once logged in or registered,runs as separate thread,listening for data.
         
4.JDBC:Database connectivity

5.execute:This class is the main class that connects all the other four class,it contains actionFunction method which is called by                   TcpServer,UdpServer and Client class
