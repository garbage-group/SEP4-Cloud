package garbagegroup.cloud.apicontrollers;

import garbagegroup.cloud.controller.BinController;
import garbagegroup.cloud.DTOs.UpdateBinDto;
import garbagegroup.cloud.service.serviceInterface.IBinService;
import org.junit.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;

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
        doNothing().when(binService).updateBin(updateBinDto);

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


}
