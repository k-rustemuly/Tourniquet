package StandTcp;
import java.sql.*;

public class DB {
    final String DB_URL = "jdbc:mysql://localhost/ss";
    final String USER = "root";
    final String PASS = "";

    public Statement con(){
        try{
            Class.forName("com.mysql.cj.jdbc.Driver");
            Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);
            return conn.createStatement();
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
        return null;
    }
}
