package garbagegroup.cloud.service.serviceImplementation;

import garbagegroup.cloud.model.Bin;
import garbagegroup.cloud.repository.IBinRepository;
import garbagegroup.cloud.service.serviceInterface.IBinService;
import garbagegroup.cloud.service.serviceInterface.INotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

@Service
public class NotificationService implements INotificationService {

    private final Timer timer;
    private IBinService binService;
    private IBinRepository binRepository;

    @Autowired
    public NotificationService(IBinRepository binRepository, IBinService binService) {
        this.binService = binService;
        this.binRepository = binRepository;

        // Schedule a periodic task of fetching fill level every hour
        timer = new Timer();
    }

    // NOTE: This is not implemented anywhere, feel free to delete it
    public void startPeriodicTask() {
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                fetchFillLevelFromDevices();
            }
        };

        // Schedule the task to run every 1 hour (3600000 milliseconds) with an initial delay of 0
        timer.scheduleAtFixedRate(task, 0, 3600000);
    }


    // TODO: Need to figure out how to run this method every hour
    private void fetchFillLevelFromDevices() {
        List<Bin> bins = binRepository.findAll();
        for (Bin bin : bins) {
            binService.getIoTData(bin.getId().intValue(), bin.getDeviceId(), "getFillLevel"); // This method will handle the data fetching and saving to the DB
            // TODO: Handle the bin data further
        }
    }
}
