package project.chatbox;

import java.net.*;
import java.io.*;
import java.util.*;

public class Client {

    private ObjectInputStream fromServer;
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

    public static void main(String[] args) {

        int portNo = 8080;
        String svrAdd = "localhost";
        String userName = "";

        // depending of the number of arguments provided we fall through
        switch (args.length) {
            case 3:
                svrAdd = args[2];
            case 2:
				try {
                portNo = Integer.parseInt(args[1]);
            } catch (Exception e) {
                System.out.println("Invalid port number.");
                System.out.println("Usage is: > java Client [username] [portNo] [svrAdd]");
                return;
            }
            case 1:
                userName = args[0];
            case 0:
                break;
            default:
                System.out.println("Usage is: > java Client [username] [portNo] {svrAdd]");
                return;
        }
        Client client = new Client(svrAdd, portNo, userName);

        if (!client.start()) {
            return;
        }

        // wait for messages from user
        Scanner scan = new Scanner(System.in);

        while (true) {
            System.out.print("> ");
            String msg = scan.nextLine(); // read message from user          
            if (msg.equalsIgnoreCase("LOGOUT")) {  // logout if message is LOGOUT
                client.sendMessage(new Messages(Messages.LOGOUT, ""));
                break; // break to do the disconnect
            } else if (msg.equalsIgnoreCase("ONLINE")) { // message ONLINE
                client.sendMessage(new Messages(Messages.ONLINE, ""));
            } else {				// default to ordinary message
                client.sendMessage(new Messages(Messages.MESSAGE, msg));
            }
        }
        client.disconnect();
    }

    public boolean start() {
        try { // try to connect to the server
            socket = new Socket(server, port);
        } // if it failed not much I can so
        catch (Exception ec) {
            display("Error connectiong to server:" + ec);
            return false;
        }

        String msg = " Connection accepted " + socket.getInetAddress() + ":" + socket.getPort();
        display(msg);

        try {
            fromServer = new ObjectInputStream(socket.getInputStream());
            toServer = new ObjectOutputStream(socket.getOutputStream());
        } catch (IOException eIO) {
            display("Exception creating new Input/output Streams: ");
            return false;
        }
        // creates the Thread to listen from the server       
        new ListenFromServer().start(); //create Thread

        try {
            toServer.writeObject(username); // Send our username to the server as String
        } catch (IOException eIO) {
            display("Exception doing login : ");
            disconnect();
            return false;
        }
        return true;
    }

    private void display(String msg) {
        if (cg == null) {
            System.out.println(msg); // println in console mode
        } else {
            cg.append(msg + "\n"); // append to the TextArea in GUI
        }
    }

    void sendMessage(Messages msg) { //send message to server
        try {
            toServer.writeObject(msg);
        } catch (IOException e) {
            display("Exception writing to server: ");
        }
    }

    private void disconnect() { //when user suddenly disconnect
        try {
            if (fromServer != null) {
                fromServer.close();
            }
        } catch (Exception e) {
        } // not much else I can do
        try {
            if (toServer != null) {
                toServer.close();
            }
        } catch (Exception e) {
        } // not much else I can do
        try {
            if (socket != null) {
                socket.close();
            }
        } catch (Exception e) {
        } // not much else I can do

        // inform the GUI
        if (cg != null) {
            cg.connectionFailed();
        }

    }

    /*
	 * waits for message from the server and append to the JTextArea
     */
    class ListenFromServer extends Thread {

        public void run() {
            while (true) {
                try {
                    String msg = (String) fromServer.readObject();
                    cg.append(msg);
                } catch (IOException e) {
                    display("Server has close the connection: " + e);
                    if (cg != null) {
                        cg.connectionFailed();
                    }
                    break;
                } catch (ClassNotFoundException e2) {
                }
            }
        }
    }
}