# JAVA and RDBMS MINIPROJECT
Unity is used as front end which is a multiplayer game,java is used as backend,mysql database
![alt text](https://github.com/arjungaonkar/MiniProject/blob/master/images/Screenshot_20181025-190921.png "MiniProject")
## Java Side
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

5.execute:This class is the main class that connects all the other four classes,it contains actionFunction method which is called by                   TcpServer,UdpServer and Client class

![alt text](https://github.com/arjungaonkar/MiniProject/blob/master/images/Screenshot%20(329).png "MiniProject")

[Source code of Java side](https://github.com/arjungaonkar/MiniProject/tree/master/Java)
## RDBMS Side
Screenshot of table list and tables

![alt text](https://github.com/arjungaonkar/MiniProject/blob/master/images/Screenshot%20(331).png "MiniProject")

Queries used in miniproject database:

1.For login verification,if returned table has one row then valid,else invalid
>select Score,Credit,SkinIDuse from PLAYER where Username=%s and Password=%s

2.For login verification,if returned table has one row then valid,else invalid
>select Score,Credit,SkinIDuse from PLAYER where Username=%s and Password=%s

