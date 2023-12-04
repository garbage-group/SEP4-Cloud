package garbagegroup.cloud.services;

import garbagegroup.cloud.DTOs.BinDto;
import garbagegroup.cloud.DTOs.CreateBinDTO;
import garbagegroup.cloud.DTOs.UpdateBinDto;
import garbagegroup.cloud.model.Bin;
import garbagegroup.cloud.model.Humidity;
import garbagegroup.cloud.model.Level;
import garbagegroup.cloud.model.Temperature;
import garbagegroup.cloud.repository.IBinRepository;
import garbagegroup.cloud.tcpserver.ITCPServer;
import garbagegroup.cloud.service.serviceImplementation.BinService;
import garbagegroup.cloud.tcpserver.ServerSocketHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.net.Socket;
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
    public void updateBinFields_ThrowsInvalidArgumentExceptions_ForInvalidLongitude() {
        //Arrange
        UpdateBinDto updateBinDto = new UpdateBinDto(1L, 1000D, 40D, 40D);
        Bin bin = new Bin(40D, 40D, 1000D, 40D, null, null);

        //Act and assert
        assertThrows(IllegalArgumentException.class, () -> {
            binService.updateBinFields(bin, updateBinDto);
        });
    }

    @Test
    public void updateBinFields_ThrowsInvalidArgumentExceptions_ForInvalidLatitude() {
        //Arrange
        UpdateBinDto updateBinDto = new UpdateBinDto(1L, 10D, 1000D, 40D);
        Bin bin = new Bin(40D, 40D, 1000D, 40D, null, null);

        //Act and assert
        assertThrows(IllegalArgumentException.class, () -> {
            binService.updateBinFields(bin, updateBinDto);
        });
    }

    @Test
    public void updateBinFields_ThrowsInvalidArgumentExceptions_ForInvalidFillThreshold() {
        //Arrange
        UpdateBinDto updateBinDto = new UpdateBinDto(1L, 10D, 40D, 180D);
        Bin bin = new Bin(40D, 40D, 1000D, 40D, null, null);

        //Act and assert
        assertThrows(IllegalArgumentException.class, () -> {
            binService.updateBinFields(bin, updateBinDto);
        });
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

    @Test
    public void testHandleIoTData_HumidityDataReceived_SavesHumidity() {
        // Arrange
        int binId = 1;
        String data = "humid:45.0";
        when(binRepository.findById((long) binId)).thenReturn(Optional.of(new Bin()));

        // Act
        binService.handleIoTData(binId, data);

        // Assert
        verify(binRepository).save(any(Bin.class));
    }

    @Test
    public void testHandleIoTData_FillLevelDataReceived_SavesFillLevel() {
        // Arrange
        int binId = 1;
        String data = "level:45.0";
        when(binRepository.findById((long) binId)).thenReturn(Optional.of(new Bin()));

        // Act
        binService.handleIoTData(binId, data);

        // Assert
        verify(binRepository).save(any(Bin.class));
    }

    @Test
    public void testHandleIoTData_TemperatureDataReceived_SavesTemperature() {
        // Arrange
        int binId = 1;
        String data = "tempe:33.0";
        when(binRepository.findById((long) binId)).thenReturn(Optional.of(new Bin()));

        // Act
        binService.handleIoTData(binId, data);

        // Assert
        verify(binRepository).save(any(Bin.class));
    }

    @Test
    public void testGetAvailableDevice_NoAvailableDevice_ReturnsZero() {
        // Arrange
        when(tcpServer.getIoTDevices()).thenReturn(new ArrayList<>());

        // Act
        int result = binService.getAvailableDevice();

        // Assert
        assertEquals(0, result);
    }

    @Test
    public void testCreate_WithNoAvailableDevice_CreatesBinWithFakeDevice() {
        // Arrange
        int binId = 1;
        CreateBinDTO binDTO = new CreateBinDTO(20.34, 50.34, 56, 78);
        when(tcpServer.getIoTDevices()).thenReturn(new ArrayList<>());
        Bin bin = new Bin();
        bin.setId((long) 12);
        when(binRepository.save(any(Bin.class))).thenReturn(bin);
        when(binRepository.findById((long) binId)).thenReturn(Optional.of(new Bin()));

        // Act
        Bin result = binService.create(binDTO);

        // Assert
        assertNotNull(result.getDeviceId());
        assertTrue(result.getDeviceId() >= 1000 && result.getDeviceId() < 2000); // Assuming fake device ID is in this range
        verify(binRepository).save(any(Bin.class));
    }

    @Test
    void testCreateBin_WithDeviceIdNotZero() {
        // Arrange
        CreateBinDTO binDTO = new CreateBinDTO(20.34, 50.34, 56, 78);
        Socket mockedSocket = mock(Socket.class);

        when(binRepository.save(any(Bin.class))).thenReturn(new Bin());
        List<ServerSocketHandler> IoTDevices = new ArrayList<>();
        ServerSocketHandler ssh = new ServerSocketHandler(mockedSocket);
        ssh.setDeviceId(123);
        IoTDevices.add(ssh);
        when(tcpServer.getIoTDevices()).thenReturn(IoTDevices);

        // Act
        Bin createdBin = binService.create(binDTO);

        // Assert
        assertNotNull(createdBin);
        assertEquals(123, createdBin.getDeviceId()); // Ensure deviceId is set correctly
        verify(binRepository, times(1)).save(any(Bin.class)); // Verify that save method is called once
    }

    @Test
    public void testSaveHumidityByBinId_BinNotFound_ReturnsFalse() {
        // Arrange
        int binId = 1;
        double humidity = 50.0;
        LocalDateTime dateTime = LocalDateTime.now();
        when(binRepository.findById((long) binId)).thenReturn(Optional.empty());

        // Act
        boolean result = binService.saveHumidityByBinId(binId, humidity, dateTime);

        // Assert
        assertFalse(result);
    }

    @Test
    void testGetIoTData_WithActiveDevice() {
        // Arrange
        int binId = 1;
        int deviceId = 123;
        String payload = "somePayload";
        String expectedResponse = "Real IoT data";
        Socket mockedSocket = mock(Socket.class);


        when(tcpServer.getDataById(deviceId, payload)).thenReturn(expectedResponse);
        List<ServerSocketHandler> IoTDevices = new ArrayList<>();
        ServerSocketHandler ssh = new ServerSocketHandler(mockedSocket);
        ssh.setDeviceId(deviceId);
        IoTDevices.add(ssh);
        when(tcpServer.getIoTDevices()).thenReturn(IoTDevices);

        // Act
        String response = binService.getIoTData(binId, deviceId, payload);

        // Assert
        assertEquals(expectedResponse, response);
        verify(tcpServer, times(1)).getDataById(deviceId, payload);
    }

    @Test
    void testGetAvailableDevice_ReturnsOneAvailableDevice_MoreDevicesActive() {
        // Arrange
        Socket mockedSocket = mock(Socket.class);
        List<ServerSocketHandler> IoTDevices = new ArrayList<>();
        ServerSocketHandler ssh1 = new ServerSocketHandler(mockedSocket);
        ssh1.setDeviceId(1);
        IoTDevices.add(ssh1);
        ServerSocketHandler ssh2 = new ServerSocketHandler(mockedSocket);
        ssh1.setDeviceId(2);
        IoTDevices.add(ssh2);
        when(tcpServer.getIoTDevices()).thenReturn(IoTDevices);

        // Act
        int response = binService.getAvailableDevice();

        // Assert
        assertEquals(2, response);  // Make sure the available device has the ID of the last device
        verify(tcpServer, times(1)).getIoTDevices();
    }

    @Test
    void testGetAvailableDevice_Returns0_NoAvailableDevices() {
        // Arrange
        Bin bin1 = new Bin();
        bin1.setDeviceId(1);
        Bin bin2 = new Bin();
        bin2.setDeviceId(2);

        Socket mockedSocket = mock(Socket.class);
        List<ServerSocketHandler> IoTDevices = new ArrayList<>();
        ServerSocketHandler ssh1 = new ServerSocketHandler(mockedSocket);
        ssh1.setDeviceId(1);
        IoTDevices.add(ssh1);
        ServerSocketHandler ssh2 = new ServerSocketHandler(mockedSocket);
        ssh2.setDeviceId(2);
        IoTDevices.add(ssh2);

        List<Bin> bins = Arrays.asList(bin1, bin2);
        when(binRepository.findAll()).thenReturn(bins);
        when(tcpServer.getIoTDevices()).thenReturn(IoTDevices);

        // Act
        int response = binService.getAvailableDevice();

        // Assert
        assertEquals(0, response);
        verify(tcpServer, times(1)).getIoTDevices();
    }

    @Test
    void testGetAvilableDevice_ReturnsOneDevice() {
        // Arrange
        Bin bin1 = new Bin();
        bin1.setDeviceId(1);

        Socket mockedSocket = mock(Socket.class);
        List<ServerSocketHandler> IoTDevices = new ArrayList<>();
        ServerSocketHandler ssh1 = new ServerSocketHandler(mockedSocket);
        ssh1.setDeviceId(1);
        IoTDevices.add(ssh1);
        ServerSocketHandler ssh2 = new ServerSocketHandler(mockedSocket);
        ssh2.setDeviceId(2);
        IoTDevices.add(ssh2);

        List<Bin> bins = Arrays.asList(bin1);
        when(binRepository.findAll()).thenReturn(bins);
        when(tcpServer.getIoTDevices()).thenReturn(IoTDevices);

        // Act
        int response = binService.getAvailableDevice();

        // Assert
        assertEquals(2, response);
        verify(tcpServer, times(1)).getIoTDevices();
    }

    @Test
    void testHasActiveDevice_ReturnsFalse() {
        // Arrange
        Socket mockedSocket = mock(Socket.class);
        List<ServerSocketHandler> IoTDevices = new ArrayList<>();
        ServerSocketHandler ssh1 = new ServerSocketHandler(mockedSocket);
        ssh1.setDeviceId(1);
        IoTDevices.add(ssh1);
        when(tcpServer.getIoTDevices()).thenReturn(IoTDevices);

        // Act
        boolean response = binService.hasActiveDevice(2);

        // Assert
        assertFalse(response);
        verify(tcpServer, times(1)).getIoTDevices();
    }

    @Test
    public void updateBin_WithNonExistingBinToUpdate_ThrowsNoSuchElementException() {
        UpdateBinDto updateBinDto = new UpdateBinDto(15L, 40D, 20D, 80D);

        //Mock
        when(binRepository.findById(15L)).thenReturn(Optional.empty());

        // Act and assert
        assertThrows(NoSuchElementException.class, () -> binService.updateBin(updateBinDto));
    }

    @Test
    public void updateBin_DatabaseError_ReturnsFalse() {
        //Arrange
        UpdateBinDto updateBinDto = new UpdateBinDto(15L, 40D, 20D, 80D);
        Bin bin = new Bin(40D, 20D, null, 80D, null, null);

        //Mock
        when(binRepository.findById(15L)).thenReturn(Optional.of(bin));
        when(binRepository.save(any(Bin.class))).thenThrow(IllegalArgumentException.class);

        //Act
        boolean result = binService.updateBin(updateBinDto);

        // Act and assert
        assertFalse(result);
    }

    @Test
    public void updateBin_SuccessfulUpdate_ReturnsTrue() {
        //Arrange
        UpdateBinDto updateBinDto = new UpdateBinDto(15L, 40D, 20D, 80D);
        Bin bin = new Bin(40D, 20D, null, 80D, null, null);

        //Mock
        when(binRepository.findById(15L)).thenReturn(Optional.of(bin));

        //Act
        boolean result = binService.updateBin(updateBinDto);

        // Act and assert
        assertTrue(result);
    }
}
