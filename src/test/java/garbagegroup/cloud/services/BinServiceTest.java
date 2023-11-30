package garbagegroup.cloud.services;

import garbagegroup.cloud.DTOs.BinDto;
import garbagegroup.cloud.DTOs.DTOConverter;
import garbagegroup.cloud.model.Bin;
import garbagegroup.cloud.model.Humidity;
import garbagegroup.cloud.repository.IBinRepository;
import garbagegroup.cloud.tcpserver.ITCPServer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import garbagegroup.cloud.service.serviceImplementation.BinService;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.extension.ExtendWith;

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
    void getCurrentHumidityByBinIdEdgeCases() {
        //Arrange
        Bin bin = new Bin();
        bin.setId(1L);
        bin.setDeviceId(1);
        Bin bin2 = new Bin();
        bin.setId(2L);
        bin.setDeviceId(2);
        Bin bin4 = new Bin();
        bin.setId(4L);
        bin.setDeviceId(4);
        Bin bin5 = new Bin();
        bin.setId(5L);
        bin.setDeviceId(5);

        Humidity humidity1 = new Humidity();
        humidity1.setDateTime(LocalDateTime.now().minusMinutes(30));
        humidity1.setValue(50.0);
        Humidity humidity2 = new Humidity();
        humidity2.setDateTime(LocalDateTime.now().minusMinutes(90));
        humidity2.setValue(60.0);
        Humidity humidity5 = new Humidity();
        humidity5.setDateTime(LocalDateTime.now().minusMinutes(90));
        humidity5.setValue(60.0);

        List<Humidity> humidityList = new ArrayList<>();
        humidityList.add(humidity1);
        List<Humidity> humidityList2 = new ArrayList<>();
        humidityList2.add(humidity2);
        List<Humidity> humidityList4 = new ArrayList<>();
        List<Humidity> humidityList5 = new ArrayList<>();
        humidityList5.add(humidity5);

        bin.setHumidity(humidityList);
        bin2.setHumidity(humidityList2);
        bin4.setHumidity(humidityList4);
        bin5.setHumidity(humidityList5);

        // Mock repository and TCP Server behaviors
        when(binRepository.findById(1L)).thenReturn(Optional.of(bin));
        when(binRepository.findById(2L)).thenReturn(Optional.of(bin2));
        when(binRepository.findById(3L)).thenReturn(Optional.empty());
        when(binRepository.findById(4L)).thenReturn(Optional.of(bin4));
        when(binRepository.findById(5L)).thenReturn(Optional.of(bin5));
        when(tcpServer.getHumidityById(2)).thenReturn("humid:25");
        when(tcpServer.getHumidityById(5)).thenReturn("Device with ID 5 is currently unavailable");


        // Act
        Optional<Humidity> result = binService.getCurrentHumidityByBinId(1L);
        Optional<Humidity> result2 = binService.getCurrentHumidityByBinId(2L);
        Optional<Humidity> result3 = binService.getCurrentHumidityByBinId(3L);
        Optional<Humidity> result4 = binService.getCurrentHumidityByBinId(4L);
        Optional<Humidity> result5 = binService.getCurrentHumidityByBinId(5L);

        // Assert
        assertEquals(50.0, result.orElseThrow().getValue());
        assertEquals(25.0, result2.get().getValue());
        assertEquals(Optional.empty(), result3);
        assertEquals(Optional.empty(), result4);
        assertEquals(Optional.empty(), result5);
    }

    @Test
    public void saveHumidityById() {
        //Arrange

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


}
