import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.sql.*;

public class RegIdManager {
	static Connection con;
	
	public RegIdManager(){
		dbConnect();
	}
	
	public synchronized void writeToFile(String pNo, String regId){

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
	 * Get the profile picture from the database
	 * @throws SQLException 
	 * */
	public Map<String, byte[]> getImage(String pNo) throws SQLException{
		Map<String, byte[]> photoMap = new HashMap<>();
		String query = "Select * from chatServer.ProfileTable WHERE UserMobile ='" + pNo + "'";
		
        Statement s = con.createStatement();
        ResultSet rs = s.executeQuery(query);
        
        while (rs.next()) {
			photoMap.put(rs.getString(1), rs.getBytes(2));
		}
		return photoMap;
	}
	
	public boolean deleteContact(String mobile){
		PreparedStatement ps;
		try {
			ps = con.prepareStatement("DELETE FROM chatServer.RegTable WHERE Mobile = " + mobile);
			ps.executeUpdate();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}	
		return true;
	}

	/**
	 * Save the image to database
	 * */
	public void saveImage(String pNo, byte[] img){
		PreparedStatement ps;
		String sql = "INSERT INTO chatServer.ProfileTable(UserMobile, UserPhoto) VALUES (?,?)";
		try {

			if(getImage(pNo) != null | getImage(pNo).size()>0){
				sql = "UPDATE chatServer.ProfileTable SET UserMobile = ? WHERE UserPhoto=?";
			}
			
			ps = con.prepareStatement(sql);
			ps.setString(1, pNo);
			ps.setBytes(2, img);
			ps.executeUpdate();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**	
	 * creating database connection with auto shutdown
	 **/
	protected static void dbConnect(){
		String p = new File("src/db_script.script").getAbsolutePath();
		String connpath = p.substring(0,p.lastIndexOf(".")).replaceAll("/", "\\\\");

		try {
            Class.forName("org.hsqldb.jdbcDriver");
            System.out.println("loaded class");
            con = DriverManager.getConnection("jdbc:hsqldb:file:"+connpath+";shutdown=true", "sa", "");
            System.out.println("created con");
		}catch(ClassNotFoundException a){
			System.out.println("Exception: " + a);
        } catch (Exception e) {
            System.out.println("Exception: " + e);
        }
	}

	/**	
	 * End database connection
	 **/
	public void dbShutdown(){
		Statement st;
		try {
			System.out.println("SHUTTING DOWN...");
			st = con.createStatement();
			st.execute("SHUTDOWN");
			st.close();
		} catch (SQLException e) {
            System.out.println("SQL_Exception: " + e);
		} catch(NullPointerException a){
			System.out.println("NULL_Exception: " + a);
		}
	}
	

}
