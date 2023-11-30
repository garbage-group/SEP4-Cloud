package garbagegroup.cloud.tcpserver;

import java.io.*;
import java.net.Socket;

public class ServerSocketHandler {
  private int deviceId;
  private Socket socket;
  private InputStream inFromClient;
  private OutputStream outToClient;

  public ServerSocketHandler(Socket socket) {
    this.socket = socket;
    try {
      outToClient = socket.getOutputStream();
      inFromClient = socket.getInputStream();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public String sendMessage(String message) {
    try {
      System.out.println("Sending " + message + " to device with ID: " + deviceId);
      outToClient.write(message.getBytes());
      outToClient.flush();

      // Read response
      byte[] buffer = new byte[1024];
      int bytesRead = inFromClient.read(buffer);
      String response = new String(buffer, 0, bytesRead);

      System.out.println("Received response from device with ID " + deviceId + ": " + response);
      return response;
    } catch (IOException e) {
      System.out.println("Error sending/receiving message with device ID: " + deviceId);
      return "Client with an ID: " + deviceId + " disconnected";
    } catch (NullPointerException e) {
      System.out.println("Output stream not initialized for device ID: " + deviceId);
      return "Client with an ID: " + deviceId + " disconnected";
    }
  }

  public int getDeviceId() {
    return deviceId;
  }

  public void setDeviceId(int deviceId) {
    this.deviceId = deviceId;
  }

}
