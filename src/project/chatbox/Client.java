/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package project.chatbox;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.Socket;

/**
 *
 * @author hor_s
 */
public class Client extends Frame implements ActionListener, Runnable{

    Socket skt;
    Panel topCP, bottomCP;
    List chatList;
    Label username, ip, portNo;
    TextField iptext, port, message;
    public TextField name;
    Button connect, send, exit;
    BufferedWriter bw;
    BufferedReader br;
    Thread thread;
    

     
     public Client (String string){
          super(string);
          topControlPanel();
          bottomControlPanel();

          setSize(800, 400);
          setLocation(50, 250);
          setBackground (Color.decode("#D8BE7E"));
          setVisible(true);
          
     }


     public void topControlPanel(){
          topCP = new Panel();
          username = new Label("Username");
          ip = new Label("IP address");
          portNo = new Label("Port");
          
          name = new TextField(20);
          iptext = new TextField(10);
          port = new TextField(5);
          connect = new Button("Connect");
          chatList = new List();

          //add into Panel
          topCP.add(username);
          topCP.add(name);
          topCP.add(ip);
          topCP.add(iptext);
          topCP.add(portNo);
          topCP.add(port);
          topCP.add(connect);

          //set location of elements
          add(topCP, BorderLayout.NORTH);
          add(chatList, BorderLayout.CENTER);

          //CLICK LISTENER
          connect.addActionListener(this);


     }

     public void bottomControlPanel(){
          bottomCP = new Panel();
          message = new TextField(50);
          send = new Button("Send");
          exit = new Button("Exit");

          //add into panel
          bottomCP.add(message);
          bottomCP.add(send);
          bottomCP.add(exit);

          //set location of elements
          add(bottomCP, BorderLayout.SOUTH);

          //CLICK LISTENER
          send.addActionListener(this);
          exit.addActionListener(this);


     }
     
     @Override
    public void actionPerformed(ActionEvent e) {
        String user = name.getText();
        
        if(e.getSource().equals(exit)){
             System.exit(0);
        }else if (e.getSource().equals(connect)){

           
             try{
                  skt = new Socket(iptext.getText(), Integer.parseInt(port.getText()));
                  bw = new BufferedWriter (new OutputStreamWriter(skt.getOutputStream()));
                  br = new BufferedReader (new InputStreamReader(skt.getInputStream()));
                  
                  thread = new Thread (this);
                  thread.start();

             }catch (IOException ioe){
                  ioe.getMessage();
             }
        }else {
             try{
                  
                 if(bw != null){
                       bw.write(message.getText());
                       bw.newLine();
                       bw.flush();
                       chatList.add(user+ ": " + message.getText());
                       message.setText("");
                  }
             }catch(IOException ioe){
                       ioe.getMessage();
             }
        }
    }

    @Override
     public void run() {
        try {
            skt.setSoTimeout(1);
        } catch (Exception e) {
            iptext.setText(e.getMessage());
        }
        while (true) {
            try {
                String message = br.readLine();
                if(message == null) {
                    break;
                }
                chatList.add("Admin: " + message);
            } catch (Exception e) {
                
            }            
        }
    }
     
    public static void main(String[] args){
         new Client("Client Chat Box");
     }

    
}
