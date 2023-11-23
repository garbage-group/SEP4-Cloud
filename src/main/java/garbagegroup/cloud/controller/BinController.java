package garbagegroup.cloud.controller;

import garbagegroup.cloud.model.Humidity;
import garbagegroup.cloud.service.serviceImplementation.BinService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Optional;

@RestController
@RequestMapping("/bins")
@CrossOrigin // Needed to send requests from/to URLs on different ports (so for the frontend, because IoT doesn't care since it is TCP)
public class BinController {
    private BinService binService;
    private Logger logger = LoggerFactory.getLogger(BinController.class);

    @Autowired
    public BinController(BinService binService) {
        this.binService = binService;
    }


    //The method tries to retrieve humidity information from a service based on a provided ID,
    // handles cases where the humidity is present or not,
    // and logs and returns an error response in case of an exception.
    @GetMapping("/{id}/humidity")
    public ResponseEntity<Humidity> getCurrentHumidityByBinId(@PathVariable Long id) {
        try {
            Optional<Humidity> humidity = binService.getCurrentHumidityByBinId(id);

            return humidity.map(value -> new ResponseEntity<>(value, HttpStatus.OK))
                    .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));

        } catch (Exception e) {
            logger.error("Error retrieving humidity for bin with id {}", id, e);
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }
}

