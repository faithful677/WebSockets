package Assignment.Server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class ServerMain {
    private final static int port = 7736;
    public static int ID;
    //array list to store the IDs of all customers in the market
    public static final ArrayList<Integer> allTraders = new ArrayList<>();

    public static void main(String[] args) {
        RunServer();
    }
    private static void RunServer() {
        ServerSocket serverSocket = null;
        try {
            serverSocket = new ServerSocket(port);//listen for incoming connections at port 7736
            System.out.println("Waiting for incoming connections...");
            while (true) {
                Socket socket = serverSocket.accept();//this returns a socket object when a client connects
                ServerMain.ID = ServerMain.ID + 1;
                new Thread(new TraderHandler(socket, ServerMain.ID )).start();



            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
