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
    TextField name, port, message;
    Button connect, send, exit;
    BufferedWriter bw;
    BufferedReader br;
    Thread thread;
     
     public Client (String string){
          super(string);
          topControlPanel();
          bottomControlPanel();

          setSize(600, 400);
          setLocation(700, 100);
          setBackground (Color.decode("#D8BE7E"));
          setVisible(true);
     }

     public void topControlPanel(){
          topCP = new Panel();
          name = new TextField(20);
          port = new TextField(5);
          connect = new Button("Connect");
          chatList = new List();

          //add into Panel
          topCP.add(name);
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
          message = new TextField(20);
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
        if(e.getSource().equals(exit)){
             System.exit(0);
        }else if (e.getSource().equals(connect)){

             try{
                  skt = new Socket(name.getText(), Integer.parseInt(port.getText()));
                  bw = new BufferedWriter (new OutputStreamWriter(skt.getOutputStream()));
                  br = new BufferedReader (new InputStreamReader(skt.getInputStream()));
                  name.setText(name.getText());

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
                       chatList.add("You: " + message.getText());
                       message.setText("");
                  }
             }catch(IOException ioe){
                       ioe.getMessage();
             }
        }
    }

    @Override
    public void run() {
        try{
             skt.setSoTimeout(1000);
        }catch(Exception e){
             e.getMessage();
        }

        while (true){
             try{
                  String msg = br.readLine();
                  if(msg == null){
                       break;
                  }
                  chatList.add("Admin: " + msg);
             }catch (Exception e){}
        }
    }

    public static void main(String[] args){
         new Client("Client Chat Box");
     }
    
}
