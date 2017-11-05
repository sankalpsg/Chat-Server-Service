package ScalableComputing;

import java.io.PrintStream;
import java.util.HashMap;
import java.util.Vector;

public class Data {

	static  HashMap<String,Integer> chatRooms=new HashMap<String,Integer>();  //Stores  ChatRoom ID and Name 
	static int chatRoomsIndex=0;											  //Count of Chat Rooms
	static HashMap<Integer,PrintStream> writers = new HashMap<Integer,PrintStream>();	//Client ids O/P Stream.
	static HashMap<Integer,Integer> clients = new HashMap<Integer,Integer>();	//Client ID and Chat room Relationship
}
