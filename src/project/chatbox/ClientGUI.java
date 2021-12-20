package project.chatbox;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;


/*
 * The Client with its GUI
 */
public class ClientGUI extends JFrame implements ActionListener {

	private static final long serialVersionUID = 1L;	
	private JTextField tf; // hold messages	
	private JTextField tfServer, tfPort, tfName; // to hold the server address, port number and username	
	private JButton login, logout, online; // to login, Logout and see online users
	private JTextArea chatArea; // for the chat room	
	private boolean connected; // for connection
	private Client client;	
	private int defaultPort; //default port number
	private String defaultHost; // default host 

	// Constructor connection receiving a socket number
	ClientGUI(String host, int port) {

		super("Chat Room");
		defaultPort = port;
		defaultHost = host;
		
		//NorthPanel
		JPanel northPanel = new JPanel(new GridLayout(4,2,6,10));
		JPanel serverAndPort = new JPanel(new GridLayout(1,5, 1, 3));
		tfServer = new JTextField(host);
		tfPort = new JTextField("" + port);
                tfName = new JTextField("");              
		tfName.setHorizontalAlignment(SwingConstants.RIGHT);
                
		serverAndPort.add(new JLabel("Server Address:  "));
		serverAndPort.add(tfServer);
		serverAndPort.add(new JLabel("Port Number:  "));
		serverAndPort.add(tfPort);
                serverAndPort.add(new JLabel("Username: "));
                serverAndPort.add(tfName);
		serverAndPort.add(new JLabel(""));
		northPanel.add(serverAndPort);
               
                login = new JButton("Login");
		login.addActionListener(this);		
		logout = new JButton("Logout");
		logout.addActionListener(this);
		logout.setEnabled(false);// you have to login before being able to logout
		online = new JButton("Online Users");
		online.addActionListener(this);
		online.setEnabled(false);// you have to login before being able to Who is in		
		northPanel.add(login);
		northPanel.add(logout);
		northPanel.add(online);               
		add(northPanel, BorderLayout.NORTH);
		
                //CenterPanel
		chatArea = new JTextArea("Welcome to the Chat room\n", 30,30);
		JPanel centerPanel = new JPanel(new GridLayout(1,1));
		centerPanel.add(new JScrollPane(chatArea));
		chatArea.setEditable(false);
		add(centerPanel, BorderLayout.CENTER);
                
                //SouthPanel
		JPanel southPanel = new JPanel(new GridLayout(2,1,5,5));
		tf = new JTextField("Enter message here");
		tf.setBackground(Color.WHITE);
		southPanel.add(tf);
		add(southPanel, BorderLayout.SOUTH);

		setDefaultCloseOperation(EXIT_ON_CLOSE);
		setSize(450, 250);
		setVisible(true);
		tf.requestFocus();

	}

	// Client to append text in the chat text area 
	void append(String str) {
		chatArea.append(str);
		chatArea.setCaretPosition(chatArea.getText().length() - 1);
	}
        
	void connectionFailed() { // reset gui to default settings
		login.setEnabled(true);
		logout.setEnabled(false);
		online.setEnabled(false);
		tfPort.setText("" + defaultPort); //goes back to default
		tfServer.setText(defaultHost); //goes back to default
		tfServer.setEditable(false);
		tfPort.setEditable(false);
		tf.removeActionListener(this);
		connected = false;
	}
		
	public void actionPerformed(ActionEvent e) {
		Object o = e.getSource();
		
		if(o == logout) { // if logout
			client.sendMessage(new ChatMessage(ChatMessage.LOGOUT, ""));
			return;
		}		
		if(o == online) { // if check online users
			client.sendMessage(new ChatMessage(ChatMessage.ONLINE, ""));				
			return;
		}
		
		if(connected) { // user connected
			// just have to send the message to Server
			client.sendMessage(new ChatMessage(ChatMessage.MESSAGE, tf.getText()));				
			tf.setText("");
			return;
		}
		

		if(o == login) {
			String username = tfName.getText().trim();
			if(username.length() == 0)
				return;
			String server = tfServer.getText().trim();
			if(server.length() == 0)
				return;
			String portNumber = tfPort.getText().trim();
			if(portNumber.length() == 0)
				return;
			int port = 0;
			try {
				port = Integer.parseInt(portNumber);
			}
			catch(Exception en) {
				return; 
			}

			//create new Client
			client = new Client(server, port, username, this);
			if(!client.start())  //test if can start new Client
				return;
                        tf.setText("");
			connected = true;	
			login.setEnabled(false);// disable login button			
			logout.setEnabled(true);// enable the 2 buttons
			online.setEnabled(true);			
			tfServer.setEditable(false);// disable the Server 
			tfPort.setEditable(false); //disable Port			
			tf.addActionListener(this);// Action listener for when the user enter a message
		}

	}

	// to start the whole thing the server
	public static void main(String[] args) {
		new ClientGUI("localhost", 8080);
	}

}
