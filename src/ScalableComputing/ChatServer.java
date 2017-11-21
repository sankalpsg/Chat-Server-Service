package ScalableComputing;
import java.io.BufferedReader;
import ScalableComputing.Data;
import java.io.PrintStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;


import ScalableComputing.Support_Functions;

import java.net.ServerSocket;

// Multiple Clients : Multiple Chat room 

public class ChatServer 
{

	private static ServerSocket Server_Sock = null; // Server socket
	private static Socket Client_Soc = null; 	// Client socket

	private static final int Max_Clients = 50;
	private static final clientThread[] ClientThread = new clientThread[Max_Clients];

	public static void main(String args[]) 
	{

		
		Data.chatRoomsIndex = 0;
		String IPAddress = "127.0.0.1";  //IP Address
		int PortNo = 8050;    // Port Number
		if (args.length < 2) 
		{
			System.out.println("Issue in the .sh script but it will still execute with localhost and port 8050");
		} 
		else 
		{
			PortNo = Integer.valueOf(args[0]).intValue();
			IPAddress=args[1];
		}
		
		Support_Functions.loadProperties(IPAddress,PortNo);
		//Open a Server Socket
		
		try 
		{
			Server_Sock = new ServerSocket(PortNo);
			
		} 
		catch (IOException e) 
		{
			System.out.println(e);
		}

		//Open a Client Socket
		
		while (true) 
		{
			try 
			{
				Client_Soc = Server_Sock.accept();
				int i = 0;
				for (i = 0; i < Max_Clients; i++) 
				{
					if (ClientThread[i] == null) 
					{
						(ClientThread[i] = new clientThread(Client_Soc, ClientThread)).start();
						 break;
					}
				}
				
				if (i == Max_Clients) 
				{
					PrintStream output = new PrintStream(Client_Soc.getOutputStream());
					output.println("Too Many Clients : Server Busy");
					output.close();
					Client_Soc.close();
				}
				
			} 
			catch (IOException e) 
			{
				System.out.println(e);
			}
		}
	}
}

// Client Thread

class clientThread extends Thread 
{
	BufferedReader input;
	PrintStream output;
	private Socket Client_Soc = null;

	public clientThread(Socket Client_Soc, clientThread[] ClientThread) 
	{
		this.Client_Soc = Client_Soc;
	}

	public void run() 
	{
		String[] l = new String[100];
		try 
		{
			input = new BufferedReader(new InputStreamReader(Client_Soc.getInputStream()));
			output = new PrintStream(Client_Soc.getOutputStream());
			while(true) 
			{
				System.out.println("Input waiting..");
				l[0]=input.readLine();
				System.out.println(" 1st line: "+l[0]+"\n");
				if(null!= l[0] && l[0].startsWith("JOIN_CHATROOM: ")) 
				{
					l[1]= input.readLine();
					l[2]= input.readLine();
					l[3]= input.readLine();
					System.out.println("Enter JOIN_CHATROOM Message:\n"+l[0]+l[1]+l[2]+l[3]);
				}
				else if(null!= l[0] &&l[0].startsWith("LEAVE_CHATROOM: ")) 
				{
					l[1] = input.readLine();
					l[2] = input.readLine();
					System.out.println("Enter LEAVE_CHATROOM Message:\n"+l[0]+l[1]+l[2]);
				}
				else if(null!= l[0] &&l[0].startsWith("CHAT: ")) 
				{
					l[1] = input.readLine();
					l[2] = input.readLine();
					int i = 3;
					while(true)
					{
						l[i] = input.readLine();
						if(l[i].isEmpty())
						{
							break;
						}
						i++;
					}
					System.out.println("Enter CHAT Message:\n"+l[0]+l[1]+l[2]+l[3]+l[4]);
				}
				else if(null != l[0] && l[0].startsWith("KILL_SERVICE")) {
					System.out.println("Enter KILL_SERVICE Message:\n"+l[0]);
					Client_Soc.close();
					System.exit(0);
				}else if(null != l[0] && l[0].startsWith("HELO ")) {
					System.out.println("Enter HELO Message:\n"+l[0]);
				}else if(null != l[0] && l[0].startsWith("DISCONNECT: ")){
					l[1] = input.readLine();
					l[2] = input.readLine();
					Support_Functions sf = new Support_Functions();
					sf .processDisconnectMessage(l[0],l[1],l[2],output);
					Client_Soc.close();
					return;
				}
				else if(null == l[0])
				{
				}
				else 
				{
					System.out.println("Enter ERROR Message:\n"+l[0]);
					Support_Functions sf = new Support_Functions();
					sf .goErrorMessage(l[0],output);
				}
				new ClientStreamThread(output,l).start();
			}
		}
		
		

		catch (IOException e) 
		{
			System.out.println("IO Exception of Main Thread::"+e +"::");
			e.printStackTrace();
		}
		
		finally
		{
			try 
			{
				input.close();
				output.close();
				Client_Soc.close();
			} 
			catch (IOException err) 
			{
				System.out.println("IO Exception of Main Thread Finally block::"+ err +"::");
				err.printStackTrace();
			}
			catch(NullPointerException nullpexc)
			{
				System.out.println("NullPointerException of Main Thread Finally block::"+nullpexc +"::");
				nullpexc.printStackTrace();
			}
		}
	}
}