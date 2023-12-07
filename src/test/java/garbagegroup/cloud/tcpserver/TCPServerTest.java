package garbagegroup.cloud.tcpserver;

import garbagegroup.cloud.model.Bin;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.autoconfigure.couchbase.CouchbaseProperties;
import org.springframework.boot.test.context.SpringBootTest;

import javax.imageio.IIOException;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.BindException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;

import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest
class TCPServerTest {
    private final TCPServer tcpServer = new TCPServer();
    @Mock
    private ServerSocket mockedServerSocket;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        tcpServer.serverSocket = mockedServerSocket;
    }

    @Test
    void testGetHumidityById_ReturnsEmptyString_DeviceIsNotPresent() {
        // Arrange
        tcpServer.IoTDevices = Collections.emptyList();

        // Act
        String response = tcpServer.getDataById(1, "getHumidity");

        // Assert
        assertEquals("", response);
    }

    @Test
    void testGetHumidityById_DeviceAvailable() {
        // Arrange
        ServerSocketHandler mockHandler = mock(ServerSocketHandler.class);
        when(mockHandler.getDeviceId()).thenReturn(1);
        when(mockHandler.sendMessage(anyString())).thenReturn("humidityData");
        tcpServer.IoTDevices = Collections.singletonList(mockHandler);

        // Act
        String response = tcpServer.getDataById(1, "getHumidity");

        // Assert
        assertEquals("humidityData", response);
    }

    @Test
    void testGetIoTDevices() {
        // Arrange / Act
        ServerSocketHandler mockHandler = mock(ServerSocketHandler.class);
        tcpServer.IoTDevices = Collections.singletonList(mockHandler);

        // Assert
        assertEquals(Collections.singletonList(mockHandler), tcpServer.getIoTDevices());
    }

    @Test
    void testGetIoTSerialNumber() {
        // Arrange
        ServerSocketHandler mockHandler = mock(ServerSocketHandler.class);
        when(mockHandler.sendMessage(anyString())).thenReturn("123");
        tcpServer.socketHandler = mockHandler;

        // Act
        int serialNumber = tcpServer.getIoTSerialNumber();

        // Assert
        assertEquals(123, serialNumber);
    }

    @Test
    public void testSetIoTData_CalibrateDevice_Success() {
        // Arrange
        String payload = "calibrateDevice";
        int deviceId = 123;

        ServerSocketHandler mockHandler = mock(ServerSocketHandler.class);
        when(mockHandler.getDeviceId()).thenReturn(deviceId);
        when(mockHandler.sendMessage(payload)).thenReturn("OK");
        tcpServer.IoTDevices = Collections.singletonList(mockHandler);

        // Act
        boolean result = tcpServer.setIoTData(deviceId, payload);

        // Assert
        Assertions.assertTrue(result);
        verify(mockHandler).sendMessage(payload);
    }

    @Test
    public void testSetIoTData_NonMatchingDeviceId_Failure() {
        // Arrange
        String payload = "calibrateDevice";
        int deviceId = 123;

        ServerSocketHandler mockHandler = mock(ServerSocketHandler.class);
        when(mockHandler.getDeviceId()).thenReturn(deviceId + 1);
        tcpServer.IoTDevices = Collections.singletonList(mockHandler);

        // Act
        boolean result = tcpServer.setIoTData(deviceId, payload);

        // Assert
        Assertions.assertFalse(result);
    }


    /**
     * This test is a bit funny because it tests the Thread-native run() method which just iterates forever and waits for new clients to connect
     * Therefore, this test only tries the first iteration of the TCP server registering a client and then the code throws StringIndexOutOfBoundsException
     * Because it tries to request a serial number from the second connected client (as the mocked clients just keep coming and connecting)
     * But the OutputStream set up below only returns the message once (so only for the first client).
     *
     * @throws IOException because of mockedServerSocket.accept()
     */
    @Test
    public void testRun_ClientConnection_Success() throws IOException {
        // Arrange
        int deviceId = 123;
        Socket mockedClientSocket = mock(Socket.class);
        ServerSocketHandler mockHandler = mock(ServerSocketHandler.class);

        when(mockedServerSocket.accept()).thenReturn(mockedClientSocket);
        when(mockHandler.getDeviceId()).thenReturn(deviceId);

        tcpServer.setIoTDevices(new ArrayList<>());

        // Set up for the Server Socket Handler's input and output streams, so it actually send some data
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ByteArrayInputStream inputStream = new ByteArrayInputStream("123".getBytes());

        try {
            when(mockedClientSocket.getOutputStream()).thenReturn(outputStream);
            when(mockedClientSocket.getInputStream()).thenReturn(inputStream);
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Act and Assert
        assertThrows(StringIndexOutOfBoundsException.class, () -> {
            tcpServer.run();

            verify(mockedServerSocket).accept(); // Verify the server accepted a connection
            verify(mockHandler).getDeviceId(); // Verify the device ID was retrieved
            verify(mockedClientSocket).close();
        });
    }

    /**
     *
     * @throws IOException
     * @throws NullPointerException because it tries to read the ID of the client that just disconnected but it is null
     */
    @Test
    public void testRun_ClientConnection_Failure() throws IOException {
        // Arrange
        when(mockedServerSocket.accept()).thenThrow(IOException.class);

        // Act and Assert
        assertThrows(NullPointerException.class, () -> {
            tcpServer.run();

            verify(mockedServerSocket).accept();
        });


    }
}

