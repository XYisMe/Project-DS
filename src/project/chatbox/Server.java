package project.chatbox;

import java.io.*;
import java.net.*;
import java.text.SimpleDateFormat;
import java.util.*;

public class Server {
	
	private static int uniqueId;// a unique ID for each connection made by clients	
	private ArrayList<ClientThread> clientThread; // an ArrayList to keep Clients
	private ServerGUI svg; //get ServerGUI	
	private SimpleDateFormat sdf; // to display time	
	private int port; // the port number to get connection	
	private boolean serverStart; // true/false for server start/stop
	
        public static void main(String[] args) {
		Server server = new Server(8080); //port is 8080
		server.start();
	}	
	public Server(int port) {
		this(port, null);
	}
	
	public Server(int port, ServerGUI svg) {
			
		this.port = port; // the port		
		sdf = new SimpleDateFormat("HH:mm:ss"); // to display time for each msg		
		clientThread = new ArrayList<ClientThread>(); // ArrayList to store Clients
                this.svg = svg;	
	}
	
	public void start() { //run in loops and called from ServerGUI
		serverStart = true; //server Start
		/* create socket server and wait for client connect*/
		try 
		{	
			ServerSocket serverSocket = new ServerSocket(port); // the socket used by the server		
			while(serverStart)  //wait clients to connect
			{	
				display("Server waiting on port " + port + ".");				
				Socket socket = serverSocket.accept();  	// accept connection
				
				if(!serverStart) //server stop
					break;
				ClientThread th = new ClientThread(socket);  // make a client thread
				clientThread.add(th);// save clients in the ArrayList
				th.start();
			}
			//server stop and close socket
			try {
				serverSocket.close();
				for(int i = 0; i < clientThread.size(); ++i) { //stop all input/output and socket
					ClientThread th = clientThread.get(i);
					try {
					th.toServer.close();
					th.fromServer.close();
					th.socket.close();
					}
					catch(IOException ioE) {
					}
				}
			}
			catch(Exception e) {
			}
		}
		//server crash
		catch (IOException e) {
            String msg = sdf.format(new Date()) + e + "\n";
			display(msg);
		}
	}		

	protected void stop() {
		serverStart = false;
		try {
			new Socket("localhost", port);
		}
		catch(Exception e) {
		}

	}

        
	private void display(String msg) {
		String time = sdf.format(new Date()) + " " + msg;

			svg.appendActivity(time + "\n");
	}

	private synchronized void broadcast(String message) {		
		String time = sdf.format(new Date()); // add HH:mm:ss infront of messages
		String msgChat = time + " " + message + "\n";		
                svg.appendActivity(msgChat);// display chatmessages in the room window		
		for(int i = clientThread.size(); --i >= 0;) { //client disconnected
			ClientThread th = clientThread.get(i);
		}
	}

	//when client logout
	synchronized void remove(int id) {
		//find ID and remove ID from clientThread
		for(int i = 0; i < clientThread.size(); ++i) {
			ClientThread th = clientThread.get(i);
			if(th.id == id) {
				clientThread.remove(i);
				return;
			}
		}
	}
	



	/** One instance of this thread will run for each client */
	class ClientThread extends Thread {
		
		Socket socket; // the socket where to connect and talk
		ObjectInputStream toServer; // to server
		ObjectOutputStream fromServer; //from Server	
		int id; // my unique id (easier for deconnection)		
		String username; // the Username of the Client		
		ChatMessage cm; // the only type of message a will receive		
		String date; 

		ClientThread(Socket socket) {
			
			id = ++uniqueId; // a unique id
			this.socket = socket;
			try
			{
                            fromServer = new ObjectOutputStream(socket.getOutputStream());
                            toServer  = new ObjectInputStream(socket.getInputStream());				
                            username = (String) toServer.readObject(); //get the username
                            display(username + " just connected.");
			}
			catch (IOException e) {
				return;
			}
			catch (ClassNotFoundException e) {
			}
            date = new Date().toString() + "\n";
		}

		//loop
		public void run() {
			// to loop until LOGOUT
			boolean serverStart = true;
			while(serverStart) {
				// read a String (which is an object)
				try {
					cm = (ChatMessage) toServer.readObject();
				}
				catch (IOException e) {
					display(username + " Exception reading Streams: " + e);
					break;				
				}
				catch(ClassNotFoundException e2) {
					break;
				}				
				String message = cm.getMessage(); // messaged passed
				
				switch(cm.getType()) { // Switch on the type of message receive

				case ChatMessage.MESSAGE:
					broadcast(username + ": " + message); //all clients can see message
					break;
				case ChatMessage.LOGOUT:
					display(username + " disconnected with a LOGOUT message.");
					serverStart = false;
					break;
				case ChatMessage.ONLINE:
					onlineUsers("Online users on port "+ port + sdf.format(new Date()) + "\n");
					//check online users
					for(int i = 0; i < clientThread.size(); ++i) {
						ClientThread th = clientThread.get(i);
						onlineUsers((i+1) + ") " + th.username + " since " + th.date);
					}
					break;
				}
			}
			remove(id); //remove logout clients from clientThread array list
			close();
		}
		
		// try to close everything
		private void close() {
			// try to close the connection
			try {
				if(fromServer != null) fromServer.close();
			}
			catch(Exception e) {}
			try {
				if(toServer != null) toServer.close();
			}
			catch(Exception e) {};
			try {
				if(socket != null) socket.close();
			}
			catch (Exception e) {}
		}
                
		private boolean onlineUsers(String msg) { //get online users toClient
			// if Client is still connected send the message to it
			if(!socket.isConnected()) {
				close();
				return false;
			}
			// write the message to the stream
			try {
				fromServer.writeObject(msg);
			}
			// if an error occurs, do not abort just inform the user
			catch(IOException e) {
				display("Error sending message to " + username);
				display(e.toString());
			}
			return true;
		}
	}
}
