
/*This class represents traders*/
package Assignment.Client;

import java.io.*;
import java.net.Socket;

import java.util.ArrayList;

import java.util.Scanner;
import java.util.concurrent.atomic.AtomicInteger;




public class Trader implements Cloneable{
    private final int port = 7736;
    private Scanner reader;
    private PrintWriter writer;
    Socket socket;
    private int id;//id identifies each trader
    private int currentStockHolder;
    private static final String STOCK_OWNER_FILE = "stockOwner.txt";//stores the ID of the current stock owner
    //string field stores all current traders
    private static final String TRADER_FILE = "traders.txt";

    //boolean field determines if the current trader has the stock
    private boolean holdingStock;
    private int traderCount;
    //fileStockOwner retrieves the ID of the current stock owner from the STOCK_OWNER_FILE
    private int fileStockOwner;
    //List fields to keep a list of the current traders
    private ArrayList<Integer> currentTraders, monitorTraders ;
    ObjectInputStream objectInputStream;

    /*boolean field to determine whether currentTraders array list needs to be cloned
    depending on how different it is to monitorTraders array list */
    boolean cloneNeeded = false;
    public Trader(int id) throws Exception {
        this.id = id;
        socket = new Socket("localhost", port);
        reader = new Scanner(socket.getInputStream());

        // Automatically flushes the stream with every command
        writer = new PrintWriter(socket.getOutputStream(), true);

        //send protocol message to the server for authentication
        writer.println("CREATE_ACCOUNT");
        String[] serverAuthentication = reader.nextLine().trim().split(" ");
        if (serverAuthentication[0].compareToIgnoreCase("success") != 0)
            throw new Exception();

        //Create & assign a new trader ID if successful
        this.id = Integer.parseInt(serverAuthentication[1]);
        //
        traderCount = Integer.parseInt(serverAuthentication[2]);
        AtomicInteger atomicInteger = new AtomicInteger(traderCount);

        holdingStock = false;
        while (true) {
            System.out.println("<- Trader "+this.id+" User Interface ->");
            //newTrader = atomicInteger.get();//to monitor changes to the current market
            System.out.println();
            System.out.println();
            //Trader initiates a conversation: Find out the current stock holder
            String[] serverStockResponse;
            writer.println("CURRENT_STOCK_HOLDER ");
            serverStockResponse = reader.nextLine().split(" ");
            if (Integer.parseInt(serverStockResponse[1]) == -1){
                System.out.println("Current stock holder (default value) = " + serverStockResponse[1]);
            }
            else{
                System.out.println("Current stock holder = " + serverStockResponse[1]);
            }

            System.out.println("MY ID: " + this.id);

            //Display Current list of traders
            FileInputStream fis = new FileInputStream(TRADER_FILE);
            ObjectInputStream ois = new ObjectInputStream(fis);

            currentTraders = (ArrayList<Integer>) ois.readObject();
            ois.close();
            fis.close();
            //make a clone of currentTraders arrayList to know who is new and who has just left
            monitorTraders = clone(currentTraders);


            //
            currentStockHolder = Integer.parseInt(serverStockResponse[1]);
            if (currentStockHolder == this.id) {
                holdingStock = true;
            }
            //if no body has the stock then request for it
            if (currentStockHolder == -1) {
                //System.out.println("Requesting for stock");
                writer.println("REQUEST_FOR_STOCK " + this.id);
                serverStockResponse = reader.nextLine().split(" ");
                if (serverStockResponse[0].compareToIgnoreCase("stock_available") == 0) {
                    //System.out.println("Current stock holder = " + serverStockResponse[1]);
                    traderCount = Integer.parseInt(serverStockResponse[1]);
                    atomicInteger.set(Integer.parseInt(serverStockResponse[1]));
                    holdingStock = true;
                } else if (serverStockResponse[0].compareToIgnoreCase("stock_taken") == 0) {
                    traderCount = Integer.parseInt(serverStockResponse[1]);
                    atomicInteger.set(Integer.parseInt(serverStockResponse[1]));
                    holdingStock = false;
                }
//
            }
            //Retrieve stockOwner from file called "stockOwner.txt"

            fis = new FileInputStream(STOCK_OWNER_FILE);
            objectInputStream = new ObjectInputStream(fis);
            if (objectInputStream.available()>0){
                fileStockOwner = objectInputStream.read();
                fis.close();
                objectInputStream.close();
                currentStockHolder = fileStockOwner;
            }


            //Ask if the current stock holder wants to trade the stock
            if (currentStockHolder == this.id || holdingStock) {
                while (holdingStock) {
                    //Retrieving the current list of traders
                    fis = new FileInputStream(TRADER_FILE);
                    ois = new ObjectInputStream(fis);

                    currentTraders = (ArrayList<Integer>) ois.readObject();
                    ois.close();
                    fis.close();
                    System.out.println();
                    System.out.println("List of Traders available in the market:");

                    if (currentTraders != null || currentTraders.isEmpty()){
                        for (Integer i: currentTraders) {
                            //measure the differences between both lists
                            if (currentTraders.size() > monitorTraders.size()){
                                if (!monitorTraders.contains(i)) {
                                    System.out.println("Trader ID: " + i + " (NEW*)");
                                    cloneNeeded = true;
                                }

                            }
                            else if (currentTraders.size() < monitorTraders.size()){
                                if (monitorTraders.contains(i) && !currentTraders.contains(i)){
                                    System.out.println("Trader ID: "+i+ " (GONE)");
                                    cloneNeeded = true;
                                }
                            }
                            else {
                                System.out.println("Trader ID: " + i);
                            }
                        }
                        if (cloneNeeded) {
                            monitorTraders = clone(currentTraders);
                        }
                    }
                    System.out.println();
                    System.out.println("Current Stock Holder ID: "+ currentStockHolder);
                    System.out.println();
                    System.out.println("Choose one of the following options: ");
                    System.out.println();
                    System.out.println("1. Enter 'Y' or 'N': Do you want to give the stock to another trader (including yourself)?");
                    System.out.println("2. Press any other key to exit");
                    System.out.println();
                    Scanner scanner = new Scanner(System.in);
                    String input = scanner.next();
                    input = input.trim().toUpperCase();
                    if (input.equals("Y")) {
                        System.out.println("Enter the ID of the trader to sell to: ");
                        input = scanner.next();
                        int otherTraderID = Integer.parseInt(input.trim());
                        //check if the chosen trader exists in the market
                        //by checking the file "traders.txt"
                        fis = new FileInputStream(TRADER_FILE);
                        ois = new ObjectInputStream(fis);
                        currentTraders = (ArrayList<Integer>) ois.readObject();
                        ois.close();
                        fis.close();

                        if (currentTraders != null){
                            if (!currentTraders.contains(otherTraderID)) {
                                System.out.println();
                                System.out.println("Trader " + otherTraderID + " doesn't exist in the market");
                            }

                            else {
                                //Give the stock to the chosen trader along with your ID via the server
                                writer.println("TRADE_STOCK " + otherTraderID + " " + this.id);
                                serverStockResponse = reader.nextLine().split(" ");
                                if (serverStockResponse[0].compareToIgnoreCase("success") == 0) {
                                    System.out.println("Success!");
                                    atomicInteger.set(Integer.parseInt(serverStockResponse[1]));
                                    currentStockHolder = otherTraderID;
                                    holdingStock = false;
                                } else if (serverStockResponse[0].compareToIgnoreCase("stock_taken") == 0) {
                                    holdingStock = false;
                                } else {
                                    throw new Exception();
                                }
                            }
                        }



                    } else if (input.equals("N")) {
                        continue;
                    }
                    else {
                        //System.out.println("Trader "+this.id + " is leaving the market.");
                        System.exit(1);

                    }
                    //Retrieve stockOwner from file called "stockOwner.txt"
                    fis = new FileInputStream(STOCK_OWNER_FILE);
                    objectInputStream = new ObjectInputStream(fis);
                    fileStockOwner = objectInputStream.read();

                    //Non-stock holders busy-wait here
                    while (fileStockOwner != this.id ){
                        //busy-wait
                        Thread.sleep(3500);
                        fis = new FileInputStream(STOCK_OWNER_FILE);
                        objectInputStream = new ObjectInputStream(fis);
                        fileStockOwner = objectInputStream.read();
                    }



                }

            }
            else{
                holdingStock = false;
                fis = new FileInputStream(STOCK_OWNER_FILE);
                objectInputStream = new ObjectInputStream(fis);
                fileStockOwner = objectInputStream.read();
                //System.out.println("BUSY-WAITING");

                while (fileStockOwner != this.id ){
                    //busy-wait
                    Thread.sleep(3500);
                    fis = new FileInputStream(STOCK_OWNER_FILE);
                    objectInputStream = new ObjectInputStream(fis);
                    fileStockOwner = objectInputStream.read();

                }

                fis.close();
                objectInputStream.close();
            }



        }
    }
    // method to clone the current list of traders
    private static ArrayList<Integer> clone(ArrayList<Integer> old){
        ArrayList<Integer> copy = new ArrayList<>(old.size());
        for (Integer i : old){
            copy.add(new Integer(i));
        }
        return copy;
    }

        }


