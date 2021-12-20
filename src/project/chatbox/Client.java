package project.chatbox;

import java.net.*;
import java.io.*;
import java.util.*;
import java.text.SimpleDateFormat;

public class Client  {

	private ObjectInputStream fromServer;// to read from server socket
	private ObjectOutputStream toServer;// to write on the serversocket
	private Socket socket;
        private SimpleDateFormat sdf; // to display time
	private ClientGUI cvg;
	private String server, username;
	private int port;


	Client(String server, int port, String username) {	
            this(server, port, username, null);// calls the common constructor and GUI set to null
	}

	Client(String server, int port, String username, ClientGUI cvg) {
		this.server = server;
                sdf = new SimpleDateFormat("HH:mm:ss");
		this.port = port;
		this.username = username;
		this.cvg = cvg;
	}
	
	public boolean start() {
		// try to connect to the server
		try {
			socket = new Socket(server, port);
		} 
		catch(Exception ec) {
			return false;
		}
		
		String msg = "Connection accepted " + socket.getInetAddress() + ":" + socket.getPort();
		display(msg);
	
		try
		{
			fromServer  = new ObjectInputStream(socket.getInputStream());
			toServer = new ObjectOutputStream(socket.getOutputStream());
		}
		catch (IOException eIO) {
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
			disconnect();
			return false;
		}
		// success we inform the caller that it worked
		return true;
	}


    private void display(String msg) {
        String time = sdf.format(new Date()) + " " + msg;
        cvg.append(time + msg + "\n");
    }
	
	void sendMessage(ChatMessage msg) {
		try {
                    String time = sdf.format(new Date());
                    String msgChat = time + " " + msg + "\n";	
                    toServer.writeObject(msgChat);
		}
		catch(IOException e) {
		}
	}

	private void disconnect() {
		try { 
			if(fromServer!= null) fromServer.close();
		}
		catch(Exception e) {} 
		try {
			if(toServer != null) toServer.close();
		}
		catch(Exception e) {} 
        try{
			if(socket != null) socket.close();
		}
		catch(Exception e) {} // not much else I can do
		
		if(cvg != null)
			cvg.connectionFailed();
			
	}

        public static void main(String[] args) {
            // default values
            int portNumber = 8080;
            String serverAddress = "localhost";
            String userName = "";

            // depending of the number of arguments provided we fall through
            switch(args.length) {
			// > javac Client username portNumber serverAddr
			case 3:
				serverAddress = args[2];
			// > javac Client username portNumber
			case 2:
				try {
					portNumber = Integer.parseInt(args[1]);
				}
				catch(Exception e) {
					System.out.println("Invalid port number.");
					System.out.println("Usage is: > java Client [username] [portNumber] [serverAddress]");
					return;
				}
			// > javac Client username
			case 1: 
				userName = args[0];
			// > java Client
			case 0:
				break;
			// invalid number of arguments
			default:
				System.out.println("Usage is: > java Client [username] [portNumber] {serverAddress]");
			return;
		}
		// create the Client object
		Client client = new Client(serverAddress, portNumber, userName);
		// test if we can start the connection to the Server
		// if it failed nothing we can do
		if(!client.start())
			return;
		
		// wait for messages from user
		Scanner scan = new Scanner(System.in);
		// loop forever for message from the user
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
			else if(msg.equalsIgnoreCase("ONLINE")) {
				client.sendMessage(new ChatMessage(ChatMessage.ONLINE, ""));				
			}
			else {				// default to ordinary message
				client.sendMessage(new ChatMessage(ChatMessage.MESSAGE, msg));
			}
		}
		client.disconnect();	
	}
                

	
	//a class that waits for the message from the server and append them to the JTextArea
	 
	class ListenFromServer extends Thread {

		public void run() {
			while(true) {
				try {
					String msg = (String) fromServer.readObject();

						cvg.append(msg);

					
				}
				catch(IOException e) {
					display("Server has close the connection: " + e);
					if(cvg != null) 
						cvg.connectionFailed();
					break;
				}
				catch(ClassNotFoundException e2) {
				}
			}
		}
	}
}
