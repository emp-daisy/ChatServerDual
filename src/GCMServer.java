
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPException;

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
		
       
    @Override
		public void init() throws ServletException {
			// TODO Auto-generated method stub
			super.init();
			SmackClient ccsClient = new SmackClient();
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
		out.println("Hi<BR>");
		out.println("Calling DB<BR>");
        db = new RegIdManager();
		out.println("Called<BR>");
		
			try {
				String regKey = request.getParameter("RegNo");
				String mobile = request.getParameter("MobileNo");
				out.println("Entered<BR>");
				
				if(request.getParameter("Register") != null){
					db.writeToFile(mobile, regKey);
					System.out.println("Registered: " + mobile);
					out.println("Registered: " + mobile + "<BR>");
					request.setAttribute("pushStatus", "Registered.");
				}else if(request.getParameter("Type").equals("Feed")){
					String feed = request.getParameter("GCM_Feed");
					String text = request.getParameter("msg");
					out.println(feed);
					System.out.println(feed);
					System.out.println(text);
				}
				if(db != null){db.dbShutdown();}
			} catch (Exception ioe) {
				request.setAttribute("pushStatus","RegId required: " + ioe.toString());
			}finally{
				System.out.println("Push Status: " + request.getAttribute("pushStatus"));
				out.println("Push Status: " + request.getAttribute("pushStatus"));
				out.close();
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
