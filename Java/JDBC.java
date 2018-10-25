import java.sql.*;

public class JDBC {
final String JDBC_DRIVER="com.mysql.cj.jdbc.Driver";
Connection con;
Statement stmt;

public JDBC(String DB_url,String username,String password) {
	try {
		//Class.forName(JDBC_DRIVER);
		con=DriverManager.getConnection(DB_url, username, password);
		stmt=con.createStatement();
	} catch (Exception e) {
		e.printStackTrace();
	}

}

 boolean query(String query)
{
	try {
		int i=stmt.executeUpdate(query);
		if(i==0)
		{
			return false;
		}
	} catch (Exception e) {
		System.out.println(e.toString());
		return false;
	}
	return true;
}

 ResultSet select(String query)
{
	try {
		ResultSet r=stmt.executeQuery(query);
		return r;
	} catch (Exception e) {
		System.out.println(e.toString());
		return null;
	}
	
}

}
