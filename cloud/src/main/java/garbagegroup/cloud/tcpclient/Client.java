package garbagegroup.cloud.tcpclient;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Scanner;

public class Client {
    private ObjectOutputStream outToServer;
    private ObjectInputStream inFromServer;

    public void startClient() {
        try{
            Socket socket = new Socket("localhost", 2910);
            System.out.println("Connected to the server");

            outToServer = new ObjectOutputStream(socket.getOutputStream());
            inFromServer = new ObjectInputStream(socket.getInputStream());

            Thread t = new Thread(this::listenToMessages);
            t.setDaemon(true);
            t.start();

            Scanner scanner = new Scanner(System.in);

            while (true) {
                //System.out.println("Please type message >");
                String scanned = scanner.nextLine();
                outToServer.writeObject(scanned);

                if (scanned.equalsIgnoreCase("exit")) {
                    socket.close();
                    System.out.println("Client exits");
                    break;
                }
            }



        } catch(IOException e){
            e.printStackTrace();
        }
    }

    public void listenToMessages() {
        while(true){
            try
            {
                String result = (String) inFromServer.readObject();
                System.out.println("Client received: " + result);
            }
            catch (IOException | ClassNotFoundException e)
            {
                e.printStackTrace();
            }


        }
    }
}
