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
    void testGetHumidityById_ReturnsEmptyString_DeviceIsNotPresent() {
        // Arrange
        tcpServer.IoTDevices = Collections.emptyList();

        // Act
        String response = tcpServer.getDataById(1, "getHumidity");

        // Assert
        assertEquals("", response);
    }

//    @Test
//    void testGetHumidityById_ReturnsEmptyString_DeviceWithID1NotPresent() {
//        // Arrange
//        ServerSocketHandler mockHandler = mock(ServerSocketHandler.class);
//        when(mockHandler.getDeviceId()).thenReturn(1);
//        when(mockHandler.sendMessage(anyString())).thenReturn("humidityData");
//        tcpServer.IoTDevices = Collections.singletonList(mockHandler);
//
//        // Act
//        String response = tcpServer.getDataById(2, "getHumidity");
//
//        // Assert
//        assertEquals("", response);
//    }

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
}

