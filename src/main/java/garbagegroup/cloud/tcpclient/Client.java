package garbagegroup.cloud.tcpclient;

import java.io.*;
import java.net.Socket;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Scanner;

public class Client {
    private OutputStream outToServer;
    private InputStream inFromServer;

    public void startClient() {
        try{
            Socket socket = new Socket("localhost", 2910);
            System.out.println("Connected to the server");

            outToServer = socket.getOutputStream();
            inFromServer = socket.getInputStream();

            Thread t = new Thread(this::listenToMessages);
            t.setDaemon(true);
            t.start();

            Scanner scanner = new Scanner(System.in);

            while (true) {
                String scanned = scanner.nextLine();
                outToServer.write(scanned.getBytes());
                outToServer.flush();

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
                // Read response
                byte[] buffer = new byte[1024];
                int bytesRead = inFromServer.read(buffer);
                String result = new String(buffer, 0, bytesRead);
                System.out.println("Client received: " + result);
                if (result.equals("getHumidity")) {
                    outToServer.write(("humid:25.0").getBytes());
                    outToServer.flush();
                }
                else if (result.equals("getSerialNumber")) {
                    outToServer.write("3456".getBytes());
                    outToServer.flush();
                }
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }
    }
}
