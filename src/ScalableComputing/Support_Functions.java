package ScalableComputing;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import ScalableComputing.Info;

import java.util.Properties;

public class Support_Functions {

	public String decode(String readLine) {
		String message = readLine.replace("$$", "\n");
		return message;
	}
	public Info processJoinMessage(String l1,String l2,String l3,String l4, PrintStream output) {
		Info message = new Info();
		if(l1.startsWith("JOIN_CHATROOM") && l2.startsWith("CLIENT_IP") && l3.startsWith("PORT") && l4.startsWith("CLIENT_NAME")) {

			String[] msg_part = l1.split(": ");
			String l1val = msg_part[1];
			msg_part = l2.split(": ");
			String l2Val = msg_part[1];
			msg_part = l3.split(": ");
			String l3Val = msg_part[1];
			msg_part = l4.split(": ");
			String l4Val = msg_part[1];


			//check if chat room already exists
			if(!Data.chatRooms.containsKey(l1val))//Create new chat room
			{
				Data.chatRooms.put(l1val, Data.chatRoomsIndex);
				Data.chatRoomsIndex++;
			}
			message.setJOIN_CHATROOM(l1val);
			message.setCLIENT_IP(l2Val);
			message.setPORT(l3Val);
			message.setCLIENT_NAME(l4Val);

			//No Error, Add the client and output stream to storage
			Data.writers.put(Integer.parseInt(String.valueOf(Thread.currentThread().getId())),output);
			Data.clients.put(Integer.parseInt(String.valueOf(Thread.currentThread().getId())), Data.chatRooms.get(l1val));

			return message;
		}
		else {
			message.setErrorCode("1");
			message.setErrorDescription("Input Message not valid");
			return message;
		}
	}
	public String makeReplyMessage(Info inBundle) {



		Info outBundle = new Info();
		outBundle.setJoinedChatroom(inBundle.JOIN_CHATROOM);
		outBundle.setServerIp(PropertiesServr.IPserver);
		outBundle.setPORT(PropertiesServr.Port_Server);
		outBundle.setRoomRef(Data.chatRooms.get(inBundle.JOIN_CHATROOM).toString());
		outBundle.setJoinId(String.valueOf(Thread.currentThread().getId()));		
		String reply_message = outBundle.joinReplyToString();

		return reply_message;

	}
	public static void loadProperties() {
		Properties props = new Properties();

		try {
			props.load(new FileInputStream("conf/keywords.properties"));
		} catch (IOException e) {
			e.printStackTrace();
		}
		PropertiesServr.IPserver = props.getProperty("IPserver");
		PropertiesServr.Port_Server = props.getProperty("Port_Server");
	}
	public Info processChatMessage(String l1, String l2, String l3, String l4, PrintStream output) {
		Info message = new Info();
		if(l1.startsWith("CHAT") && l2.startsWith("JOIN_ID") && l3.startsWith("CLIENT_NAME") && l4.startsWith("MESSAGE")) {
			String[] msg_part = l1.split(": ");
			String l1val = msg_part[1];
			msg_part = l2.split(": ");
			String l2Val = msg_part[1];
			msg_part = l3.split(": ");
			String l3Val = msg_part[1];
			msg_part = l4.split(": ");
			String l4Val = msg_part[1];

			//Validate existing chat room 
			if(!Data.chatRooms.containsValue(l1val)) { // New chat room
				message.setErrorCode("1");
				message.setErrorDescription("Input Message not valid");
				return message;
			}
			PrintStream op2;
			String str_message=null;
			str_message = "CHAT: "+l1val +"\n"
					+"CLIENT_NAME: "+l3Val +"\n"
					+"MESSAGE: "+ l4Val +"\n\n";
			
			for (Entry<Integer, PrintStream> entry : Data.writers.entrySet()) 
			{
		        
		        if(String.valueOf(Data.clients.get(entry.getKey()))==l1val) 
		        {	
		        	op2 = entry.getValue();
		        	if(op2!=output)     	// Avoid Duplicate message to the client who is sending it
		        		op2.println(str_message);
		        }
		    }

			return message;
		}
		else {
			message.setErrorCode("1");
			message.setErrorDescription("INVALID I/P MESSAGE");
			return message;
		}
	}
}
