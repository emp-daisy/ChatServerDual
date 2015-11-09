import java.io.IOException;
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
import org.jivesoftware.smack.util.stringencoder.Base64;
import org.json.simple.JSONValue;
import org.json.simple.parser.ParseException;
import org.xmlpull.v1.XmlPullParser;

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
	
	public void sendReceipt(String from, String id, String receiptNo) throws NotConnectedException{
		String messageId = getRandomMessageId();
		Map<String, String> payload = new HashMap<String, String>();
		payload.put("Type", "Receipt");
		payload.put("receiptNo", receiptNo);
		payload.put("msgId", id);
		String collapseKey = "sample";
		Long timeToLive = 10000L;
		Boolean delayWhileIdle = true;
		send(createJsonMessage(from, messageId, payload, collapseKey, timeToLive, delayWhileIdle));
		System.out.println("Server Receipt to: '" + from);
	}
	
	public void sendMessage(String id, String toDeviceRegId, final String GOOGLE_SERVER_KEY , String message, String contactId, String sender, String type, String time) throws SmackException, IOException, ClassNotFoundException {
		
		if(contactId == "")
			contactId = "";
		if(type == "")
			type = "msg";
		if(time == "")
			time = "05/05/05 22:00:00";
			
		String messageId = getRandomMessageId();
		Map<String, String> payload = new HashMap<String, String>();
		payload.put("GCM_msgId", id);
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
		payload.put("Type", "Contact");
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
		if(!contactList.isEmpty()){
			MulticastResult result = sender.send(messageBuilder, contactList, 1);
			System.out.println(result);
		}
	}
	
	public void sendRequest(String toDeviceRegId, final String GOOGLE_SERVER_KEY, String from) throws SmackException, IOException, ClassNotFoundException{
		String messageId = getRandomMessageId();
		Map<String, String> payload = new HashMap<String, String>();
		payload.put("Type", "Request");
		payload.put("Number", from);
		String collapseKey = "sample";
		Long timeToLive = 10000L;
		Boolean delayWhileIdle = true;
		send(createJsonMessage(toDeviceRegId, messageId, payload,
				collapseKey, timeToLive, delayWhileIdle));
		System.out.println("Reuest Sent");
	}
	
	public void sendContactsPhoto(String toDeviceRegId, final String GOOGLE_SERVER_KEY , ArrayList<Map<String, byte[]>> sendPhoto) throws SmackException, IOException, ClassNotFoundException {
		String messageId = getRandomMessageId();
		Map<String, String> payload = new HashMap<String, String>();
		Gson gson = new Gson();
		String jsonPhoneList = gson.toJson(sendPhoto);
		payload.put("Type", "ContactsPhoto");
		payload.put("ListPhoto", jsonPhoneList);
		String collapseKey = "sample";
		Long timeToLive = 10000L;
		Boolean delayWhileIdle = true;
		send(createJsonMessage(toDeviceRegId, messageId, payload,
				collapseKey, timeToLive, delayWhileIdle));
		System.out.println("Contacts Photos have been sent");
	}
	
	public void sendDeleteInfo(String number, final String GOOGLE_SERVER_KEY , String confirm) throws SmackException, IOException, ClassNotFoundException {
		String messageId = getRandomMessageId();
		Map<String, String> payload = new HashMap<String, String>();
		payload.put("Type", "Deactivate");
		payload.put("Confirm", confirm);
		String collapseKey = "sample";
		Long timeToLive = 10000L;
		Boolean delayWhileIdle = true;
		send(createJsonMessage(number, messageId, payload,
				collapseKey, timeToLive, delayWhileIdle));
		System.out.println("Delete Info Sent");
	}
	
	public void sendTyping(String to, String isTyping) throws NotConnectedException{
		String messageId = getRandomMessageId();
		Map<String, String> payload = new HashMap<String, String>();
		payload.put("Type", "Typing");
		payload.put("isUserTyping", isTyping);
		String collapseKey = "sample";
		Long timeToLive = 10000L;
		Boolean delayWhileIdle = true;
		send(createJsonMessage(to, messageId, payload,
				collapseKey, timeToLive, delayWhileIdle));
		System.out.println("is Typing Sent");
	}
	
	public void sendContactDelete(ArrayList<String> contactList, String contact, String id) throws SmackException, IOException, ClassNotFoundException {
		Sender sender = new Sender(GOOGLE_SERVER_KEY);
		
		//ServerIP
		com.google.android.gcm.server.Message messageBuilder = new com.google.android.gcm.server.Message.Builder().timeToLive(30)
				.delayWhileIdle(true)
				.addData("Type", "DeleteContactFromGroup")
				.addData("GCM_contactId", contact)
				.addData("GCM_groupId", id)
				.build();

		for(String x : contactList)
			System.out.println(x);
		MulticastResult result = sender.send(messageBuilder, contactList, 1);
		System.out.println(result);
	}
	
	public void sendPhoto(ArrayList<String> contactList, String from) throws SmackException, IOException, ClassNotFoundException {
		Sender sender = new Sender(GOOGLE_SERVER_KEY);
		
		//ServerIP
		com.google.android.gcm.server.Message messageBuilder = new com.google.android.gcm.server.Message.Builder().timeToLive(30)
				.delayWhileIdle(true)
				.addData("Type", "Photo")
				.addData("GCM_FROM", from)
				.build();

		System.out.println("Image from: " + from);
		for(String x : contactList)
			System.out.println(x);
		MulticastResult result = sender.send(messageBuilder, contactList, 1);
		System.out.println(result);
	}
	
	public void sendGroupList(String name, ArrayList<String> contactList, String list, String groupId, String creator) throws SmackException, IOException, ClassNotFoundException {
		Sender sender = new Sender(GOOGLE_SERVER_KEY);
		Gson gson = new Gson();
		//ServerIP
		com.google.android.gcm.server.Message messageBuilder = new com.google.android.gcm.server.Message.Builder().timeToLive(30)
				.delayWhileIdle(true)
				.addData("Type", "NewGroup")
				.addData("groupName", name)
				.addData("groupList", list)
				.addData("groupId", groupId)
				.addData("creator", creator)
				.build();

		for(String x : contactList)
			System.out.println("Group Contact: " + x);
		MulticastResult result = sender.send(messageBuilder, contactList, 1);
		System.out.println(result);
	}
	
	public void sendGroupMessage(ArrayList<String> contactList, String from, String message, String groupName, String id, String time, String contactListString) throws SmackException, IOException, ClassNotFoundException {
		Sender sender = new Sender(GOOGLE_SERVER_KEY);
		
		//ServerIP
		com.google.android.gcm.server.Message messageBuilder = new com.google.android.gcm.server.Message.Builder().timeToLive(30)
				.delayWhileIdle(true)
				.addData("Type", "GroupMessage")
				.addData("GCM_sender", from)
				.addData("GCM_msg", message)
				.addData("GroupName", groupName)
				.addData("GCM_msgId", id)
				.addData("GCM_time", time)
				.addData("contacts", contactListString)
				.build();

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
		RegIdManager db = new RegIdManager();
		switch(action){
			case "msg":
				{String type = payload.get("Type");
				String userMessage = payload.get("GCM_msg");
				String time = payload.get("GCM_time");
				String mobileNumTo = payload.get("GCM_contactId");
				String mobileNumFrom = payload.get("GCM_sender");
				String msgId = payload.get("GCM_msgId");
				System.out.println("Message is: " + userMessage + " : Contact is: " + mobileNumTo + " : Sender is: " + mobileNumFrom);
				try{
					Set<String> regIdSet = db.readFromFile(mobileNumTo);
					Set<String> regIdSet2 = db.readFromFileForSender(mobileNumFrom);
					String toDeviceRegId = (String) (regIdSet.toArray())[0];
					String fromDevice = (String) (regIdSet2.toArray())[0];
					sendReceipt(fromDevice, msgId, "1");
					sendMessage(msgId, toDeviceRegId, GOOGLE_SERVER_KEY, userMessage, mobileNumTo, mobileNumFrom, type, time);
				}catch(Exception ioe){
					ioe.printStackTrace();
				}}
				break;
			case "Contacts":
				{String list = payload.get("List");
				String goingTo =  payload.get("Phone");
				Gson gson = new Gson();
				TypeToken<List<String>> token = new TypeToken<List<String>>() {};
				List<String> phoneContacts = gson.fromJson(list, token.getType());
				ArrayList<String> sendContacts = new ArrayList<>();
				try {
					for(String x : phoneContacts){
						Set<String> regIdSet = db.readFromFile(x);
						if(!regIdSet.isEmpty()){
							if(regIdSet.toArray()[0] != null){sendContacts.add(x);}
						}
				}	System.out.println("PHONE CONTACT: " + phoneContacts + " : PHONE:  " + goingTo);
					if(!sendContacts.isEmpty()){sendContacts(goingTo, GOOGLE_SERVER_KEY, sendContacts);}
				} catch (ClassNotFoundException | SmackException | IOException e) {
					e.printStackTrace();
				}}
				break;
			case "Feed":
				{String message = payload.get("msg");
				String sender = payload.get("GCM_FROM");
				String time = payload.get("GCM_time");
				String listContacts = payload.get("Contacts");
				Gson gson = new Gson();
				TypeToken<List<String>> token = new TypeToken<List<String>>() {};
				ArrayList<String> phoneContacts = gson.fromJson(listContacts, token.getType());
				ArrayList<String> list = new ArrayList<String>();
				System.out.println("FEED LIST: " + phoneContacts);
				try {
					for(String x : phoneContacts){
						Set<String> regIdSet = db.readFromFile(x);
						list.add((String)regIdSet.toArray()[0]);
						}
					sendFeed(list, message, sender, time);
				} catch (ClassNotFoundException | SmackException | IOException e) {
					e.printStackTrace();
				}}
				break;
			case "ProfilePhoto":
					{String profileImage =  payload.get("ProfilePic");
					String contactList = payload.get("ContactList");
					String user = payload.get("UserOwner");
					Gson gson = new Gson();
					TypeToken<List<String>> token = new TypeToken<List<String>>() {};
					ArrayList<String> phoneContacts = gson.fromJson(contactList, token.getType());
					System.out.println("Image STRING: " + profileImage + " + Image CONTACT: " + contactList + " : Image USER: " + user);
					byte[] imageByte = Base64.decode(profileImage);
					System.out.println("Image BYTES: " + imageByte);
					db.saveImage(user, imageByte);//Save image to database
					try{
						sendPhoto(phoneContacts, user);
					}catch(ClassNotFoundException | SmackException | IOException e){
						e.printStackTrace();	
					}}
					break;
			case "GetPhoto":
				{String list = payload.get("List");
				String goingTo =  payload.get("Phone");
				Gson gson = new Gson();
				TypeToken<List<String>> token = new TypeToken<List<String>>() {};
				List<String> phoneContacts = gson.fromJson(list, token.getType());
				ArrayList<Map<String, byte[]>> sendPhoto = new ArrayList<>();
				try {
					for(String x : phoneContacts){
						Map<String, byte[]> mapPhoto;
						mapPhoto = db.getImage(x);
						if(!mapPhoto.isEmpty()){
							sendPhoto.add(mapPhoto);
						}
				}
					sendContactsPhoto(goingTo, GOOGLE_SERVER_KEY, sendPhoto);
				} catch (ClassNotFoundException | SmackException | IOException e) {
					e.printStackTrace();
				}}
				break;
			case "Deactivate":
				{String number = payload.get("number");
				Set<String> regIdSet = db.readFromFile(number);
				String toDeviceRegId = (String) (regIdSet.toArray())[0];
				String confirm = (db.deleteContact(number) ? "y" : "n");
				try {						
					sendDeleteInfo(toDeviceRegId, GOOGLE_SERVER_KEY, confirm);
				} catch (ClassNotFoundException | SmackException | IOException e) {
					e.printStackTrace();
				}}
				break;
			case "Typing":
				{String type = payload.get("isUserTyping");
				String mobileNumTo = payload.get("GCM_contactId");
				try {
					Set<String> regIdSet = db.readFromFile(mobileNumTo);
					String toDeviceRegId = (String) (regIdSet.toArray())[0];
					String isit = (type.equals("y")?"y" : "n");
					sendTyping(toDeviceRegId, isit);
				} catch (NotConnectedException e) {
					e.printStackTrace();
				}}			
				break;
			case "Receipt":
				{String id = payload.get("msgId");
				String GCM_number = payload.get("GCM_number");
				try {
					Set<String> regIdSet = db.readFromFile(GCM_number);
					String toDeviceRegId = (String) (regIdSet.toArray())[0];
					sendReceipt(toDeviceRegId, id, "2");
				} catch (NotConnectedException e) {
					e.printStackTrace();
				}}
				break;
			case "GroupMessage":
			{String message = payload.get("GCM_msg");
			String sender = payload.get("GCM_FROM");
			String groupName = payload.get("GroupName");
			String time = payload.get("GCM_time");
			String listContacts = payload.get("contacts");
			System.out.println(listContacts);
			String msgId = payload.get("GCM_msgId");
			Gson gson = new Gson();
			TypeToken<List<String>> token = new TypeToken<List<String>>() {};
			ArrayList<String> phoneContacts = gson.fromJson(listContacts, token.getType());
			ArrayList<String> list = new ArrayList<String>();
			System.out.println("Group LIST: " + phoneContacts);
			try {
				for(String x : phoneContacts){
					System.out.println(x + " and " + sender);
					if(!x.equals(sender)){
						Set<String> regIdSet = db.readFromFile(x);
						list.add((String)regIdSet.toArray()[0]);
					}
				}
				sendGroupMessage(list, sender, message, groupName, msgId, time, listContacts);
				//sendGroupMessage(list, message, sender, time);
			} catch (ClassNotFoundException | SmackException | IOException e) {
				e.printStackTrace();
			}}
				break;
			case "NewGroup":
				String groupName = payload.get("groupName");
				String groupList = payload.get("groupList");
				String groupId = payload.get("groupId");
				String creator = payload.get("creator");
				
				Gson gson = new Gson();
				TypeToken<List<String>> token = new TypeToken<List<String>>() {};
				ArrayList<String> groupContacts = gson.fromJson(groupList, token.getType());
				ArrayList<String> list = new ArrayList<String>();
				System.out.println("Group LIST: " + groupContacts);
				
				try {
					for(String x : groupContacts){
						if(x != creator){
							Set<String> regIdSet = db.readFromFile(x);
							list.add((String)regIdSet.toArray()[0]);
						}
					}
					sendGroupList(groupName, list, groupList, groupId, creator);
					//sendGroupMessage(list, sender, message, groupName, msgId, time, listContacts);
					//sendGroupMessage(list, message, sender, time);
				} catch (ClassNotFoundException | SmackException | IOException e) {
					e.printStackTrace();
				}
				break;
			case "DeleteUserFromGroup":
				String contact = payload.get("GCM_contactId");
				String contactList = payload.get("contacts");
				String groupID = payload.get("GCM_groupId");
				
				Gson gson1 = new Gson();
				TypeToken<List<String>> token1 = new TypeToken<List<String>>() {};
				ArrayList<String> groupContacts1 = gson1.fromJson(contactList, token1.getType());
				ArrayList<String> list1 = new ArrayList<String>();
				System.out.println("Group LIST: " + groupContacts1);
				
				try {
					for(String x : groupContacts1){
						if(!x.equals(contact)){
							Set<String> regIdSet = db.readFromFile(x);
							list1.add((String)regIdSet.toArray()[0]);
						}
					}
					sendContactDelete(list1, contact, groupID);
				} catch (ClassNotFoundException | SmackException | IOException e) {
					e.printStackTrace();
				}
				break;
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
			System.out.println("Process packet...");
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


