package garbagegroup.cloud.service;

import garbagegroup.cloud.model.Bin;
import garbagegroup.cloud.repository.BinRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class BinService implements IBinService{
    private BinRepository binRepository;

    @Autowired
    public BinService(BinRepository binRepository) {
        this.binRepository = binRepository;
    }


    //look up a 'Bin' by its id, retreive the associate 'Measurement' and return the humidity value
    @Override
    public Optional<Double> getHumidityById(Long binId) {
        Optional<Bin> bin = binRepository.findById(binId);

        return bin.map(value -> value.getMeasurement().getHumidity());
    }
}
