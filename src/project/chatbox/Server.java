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
    private final int port = 8080;
    ServerSocket serverSocket;
    Socket skt;
    
    Label status;
    List chat;
    Panel controlPanel;
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
        chat = new List();
        message = new TextField(50);
        controlPanel = new Panel();
        send = new Button("Send");
        exit = new Button("Exit");
        
        //add control to the panel
        controlPanel.setLayout(new FlowLayout());
        controlPanel.add(message);
        controlPanel.add(send);
        controlPanel.add(exit);

        //add to Frame
        add(status, BorderLayout.NORTH);
        add(chat, BorderLayout.CENTER);
        add(controlPanel, BorderLayout.SOUTH);

        //click listener
        send.addActionListener(this);
        exit.addActionListener(this);
        
        setSize(600, 400);
        setLocation(50, 100);
        setBackground (Color.decode("#E8B16D"));
        setVisible(true);
    }

    public void listen(){
        try{
            serverSocket = new ServerSocket(port);
            status.setText("Listening on Port: " + port);
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

                chat.add("Admin:    " + message.getText());
                message.setText("");

            }catch (IOException ioe){
                status.setText(ioe.getMessage());
            }
        }
    }

    @Override
    public void run() {
        try{
            skt.setSoTimeout(3000);
        }catch (Exception e){}

        status.setText("- Client Connected -");
        while(true){
            try{
                String msgA = br.readLine();
                if(msgA == null){
                    serverSocket.close();
                    break;
                }
                chat.add(client.name.getText()+": "+ msgA);
            }catch(IOException ioe){}
        }

    }
    
        
    // main class
    public static void main(String[] args){
        // TODO code application logic her
        new Server("Server Program");
    }
    
}
