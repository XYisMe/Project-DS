package project.chatbox;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

/*
 * The server as a GUI
 */
public class ServerGUI extends JFrame implements ActionListener, WindowListener {
	
	private static final long serialVersionUID = 1L;	
	private JButton startStop; // the start and stop buttons	
	private JTextArea activity; //for the logs
	private JTextField portNo; // The port number	
	private Server server; // server
	
	
	// server constructor that receive the port to listen to for connection as parameter
	ServerGUI(int port) {
		super("Server");
		server = null;
		// in the NorthPanel the PortNumber the Start and Stop buttons
		JPanel north = new JPanel();
		north.add(new JLabel("Port number: "));
		portNo = new JTextField("  " + port);
		north.add(portNo);
		// to stop or start the server, we start with "Start"
		startStop = new JButton("Start");
		startStop.addActionListener(this);
		north.add(startStop);
		add(north, BorderLayout.NORTH);
		
		// the event and chat room
		JPanel center = new JPanel(new GridLayout(1,1));

		activity = new JTextArea(80,80);
		activity.setEditable(false);
		appendActivity("Activity log.\n");
		center.add(new JScrollPane(activity));	
		add(center);
		
		// need to be informed when the user click the close button on the frame
		setDefaultCloseOperation(EXIT_ON_CLOSE);
                addWindowListener(this);
		setSize(400, 600);
		setVisible(true);
	}		


	void appendActivity(String str) {
		activity.append(str);
		activity.setCaretPosition(activity.getText().length() - 1);
		
	}
	
	// start or stop where clicked
	public void actionPerformed(ActionEvent e) { //server start
		//loop start and stop button
		if(server != null) { //server is not running
			server.stop(); //refresh the server 
			server = null;
			portNo.setEditable(true);
			startStop.setText("Start"); //start the server
			return;
		}
            //start the server	
		int port;
		try {
			port = Integer.parseInt(portNo.getText().trim()); //get port number
		}
		catch(Exception er) {
			appendActivity("Invalid port number");
			return;
		}
		
		server = new Server(port, this); // create a new Server		
		new ServerRunning().start(); // and start it as a thread
		startStop.setText("Stop"); //button display as Stop and press again to Stop
		portNo.setEditable(false); //port number cannot be edited after Stop
	}
	
	public static void main(String[] arg) {		
		new ServerGUI(8080); //set server default port 8080
	}

	public void windowClosing(WindowEvent e) { //X button to close application
                server.stop(); //stop server on close application		
		dispose(); // dispose the frame
		System.exit(0);
	}
	//does not use the windows but need to implement
	public void windowClosed(WindowEvent e) {}
	public void windowOpened(WindowEvent e) {}
	public void windowIconified(WindowEvent e) {}
	public void windowDeiconified(WindowEvent e) {}
	public void windowActivated(WindowEvent e) {}
	public void windowDeactivated(WindowEvent e) {}

	/*
	 * A thread to run the Server
	 */
	class ServerRunning extends Thread {
		public void run() {
			server.start();   // should execute until if fails
			// the server crash
			startStop.setText("Start");
			portNo.setEditable(true);
			appendActivity("Server crashed\n");
			server = null;
		}
	}

}
