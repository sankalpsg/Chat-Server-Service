package ScalableComputing;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.net.Socket;

public class ClientStreamThread extends Thread 
{
	private PrintStream output = null;
	private String[] l;

	public ClientStreamThread(PrintStream output1, String[] l1) 
	{
		this.l = l1;
		this.output = output1;
	}

	public void run() 
	{
		System.out.println("ClientWriter "+Thread.currentThread().getId()+ " : Created a thread");
		Support_Functions sf = new Support_Functions();

		if(l[0].startsWith("JOIN_CHATROOM: ")) 
		{
			System.out.println("....Start Thread "+Thread.currentThread().getId()+" Stream: Join chatroom block.......");
			sf.processJoinMessage(l[0],l[1],l[2],l[3],output);
			System.out.println("....End of the thread... "+Thread.currentThread().getId()+" Stream: Join chatroom block......");
		}
		else if(l[0].startsWith("CHAT: ")) 
		{
			System.out.println(".......Start Thread  "+Thread.currentThread().getId()+" Stream: in Chat Block..........");
			sf.processChatMessage(l[0],l[1],l[2],l[3],output);
			System.out.println("****End "+Thread.currentThread().getId()+"  Stream: in Chat Block...........");
		}
		else if(l[0].startsWith("HELO ")) 
		{
			System.out.println(".....Start Thread  "+Thread.currentThread().getId()+" Stream: Hello Block...........");
			sf.processHeloMessage(l[0],output);
			System.out.println(".....End Thread  "+Thread.currentThread().getId()+" Stream: Hello Block...........");
			return;
		}
		else if(l[0].startsWith("LEAVE_CHATROOM: ")) 
		{
			System.out.println("****Start "+Thread.currentThread().getId()+"  WriterThread: In leave chatroom if block****");
			sf.processLeaveMessage(l[0],l[1],l[2],output);
			return;
		}
	}
}

