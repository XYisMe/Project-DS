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
    private JTextField tfServer, tfPort, tfName, enterMsg;
    private JButton login, logout, online, send;
    private JTextArea chatTextArea;
    private boolean connected;
    private Client client;
    private int defaultPort;
    private String defaultHost;

    ClientGUI(String host, int port) {

        super("Chat Room");
        defaultPort = port;
        defaultHost = host;

        //NorthPanel
        JPanel header = new JPanel(new GridLayout(2,1,1,3));
        JPanel northPanel = new JPanel(new FlowLayout(1));
        JPanel topPanel = new JPanel(new FlowLayout(1));
        tfServer = new JTextField(10);
        tfServer.setText(host);
        tfPort = new JTextField(5);
        tfPort.setText("" + port);
        tfName = new JTextField(10);
        tfName.setHorizontalAlignment(SwingConstants.LEFT);

        //add into Panel
        topPanel.add(new JLabel("Server Address:  "));
        topPanel.add(tfServer);
        topPanel.add(new JLabel("Port Number:  "));
        topPanel.add(tfPort);
        topPanel.add(new JLabel("Username: "));
        topPanel.add(tfName);
        topPanel.add(new JLabel(""));
        

        //buttons into North Panel
        login = new JButton("Login");
        login.addActionListener(this);
        logout = new JButton("Logout");
        logout.addActionListener(this);
        logout.setEnabled(false);// login before being able to logout
        online = new JButton("Online Users");
        online.addActionListener(this);
        online.setEnabled(false);// login before being able to Who is in		
        northPanel.add(login);
        northPanel.add(logout);
        northPanel.add(online);

        //header
        header.add(topPanel);
        header.add(northPanel);
        add(header, BorderLayout.NORTH);

        //center Panel for Chat Text Area
        chatTextArea = new JTextArea("Welcome to the Chat room\n", 50, 50);
        JPanel centerPanel = new JPanel(new GridLayout(1, 1));
        centerPanel.add(new JScrollPane(chatTextArea));
        chatTextArea.setEditable(false);
        add(centerPanel, BorderLayout.CENTER);

        //South Panel for sending message and button
        JPanel southPanel = new JPanel(new FlowLayout());
        enterMsg = new JTextField(30);
        enterMsg.setText("Enter your message here");
        southPanel.add(enterMsg);
        send = new JButton("Send");
        send.addActionListener(this);
        southPanel.add(send);
        add(southPanel, BorderLayout.SOUTH);

        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocation(800, 0);
        setSize(700, 600);
        setVisible(true);
    }

    void append(String str) {
        chatTextArea.append(str);
        chatTextArea.setCaretPosition(chatTextArea.getText().length() - 1);
    }

    void connectionFailed() {
        login.setEnabled(true);
        logout.setEnabled(false);
        online.setEnabled(false);

        tfPort.setText("" + defaultPort);
        tfServer.setText(defaultHost);
        tfServer.setEditable(false);
        tfPort.setEditable(false);
        enterMsg.removeActionListener(this);
        send.removeActionListener(this);
        connected = false;
    }

    public static void main(String[] args) {
        new ClientGUI("localhost", 8080);
    }
    
    public void actionPerformed(ActionEvent e) {
        Object o = e.getSource();
        String username = tfName.getText().trim();
        if (o == logout) {
            append(username + " has left the chat room.\n");
            client.sendMessage(new Messages(Messages.LOGOUT, ""));
            return;
        }
        if (o == online) {
            client.sendMessage(new Messages(Messages.ONLINE, ""));
            return;
        }

        if (connected) {

            client.sendMessage(new Messages(Messages.MESSAGE, enterMsg.getText()));
            enterMsg.setText("");
            return;
        }

        if (o == login) {
            append(username + " has joined the chat room.\n");
            if (username.length() == 0) {
                return;
            }
            String server = tfServer.getText().trim();
            if (server.length() == 0) {
                return;
            }
            String portNumber = tfPort.getText().trim();
            if (portNumber.length() == 0) {
                return;
            }
            int port = 0;
            try {
                port = Integer.parseInt(portNumber);
            } catch (Exception en) {
                return;
            }

            client = new Client(server, port, username, this);
            if (!client.start()) {
                return;
            }
            enterMsg.setText("");
            connected = true;

            login.setEnabled(false);
            logout.setEnabled(true);
            online.setEnabled(true);
            tfServer.setEditable(false);
            tfPort.setEditable(false);
            enterMsg.addActionListener(this);
        }
    }
}