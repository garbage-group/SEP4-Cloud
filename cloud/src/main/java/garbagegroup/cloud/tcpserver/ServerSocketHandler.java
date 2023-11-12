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
  private TCPServer tcpServer;


  public ServerSocketHandler(int deviceId, Socket socket, TCPServer tcpServer) {
    this.deviceId = deviceId;
    this.tcpServer = tcpServer;
    this.socket = socket;
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
      String read = (String) inFromClient.readObject();

      while (true)
      {
        System.out.println("Received from client: " + read);
        if (read.equalsIgnoreCase("exit"))
        {
          socket.close();
          System.out.println("Client disconnected");
          break;
        }
        // TCP Server handles the incoming data
        tcpServer.handleIoTData(deviceId, read);
      }
    }
    catch (IOException | ClassNotFoundException e) {
      e.printStackTrace();
    }
  }

  public void sendMessage(String message) {
    try {
      outToClient.writeObject(message);
    }
    catch (IOException e) {
      e.printStackTrace();
    }
  }

  public int getDeviceId() { return deviceId; }
}
