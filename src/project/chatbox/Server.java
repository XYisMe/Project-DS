package project.chatbox;

import java.io.*;
import java.net.*;
import java.text.SimpleDateFormat;
import java.util.*;

public class Server {

    private static int uniqueId;
    private ArrayList<ThreadA> arrayL; //Client Thread
    private ServerGUI sg;
    private SimpleDateFormat sdf;
    private int port;
    private boolean serverStart;

    public Server(int port) { //server is bound to specified port number
        this(port, null);
    }

    public Server(int port, ServerGUI sg) {
        this.sg = sg;
        this.port = port;
        sdf = new SimpleDateFormat("HH:mm:ss");
        arrayL = new ArrayList<ThreadA>();
    }

        // for a client who logoff using the LOGOUT message
    synchronized void remove(int id) {
        // scan the array list until we found the Id
        for (int i = 0; i < arrayL.size(); ++i) {
            ThreadA ct = arrayL.get(i);
            // found it
            if (ct.id == id) {
                arrayL.remove(i);
                return;
            }
        }
    }

    public static void main(String[] args) {
        int portNo = 8080;
        switch (args.length) {
            case 1:
				try {
                portNo = Integer.parseInt(args[0]);
            } catch (Exception e) {
                System.out.println("Invalid port number.");
                System.out.println("Usage is: > java Server [portNumber]");
                return;
            }
            case 0:
                break;
            default:
                System.out.println("Usage is: > java Server [portNumber]");
                return;

        }

        Server server = new Server(portNo); //create a new socket and binds it to specified port number
        server.start();
    }
    
    public void start() {
        serverStart = true; 
        /* create socket server and wait for connection requests */
        try {
            ServerSocket svrSocket = new ServerSocket(port); //create the socket with the port number

            while (serverStart) {
                // format message saying we are waiting
                display("Server waiting for Clients on port " + port + ".");
                Socket skt = svrSocket.accept(); //accept socket to start listening for incoming client requests

                if (!serverStart) { //server stop
                    break;
                }
                ThreadA t = new ThreadA(skt); //incoming clients are stored in array 
                arrayL.add(t);
                t.start();
            }

            try {
                svrSocket.close();
                for (int i = 0; i < arrayL.size(); ++i) {
                    ThreadA tc = arrayL.get(i);
                    try {
                        tc.fromClient.close();
                        tc.toClient.close();
                        tc.socket.close();
                    } catch (IOException ioE) {
                        ioE.getMessage();
                    }
                }
            } catch (Exception e) {
                display("Exception closing the server and clients: " + e);
            }
        } catch (IOException e) {

            String msg = sdf.format(new Date()) + " Exception on new ServerSocket: " + e + "\n";
            display(msg);
        }
    }

    //GUI - stop function
    protected void stop() {
        serverStart = false;

        try {
            new Socket("localhost", port); 
        } catch (Exception e) {
            e.getMessage();
        }
    }

    private void display(String msg) {
        String time = sdf.format(new Date()) + " " + msg;
        if (sg == null) {
            System.out.println(time);
        } else {
            sg.appendActivity(time + "\n");
        }
    }

    //broadcast
    private synchronized void broadcast(String message) {

        String time = sdf.format(new Date());
        String messageLf = time + " " + message + "\n";

        if (sg == null) {
            System.out.print(messageLf);
        } else {
            sg.appendRoom(messageLf);     // append in the room window

            // we loop in reverse order in case we would have to remove a Client
            // because it has disconnected
            for (int i = arrayL.size(); --i >= 0;) {
                ThreadA ct = arrayL.get(i);
                // try to write to the Client if it fails remove it from the list
                if (!ct.writeMsg(messageLf)) {
                    arrayL.remove(i);
                    display("Disconnected Client " + ct.username + " removed from list.");
                }

            }
        }
    }



    class ThreadA extends Thread { //Client Thread

        Socket socket;
        ObjectInputStream fromClient;
        ObjectOutputStream toClient;
        int id;
        String username;
        Messages msgs;
        String date;

        ThreadA(Socket socket) {
            id = ++uniqueId;
            this.socket = socket;

            System.out.println("Thread trying to create Object Input/Output Streams");
            try {
                toClient = new ObjectOutputStream(socket.getOutputStream()); //send object to Messages (serialized) then to client after deserialization
                fromClient = new ObjectInputStream(socket.getInputStream());
                username = (String) fromClient.readObject(); //deserialize username
                display("Data is being deserialize");
                display(username + " just connected.");
            } catch (IOException e) {
                display("Exception creating new Input/output Streams: " + e);
                return;
            } catch (ClassNotFoundException e) {
                e.getMessage();
            }
            date = new Date().toString() + "\n";
        }

        public void run() {
            boolean serverStart = true;
            while (serverStart) {              
                try {
                    msgs = (Messages) fromClient.readObject(); // deserialize message (get msg)
                    display("Data is being deserialize");
                } catch (IOException e) {
                    display(username + " Exception reading Streams: " + e);
                    break;
                } catch (ClassNotFoundException e2) {
                    break;
                }
                String message = msgs.getMessage();

                switch (msgs.getType()) {
                    case Messages.ONLINE:
                        writeMsg("List of the users connected at " + sdf.format(new Date()) + "\n");
                        for (int i = 0; i < arrayL.size(); ++i) {
                            ThreadA ct = arrayL.get(i);
                            writeMsg((i + 1) + ") " + ct.username + " since " + ct.date);
                             break;
                        }
                    case Messages.MESSAGE:
                        broadcast(username + ": " + message);
                        break;
                    case Messages.LOGOUT:
                        display(username + " disconnected with a LOGOUT message.");
                        serverStart = false;
                        break;                   
                }
            }

            remove(id);
            close(); //server terminated (all connected clients will be terminated)
        }

        private void close() {
            // try to close the connection
            try {
                if (toClient != null) {
                    toClient.close();
                }
            } catch (Exception e) {
            }
            try {
                if (fromClient != null) {
                    fromClient.close();
                }
            } catch (Exception e) {
            };
            try {
                if (socket != null) {
                    socket.close();
                }
            } catch (Exception e) {
            }
        }

        private boolean writeMsg(String msg) {
            if (!socket.isConnected()) {
                close();
                return false;
            }
            try {
                toClient.writeObject(msg);
            } catch (IOException e) {
                display("Error sending message to " + username);
                display(e.toString());
            }
            return true;
        }
    }
}