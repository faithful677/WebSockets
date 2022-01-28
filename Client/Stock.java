package Assignment.Client;

//This class represents the stock which is traded
public class Stock {
    private int currentOwner;
    public Stock(int currentOwner){
        this.currentOwner = currentOwner;
    }

    public int getCurrentOwner() {
        return currentOwner;
    }

    public void setCurrentOwner(int currentOwner) {
        this.currentOwner = currentOwner;
    }
}
