/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Main.java to edit this template
 */
package project.chatbox;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

/**
 *
 * @author tanxinyii
 */
public class Server extends Frame implements ActionListener, Runnable{

    //declaration
    Client client;
    int port = 8080;
    ServerSocket serverSocket;
    Socket skt;
    
    Label status,connection;
    List chat;
    Panel controlPanel1, controlPanel2;
    TextField message;
    Button send,exit;
    BufferedReader br;
    BufferedWriter bw;
    
    
   
    public Server(String msg) {
        super(msg);
        
        controlPanel();
        listen();
    }
    
    public void controlPanel(){
        status = new Label("Status");
        connection = new Label("(No Client Connected)");
        chat = new List();
        message = new TextField(50);
        controlPanel1 = new Panel();
        controlPanel2 = new Panel();
        send = new Button("Send");
        exit = new Button("Exit");
        
        //add control to the panel
        controlPanel1.setLayout(new FlowLayout());
        controlPanel1.add(message);
        controlPanel1.add(send);
        controlPanel1.add(exit);
        
        controlPanel2.setLayout(new FlowLayout());
        controlPanel2.add(status);
        controlPanel2.add(connection);


        //add to Frame
        add(controlPanel2, BorderLayout.NORTH);
        add(chat, BorderLayout.CENTER);
        add(controlPanel1, BorderLayout.SOUTH);

        //click listener
        send.addActionListener(this);
        exit.addActionListener(this);
        
        setSize(800, 400);
        setLocation(50, 50);
        setBackground (Color.decode("#E8B16D"));
        setVisible(true);
    }

    public void listen(){
        try{
            serverSocket = new ServerSocket(port);
            status.setText("Listening on IP: "+serverSocket.getInetAddress().getHostName()+", Port: " + port );
            connection.setText("(No Client Connected)");
            skt = serverSocket.accept();
            br = new BufferedReader(new InputStreamReader(skt.getInputStream()));
            bw = new BufferedWriter(new OutputStreamWriter(skt.getOutputStream()));
            
            bw.write("Hi! Welcome to Chat-RELAX!!");
            bw.newLine();
            bw.flush();
                     
           
            
            Thread th;
            th = new Thread(this);
            th.start();
            
        }catch (Exception e) {
            status.setText(e.getMessage());
        }
    }
    
 

    @Override
    public void actionPerformed(ActionEvent e) {
        
        if(e.getSource().equals(exit)){
            System.exit(0);
        }else{
            try{
                //enter message
                bw.write(message.getText());
                bw.newLine();
                bw.flush();
                chat.add("Admin: " + message.getText());
                message.setText("");
                

            }catch (IOException ioe){
                status.setText(ioe.getMessage());
            }
        }
    }

    @Override
    public void run() {
        try{
            skt.setSoTimeout(1000);
        }catch (Exception e){}

        connection.setText("(Client Connected)");
        while(true){
            try{
                String msgA = br.readLine();
                if(msgA == null){
                    serverSocket.close();
                    break;
                }
                
                chat.add("Client: "+ msgA);
                
            }catch(IOException ioe){}
        }

    }
    
        
    // main class
    public static void main(String[] args){
        // TODO code application logic her
        new Server("Server Program");
    }
    
}
