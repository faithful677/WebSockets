package Assignment.Client;

/*Client-side UI layer */
import java.util.Scanner;


public class TraderUI {
    public static void main(String[] args) throws Exception {
        if (true){
            System.out.println("Welcome to the stock market!");
            System.out.println("Enter option: ");
            System.out.println("a. Join stock market: ");
            System.out.println("x. Leave stock market:");
            System.out.println();
            System.out.println();

            Scanner in = new Scanner(System.in);
            String input = in.nextLine().trim();
            System.out.println();
            System.out.println();
            if (input.toLowerCase().equals("a")){
                Trader trader = new Trader(-1);
            }
            else{
                System.exit(1);
            }
        }
    }
}
