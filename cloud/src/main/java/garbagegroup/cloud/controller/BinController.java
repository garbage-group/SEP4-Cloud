package garbagegroup.cloud.controller;

import garbagegroup.cloud.service.BinService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RestController;

@RestController
@CrossOrigin // Needed to send requests from/to URLs on different ports (so for the frontend, because IoT doesn't care since it is TCP)
public class BinController {
    private BinService binService;
    private Logger logger = LoggerFactory.getLogger(BinController.class);

    public BinController(BinService binService) {
        this.binService = binService;
    }

    // Here go the endpoints to be used with the frontend

}
