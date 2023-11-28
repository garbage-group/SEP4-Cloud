package garbagegroup.cloud.tcpserver;

import org.hibernate.result.Output;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.*;
import java.net.Socket;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class ServerSocketHandlerTest {
    @Mock
    private Socket mockedSocket;

    private ServerSocketHandler serverSocketHandler;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testSendMessage_Success() {
        // Arrange
        String message = "Test message";

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ByteArrayInputStream inputStream = new ByteArrayInputStream("Response".getBytes());

        try {
            when(mockedSocket.getOutputStream()).thenReturn(outputStream);
            when(mockedSocket.getInputStream()).thenReturn(inputStream);
        } catch (IOException e) {
            e.printStackTrace();
        }
        serverSocketHandler = new ServerSocketHandler(mockedSocket);
        serverSocketHandler.setDeviceId(123);

        // Act
        String response = serverSocketHandler.sendMessage(message);

        // Assert
        assertEquals("Response", response);
    }

    @Test
    void testSendMessage_OutputStreamThrowsIOException() {
        // Arrange
        String message = "Test message";

        // Mock an IOException when getting output stream
        try {
            when(mockedSocket.getOutputStream()).thenThrow(new IOException());
        } catch (IOException e) {
            e.printStackTrace();
        }

        serverSocketHandler = new ServerSocketHandler(mockedSocket);
        serverSocketHandler.setDeviceId(123);

        // Act
        String response = serverSocketHandler.sendMessage(message);

        // Assert
        assertEquals("Client with an ID: 123 disconnected", response);
    }

    @Test
    void testSendMessage_throwsNullPointerException_DueToInitializedOutputStream() {
        // Arrange
        String message = "Test message";

        // Mock a null output stream to simulate uninitialized output stream
        try {
            when(mockedSocket.getOutputStream()).thenReturn(null);
        } catch (IOException e) {
            e.printStackTrace();
        }

        serverSocketHandler = new ServerSocketHandler(mockedSocket);
        serverSocketHandler.setDeviceId(123);

        // Act
        String response = serverSocketHandler.sendMessage(message);

        // Assert
        assertEquals("Client with an ID: 123 disconnected", response);
    }



    @Test
    void testGetDeviceId_Returns1() {
        // Arrange
        serverSocketHandler = new ServerSocketHandler(mockedSocket);
        serverSocketHandler.setDeviceId(123);

        // Act
        int response = serverSocketHandler.getDeviceId();

        // Assert
        assertEquals(123, response);
    }
}


