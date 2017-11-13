package ScalableComputing;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.awt.List;
import java.io.PrintWriter;
import java.net.SocketException;
import java.util.Map.Entry;

import ScalableComputing.Data;

import java.util.Set;
import java.util.HashSet;

import ScalableComputing.Info;

import java.util.Properties;

public class Support_Functions 
{

	
	public void goErrorMessage(String string, PrintStream output) 
	{
		String str = "ERROR_CODE: 1\nERROR_DESCRIPTION: Invalid Input\n";
		output.print(str);
		System.out.println("Output ERROR:\n " +  str);
	}
	
	public String decode(String readLine) 
	{
		String message = readLine.replace("$$", "\n");
		return message;
	}
	
	public Boolean processJoinMessage(String l1,String l2,String l3,String l4, PrintStream output) 
	{
		Info message = new Info();
		if(l1.startsWith("JOIN_CHATROOM") && 
				l2.startsWith("CLIENT_IP") && 
				l3.startsWith("PORT") && 
				l4.startsWith("CLIENT_NAME")) 
		{

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
			if(Data.clients.get(Integer.parseInt(String.valueOf(Thread.currentThread().getId()))) != null)
			{
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
		
			for (Entry<Integer, PrintStream> newip : Data.stream.entrySet()) 
			{
				System.out.println(newip.getKey().toString());
				if(Data.clients.get(newip.getKey()).contains(Data.chatRooms.get(l1Val))) 
				{
					output2 = newip.getValue();
					if(output!=output2)
					{
						output2.println(StrMsg);
						System.out.println("Output  "+output2+"  JOIN_CHATROOM\n" +  StrMsg);
					}
				}

			}
			output.println(StrMsg);
			return true;
		}
		else 
		{
			message.setErrorCode("1");
			message.setErrorDescription("Input Message Invalid");
			return false;
		}
	}
	
	
	public String makeReplyMessage(Info inBundle) 
	{
		Info outBundle = new Info();
		outBundle.setJoinedChatroom(inBundle.JOIN_CHATROOM);
		outBundle.setServerIp(PropertiesServr.IPserver);
		outBundle.setPORT(PropertiesServr.Port_Server);
		outBundle.setRoomRef(Data.chatRooms.get(inBundle.JOIN_CHATROOM).toString());
		outBundle.setJoinId(String.valueOf(Thread.currentThread().getId()));		
		String reply_message = outBundle.joinReplyToString();
		return reply_message;
	}
	
	
	public static void loadProperties() 
	{
		PropertiesServr.IPserver = "134.226.50.182";
		PropertiesServr.Port_Server = "8050";
	}
	
	public Boolean processChatMessage(String l1, String l2, String l3, String l4, PrintStream output) {
		Info message = new Info();
		if(l1.startsWith("CHAT") && l2.startsWith("JOIN_ID") && l3.startsWith("CLIENT_NAME") && l4.startsWith("MESSAGE")) 
		{
			String[] msg_part = l1.split(": ");
			String l1Val = msg_part[1];
			msg_part = l2.split(": ");
			String l2Val = msg_part[1];
			msg_part = l3.split(": ");
			String l3Val = msg_part[1];
			msg_part = l4.split(": ");
			String l4Val = msg_part[1];

			//Validate existing chat room 
			if(!Data.chatRoomsInverse.containsKey(Integer.parseInt(l1Val))) 
			{ // New chat room
				message.setErrorCode("1");
				message.setErrorDescription("Invalid input Message");
				return false;
			}
			
			PrintStream op2;
			String str_message=null;
			str_message = "CHAT: "+l1Val +"\n"
					+"CLIENT_NAME: "+l3Val +"\n"
					+"MESSAGE: "+ l4Val +"\n\n";
			output.print(str_message);
			for (Entry<Integer, PrintStream> newip : Data.stream.entrySet())
			{
				if(Data.clients.get(newip.getKey()).contains(Integer.parseInt(l1Val)))
		        	{	op2 = newip.getValue();
		        		if(op2!=output)     	// Avoid Duplicate message to the client who is sending it
		        		op2.println(str_message);
		        	}
		    }

			return true;
		}
		else 
		{
			message.setErrorCode("1");
			message.setErrorDescription("INVALID I/P MESSAGE");
			return false;
		}
	}
	
	public Boolean processLeaveMessage(String l1, String l2, String l3, PrintStream output) 
	{
		
		if(l1.startsWith("LEAVE_CHATROOM") 
				&& l2.startsWith("JOIN_ID") 
				&& l3.startsWith("CLIENT_NAME")) 
		{
			String[] parts = l1.split(": ");
			String l1Val = parts[1];
			parts = l2.split(": ");
			String l2Val = parts[1];
			parts = l3.split(": ");
			String l3Val = parts[1];


			if(!Data.chatRoomsInverse.containsKey(Integer.parseInt(l1Val))) { 
				
				return false;
			}
			PrintStream op2;
			String str_message=null;
			str_message = "LEFT_CHATROOM: "+l1Val +"\n"
					+"JOIN_ID: "+l2Val+"\n";

			String str_message2 = "CHAT: "+l1Val +"\n"
					+"CLIENT_NAME: "+l3Val +"\n"
					+"MESSAGE: "+ l3Val;
			str_message+=str_message2;
			System.out.println("Output "+output+" LEAVE_CHATROOM: \n" +  str_message);
			output.println(str_message);

			Set<Integer> t0 = Data.clients.get(Integer.parseInt(l2Val));
			t0.remove(l1Val);
			Data.clients.remove(Integer.parseInt(l2Val));
			Data.clients.put(Integer.parseInt(l2Val),t0);
			Data.stream.remove(Integer.parseInt(l2Val));
		
			int a=-1;
			
			try
			{

				for (Entry<Integer, PrintStream> newip : Data.stream.entrySet()) 
				{
					a=-1;
						if(Data.clients.get(newip.getKey()).contains((Integer.parseInt(l1Val)))) 
						{	
							op2 = newip.getValue();
							a = newip.getKey();
							if(op2!=output) 
							{
								op2.println(str_message2);
								System.out.println("Output "+op2+" LEAVE_CHATROOM: \n" +  str_message2);
							}
						}
				}
			}
			
			catch(Exception elm)
			{
				System.out.println("Error in processing leave message" + elm);
				elm.printStackTrace();
			}
			
			finally
			{
				if(a>-1)
				{
					Set<Integer> t1 = Data.clients.get(a);
					Data.clients.remove(a);
					if(t1!=null)
					{
						t1.remove(l1Val);
						Data.clients.put(a,t1);
					}
					Data.stream.remove(a);
				}
			}
			
			return true;
		}
		
		else 
		{
			return false;
		}

	}
	
	public void processHeloMessage(String helo,PrintStream output)
	{
		String str_message = null;
		str_message = helo + "\nIP: 134.226.50.182\nPort: 8050\nStudentID: 17302431";
		output.print(str_message);
	}
	
}
