package garbagegroup.cloud.services;

import garbagegroup.cloud.dto.UpdateBinDto;
import garbagegroup.cloud.model.Bin;
import garbagegroup.cloud.repository.IBinRepository;
import garbagegroup.cloud.service.serviceImplementation.BinService;
import garbagegroup.cloud.tcpserver.ITCPServer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.mockito.Mockito.mock;

public class BinServiceTest {

    private BinService binService;
    private IBinRepository binRepository;
    private ITCPServer tcpServer;

    @BeforeEach
    void setUp() {
        binRepository = mock(IBinRepository.class);
        tcpServer = mock(ITCPServer.class);
        binService = new BinService(binRepository, tcpServer);
    }

    @Test
    void testUpdateBin_Success() {
        UpdateBinDto updateBinDto = new UpdateBinDto();
        updateBinDto.setId(1L);
        updateBinDto.setLongitude(50.123);
        updateBinDto.setLatitude(30.456);
        updateBinDto.setFillthreshold(75.0);

        Bin bin = new Bin();
        bin.setId(updateBinDto.getId());
        // Set other properties for bin if required...

        when(binRepository.findById(1L)).thenReturn(Optional.of(bin));

        assertDoesNotThrow(() -> binService.updateBin(updateBinDto));
        // Add further assertions based on the expected behavior after updating the bin
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


//    @Test
//    public void getHumidityById1Returns25() {
//
//    }
//}
