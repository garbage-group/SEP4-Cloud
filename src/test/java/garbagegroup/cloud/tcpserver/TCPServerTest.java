package garbagegroup.cloud.tcpserver;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.context.SpringBootTest;

import java.net.ServerSocket;
import java.util.Collections;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@SpringBootTest
class TCPServerTest {
    private TCPServer tcpServer;

    @Mock
    private ServerSocket mockedServerSocket;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        tcpServer = new TCPServer();
        tcpServer.serverSocket = mockedServerSocket;
    }

    @Test
    void testGetHumidityById_DeviceUnavailable_DueToNoDevices() {
        // Arrange
        tcpServer.IoTDevices = Collections.emptyList();

        // Act
        String response = tcpServer.getHumidityById(1);

        // Assert
        assertEquals("Device with ID 1 is currently unavailable", response);
    }

    @Test
    void testGetHumidityById_DeviceUnavailable_DeviceWithID1NotPresent() {
        // Arrange
        ServerSocketHandler mockHandler = mock(ServerSocketHandler.class);
        when(mockHandler.getDeviceId()).thenReturn(1);
        when(mockHandler.sendMessage(anyString())).thenReturn("humidityData");
        tcpServer.IoTDevices = Collections.singletonList(mockHandler);

        // Act
        String response = tcpServer.getHumidityById(2);

        // Assert
        assertEquals("Device with ID 2 is currently unavailable", response);
    }

    @Test
    void testGetHumidityById_DeviceAvailable() {
        // Arrange
        ServerSocketHandler mockHandler = mock(ServerSocketHandler.class);
        when(mockHandler.getDeviceId()).thenReturn(1);
        when(mockHandler.sendMessage(anyString())).thenReturn("humidityData");
        tcpServer.IoTDevices = Collections.singletonList(mockHandler);

        // Act
        String response = tcpServer.getHumidityById(1);

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
}

