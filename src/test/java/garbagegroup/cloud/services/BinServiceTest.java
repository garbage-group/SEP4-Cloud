package garbagegroup.cloud.services;

import garbagegroup.cloud.DTOs.BinDto;
import garbagegroup.cloud.DTOs.DTOConverter;
import garbagegroup.cloud.model.Bin;
import garbagegroup.cloud.repository.IBinRepository;
import garbagegroup.cloud.service.serviceImplementation.BinService;
import static org.junit.jupiter.api.Assertions.*;
import garbagegroup.cloud.tcpserver.ITCPServer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

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


    @Test
    public void getHumidityById1Returns25() {

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
