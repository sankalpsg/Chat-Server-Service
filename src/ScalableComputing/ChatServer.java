 package ScalableComputing;
import java.io.BufferedReader;
import java.io.DataInputStream;

import ScalableComputing.Data;
import java.io.PrintStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.nio.CharBuffer;
import java.util.Arrays;
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

		Support_Functions.loadProperties();
		Data.chatRoomsIndex = 0;
		String IPAddress = "134.226.50.51";  //IP Address
		int PortNo = 8050;    // Port Number
		if (args.length < 1) 
		{
			System.out.println("Using port number=" + PortNo +"\n IP= "+ IPAddress);
		} 
		else 
		{
			PortNo = Integer.valueOf(args[0]).intValue();
		}

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
				if(l[0].startsWith("JOIN_CHATROOM: ")) 
				{
					l[1]= input.readLine();
					l[2]= input.readLine();
					l[3]= input.readLine();
					System.out.println("Input JOIN_CHATROOM Message:\n"+l[0]+l[1]+l[2]+l[3]);
				}
				else if(l[0].startsWith("LEAVE_CHATROOM: ")) 
				{
					l[1] = input.readLine();
					l[2] = input.readLine();
					System.out.println("Input LEAVE_CHATROOM Message:\n"+l[0]+l[1]+l[2]);
				}
				else if(l[0].startsWith("CHAT: ")) 
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
					System.out.println("Input CHAT Message:\n"+l[0]+l[1]+l[2]+l[3]+l[4]);
				}
				else 
				{
					System.out.println("Input ERROR Message:\n"+l[0]);
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