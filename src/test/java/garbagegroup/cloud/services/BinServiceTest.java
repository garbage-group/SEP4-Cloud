package garbagegroup.cloud.services;

import garbagegroup.cloud.model.Bin;
import garbagegroup.cloud.model.Humidity;
import garbagegroup.cloud.repository.IBinRepository;
import garbagegroup.cloud.service.BinService;
import garbagegroup.cloud.service.IBinService;
import garbagegroup.cloud.tcpserver.ITCPServer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.mockito.Mockito.mock;

class BinServiceTest {

    @Mock
    private ITCPServer tcpServer;

    @Mock
    private IBinRepository binRepository;

    @InjectMocks
    private BinService binService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void getCurrentHumidityByBinId1ReturnsHumidity() {
        // Mocking data
//        Bin bin = new Bin();
//        bin.setId(1L);
//        bin.setDeviceId(1);
//
//        Humidity humidity1 = new Humidity();
//        humidity1.setDateTime(LocalDateTime.now().minusMinutes(30));
//        humidity1.setValue(50.0);
//
//        Humidity humidity2 = new Humidity();
//        humidity2.setDateTime(LocalDateTime.now().minusMinutes(90));
//        humidity2.setValue(60.0);
//
//        bin.setHumidity(Arrays.asList(humidity1, humidity2));
//
//        // Mock repository behavior
//        when(binRepository.findById(1L)).thenReturn(Optional.of(bin));
//        when(tcpServer.getHumidityById(1)).thenReturn("Humidity data from IoT");
//
//        // Perform the test
//        Optional<Humidity> result = binService.getCurrentHumidityByBinId(1L);
//
//        // Verify the interactions and assertions
//        verify(binRepository, times(1)).findById(1L);
//        verify(tcpServer, times(1)).getHumidityById(1);
//        verify(binRepository, times(1)).save(any(Bin.class)); // Assuming save method is called when handling IoT data
//
//        // Assert the result
//        assertEquals(50.0, result.orElseThrow().getValue());
    }
}
