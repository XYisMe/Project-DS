package project.chatbox;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;


/*
 * The Client with its GUI
 */
public class ClientGUI extends JFrame implements ActionListener {

	private static final long serialVersionUID = 1L;
	
	private JLabel label;
	private JTextField tf;
	private JTextField tfServer, tfPort, tfName;
	private JButton login, logout, online;
	private JTextArea ta;
	private boolean connected;
	private Client client;
	private int defaultPort;
	private String defaultHost;

	ClientGUI(String host, int port) {

		super("Chat Client");
		defaultPort = port;
		defaultHost = host;
		
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
		logout.setEnabled(false);		// login before being able to logout
		online = new JButton("Online Users");
		online.addActionListener(this);
		online.setEnabled(false);		// login before being able to Who is in		
		northPanel.add(login);
		northPanel.add(logout);
		northPanel.add(online);               
		add(northPanel, BorderLayout.NORTH);
		
		ta = new JTextArea("Welcome to the Chat room\n", 50, 50);
		JPanel centerPanel = new JPanel(new GridLayout(1,1));
		centerPanel.add(new JScrollPane(ta));
		ta.setEditable(false);
		add(centerPanel, BorderLayout.CENTER);


		JPanel southPanel = new JPanel(new GridLayout(2,1,5,5));
		tf = new JTextField("Enter message here");
		tf.setBackground(Color.WHITE);
		southPanel.add(tf);
		

		add(southPanel, BorderLayout.SOUTH);

		setDefaultCloseOperation(EXIT_ON_CLOSE);
		setSize(600, 600);
		setVisible(true);
		tf.requestFocus();

	}

	void append(String str) {
		ta.append(str);
		ta.setCaretPosition(ta.getText().length() - 1);
	}

        
	void connectionFailed() {
		login.setEnabled(true);
		logout.setEnabled(false);
		online.setEnabled(false);

		tfPort.setText("" + defaultPort);
		tfServer.setText(defaultHost);
		tfServer.setEditable(false);
		tfPort.setEditable(false);
		tf.removeActionListener(this);
		connected = false;
	}

        //action for button
	public void actionPerformed(ActionEvent e) {
		Object o = e.getSource();
		if(o == logout) {
			client.sendMessage(new ChatMessage(ChatMessage.LOGOUT, ""));
			return;
		}
		if(o == online) {
			client.sendMessage(new ChatMessage(ChatMessage.ONLINE, ""));				
			return;
		}

		if(connected) {
			// just have to send the message
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

			client = new Client(server, port, username, this);
			if(!client.start()) 
				return;
			tf.setText("");
			connected = true;
			
			login.setEnabled(false);
			logout.setEnabled(true);
			online.setEnabled(true);
			tfServer.setEditable(false);
			tfPort.setEditable(false);
			tf.addActionListener(this);
		}

	}

	public static void main(String[] args) {
		new ClientGUI("localhost", 8080);
	}

}
