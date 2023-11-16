package garbagegroup.cloud.service;

import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public interface IBinService {

    Optional<Double> getCurrentHumidityByBinId(Long binId);
}
