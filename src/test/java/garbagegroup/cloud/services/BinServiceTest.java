package garbagegroup.cloud.services;

import garbagegroup.cloud.DTOs.BinDto;
import garbagegroup.cloud.DTOs.DTOConverter;
import garbagegroup.cloud.DTOs.UpdateBinDto;
import garbagegroup.cloud.model.Bin;
import garbagegroup.cloud.model.Humidity;
import garbagegroup.cloud.repository.IBinRepository;
import garbagegroup.cloud.tcpserver.ITCPServer;
import garbagegroup.cloud.service.serviceImplementation.BinService;
import garbagegroup.cloud.tcpserver.ITCPServer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.IOException;
import java.net.Socket;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import garbagegroup.cloud.service.serviceImplementation.BinService;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.NoSuchElementException;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class BinServiceTest {

    private DTOConverter dtoConverter = new DTOConverter();

    @Mock
    private IBinRepository binRepository;

    @Mock
    private ITCPServer tcpServer;

    @InjectMocks
    private BinService binService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void GetCurrentHumidityByBinId_WithBinWitchEmptyHumidityList_ReturnsFakeHumidity26() {
        //Arrange
        Bin bin = new Bin();
        bin.setId(1L);
        bin.setDeviceId(1);
        List<Humidity> humidityList = new ArrayList<>();
        bin.setHumidity(humidityList);

        //Mock
        when(binRepository.findById(1L)).thenReturn(Optional.of(bin));

        //Act
        Optional<Humidity> result = binService.getCurrentHumidityByBinId(1L);

        //Assert
        assertEquals(26.0, result.get().getValue());
    }

    @Test
    void GetCurrentHumidityByBinId_WithNoBin_ReturnsEmptyOptional() {
        //Arrange
        //Mock
        when(binRepository.findById(1L)).thenReturn(Optional.empty());
        //Act
        Optional<Humidity> result = binService.getCurrentHumidityByBinId(1L);
        //Assert
        assertEquals(Optional.empty(), result);
    }

    @Test
    void getCurrentHumidityByBinId_WithRecentHumidity_ReturnsHumidity50() {
        //Arrange
        Bin bin = new Bin();
        bin.setId(1L);
        Humidity humidity = new Humidity();
        humidity.setDateTime(LocalDateTime.now().minusMinutes(30));
        humidity.setValue(50.0);
        List<Humidity> humidityList = new ArrayList<>();
        humidityList.add(humidity);
        bin.setHumidity(humidityList);

        //Mock
        when(binRepository.findById(1L)).thenReturn(Optional.of(bin));

        //Act
        Optional<Humidity> result3 = binService.getCurrentHumidityByBinId(1L);

        //Assert
        assertEquals(50.0, result3.get().getValue());
    }

    @Test
    void getCurrentHumidityByBinId_WithOldHumidity_ReturnFakeRequestedHumidity() {
        //Arrange
        Bin bin = new Bin();
        bin.setId(1L);
        bin.setDeviceId(1);
        Humidity humidity = new Humidity();
        humidity.setDateTime(LocalDateTime.now().minusMinutes(90));
        humidity.setValue(60.0);
        List<Humidity> humidityList = new ArrayList<>();
        humidityList.add(humidity);
        bin.setHumidity(humidityList);

        //Mocks
        when(binRepository.findById(1L)).thenReturn(Optional.of(bin));

        //Act
        Optional<Humidity> result = binService.getCurrentHumidityByBinId(1L);

        //Assert
        assertEquals(26.0, result.get().getValue());
    }

    @Test
    public void saveHumidityById_SuccessfulSave() {
        // Arrange
        int binId = 1;
        double humidity = 50.0;
        LocalDateTime dateTime = LocalDateTime.now();

        Bin bin = new Bin();
        bin.setId((long) binId);

        when(binRepository.findById((long) binId)).thenReturn(Optional.of(bin));
        when(binRepository.save(any(Bin.class))).thenReturn(bin);

        // Act
        boolean result = binService.saveHumidityById(binId, humidity, dateTime);

        // Assert
        assertTrue(result);
        verify(binRepository, times(1)).findById((long) binId);
        verify(binRepository, times(1)).save(any(Bin.class));
        binRepository = mock(IBinRepository.class);
        tcpServer = mock(ITCPServer.class);
        binService = new BinService(binRepository, tcpServer);
    }

    @Test
    public void testIsValidLongitude() {
        BinService binService = new BinService(); // Assuming no-args constructor available

        assertTrue(binService.isValidLongitude(0.0)); // Test valid longitude (0.0)
        assertTrue(binService.isValidLongitude(-180.0)); // Test valid longitude (-180.0)
        assertTrue(binService.isValidLongitude(180.0)); // Test valid longitude (180.0)

        assertFalse(binService.isValidLongitude(-190.0)); // Test invalid longitude (-190.0)
        assertFalse(binService.isValidLongitude(190.0)); // Test invalid longitude (190.0)
    }

    @Test
    public void testDeleteBinById_WhenBinExists() {
        long binId = 1L;
        when(binRepository.existsById(binId)).thenReturn(true);

        binService.deleteBinById(binId);

        verify(binRepository).deleteById(binId);
    }

    @Test
    public void testDeleteBinById_WhenBinDoesNotExist() {
        long binId = 1L;
        when(binRepository.existsById(binId)).thenReturn(false);

        assertThrows(NoSuchElementException.class, () -> {
            binService.deleteBinById(binId);
        });
    }
    @Test
    public void testFindAllBins() {
        List<Bin> bins = Arrays.asList(new Bin(), new Bin());
        when(binRepository.findAll()).thenReturn(bins);

        List<BinDto> result = binService.findAllBins();

        assertEquals(2, result.size());
        verify(binRepository).findAll();
    }

    @Test
    public void testFindBinById_WhenBinExists() {
        long binId = 1L;
        Bin bin = new Bin();
        when(binRepository.findById(binId)).thenReturn(Optional.of(bin));

        Optional<BinDto> result = binService.findBinById(binId);

        assertTrue(result.isPresent());
        assertEquals(bin.getId(), result.get().getId());
        verify(binRepository).findById(binId);
    }

    @Test
    public void testFindBinById_WhenBinDoesNotExist() {
        long binId = 1L;
        when(binRepository.findById(binId)).thenReturn(Optional.empty());

        Optional<BinDto> result = binService.findBinById(binId);

        assertFalse(result.isPresent());
    }



    @Test
    public void testIsValidLatitude() {
        BinService binService = new BinService(); // Assuming no-args constructor available

        assertTrue(binService.isValidLatitude(0.0)); // Test valid latitude (0.0)
        assertTrue(binService.isValidLatitude(-90.0)); // Test valid latitude (-90.0)
        assertTrue(binService.isValidLatitude(90.0)); // Test valid latitude (90.0)

        assertFalse(binService.isValidLatitude(-100.0)); // Test invalid latitude (-100.0)
        assertFalse(binService.isValidLatitude(100.0)); // Test invalid latitude (100.0)
    }

    @Test
    public void testIsValidThreshold() {
        BinService binService = new BinService(); // Assuming no-args constructor available

        assertTrue(binService.isValidThreshold(0.0)); // Test valid threshold (0.0)
        assertTrue(binService.isValidThreshold(50.0)); // Test valid threshold (50.0)
        assertTrue(binService.isValidThreshold(100.0)); // Test valid threshold (100.0)

        assertFalse(binService.isValidThreshold(-10.0)); // Test invalid threshold (-10.0)
        assertFalse(binService.isValidThreshold(110.0)); // Test invalid threshold (110.0)
    }
}
