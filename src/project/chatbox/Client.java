package project.chatbox;

import java.net.*;
import java.io.*;
import java.util.*;

public class Client  {

	private ObjectInputStream fromClient;		
	private ObjectOutputStream toServer;
	private Socket socket;
	private ClientGUI cg;
	private String server, username;
	private int port;

	Client(String server, int port, String username) {
		// which calls the common constructor with the GUI set to null
		this(server, port, username, null);
	}
        
	Client(String server, int port, String username, ClientGUI cg) {
		this.server = server;
		this.port = port;
		this.username = username;
		this.cg = cg;
	}
	
	public boolean start() {
		// try to connect to the server
		try {
			socket = new Socket(server, port);
		} 
		// if it failed not much I can so
		catch(Exception ec) {
			display("Error connectiong to server:" + ec);
			return false;
		}
		
		String msg = "Connection accepted " + socket.getInetAddress() + ":" + socket.getPort();
		display(msg);
	
		try
		{
			fromClient  = new ObjectInputStream(socket.getInputStream());
			toServer = new ObjectOutputStream(socket.getOutputStream());
		}
		catch (IOException eIO) {
			display("Exception creating new Input/output Streams: ");
			return false;
		}

		// creates the Thread to listen from the server 
		new ListenFromServer().start(); //create Thread
		// Send our username to the server this is the only message that we
		// will send as a String. All other messages will be ChatMessage objects
		try
		{
			toServer.writeObject(username);
		}
		catch (IOException eIO) {
			display("Exception doing login : " );
			disconnect();
			return false;
		}
		// success we inform the caller that it worked
		return true;
	}

	/*
	 * To send a message to the console or the GUI
	 */
	private void display(String msg) {
		if(cg == null)
			System.out.println(msg);      // println in console mode
		else
			cg.append(msg + "\n");		// append to the ClientGUI JTextArea (or whatever)
	}
	
	/*
	 * To send a message to the server
	 */
	void sendMessage(ChatMessage msg) {
		try {
			toServer.writeObject(msg);
		}
		catch(IOException e) {
			display("Exception writing to server: ");
		}
	}

	/*
	 * When something goes wrong
	 * Close the Input/Output streams and disconnect not much to do in the catch clause
	 */
	private void disconnect() {
		try { 
			if(fromClient != null) fromClient.close();
		}
		catch(Exception e) {} // not much else I can do
		try {
			if(toServer != null) toServer.close();
		}
		catch(Exception e) {} // not much else I can do
        try{
			if(socket != null) socket.close();
		}
		catch(Exception e) {} // not much else I can do
		
		// inform the GUI
		if(cg != null)
			cg.connectionFailed();
			
	}

	public static void main(String[] args) {

            int portNumber = 8080;
		String serverAddress = "localhost";
		String userName = "";

		// depending of the number of arguments provided we fall through
		switch(args.length) {
			case 3:
				serverAddress = args[2];
			case 2:
				try {
					portNumber = Integer.parseInt(args[1]);
				}
				catch(Exception e) {
					System.out.println("Invalid port number.");
					System.out.println("Usage is: > java Client [username] [portNumber] [serverAddress]");
					return;
				}
			case 1: 
				userName = args[0];
			case 0:
				break;
			default:
				System.out.println("Usage is: > java Client [username] [portNumber] {serverAddress]");
			return;
		}
		Client client = new Client(serverAddress, portNumber, userName);
		
                
		if(!client.start())
			return;
		
		// wait for messages from user
		Scanner scan = new Scanner(System.in);
          	while(true) {
			System.out.print("> ");
			// read message from user
			String msg = scan.nextLine();
			// logout if message is LOGOUT
			if(msg.equalsIgnoreCase("LOGOUT")) {
				client.sendMessage(new ChatMessage(ChatMessage.LOGOUT, ""));
				// break to do the disconnect
				break;
			}
			// message WhoIsIn
			else if(msg.equalsIgnoreCase("WHOISIN")) {
				client.sendMessage(new ChatMessage(ChatMessage.ONLINE, ""));				
			}
			else {				// default to ordinary message
				client.sendMessage(new ChatMessage(ChatMessage.MESSAGE, msg));
			}
		}
		// done disconnect
		client.disconnect();	
	}

	/*
	 * a class that waits for the message from the server and append them to the JTextArea
	 */
	class ListenFromServer extends Thread {

		public void run() {
			while(true) {
				try {
					String msg = (String) fromClient.readObject();
						cg.append(msg);
				}
				catch(IOException e) {
					display("Server has close the connection: " + e);
					if(cg != null) 
						cg.connectionFailed();
					break;
				}
				catch(ClassNotFoundException e2) {
				}
			}
		}
	}
}
