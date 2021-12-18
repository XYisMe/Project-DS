/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Main.java to edit this template
 */
package project.chatbox;

import java.net.Socket;

/**
 *
 * @author tanxinyii
 */
public class Server {

    private int port;
    private boolean keepGoing;

    Server(int port, ServerGUI aThis) {
        this.port = port;
    }

    private Server(int port) {
        this(port,null);
    }

    public void start(){
        keepGoing = true;
        System.out.println("Connection Established!!");      
    }
    
    public void stop(){
        keepGoing = false;
        System.out.println("Connection Closed!!");
        try 
        {
            new Socket("localhost", port);
	}
            catch(Exception e) 
        {
            //stop
	}
        
    }
     
    public static void main(String[] args){
        // TODO code application logic her
        new ServerGUI().setVisible(true);
    }
    
}
