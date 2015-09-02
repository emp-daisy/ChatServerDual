import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.net.ssl.SSLSocketFactory;

import org.jivesoftware.smack.ConnectionListener;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.SmackException.NotConnectedException;
import org.jivesoftware.smack.StanzaListener;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.ConnectionConfiguration.SecurityMode;
import org.jivesoftware.smack.filter.StanzaFilter;
import org.jivesoftware.smack.packet.DefaultExtensionElement;
import org.jivesoftware.smack.packet.ExtensionElement;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Stanza;
import org.jivesoftware.smack.provider.ExtensionElementProvider;
import org.jivesoftware.smack.provider.ProviderManager;
import org.jivesoftware.smack.roster.Roster;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.jivesoftware.smack.tcp.XMPPTCPConnectionConfiguration;
import org.jivesoftware.smack.util.StringUtils;
import org.json.simple.JSONValue;
import org.json.simple.parser.ParseException;
import org.xmlpull.v1.XmlPullParser;



/*import com.google.android.gcm.server.Message;*/
import com.google.android.gcm.server.MulticastResult;
import com.google.android.gcm.server.Sender;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;



public class SmackClient {
	static final String MESSAGE_KEY = "SERVER_MESSAGE";
	Logger logger = Logger.getLogger("SmackCcsClient");
	private static final String GOOGLE_SERVER_KEY = "AIzaSyDZ60w-JN-RzBHk1litPqzKtzqThmZnpaY";

	public static final String GCM_SERVER = "gcm.googleapis.com";
	public static final int GCM_PORT = 5235;
	public static final String YOUR_PROJECT_ID = "25515784135";
	
	public static final String GCM_ELEMENT_NAME = "gcm";
	public static final String GCM_NAMESPACE = "google:mobile:data";
	
	static Random random = new Random();
	XMPPTCPConnection  connection;
	XMPPTCPConnectionConfiguration  config;
	
	static {

        ProviderManager.addExtensionProvider(GCM_ELEMENT_NAME, GCM_NAMESPACE, new  ExtensionElementProvider<ExtensionElement>() {
            @Override
            public DefaultExtensionElement parse(XmlPullParser parser,int initialDepth) throws org.xmlpull.v1.XmlPullParserException,
            IOException {
                String json = parser.nextText();
                return new GcmPacketExtension(json);
            }
        });
    }
	
	public void send(String jsonRequest) throws NotConnectedException {
		Stanza request = new GcmPacketExtension(jsonRequest).toPacket();
		connection.sendStanza(request);
	}
	
	public void connect(String senderId, String apiKey) throws XMPPException, SmackException, IOException{
	
		XMPPTCPConnectionConfiguration config =
    			XMPPTCPConnectionConfiguration.builder()
    			.setServiceName(GCM_SERVER)
    		     .setHost(GCM_SERVER)
    		     .setCompressionEnabled(false)
    		     .setPort(GCM_PORT)
    		     .setConnectTimeout(30000)
    		     .setSecurityMode(SecurityMode.disabled)
    		     .setSendPresence(false)
    		     .setSocketFactory(SSLSocketFactory.getDefault())
    		    .build();
		
		connection = new XMPPTCPConnection(config);
		Roster roster = Roster.getInstanceFor(connection);  
	    roster.setRosterLoadedAtLogin(false); 
		logger.info("Connecting...");
		connection.connect();
		connection.addConnectionListener(new ConnectionListener() {

			@Override
			public void reconnectionSuccessful() {
				logger.info("Reconnecting..");
				System.out.println("Reconnecting..");
			}

			@Override
			public void reconnectionFailed(Exception e) {
				logger.log(Level.INFO, "Reconnection failed.. ", e);
				System.out.println("Reconnection failed.. " +  e);
			}

			@Override
			public void reconnectingIn(int seconds) {
				logger.log(Level.INFO, "Reconnecting in %d secs", seconds);
			}

			@Override
			public void connectionClosedOnError(Exception e) {
				logger.log(Level.INFO, "Connection closed on error.");
			}

			@Override
			public void connectionClosed() {
				logger.info("Connection closed.");
				System.out.println("Connection closed.");
			}

			@Override
			public void connected(XMPPConnection arg0) {
				// TODO Auto-generated method stub
				//logger.info(".");
				System.out.println("Connected.");
			}

			@Override
			public void authenticated(XMPPConnection arg0, boolean arg1) {
				// TODO Auto-generated method stub
				
			}

		});

		connection.login(YOUR_PROJECT_ID + "@gcm.googleapis.com" , apiKey);
		
		FilterStanza filter = new FilterStanza();
		StanzaListenTool stanza = new StanzaListenTool();
		connection.addAsyncStanzaListener(stanza, filter);
	}
	
	public void sendMessage(String toDeviceRegId, final String GOOGLE_SERVER_KEY , String message, String contactId, String sender, String type, String time) throws SmackException, IOException, ClassNotFoundException {
		
		if(contactId == "")
			contactId = "07944447710";
		if(type == "")
			type = "msg";
		if(time == "")
			time = "05/05/05 22:00:00";
			
		String messageId = getRandomMessageId();
		Map<String, String> payload = new HashMap<String, String>();
		payload.put("GCM_msg", message);
		payload.put("Type", type);
		payload.put("GCM_contactId", contactId);
		payload.put("GCM_time", time);
		payload.put("GCM_sender", sender);
		String collapseKey = "sample";
		Long timeToLive = 10000L;
		Boolean delayWhileIdle = true;
		send(createJsonMessage(toDeviceRegId, messageId, payload, collapseKey, timeToLive, delayWhileIdle));
		System.out.println("Message: '" + message + "' has been sent to " + toDeviceRegId);
	}
	
	public void sendContacts(String toDeviceRegId, final String GOOGLE_SERVER_KEY , ArrayList<String> message) throws SmackException, IOException, ClassNotFoundException {
			
		String messageId = getRandomMessageId();
		Map<String, String> payload = new HashMap<String, String>();
		Gson gson = new Gson();
		String jsonPhoneList = gson.toJson(message);
		payload.put("Type", "Contacts");
		payload.put("Contacts", jsonPhoneList);
		String collapseKey = "sample";
		Long timeToLive = 10000L;
		Boolean delayWhileIdle = true;
		send(createJsonMessage(toDeviceRegId, messageId, payload,
				collapseKey, timeToLive, delayWhileIdle));
		System.out.println("App Contacts: " + message);
		System.out.println("Contacts have been sent");
	}
	
	public void sendFeed(ArrayList<String> contactList,String message, String from, String time) throws SmackException, IOException, ClassNotFoundException {
		Sender sender = new Sender(GOOGLE_SERVER_KEY);
		com.google.android.gcm.server.Message messageBuilder = new com.google.android.gcm.server.Message.Builder().timeToLive(30)
				.delayWhileIdle(true)
				.addData("Type", "Feed")
				.addData("msg", message)
				.addData("GCM_FROM", from)
				.addData("GCM_time", time)
				.build();
		
		System.out.println("feed msg: " + message);
		System.out.println("feed from: " + from);
		for(String x : contactList)
			System.out.println(x);
		MulticastResult result = sender.send(messageBuilder, contactList, 1);
		System.out.println(result);
	}
	
	 //XML Packet Extension
	private static final class GcmPacketExtension extends DefaultExtensionElement{

		String json;
		
		public GcmPacketExtension(String json) {
			super(GCM_ELEMENT_NAME, GCM_NAMESPACE);
			// TODO Auto-generated constructor stub
			this.json = json;
		}
		
		public String getJson() {
			return json;
		}
		
		@Override
        public String toXML() {
            return String.format("<%s xmlns=\"%s\">%s</%s>",
                    GCM_ELEMENT_NAME, GCM_NAMESPACE,
                    StringUtils.escapeForXML(json), GCM_ELEMENT_NAME);
        }

		public String getRandomMessageId() {
			return "m-" + Long.toString(random.nextLong());
		}
		
		public Stanza toPacket() {
            Message message = new Message();
            message.addExtension(this);
            return message;
        }
		
	}

	
	/**
	 * Creates a JSON encoded GCM message.
	 *
	 * @param to
	 *            RegistrationId of the target device (Required).
	 * @param messageId
	 *            Unique messageId for which CCS will send an "ack/nack"
	 *            (Required).
	 * @param payload
	 *            Message content intended for the application. (Optional).
	 * @param collapseKey
	 *            GCM collapse_key parameter (Optional).
	 * @param timeToLive
	 *            GCM time_to_live parameter (Optional).
	 * @param delayWhileIdle
	 *            GCM delay_while_idle parameter (Optional).
	 * @return JSON encoded GCM message.
	 */
	public static String createJsonMessage(String to, String messageId,
			Map<String, String> payload, String collapseKey, Long timeToLive,
			Boolean delayWhileIdle) {
		Map<String, Object> message = new HashMap<String, Object>();
		message.put("to", to);
		if (collapseKey != null) {
			message.put("collapse_key", collapseKey);
		}
		if (timeToLive != null) {
			message.put("time_to_live", timeToLive);
		}
		if (delayWhileIdle != null && delayWhileIdle) {
			message.put("delay_while_idle", true);
		}
		message.put("message_id", messageId);
		message.put("data", payload);
		return JSONValue.toJSONString(message);
	}
	
	/**
	 * Creates a JSON encoded ACK message for an upstream message received from
	 * an application.
	 *
	 * @param to
	 *            RegistrationId of the device who sent the upstream message.
	 * @param messageId
	 *            messageId of the upstream message to be acknowledged to CCS.
	 * @return JSON encoded ack.
	 */
	public static String createJsonAck(String to, String messageId) {
		Map<String, Object> message = new HashMap<String, Object>();
		message.put("message_type", "ack");
		message.put("to", to);
		message.put("message_id", messageId);
		return JSONValue.toJSONString(message);
	}
	
	public String getRandomMessageId() {
		return "m-" + Long.toString(random.nextLong());
	}
	
	/**
	 * Handles an upstream data message from a device application.
	 *
	 * <p>
	 * This sample echo server sends an echo message back to the device.
	 * Subclasses should override this method to process an upstream message.
	 * @throws NotConnectedException 
	 */
	public void handleIncomingDataMessage(Map<String, Object> jsonObject) throws NotConnectedException {
		
		jsonObject.get("category").toString();

		// Use the packageName as the collapseKey in the echo packet
		@SuppressWarnings("unchecked")
		Map<String, String> payload = (Map<String, String>) jsonObject.get("data");

		String action = payload.get("Type");
		System.out.println("Action is: " + action);
		
		if ("msg".equals(action)) {
			String type = payload.get("Type");
			String userMessage = payload.get("GCM_msg");
			String time = payload.get("GCM_time");
			String mobileNumTo = payload.get("GCM_contactId");
			String mobileNumFrom = payload.get("GCM_sender");
			System.out.println("Message is: " + userMessage);
			System.out.println("Contact is: " + mobileNumTo);
			System.out.println("Sender is: " + mobileNumFrom);
			RegIdManager db = new RegIdManager();
			
			try{
				Set<String> regIdSet = db.readFromFile(mobileNumTo);
				String toDeviceRegId = (String) (regIdSet.toArray())[0];
				sendMessage(toDeviceRegId, GOOGLE_SERVER_KEY, userMessage, mobileNumTo, mobileNumFrom, type, time);
			}catch(Exception ioe){
				ioe.printStackTrace();
			}
		} else if ("Contacts".equals(action)) {
			String list = payload.get("List");
			Gson gson = new Gson();
			TypeToken<List<String>> token = new TypeToken<List<String>>() {};
			List<String> phoneContacts = gson.fromJson(list, token.getType());
			ArrayList<String> sendContacts = new ArrayList<>();
			RegIdManager db = new RegIdManager();
			for(String x : phoneContacts){
				Set<String> regIdSet;
				try {
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
			}
			db.dbShutdown();
			try {
				String goingTo =  payload.get("Phone");
				System.out.println("PHONE:  " + goingTo);
				sendContacts(goingTo, GOOGLE_SERVER_KEY, sendContacts);
				
				//sendContacts(goingTo, GOOGLE_SERVER_KEY, sendContacts);
			} catch (ClassNotFoundException | SmackException | IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else if ("Feed".equals(action)){
			String message = payload.get("msg");
			String sender = payload.get("GCM_FROM");
			String time = payload.get("GCM_time");
			String listContacts = payload.get("Contacts");
			Gson gson = new Gson();
			TypeToken<List<String>> token = new TypeToken<List<String>>() {};
			ArrayList<String> phoneContacts = gson.fromJson(listContacts, token.getType());
			RegIdManager db = new RegIdManager();
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
			db.dbShutdown();
			try {
				sendFeed(list, message, sender, time);
			} catch (ClassNotFoundException | SmackException | IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

	}


	/**
	 * Handles an ACK.
	 *
	 * <p>
	 * By default, it only logs a INFO message, but subclasses could override it
	 * to properly handle ACKS.
	 */
	public void handleAckReceipt(Map<String, Object> jsonObject) {
		String messageId = jsonObject.get("message_id").toString();
		String from = jsonObject.get("from").toString();
		logger.log(Level.INFO, "handleAckReceipt() from: " + from
				+ ", messageId: " + messageId);
	}

	/**
	 * Handles a NACK.
	 *
	 * <p>
	 * By default, it only logs a INFO message, but subclasses could override it
	 * to properly handle NACKS.
	 */
	public void handleNackReceipt(Map<String, Object> jsonObject) {
		String messageId = jsonObject.get("message_id").toString();
		String from = jsonObject.get("from").toString();
		logger.log(Level.INFO, "handleNackReceipt() from: " + from
				+ ", messageId: " + messageId);
	}
	
	class StanzaListenTool implements StanzaListener{

		@Override
		public void processPacket(Stanza packet) throws NotConnectedException {
			// TODO Auto-generated method stub
			System.out.println("chicken");
			logger.log(Level.INFO, "Received: " + packet.toXML());
            Message incomingMessage = (Message) packet;
            GcmPacketExtension gcmPacket =
                    (GcmPacketExtension) incomingMessage.getExtension(GCM_NAMESPACE);
            String json = gcmPacket.getJson();
            try {
                @SuppressWarnings("unchecked")
                Map<String, Object> jsonObject =
                        (Map<String, Object>) JSONValue.
                        parseWithException(json);

                // present for "ack"/"nack", null otherwise
                Object messageType = jsonObject.get("message_type");

                if (messageType == null) {
                    // Normal upstream data message
                	System.out.println("Processing message");
                	handleIncomingDataMessage(jsonObject);

                    // Send ACK to CCS
                    String messageId = (String) jsonObject.get("message_id");
                    String from = (String) jsonObject.get("from");
                    String ack = createJsonAck(from, messageId);
                    send(ack);
                } else if ("ack".equals(messageType.toString())) {
                	System.out.println("Processing ACK");
                      // Process Ack
                      handleAckReceipt(jsonObject);
                } else if ("nack".equals(messageType.toString())) {
                      // Process Nack
                	System.out.println("Processing NACK");
                      handleNackReceipt(jsonObject);
                } else {
                      logger.log(Level.WARNING,
                              "Unrecognized message type (%s)",
                              messageType.toString());
                }
            } catch (ParseException e) {
                logger.log(Level.SEVERE, "Error parsing JSON " + json, e);
            } catch (Exception e) {
                logger.log(Level.SEVERE, "Failed to process packet", e);
            }
            System.out.println("All done");
		}
		
		//Listener class for stanza
	}
	
	class FilterStanza implements StanzaFilter{

		@Override
		public boolean accept(Stanza arg0) {
			// TODO Auto-generated method stub
			if(arg0.getClass() == Stanza.class )
				return true;
			else 
			{
				if(arg0.getTo()!= null){
					if(arg0.getTo().contains(YOUR_PROJECT_ID) ){
						System.out.println(arg0.getTo());
						return true;
					}
				}
			}
			
			return false;
		}

		
		//Listener class for stanza
	}

}


