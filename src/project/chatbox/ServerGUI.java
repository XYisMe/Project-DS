package project.chatbox;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ServerGUI extends JFrame implements ActionListener, WindowListener {

    private static final long serialVersionUID = 1L;
    private JButton stop, start;
    private JTextArea chat, activity;
    private JTextField portTxt;
    private SimpleDateFormat sdf;
    private Server server;

    ServerGUI(int port) {
        super("Server");
        server = null;
        JPanel north = new JPanel();
        north.add(new JLabel("Port number: "));
        portTxt = new JTextField("  " + port);
        north.add(portTxt);
        start = new JButton("Start");
        start.addActionListener(this);
        north.add(start);

        stop = new JButton("Stop");
        stop.addActionListener(this);
        north.add(stop);
        add(north, BorderLayout.NORTH);

        JPanel center = new JPanel(new GridLayout(1, 2));
        activity = new JTextArea(80, 80);
        activity.setEditable(false);
        appendActivity("Activity log\n");
        chat = new JTextArea(80, 80);
        chat.setEditable(false);
        appendRoom("Chat Log\n");
        center.add(new JScrollPane(chat));
        center.add(new JScrollPane(activity));
        add(center);

        addWindowListener(this);
        setSize(800, 400);
        setVisible(true);
    }
    
    public static void main(String[] arg) {      
        new ServerGUI(8080); // start server default port 8080
    }
    
    void appendActivity(String str) {
        activity.append(str);
        activity.setCaretPosition(activity.getText().length() - 1);

    }

    void appendRoom(String str) {
        chat.append(str);
        chat.setCaretPosition(chat.getText().length() - 1);
    }

    public void actionPerformed(ActionEvent e) {
        Object obj = e.getSource();
        if (obj == start) {
            int port;
            try {
                port = Integer.parseInt(portTxt.getText().trim());
            } catch (Exception er) {
                appendActivity("Invalid port number");
                return;
            }           
            server = new Server(port, this);// ceate a new Server           
            new ServerRunning().start();// and start it as a thread
            portTxt.setEditable(false); //server start and cannot change port number
            return;
        }
        if (obj == stop) {
            server.stop();
            server = null;
            portTxt.setEditable(true);
            return;
        }
    }

    public void windowClosing(WindowEvent e) { //GUI X button top right corner      
        if (server != null) { // if my Server exist
            try {
                server.stop();
            } catch (Exception eClose) {
            }
            server = null;
        }
        dispose();
        System.exit(0);
    }

    public void windowClosed(WindowEvent e) {
    }

    public void windowOpened(WindowEvent e) {
    }

    public void windowIconified(WindowEvent e) {
    }

    public void windowDeiconified(WindowEvent e) {
    }

    public void windowActivated(WindowEvent e) {
    }

    public void windowDeactivated(WindowEvent e) {
    }

    class ServerRunning extends Thread {

        public void run() {
            server.start();
            portTxt.setEditable(true);
            sdf = new SimpleDateFormat("HH:mm:ss");
            String time = sdf.format(new Date());
            appendActivity(time + " ****Server has stopped****\n");
            server = null;
        }
    }

}
