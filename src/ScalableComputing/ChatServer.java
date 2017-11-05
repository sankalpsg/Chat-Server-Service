package ScalableComputing;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.PrintStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Properties;

import ScalableComputing.Info;

import java.net.InetAddress;
import java.net.ServerSocket;

// Multiple Clients : Multiple Chat room 

public class ChatServer {

	private static ServerSocket Server_Sock = null; // Server socket
	private static Socket Client_Soc = null; 	// Client socket

	private static final int Max_Clients = 50;
	private static final clientThread[] ClientThread = new clientThread[Max_Clients];

	public static void main(String args[]) {

		Support_Functions.loadProperties();
		Data.chatRoomsIndex = 0;
		String IPAddress = "127.0.0.1";  //IP Address
		int PortNo = 8050;    // Port Number
		if (args.length < 1) {
			System.out.println("Usage: java MultiThreadChatServerSync <PortNo>\n"
					+ "Now using port number=" + PortNo +"\nAnd IP= "+ IPAddress);
		} else {
			PortNo = Integer.valueOf(args[0]).intValue();
		}

		//Open a Server Socket
		
		try {
			InetAddress addr = InetAddress.getByName(IPAddress);
			Server_Sock = new ServerSocket(PortNo,50,addr);
			
		} catch (IOException e) {
			System.out.println(e);
		}

		//Open a Client Socket
		
		while (true) {
			try {
				Client_Soc = Server_Sock.accept();
				int i = 0;
				for (i = 0; i < Max_Clients; i++) {
					if (ClientThread[i] == null) {
						(ClientThread[i] = new clientThread(Client_Soc, ClientThread)).start();
						break;
					}
				}
				if (i == Max_Clients) {
					PrintStream output = new PrintStream(Client_Soc.getOutputStream());
					output.println("Too Many Clients : Server Busy");
					output.close();
					Client_Soc.close();
				}
			} catch (IOException e) {
				System.out.println(e);
			}
		}
	}
}

// Client Thread

class clientThread extends Thread {

	private String Client_Name = null;
	private DataInputStream input = null;
	private PrintStream output = null;
	private Socket Client_Soc = null;
	private final clientThread[] ClientThread;
	private int Max_Clients;


	public clientThread(Socket Client_Soc, clientThread[] ClientThread) {
		this.Client_Soc = Client_Soc;
		this.ClientThread = ClientThread;
		Max_Clients = ClientThread.length;

	}

	public void run() {
		int Max_Clients = this.Max_Clients;
		clientThread[] ClientThread = this.ClientThread;
		Support_Functions sf = new Support_Functions();
		Info pckt = new Info();
		String message_out =null;
		try {
		
			input = new DataInputStream(Client_Soc.getInputStream());
			output = new PrintStream(Client_Soc.getOutputStream());
		
			String l1= input.readLine();
			String l2= input.readLine();
			String l3= input.readLine();
			String l4= input.readLine();
			if(l1.startsWith("JOIN_CHATROOM: ")) 
			{
				pckt = sf.processJoinMessage(l1,l2,l3,l4,output);
				message_out = sf.makeReplyMessage(pckt);   // Sends Reply if pre-processing input is successful
				output.print(message_out);
				System.out.println("\nAfter decoding: "+message_out);
			}
			else if(l1.startsWith("LEFT_CHATROOM: ")) 
			{

			}
			else if(l1.startsWith("CHAT: ")) 
			{
				
				pckt = sf.processChatMessage(l1,l2,l3,l4,output);
				output.print(message_out);
				System.out.println("\nAfter decoding: "+message_out);
			}


			/*Accepting new client, if any */
			
			synchronized (this) 
			{
				for (int i = 0; i < Max_Clients; i++) 
				{
					if (ClientThread[i] == this) 
					{
						ClientThread[i] = null;
					}
				}
			}
			
			input.close();
			output.close();
			Client_Soc.close();
			
			// Exit the socket and the IP OP Streams
			
		} catch (IOException e) {
		}
	}


}
