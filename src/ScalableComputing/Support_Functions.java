package ScalableComputing;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.awt.List;
import java.io.PrintWriter;
import java.net.SocketException;
import java.util.Map.Entry;
import java.util.Set;
import java.util.HashSet;

import ScalableComputing.Info;

import java.util.Properties;

public class Support_Functions {

	public String decode(String readLine) {
		String message = readLine.replace("$$", "\n");
		return message;
	}
	public Boolean processJoinMessage(String l1,String l2,String l3,String l4, PrintStream output) {
		Info message = new Info();
		if(l1.startsWith("JOIN_CHATROOM") && 
				l2.startsWith("CLIENT_IP") && 
				l3.startsWith("PORT") && 
				l4.startsWith("CLIENT_NAME")) {

			String[] msg_part = l1.split(": ");
			String l1Val = msg_part[1];
			msg_part = l2.split(": ");
			String l2Val = msg_part[1];
			msg_part = l3.split(": ");
			String l3Val = msg_part[1];
			msg_part = l4.split(": ");
			String l4Val = msg_part[1];


			//check if chat room already exists
			if(!Data.chatRooms.containsKey(l1Val))//Create new chat room
			{
				Data.chatRooms.put(l1Val, Data.chatRoomsIndex);
				Data.chatRoomsInverse.put(Data.chatRoomsIndex, l1Val);
				Data.chatRoomsIndex++;
			}
			message.setJOIN_CHATROOM(l1Val);
			message.setCLIENT_IP(l2Val);
			message.setPORT(l3Val);
			message.setCLIENT_NAME(l4Val);

			Set<Integer> roomRefIDSet = new HashSet<Integer>();
			if(Data.clients.get(Integer.parseInt(String.valueOf(Thread.currentThread().getId()))) != null){
				roomRefIDSet = Data.clients.get(Integer.parseInt(String.valueOf(Thread.currentThread().getId())));
			}
			roomRefIDSet.add(Data.chatRooms.get(l1Val));

			Data.stream.put(Integer.parseInt(String.valueOf(Thread.currentThread().getId())),output);
			Data.clients.put(Integer.parseInt(String.valueOf(Thread.currentThread().getId())), roomRefIDSet);
			
			Set<Integer> ClientID_List = new HashSet<Integer>();
			if(null!= Data.clientNames.get(l4Val)&& !Data.clientNames.get(l4Val).isEmpty())
				ClientID_List = Data.clientNames.get(l4Val);
			ClientID_List.add(Integer.parseInt(String.valueOf(Thread.currentThread().getId())));
			Data.clientNames.put(l4Val,ClientID_List);
			
			String outMsg = makeReplyMessage(message);
			output.print(outMsg);
			System.out.println("Output  "+output+" JOIN_CHATROOM\n" +  outMsg);	
			PrintStream output2; //This send the client name to all the members
			String StrMsg = "CHAT: "+ Data.chatRooms.get(l1Val) +
					"\nCLIENT_NAME: "+l4Val+
					"\nMESSAGE: "+ l4Val;
		
			for (Entry<Integer, PrintStream> entry : Data.stream.entrySet()) {
				System.out.println(entry.getKey().toString());
				if(Data.clients.get(entry.getKey()).contains(Data.chatRooms.get(l1Val))) {
					output2 = entry.getValue();
					if(output!=output2){
						output2.println(StrMsg);
						System.out.println("Output  "+output2+"  JOIN_CHATROOM\n" +  StrMsg);
					}
				}

			}
			output.println(StrMsg);
			return true;
		}
		else {
			message.setErrorCode("1");
			message.setErrorDescription("Input Message Invalid");
			return false;
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
	
		PropertiesServr.IPserver = "134.226.50.51";
		PropertiesServr.Port_Server = "8050";
	}
	public Boolean processChatMessage(String l1, String l2, String l3, String l4, PrintStream output) {
		Info message = new Info();
		if(l1.startsWith("CHAT") && l2.startsWith("JOIN_ID") && l3.startsWith("CLIENT_NAME") && l4.startsWith("MESSAGE")) {
			String[] msg_part = l1.split(": ");
			String l1Val = msg_part[1];
			msg_part = l2.split(": ");
			String l2Val = msg_part[1];
			msg_part = l3.split(": ");
			String l3Val = msg_part[1];
			msg_part = l4.split(": ");
			String l4Val = msg_part[1];

			//Validate existing chat room 
			if(!Data.chatRoomsInverse.containsValue(l1Val)) { // New chat room
				message.setErrorCode("1");
				message.setErrorDescription("Input Message not valid");
				return false;
			}
			PrintStream op2;
			String str_message=null;
			str_message = "CHAT: "+l1Val +"\n"
					+"CLIENT_NAME: "+l3Val +"\n"
					+"MESSAGE: "+ l4Val +"\n\n";
			output.print(str_message);
			for (Entry<Integer, PrintStream> entry : Data.stream.entrySet()){
				if(String.valueOf(Data.clients.get(entry.getKey()))==l1Val) 
		        {	op2 = entry.getValue();
		        	if(op2!=output)     	// Avoid Duplicate message to the client who is sending it
		        		op2.println(str_message);
		        }
		    }

			return true;
		}
		else {
			message.setErrorCode("1");
			message.setErrorDescription("INVALID I/P MESSAGE");
			return false;
		}
	}
	
	public void processHeloMessage(String helo,PrintStream output){
		String str_message = null;
		str_message = helo + "\nIP: 134.226.50.51\nPort: 8050\nStudentID: 17302431";
		output.print(str_message);
	}

}
