package Server;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DataBaseUtil 
{
    public static Connection getConnection() throws SQLException 
    {
    	Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/riotgames", "root", "");
    	conn.setAutoCommit(false);
        return conn;
    }

}
