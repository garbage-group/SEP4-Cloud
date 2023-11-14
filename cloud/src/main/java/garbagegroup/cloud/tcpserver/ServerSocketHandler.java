package garbagegroup.cloud.tcpserver;

import garbagegroup.cloud.service.BinService;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;


public class ServerSocketHandler implements Runnable
{
  private int deviceId;
  private Socket socket;
  private ObjectInputStream inFromClient;
  private ObjectOutputStream outToClient;
  private TCPServer tcpServer;
  private BinService binService;


  public ServerSocketHandler(int deviceId, Socket socket, TCPServer tcpServer, BinService binService) {
    this.deviceId = deviceId;
    this.tcpServer = tcpServer;
    this.socket = socket;
    this.binService = binService;
    try {
      inFromClient = new ObjectInputStream(socket.getInputStream());
      outToClient = new ObjectOutputStream(socket.getOutputStream());
    }
    catch (IOException e) {
      e.printStackTrace();
    }
  }

  @Override public void run() {

    try
    {
      while (true)
      {
        String read = (String) inFromClient.readObject();
        System.out.println("Received from client with ID " + deviceId + ": " + read);
        if (read.equalsIgnoreCase("exit"))
        {
          socket.close();
          System.out.println("Client disconnected");
          break;
        }
        // Bin Service handles the incoming data
        binService.handleIoTData(deviceId, read);
      }
    }
    catch (IOException | ClassNotFoundException e) {
      System.out.println("Client disconnected");
      //e.printStackTrace();
    }
  }

  public String sendMessage(String message) {
    String response = "";
    try {
      System.out.println("Sending " + message + " to device with ID: " + deviceId);
      outToClient.writeObject(message);
      response = (String) inFromClient.readObject();
    }
    catch (IOException | ClassNotFoundException e) {
      e.printStackTrace();
    }
    return response;
  }

  public int getDeviceId() { return deviceId; }
}
