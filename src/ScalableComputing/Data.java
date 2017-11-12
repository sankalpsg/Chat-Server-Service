package ScalableComputing;

import java.io.PrintStream;
import java.util.HashMap;
import java.util.Set;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;

public class Data {

	static  HashMap<String,Integer> chatRooms=new HashMap<String,Integer>();  //Stores  ChatRoom ID and Name 
	static  HashMap<Integer,String> chatRoomsInverse=new HashMap<Integer,String>();  //Inverse of the above HashMap
	static int chatRoomsIndex=0;											  //Count of Chat Rooms
	static ConcurrentHashMap<Integer,PrintStream> stream = new ConcurrentHashMap<Integer,PrintStream>();	//Client ids O/P Stream.
	static ConcurrentHashMap<Integer, Set<Integer>> clients = new ConcurrentHashMap<Integer,Set<Integer>>();	//Client ID and Chat room Relationship
	static HashMap<String,Set<Integer>> clientNames = new HashMap<String,Set<Integer>>();		//All the Names of the clients and their ID
	static int count=0;
}
