package garbagegroup.cloud.service;

import garbagegroup.cloud.model.Humidity;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public interface IBinService {

    Optional<Double> getHumidityById(Long binId);
}
