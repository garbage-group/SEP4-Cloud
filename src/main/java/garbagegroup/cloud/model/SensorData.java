package garbagegroup.cloud.model;

import java.time.LocalDateTime;

public class SensorData {
    private LocalDateTime dateTime;

    public SensorData() {}
    public SensorData(LocalDateTime dateTime) {
        this.dateTime = dateTime;
    }

    public LocalDateTime getDateTime() {
        return dateTime;
    }

    public void setDateTime(LocalDateTime dateTime) {
        this.dateTime = dateTime;
    }
}
