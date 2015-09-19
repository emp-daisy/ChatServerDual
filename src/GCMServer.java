
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.imageio.ImageIO;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.bind.DatatypeConverter;

import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.util.stringencoder.Base64;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

/**
 * Servlet implementation class GCMServer
 */
@WebServlet("/GCMServer")
public class GCMServer extends HttpServlet {
	
	private static final long serialVersionUID = 1L;
    // Put your Google API Server Key here
	private static final String GOOGLE_SERVER_KEY = "AIzaSyDZ60w-JN-RzBHk1litPqzKtzqThmZnpaY";
	// Put your Google Project number here
	final String GOOGLE_USERNAME = "512212818580" + "@gcm.googleapis.com";
	RegIdManager db;
	String url = "http://localhost:8080/ChatServerDual/";
	String page;
	SmackClient ccsClient;
		
       
    @Override
	public void init() throws ServletException {
			// TODO Auto-generated method stub
			super.init();
			ccsClient = new SmackClient();
				try {
					ccsClient.connect("f", GOOGLE_SERVER_KEY);
				} catch (XMPPException | SmackException | IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			
		}

	/**
     * @see HttpServlet#HttpServlet()
     */
    public GCMServer() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		doPost(request, response);
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		PrintWriter out = response.getWriter();
        db = new RegIdManager();
		
			try {				
				if(request.getParameter("Register") != null){
					String regKey = request.getParameter("RegNo");
					String mobile = request.getParameter("MobileNo");
					db.writeToFile(mobile, regKey);
					System.out.println("Registered: " + mobile);
					out.println("REGISTERED");
					request.setAttribute("pushStatus", "Registered.");
				}else if(request.getParameter("Photo") != null){
					String jsonPhoneList = request.getParameter("ContactList");
					String userOwner = request.getParameter("UserOwner");
					String profilePic = request.getParameter("ProfilePic");
					System.out.println("User Owner: " + userOwner);
					sendProfilePhoto(jsonPhoneList, userOwner, profilePic);			
				}else if(request.getParameter("Contacts") != null){
					String theList = request.getParameter("List");
					String number = request.getParameter("Phone");
					System.out.println("The host number is: " + number);
					getContacts(theList, number);
				}
			} catch (Exception ioe) {
				out.println("ERROR");
				out.println("pushStatus: RegId required: " + ioe.toString());
				request.setAttribute("pushStatus","RegId required: " + ioe.toString());
			}finally{
				if(db != null){db.dbShutdown();}
				System.out.println("Push Status: " + request.getAttribute("pushStatus"));
				out.close();
			}
	}
	
	private void GetPhoto(String jsonList, String userOwner, String profilePic){
		Gson gson = new Gson();
		TypeToken<List<String>> token = new TypeToken<List<String>>() {};
		List<String> phoneContacts = gson.fromJson(jsonList, token.getType());
		ArrayList<Map<String, byte[]>> sendPhoto = new ArrayList<>();
		Set<String> regIdSet = null;
		for(String x : phoneContacts){
			Map<String, byte[]> mapPhoto;
			try {
				mapPhoto = db.getImage(x);
				if(!mapPhoto.isEmpty()){
					sendPhoto.add(mapPhoto);
				}
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			//Get regId of the number
			try {
				regIdSet = db.readFromFile(x);
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		//Send the Phhoto
		try {
			ccsClient.sendContactsPhoto((String)regIdSet.toArray()[0], GOOGLE_SERVER_KEY, sendPhoto);
		} catch (ClassNotFoundException | SmackException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private void sendProfilePhoto(String jsonList, String userOwner, String profilePic){
		Gson gson = new Gson();
		TypeToken<List<String>> token = new TypeToken<List<String>>() {};
		ArrayList<String> phoneContacts = gson.fromJson(jsonList, token.getType());
		byte[] imageByte = Base64.decode(profilePic);
		db.saveImage(userOwner, imageByte);
		generatePhoto(userOwner, imageByte);
		ArrayList<String> list = new ArrayList<String>();
		for(String x : phoneContacts){
			Set<String> regIdSet;
			try {
				regIdSet = db.readFromFile(x);
				list.add((String)regIdSet.toArray()[0]);
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		try{
			ccsClient.sendPhoto(list, userOwner);
		}catch(ClassNotFoundException | SmackException | IOException e){
			// TODO Auto-generated catch block
			e.printStackTrace();	
		}
	}
	
	private void getContacts(String list, String phone){
		Gson gson = new Gson();
		TypeToken<List<String>> token = new TypeToken<List<String>>() {};
		List<String> phoneContacts = gson.fromJson(list, token.getType());
		System.out.println("PHONE CONTACT: " + phoneContacts);
		ArrayList<String> sendContacts = new ArrayList<>();
		for(String x : phoneContacts){
			Set<String> regIdSet;
			try {
				x = x.replaceAll("\\s", "");
				regIdSet = db.readFromFile(x);
				if(!regIdSet.isEmpty()){
					String numb = (String) (regIdSet.toArray())[0];
					if(numb != null)
						sendContacts.add(x);
				}
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			try {
				System.out.println("PHONE:  " + phone);
				ccsClient.sendContacts(phone, GOOGLE_SERVER_KEY, sendContacts);
			} catch (ClassNotFoundException | SmackException | IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	private void generatePhoto(String mobile, byte[] img){
		FileOutputStream fileOuputStream;
		try {
			fileOuputStream = new FileOutputStream("WebContent/WEB-INF/ProfilePics/" + mobile + ".jpg");
			fileOuputStream.write(img);
			fileOuputStream.close();
		} catch (FileNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
	}
	
	@Override
	public void destroy() {
		// TODO Auto-generated method stub
		super.destroy();
		if(db != null)
			db.dbShutdown();
	}
	
}
