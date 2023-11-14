package garbagegroup.cloud.tcpserver;

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


  public ServerSocketHandler(int deviceId, Socket socket) {
    this.deviceId = deviceId;
    this.socket = socket;
    try {
      inFromClient = new ObjectInputStream(socket.getInputStream());
      outToClient = new ObjectOutputStream(socket.getOutputStream());
    }
    catch (IOException e) {
      e.printStackTrace();
    }
  }

  @Override public void run() {}

  public String sendMessage(String message) {
    try {
      System.out.println("Sending " + message + " to device with ID: " + deviceId);
      outToClient.writeObject(message);
      outToClient.flush();

      // Just wait for the response
      String response = (String) inFromClient.readObject();
      System.out.println("Received response from device with ID " + deviceId + ": " + response);
      return response;
    }
    catch (IOException | ClassNotFoundException e) {
      System.out.println("Error sending/receiving message with device ID: " + deviceId);
      return "Client with an ID: " + deviceId + " disconnected";
    }
  }

  public int getDeviceId() { return deviceId; }
}