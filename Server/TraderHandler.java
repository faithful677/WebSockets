package Assignment.Server;

/*Server-side Trader class to handle communications with each trader*/
import Assignment.Client.Stock;


import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Scanner;

public class TraderHandler implements Runnable {
    private final Socket socket;
    private int id;// ID identifies each trader
    public static final Stock stock = new Stock(-1);
    //Array list containing all Traders
    public static final ArrayList<Integer> traderList = new ArrayList<>();
    //string field used in file objects which stores all current traders
    private static final String TRADER_FILE_SERVER = "traders.txt";
    //stores the ID of the current stock owner
    private static final String STOCK_OWNER_FILE_SERVER = "stockOwner.txt";

    /*The objects will be used to deposit traderList.ArrayList & the current stock
    holder's ID*/
    FileOutputStream fos;
    ObjectOutputStream oos;
    boolean sellStock;

    private String[] traderStockRequest;//field used to retrieve requests from the client
    public TraderHandler(Socket socket, int id) {
        this.socket = socket;
        this.id = id;
    }

    @Override
    public void run() {
        try (
            Scanner scanner = new Scanner(socket.getInputStream());
            PrintWriter writer = new PrintWriter(socket.getOutputStream(), true)) {

            /*Client-Server authentication*/
            String clientAuthentication = scanner.nextLine();
            //if the IDs match send a success message to the client along with an ID
            if (clientAuthentication.toLowerCase().equals("create_account")) {
                writer.println("SUCCESS " + id+ " "+ServerMain.ID);

                traderList.add(id);
                //write object to file named traders.txt
                FileOutputStream fos = new FileOutputStream(TRADER_FILE_SERVER);
                ObjectOutputStream oos = new ObjectOutputStream(fos);
                oos.writeObject(traderList);
                oos.close();
                fos.close();

            } else {
                throw new Exception();
            }
            System.out.println("-> Server User Interface <-");
            System.out.println();
            //new trader joining
            System.out.println("New Connection; Trader with an ID of '" + id + "' has just joined the market!");
            System.out.println();
            System.out.println();
            while (true) {
                System.out.println();
                System.out.println("List of all traders:");
                for(Integer i : traderList){
                    System.out.println("Trader ID: "+i);
                }
                System.out.println();
                //Respond to client request for the current stock holder
                traderStockRequest = scanner.nextLine().trim().split(" ");

                if (traderStockRequest[0].compareToIgnoreCase("current_stock_holder") == 0) {
                    writer.println("CURRENT_STOCK_HOLDER " + stock.getCurrentOwner() + " " + ServerMain.ID);
                    System.out.println("Current stock holder = "+stock.getCurrentOwner());

                }
                //receive request for the stock from a trader if nobody has the stock
                if (stock.getCurrentOwner() == -1) {
                    traderStockRequest = scanner.nextLine().trim().split(" ");
                    if (traderStockRequest[0].compareToIgnoreCase("request_for_stock") == 0) {
                        synchronized (stock) {
                            if (stock.getCurrentOwner() == -1) {
                                stock.setCurrentOwner(Integer.parseInt(traderStockRequest[1]));
                                writer.println("STOCK_AVAILABLE " + ServerMain.ID);
                            } else {
                                writer.println("STOCK_TAKEN " + ServerMain.ID);
                            }

                        }
                    }
                }
                //Save current stock owner to a file called 'stockOwner.txt'
                fos = new FileOutputStream(STOCK_OWNER_FILE_SERVER);
                oos = new ObjectOutputStream(fos);
                oos.write(stock.getCurrentOwner());
                oos.close();
                fos.close();


                 sellStock = false;
                while (!sellStock) {
                    //Wait for current stock holder request to sell stock (busy-waiting)
                    traderStockRequest = scanner.nextLine().trim().split(" ");
                    System.out.println();
//                    //check if the trader is asking for the current_stock_holder instead
                    if (traderStockRequest[0].compareToIgnoreCase("current_stock_holder")==0){
                        writer.println("CURRENT_STOCK_HOLDER " + stock.getCurrentOwner() + " " + ServerMain.ID);
                    }
                    if (traderStockRequest[0].compareToIgnoreCase("trade_stock") == 0) {
                        synchronized (stock) {
                            if (!traderList.contains(stock.getCurrentOwner())){
                                if (traderList.isEmpty())
                                    stock.setCurrentOwner(-1);
                                else{
                                    stock.setCurrentOwner(0);
                                }
                                writer.println("SUCCESS " + ServerMain.ID);
                            }
                            //if the current owner wants to give the stock to themselves
                            if (Integer.parseInt(traderStockRequest[2]) == Integer.parseInt(traderStockRequest[1])
                                    && traderList.contains(Integer.parseInt(traderStockRequest[1]))) {
                                stock.setCurrentOwner(Integer.parseInt(traderStockRequest[1]));
                                writer.println("SUCCESS " + ServerMain.ID);
                                //Save current stock owner to a file called 'stockOwner.txt'
                                //write object to file named traders.txt
                                fos = new FileOutputStream(STOCK_OWNER_FILE_SERVER);
                                oos = new ObjectOutputStream(fos);
                                oos.write(stock.getCurrentOwner());
                                oos.close();
                                fos.close();
                                sellStock = true;
                            }
                            //check if the current stock owner has not changed
                            else if (stock.getCurrentOwner() != Integer.parseInt(traderStockRequest[1])
                                        && traderList.contains(Integer.parseInt(traderStockRequest[1]))) {
                                System.out.println("Stock traded from Trader " + stock.getCurrentOwner() +
                                        " to Trader "+ Integer.parseInt(traderStockRequest[1]));
                                stock.setCurrentOwner(Integer.parseInt(traderStockRequest[1]));
                                writer.println("SUCCESS "+ServerMain.ID+ " "+ stock.getCurrentOwner());
                                //Save current stock owner to a file called 'stockOwner.txt'
                                //write object to file named traders.txt
                                fos = new FileOutputStream(STOCK_OWNER_FILE_SERVER);
                                oos = new ObjectOutputStream(fos);
                                oos.write(stock.getCurrentOwner());
                                oos.close();
                                fos.close();
                                //sellStock = true;
                            }

                            else {
                                writer.println("STOCK_TAKEN ");
                            }
                        }
                    }
                    //LIST CURRENT TRADERS
                    System.out.println();
                    System.out.println("List of all traders:");
                    for(Integer i : traderList){
                        System.out.println("Trader ID: "+i);
                    }
                    System.out.println();

                }
            }

            }

        catch (Exception e){
            System.out.println(e.getMessage());
        }
        finally {
            //This section is Executed when a Trader is about to leave the market
            System.out.println("Trader with ID of '"+id+ "' has left the market");
            if (stock.getCurrentOwner() == id) {
                //remove the trader from the current list
                if (!traderList.isEmpty()) {
                    traderList.remove((Integer) id);
                }
            }
            else if (stock.getCurrentOwner() != id){
                traderList.remove((Integer) id);
            }

            if (traderList.isEmpty()) {
                System.out.println("No traders are available at the moment; set stock to default value (-1)");
                stock.setCurrentOwner(-1);
            }
            else {
                stock.setCurrentOwner(traderList.get(0));
                System.out.println("The ID of the new trader with the stock is: '"+stock.getCurrentOwner()+"'");
            }
            System.out.println();
            //update list of traders
            try (FileOutputStream fos = new FileOutputStream(STOCK_OWNER_FILE_SERVER)) {
                ObjectOutputStream oos = new ObjectOutputStream(fos);
                oos.write(stock.getCurrentOwner());
                oos.close();
                //update current list
                //write object to file named traders.txt
                FileOutputStream fileOutputStream = new FileOutputStream(TRADER_FILE_SERVER);
                oos = new ObjectOutputStream(fileOutputStream);
                oos.writeObject(traderList);
                oos.close();

            } catch (IOException e) {
                e.printStackTrace();
            }


        }
    }
}
