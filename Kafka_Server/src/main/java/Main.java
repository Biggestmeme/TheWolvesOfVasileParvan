import Auth.ClientAuthHandler;
import BZL.BZL;

import BZL.Services.MongoService;
import BZL.StockUtility;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;

import BZL.Stocks.StockSocketRequestHandler;
import Config.*;
import Mongo.Mongo;
import Socket.Client;
import Socket.Server;
import UserData.UserDataHandler;
import org.bson.types.ObjectId;
import org.json.JSONObject;

public class Main {

    public static void main(String[] args) throws Exception{

        Main.startEverything();
    }



    public static void startStockSocketServer() {
        Thread stockSocketThread = new Thread(new Runnable() {
            @Override
            public void run() {
                System.out.println("Stock Socket server started...");
                Server<Client> authServer = new Server<Client>(Config.STOCK_SOCKET_SERVER_PORT,new StockSocketRequestHandler());

            }
        });
        stockSocketThread.start();



    }
    public static void startAuthServer() {
        Thread authThread = new Thread(new Runnable() {
            @Override
            public void run() {
                System.out.println("Auth server started...");
                Server<Client> authServer = new Server<Client>(Config.ClientAuthPort,new ClientAuthHandler());

            }
        });
        authThread.start();
    }

    public static void startUserDataServer() {
        Thread userDataThread = new Thread(new Runnable() {
            @Override
            public void run() {
                System.out.println("Auth server started...");
                Server<Client> userDataServer = new Server<Client>(Config.USER_DATA_PORT,new UserDataHandler());

            }
        });
        userDataThread.start();
    }


    public static void startEverything() {
        StockUtility stockUtility = new StockUtility();
        HashMap<String,ArrayList<Thread>> stocksThread = new HashMap<String, ArrayList<Thread>>();


        Main.startAuthServer();
        Main.startStockSocketServer();
        Main.startUserDataServer();

        for(String ticker : stockUtility.getTickers()) {
            System.out.println("Current Stock BZL : " + ticker);
            ArrayList<Thread> currentTicker = new ArrayList<>();
            for(int i = 0; i < Config.PARTITIONS_PER_STOCK; i++) {
                int finalI = i;
                currentTicker.add(new Thread(new Runnable() {
                    @Override
                    public void run() {
                        Thread.currentThread().setName(ticker + " " + finalI);
                        System.out.println("Started Thread " + Thread.currentThread().getName() + " for " + ticker + " ");
                        BZL bzl = new BZL(ticker + "-receiver");
                    }
                }));
            }
            stocksThread.put(ticker,currentTicker);
        }

        for(ArrayList<Thread> threadArray : stocksThread.values()) {
            for(Thread thread : threadArray) {
                thread.start();
            }
        }

        Thread controllThreads = new Thread(new Runnable() {
            @Override
            public void run() {
                while(true) {
                    Scanner myObj = new Scanner(System.in);  // Create a Scanner object
                    String command = myObj.nextLine();  // Read user input
                    if (command.equals("Close Stock")) {
                        System.out.println("Enter Stock Name : ");
                        String closingStock = myObj.nextLine();  // Read user input
                        ArrayList<Thread> threadArray = stocksThread.get(closingStock);
                        for(Thread thread : threadArray) {
                            System.out.println("Stopped Thread " + thread.getName());
                            thread.stop();
                        }

                    }
                }
            }
        });
        controllThreads.start();
    }
}
