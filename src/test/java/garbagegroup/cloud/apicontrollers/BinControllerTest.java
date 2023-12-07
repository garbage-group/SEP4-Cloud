package garbagegroup.cloud.apicontrollers;

import garbagegroup.cloud.DTOs.*;
import garbagegroup.cloud.controller.BinController;
import garbagegroup.cloud.model.Bin;
import garbagegroup.cloud.model.Humidity;
import garbagegroup.cloud.model.Level;
import garbagegroup.cloud.model.Temperature;
import garbagegroup.cloud.service.serviceInterface.IBinService;
import io.swagger.models.Response;
import org.junit.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import javax.swing.text.html.Option;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class BinControllerTest {

    @Mock
    private IBinService binService;

    @InjectMocks
    private BinController binController;

    @BeforeEach
    public void setup() {
        // Set up any initial configurations or mocking behavior here if needed
    }

    @Test
    public void testUpdateBin_Success() {
        UpdateBinDto updateBinDto = new UpdateBinDto();
        updateBinDto.setLongitude(10.0); // Set your values for the UpdateBinDto object

        // Mock behavior of the binService.updateBin method
        when(binService.updateBin(updateBinDto)).thenReturn(true);

        ResponseEntity<String> response = binController.updateBin(1L, updateBinDto);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Bin updated successfully", response.getBody());
    }

    @Test
    public void testUpdateBin_IllegalArgumentException() {
        UpdateBinDto updateBinDto = new UpdateBinDto();
        // Set up the UpdateBinDto object to cause an IllegalArgumentException

        // Mock behavior of the binService.updateBin method to throw an IllegalArgumentException
        doThrow(IllegalArgumentException.class).when(binService).updateBin(updateBinDto);

        ResponseEntity<String> response = binController.updateBin(2L, updateBinDto);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());

        // Add assertions for the specific error message or handling as needed
    }

    @Test
    public void updateBin_BinServiceThrowsOtherException_ReturnsInternalServerErrorStatus() {
        //Arrange
        UpdateBinDto updateBinDto = new UpdateBinDto();
        //mock
        when(binService.updateBin(any(UpdateBinDto.class))).thenThrow(ArithmeticException.class);

        //Act
        ResponseEntity<String> result = binController.updateBin(1L, updateBinDto);

        //Assert
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, result.getStatusCode());
    }

    @Test
    public void getCurrentHumidityByBinId_BinServiceThrowsException_ReturnsBadRequestStatus() {
        //Mock
        when(binService.getCurrentHumidityByBinId(1L)).thenThrow(IllegalArgumentException.class);

        //Act
        ResponseEntity<Humidity> result = binController.getCurrentHumidityByBinId(1L);

        //Assert
        assertEquals(HttpStatus.BAD_REQUEST, result.getStatusCode());
    }

    @Test
    public void getCurrentHumidityByBinId_BinServiceReturnsBinWithoutHumidity_ReturnsNotFoundStatus() {
        //Mock
        when(binService.getCurrentHumidityByBinId(1L)).thenReturn(Optional.empty());

        //Act
        ResponseEntity<Humidity> result = binController.getCurrentHumidityByBinId(1L);

        //Assert
        assertEquals(HttpStatus.NOT_FOUND, result.getStatusCode());
    }

    @Test
    public void getCurrentHumidityByBinId_BinServiceReturnsBinWithHumidity_ReturnsHumidityAndOkStatus() {
        //Arrange
        Humidity humidity = new Humidity();
        humidity.setValue(15D);

        //Mock
        when(binService.getCurrentHumidityByBinId(1L)).thenReturn(Optional.of(humidity));

        //Act
        ResponseEntity<Humidity> result = binController.getCurrentHumidityByBinId(1L);

        //Assert
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(15D, result.getBody().getValue(), 0.5);
    }

    @Test
    public void getCurrentTemperatureByBinId_BinServiceThrowsException_ReturnsBadRequestStatus() {
        //Mock
        when(binService.getCurrentTemperatureByBinId(1L)).thenThrow(IllegalArgumentException.class);

        //Act
        ResponseEntity<Temperature> result = binController.getCurrentTemperatureByBinId(1L);

        //Assert
        assertEquals(HttpStatus.BAD_REQUEST, result.getStatusCode());
    }

    @Test
    public void getCurrentTemperatureByBinId_BinServiceReturnsBinWithoutTemperature_ReturnsNotFoundStatus() {
        //Mock
        when(binService.getCurrentTemperatureByBinId(1L)).thenReturn(Optional.empty());

        //Act
        ResponseEntity<Temperature> result = binController.getCurrentTemperatureByBinId(1L);

        //Assert
        assertEquals(HttpStatus.NOT_FOUND, result.getStatusCode());
    }

    @Test
    public void getCurrentTemperatureByBinId_BinServiceReturnsBinWithTemperature_ReturnsTemperatureAndOkStatus() {
        //Arrange
        Temperature temperature = new Temperature();
        temperature.setValue(15D);

        //Mock
        when(binService.getCurrentTemperatureByBinId(1L)).thenReturn(Optional.of(temperature));

        //Act
        ResponseEntity<Temperature> result = binController.getCurrentTemperatureByBinId(1L);

        //Assert
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(15D, result.getBody().getValue(), 0.5);
    }

    @Test
    public void getCurrentFillLevelByBinId_BinServiceThrowsException_ReturnsBadRequestStatus() {
        //Mock
        when(binService.getCurrentFillLevelByBinId(1L)).thenThrow(IllegalArgumentException.class);

        //Act
        ResponseEntity<Level> result = binController.getCurrentFillLevelByBinId(1L);

        //Assert
        assertEquals(HttpStatus.BAD_REQUEST, result.getStatusCode());
    }

    @Test
    public void getCurrentFillLevelByBinId_BinServiceReturnsBinWithoutFillLevel_ReturnsNotFoundStatus() {
        //Mock
        when(binService.getCurrentFillLevelByBinId(1L)).thenReturn(Optional.empty());

        //Act
        ResponseEntity<Level> result = binController.getCurrentFillLevelByBinId(1L);

        //Assert
        assertEquals(HttpStatus.NOT_FOUND, result.getStatusCode());
    }

    @Test
    public void getCurrentFillLevelByBinId_BinServiceReturnsBinWithFillLevel_ReturnsFillLevelAndOkStatus() {
        //Arrange
        Level level = new Level();
        level.setValue(15D);

        //Mock
        when(binService.getCurrentFillLevelByBinId(1L)).thenReturn(Optional.of(level));

        //Act
        ResponseEntity<Level> result = binController.getCurrentFillLevelByBinId(1L);

        //Assert
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(15D, result.getBody().getValue(), 0.5);
    }

    @Test
    public void createBin_BinServiceThrowsException_ReturnsBadRequest() {
        //Arrange
        CreateBinDTO bin = new CreateBinDTO(40D, 40d, 40d, 40d);

        //Mock
        when(binService.create(any(CreateBinDTO.class))).thenThrow(IllegalArgumentException.class);

        //Act
        ResponseEntity<Bin> result = binController.createBin(bin);

        //Assert
        assertEquals(HttpStatus.BAD_REQUEST, result.getStatusCode());
    }

    @Test
    public void createBin_BinServiceSuccess_ReturnsBinAndOkStatus() {
        //Arrange
        CreateBinDTO binDto = new CreateBinDTO(40D, 40d, 40d, 40d);
        Bin bin = new Bin();
        bin.setLatitude(40d);
        bin.setLongitude(40d);
        bin.setCapacity(40d);
        bin.setFillThreshold(40d);

        //Mock
        when(binService.create(binDto)).thenReturn(bin);

        //Act
        ResponseEntity<Bin> result = binController.createBin(binDto);

        //Assert
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(40d, result.getBody().getLatitude(), 0.5);
    }

    @Test
    public void getAllBins_BinServiceThrowsException_ReturnsInternalErrorStatus() {
        //Mock
        when(binService.findAllBins()).thenThrow(IllegalArgumentException.class);

        //Act
        ResponseEntity<List<BinDto>> result = binController.getAllBins();

        //Assert
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, result.getStatusCode());
    }

    @Test
    public void getAllBins_BinServiceReturnsBinList_ReturnsListBiggerThan0AndOkStatus() {
        //Arrange
        List<BinDto> bins = new ArrayList<>();
        BinDto bin = new BinDto();
        BinDto bin2 = new BinDto();
        bins.add(bin);
        bins.add(bin2);

        //Mock
        when(binService.findAllBins()).thenReturn(bins);

        //Act
        ResponseEntity<List<BinDto>> result = binController.getAllBins();

        //Assert
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(2, result.getBody().size());
    }

    @Test
    public void getBinById_BinServiceThrowsException_ReturnsInternalServerStatus() {
        //Mock
        when(binService.findBinById(1L)).thenThrow(IllegalArgumentException.class);

        //Act
        ResponseEntity<BinDto> result = binController.getBinById(1L);

        //Assert
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, result.getStatusCode());
    }

    @Test
    public void getBinById_BinServiceReturnsEmptyOptional_ReturnsNotFoundStatus() {
        //Mock
        when(binService.findBinById(1L)).thenReturn(Optional.empty());

        //Act
        ResponseEntity<BinDto> result = binController.getBinById(1L);

        //Assert
        assertEquals(HttpStatus.NOT_FOUND, result.getStatusCode());
    }

    @Test
    public void getBinById_BinServiceReturnsBin_ReturnsBinAndOkStatus() {
        //Arrange
        BinDto binDto = new BinDto();

        //Mock
        when(binService.findBinById(1L)).thenReturn(Optional.of(binDto));

        //Act
        ResponseEntity<BinDto> result = binController.getBinById(1L);

        //Assert
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertNotNull(result.getBody());
    }

    @Test
    public void deleteBinById_BinDoesNotExist_ReturnsNotFoundStatus() {
        //Mock
        when(binService.deleteBinById(1L)).thenThrow(NoSuchElementException.class);

        //Act
        ResponseEntity<HttpStatus> result = binController.deleteBinById(1L);

        //Assert
        assertEquals(HttpStatus.NOT_FOUND, result.getStatusCode());
    }

    @Test
    public void deleteBinById_BinServiceThrowsOtherException_ReturnsInternalServerErrorStatus() {
        //Mock
        when(binService.deleteBinById(1L)).thenThrow(IllegalArgumentException.class);

        //Act
        ResponseEntity<HttpStatus> result = binController.deleteBinById(1L);

        //Assert
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, result.getStatusCode());
    }

    @Test
    public void deleteBinById_SuccessfulRun_ReturnsOkStatus() {
        //Mock
        when(binService.deleteBinById(1L)).thenReturn(true);

        //Act
        ResponseEntity<HttpStatus> result = binController.deleteBinById(1L);

        //Assert
        assertEquals(HttpStatus.OK, result.getStatusCode());
    }

    @Test
    public void getBinsWithThresholdLessThanFillLevel_BinServiceThrowsException_ReturnsInternalServerErrorStatus() {
        //Mock
        when(binService.getBinsWithThresholdLessThanFillLevel()).thenThrow(IllegalArgumentException.class);

        //Act
        ResponseEntity<List<NotificationBinDto>> result = binController.getBinsWithThresholdLessThanFillLevel();

        //Assert
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, result.getStatusCode());
    }

    @Test
    public void getBinsWithThresholdLessThanFillLevel_SuccessfulRun_ReturnsBinsAndOkStatus() {
        //Arrange
        List<NotificationBinDto> bins = new ArrayList<>();
        bins.add(new NotificationBinDto(40d, 40L, 40d, LocalDateTime.now()));
        bins.add(new NotificationBinDto(40d, 50L, 40D, LocalDateTime.now()));

        //Mock
        when(binService.getBinsWithThresholdLessThanFillLevel()).thenReturn(bins);

        //Act
        ResponseEntity<List<NotificationBinDto>> result = binController.getBinsWithThresholdLessThanFillLevel();

        //Assert
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(2, result.getBody().size());
    }

    @Test
    public void getDeviceStatusByBinId_BinServiceThrowsNoSuchElementException_ReturnsNotFoundStatus() {
        //Mock
        when(binService.getDeviceStatusByBinId(any(Long.class))).thenThrow(NoSuchElementException.class);

        //Act
        ResponseEntity<String> result = binController.getDeviceStatusByBinId(1L);

        //Assert
        assertEquals(HttpStatus.NOT_FOUND, result.getStatusCode());
    }

    @Test
    public void getDeviceStatusByBinId_BinServiceThrowsOtherException_ReturnsNotFoundStatus() {
        //Mock
        when(binService.getDeviceStatusByBinId(any(Long.class))).thenThrow(IllegalArgumentException.class);

        //Act
        ResponseEntity<String> result = binController.getDeviceStatusByBinId(1L);

        //Assert
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, result.getStatusCode());
    }

    @Test
    public void getDeviceStatusByBinId_BinServiceReturnsFalse_ReturnsNotFoundStatusAndWarningMessage() {
        //Mock
        when(binService.getDeviceStatusByBinId(any(Long.class))).thenReturn(Boolean.FALSE);

        //Act
        ResponseEntity<String> result = binController.getDeviceStatusByBinId(1L);

        //Assert
        assertEquals(HttpStatus.NOT_FOUND, result.getStatusCode());
        assertTrue(result.getBody().contains("attention"));
    }

    @Test
    public void getDeviceStatusByBinId_SuccessfulRun_ReturnsOkStatusCodeAndPositiveMessage() {
        //Mock
        when(binService.getDeviceStatusByBinId(any(Long.class))).thenReturn(Boolean.TRUE);

        //Act
        ResponseEntity<String> result = binController.getDeviceStatusByBinId(1L);

        //Assert
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertTrue(result.getBody().contains("online"));
    }

    @Test
    public void testActivateBuzzer_Success() {
        // Mocking the dependencies
        BuzzerActivationDto validRequest = new BuzzerActivationDto(123L);

        // Mock the behavior of binService.sendBuzzerActivationToIoT() method
        when(binService.sendBuzzerActivationToIoT(123L)).thenReturn(true);

        // Call the method to be tested
        ResponseEntity<String> response = binController.activateBuzzer(validRequest);

        // Assertions
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Buzzer activation sent to IoT device for Bin ID: 123", response.getBody());

        // Verify that the method in the service was called with the correct parameter
        verify(binService, times(1)).sendBuzzerActivationToIoT(123L);
    }

    @Test
    public void testActivateBuzzer_InvalidRequest() {
        BuzzerActivationDto invalidRequest = new BuzzerActivationDto(); // Empty request

        ResponseEntity<String> response = binController.activateBuzzer(invalidRequest);

        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Invalid request. Bin ID not provided or is null.", response.getBody());

        // Ensure that binService.sendBuzzerActivationToIoT() was not called
        verify(binService, never()).sendBuzzerActivationToIoT(anyLong());
    }

}
