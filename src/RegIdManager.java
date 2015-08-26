import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;
import java.sql.*;

public class RegIdManager {
	static Connection con;
	
	public RegIdManager(){
		String p = new File("src/db_script.script").getAbsolutePath();
		String connpath = p.substring(0,p.lastIndexOf(".")).replaceAll("/", "\\\\");

		try {
            Class.forName("org.hsqldb.jdbcDriver");
            System.out.println("loaded class");
            con = DriverManager.getConnection("jdbc:hsqldb:file:"+connpath, "sa", "");
            System.out.println("created con");
		}catch(ClassNotFoundException a){
			System.out.println("Exception: " + a);
        } catch (Exception e) {
            System.out.println("Exception: " + e);
        }
	}
	
	public void writeToFile(String pNo, String regId){

        PreparedStatement ps;
		try {
			if(readFromFile(pNo).isEmpty()){
				ps = con.prepareStatement("INSERT INTO chatServer.RegTable(Mobile, RegId) VALUES (?,?)");
		        ps.setString(1, pNo);
		        ps.setString(2, regId);
		        ps.executeUpdate();	
			}
			else{
				ps = con.prepareStatement("UPDATE chatServer.RegTable SET RegId = ? WHERE Mobile=?");
				ps.setString(1, regId);
		        ps.setString(2, pNo);
		        ps.executeUpdate();
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	

	public Set<String> readFromFile(String mobi) throws SQLException{
		Set<String> regIdSet = new HashSet<String>();
		String query = "Select * from chatServer.RegTable WHERE Mobile ='" + mobi + "'";

        Statement s = con.createStatement();
        ResultSet rs = s.executeQuery(query);
        
        while (rs.next()) {
			regIdSet.add(rs.getString(2));
		}
		return regIdSet;
		
		}
 	

	/**	
	 * End database connection
	 **/
	public void dbShutdown(){
		Statement st;
		try {
			st = con.createStatement();
			st.execute("SHUTDOWN");
			System.out.println("SHUTTING DOWN");
			con.close();
		} catch (SQLException e) {
            System.out.println("Exception: " + e);
		} catch(NullPointerException a){
			System.out.println("Exception: " + a);
		}
	}
	
	/**	
	 * creating database connection
	 **/
	protected static void dbConnect(){
		try {
            Class.forName("org.hsqldb.jdbc.JDBCDriver");
            System.out.println("loaded class");
            con = DriverManager.getConnection("jdbc:hsqldb:C:\\Users\\tunde_000\\Documents\\myGit\\ChatServerDual\\src\\db_script", "sa", "");
            System.out.println("created con");
        } catch (Exception e) {
            System.out.println("Exception: " + e);
        }
	}
}
