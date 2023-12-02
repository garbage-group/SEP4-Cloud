package garbagegroup.cloud.services;

import garbagegroup.cloud.DTOs.BinDto;
import garbagegroup.cloud.DTOs.DTOConverter;
import garbagegroup.cloud.model.Bin;
import garbagegroup.cloud.model.Humidity;
import garbagegroup.cloud.model.Level;
import garbagegroup.cloud.model.Temperature;
import garbagegroup.cloud.repository.IBinRepository;
import garbagegroup.cloud.tcpserver.ITCPServer;
import garbagegroup.cloud.service.serviceImplementation.BinService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.NoSuchElementException;


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
    void GetCurrentSensorDataByBinId_WithBinWitchEmptyHumidityList_ReturnsFakeSensorData() {
        //Arrange
        Bin bin = new Bin();
        bin.setId(1L);
        bin.setDeviceId(1);
        List<Humidity> humidityList = new ArrayList<>();
        List<Level> levelList = new ArrayList<>();
        List<Temperature> temperatureList = new ArrayList<>();
        bin.setHumidity(humidityList);
        bin.setFillLevels(levelList);
        bin.setTemperatures(temperatureList);

        //Mock
        when(binRepository.findById(1L)).thenReturn(Optional.of(bin));

        //Act
        Optional<Humidity> humidityResult = binService.getCurrentHumidityByBinId(1L);
        Optional<Level> levelResult = binService.getCurrentFillLevelByBinId(1L);
        Optional<Temperature> temperatureResult = binService.getCurrentTemperatureByBinId(1L);

        //Assert
        assertEquals(26.0, humidityResult.get().getValue());
        assertEquals(37.0, levelResult.get().getValue());
        assertEquals(26.0, temperatureResult.get().getValue());
    }

    @Test
    void GetCurrentSensorDataByBinId_WithNoBin_ReturnsEmptyOptionals() {
        //Arrange
        //Mock
        when(binRepository.findById(1L)).thenReturn(Optional.empty());

        //Act
        Optional<Humidity> humidityResult = binService.getCurrentHumidityByBinId(1L);
        Optional<Temperature> temperatureResult = binService.getCurrentTemperatureByBinId(1L);
        Optional<Level> levelResult = binService.getCurrentFillLevelByBinId(1L);

        //Assert
        assertEquals(Optional.empty(), humidityResult);
        assertEquals(Optional.empty(), temperatureResult);
        assertEquals(Optional.empty(), levelResult);
    }

    @Test
    void getCurrentSensorDataByBinId_WithRecentHumidity_ReturnsHumidity50Temperature69Level38() {
        //Arrange
        Bin bin = new Bin();
        bin.setId(1L);

        Humidity humidity = new Humidity();
        humidity.setDateTime(LocalDateTime.now().minusMinutes(30));
        humidity.setValue(50.0);
        List<Humidity> humidityList = new ArrayList<>();
        humidityList.add(humidity);
        bin.setHumidity(humidityList);
        Temperature temperature = new Temperature();
        temperature.setDateTime(LocalDateTime.now().minusMinutes(30));
        temperature.setValue(69.0);
        List<Temperature> temperatureList = new ArrayList<>();
        temperatureList.add(temperature);
        bin.setTemperatures(temperatureList);
        Level level = new Level();
        level.setDateTime(LocalDateTime.now().minusMinutes(30));
        level.setValue(38.0);
        List<Level> levelList = new ArrayList<>();
        levelList.add(level);
        bin.setFillLevels(levelList);

        //Mock
        when(binRepository.findById(1L)).thenReturn(Optional.of(bin));

        //Act
        Optional<Humidity> humidityResult = binService.getCurrentHumidityByBinId(1L);
        Optional<Temperature> temperatureResult = binService.getCurrentTemperatureByBinId(1L);
        Optional<Level> levelResult = binService.getCurrentFillLevelByBinId(1L);

        //Assert
        assertEquals(50.0, humidityResult.get().getValue());
        assertEquals(69.0, temperatureResult.get().getValue());
        assertEquals(38.0, levelResult.get().getValue());
    }

    @Test
    void getCurrentSensorDataByBinId_WithOldHumidity_ReturnsFakeRequestedSensorData() {
        //Arrange
        Bin bin = new Bin();
        bin.setId(1L);
        bin.setDeviceId(1);

        Humidity humidity = new Humidity();
        humidity.setDateTime(LocalDateTime.now().minusMinutes(90));
        humidity.setValue(50.0);
        List<Humidity> humidityList = new ArrayList<>();
        humidityList.add(humidity);
        bin.setHumidity(humidityList);
        Temperature temperature = new Temperature();
        temperature.setDateTime(LocalDateTime.now().minusMinutes(90));
        temperature.setValue(69.0);
        List<Temperature> temperatureList = new ArrayList<>();
        temperatureList.add(temperature);
        bin.setTemperatures(temperatureList);
        Level level = new Level();
        level.setDateTime(LocalDateTime.now().minusMinutes(90));
        level.setValue(38.0);
        List<Level> levelList = new ArrayList<>();
        levelList.add(level);
        bin.setFillLevels(levelList);

        //Mocks
        when(binRepository.findById(1L)).thenReturn(Optional.of(bin));

        //Act
        Optional<Humidity> humidityResult = binService.getCurrentHumidityByBinId(1L);
        Optional<Temperature> temperatureResult = binService.getCurrentTemperatureByBinId(1L);
        Optional<Level> levelResult = binService.getCurrentFillLevelByBinId(1L);

        //Assert
        assertEquals(26.0, humidityResult.get().getValue());
        assertEquals(26.0, temperatureResult.get().getValue());
        assertEquals(37.0, levelResult.get().getValue());
    }

    @Test
    public void saveSensorDataByBinId_SuccessfulSave() {
        // Arrange
        int binId = 1;
        double humidity = 50.0;
        double temperature = 69.0;
        double level = 37.0;
        LocalDateTime dateTime = LocalDateTime.now();

        Bin bin = new Bin();
        bin.setId((long) binId);

        when(binRepository.findById((long) binId)).thenReturn(Optional.of(bin));

        // Act
        boolean humidityResult = binService.saveHumidityByBinId(binId, humidity, dateTime);
        boolean temperatureResult = binService.saveTemperatureByBinId(binId, temperature, dateTime);
        boolean levelResult = binService.saveFillLevelByBinId(binId, level, dateTime);

        // Assert
        assertTrue(humidityResult);
        assertTrue(temperatureResult);
        assertTrue(levelResult);
    }

    @Test
    public void saveSensorDataByBinId_ThrowsExceptionBinNotFound() {
        int binId = 1;
        double humidity = 50.0;
        double temperature = 69.0;
        double level = 1.0;
        LocalDateTime dateTime = LocalDateTime.now();

        when(binRepository.findById((long) binId)).thenReturn(Optional.empty());

        // Act
        boolean humidityResult = binService.saveHumidityByBinId(binId, humidity, dateTime);
        boolean temperatureResult = binService.saveTemperatureByBinId(binId, temperature, dateTime);
        boolean levelResult = binService.saveFillLevelByBinId(binId, level, dateTime);

        // Assert
        assertFalse(humidityResult);
        assertFalse(temperatureResult);
        assertFalse(levelResult);
    }

    @Test
    public void saveSensorDataByBinId_ThrowsExceptionSensorDataNotSaved() {
        int binId = 1;
        double humidity = 50.0;
        double temperature = 69.0;
        double level = 1.0;
        LocalDateTime dateTime = LocalDateTime.now();

        Bin bin = new Bin();
        bin.setId((long) binId);

        when(binRepository.findById((long) binId)).thenReturn(Optional.of(bin));
        when(binRepository.save(any(Bin.class))).thenThrow(new RuntimeException("Sensor save error"));

        // Act
        boolean humidityResult = binService.saveHumidityByBinId(binId, humidity, dateTime);
        boolean temperatureResult = binService.saveTemperatureByBinId(binId, temperature, dateTime);
        boolean levelResult = binService.saveFillLevelByBinId(binId, level, dateTime);

        // Assert
        assertFalse(humidityResult);
        assertFalse(temperatureResult);
        assertFalse(levelResult);
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
