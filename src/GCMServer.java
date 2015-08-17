
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

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
		
		//RequestDispatch page
		String page_url;
		RegIdManager db;
		
       
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

        db = new RegIdManager();
			try {
				out.println("Calling DB<BR>");

				out.println("Called<BR>");
				String regKey = request.getParameter("RegNo");
				String mobile = request.getParameter("MobileNo");
				out.println("Entered<BR>");
				
				if(request.getParameter("Register") != null){
					db.writeToFile(mobile, regKey);
					System.out.println("Registered");
					request.setAttribute("pushStatus", "Registered.");
				}
				else if(request.getParameter("Message") != null){
					String userMessage = request.getParameter("msg");
					String mobileNumTo = request.getParameter("MobileNumberTo");
					Set<String> regIdSet = db.readFromFile(mobileNumTo);
					System.out.println(regIdSet);
					String toDeviceRegId = (String) (regIdSet.toArray())[0];
					System.out.println(toDeviceRegId);
					out.println("ToDeviceRegID: " + toDeviceRegId + "<BR>");
					SmackClient.sendMessage(toDeviceRegId, GOOGLE_SERVER_KEY, userMessage);
					request.setAttribute("pushStatus", "Message Sent.");
				}
				page_url = "index.jsp";
				db.dbShutdown();
			} catch (Exception ioe) {
				request.setAttribute("pushStatus","RegId required: " + ioe.toString());
				page_url = "error.jsp";
			}finally{
				System.out.println("Push Status: " + request.getAttribute("pushStatus"));
				out.close();
			}
			//RequestDispatcher r = request.getRequestDispatcher(page_url);
			//r.forward(request, response);
	}

	@Override
	public void destroy() {
		// TODO Auto-generated method stub
		super.destroy();
		if(db != null)
			db.dbShutdown();
	}
	
	

}
