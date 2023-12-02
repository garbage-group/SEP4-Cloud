package garbagegroup.cloud.DTOs;

import garbagegroup.cloud.model.Level;

import java.time.LocalDateTime;
import java.util.List;

public class NotificationBinDto {

    private Double fillThreshold;
    private Long binId;
    private Double levelValue;
    private LocalDateTime timestamp;

    public NotificationBinDto(Double fillThreshold, Long binId, Double levelValue, LocalDateTime timestamp) {
        this.fillThreshold = fillThreshold;
        this.binId = binId;
        this.levelValue = levelValue;
        this.timestamp = timestamp;
    }

    public Double getFillThreshold() {
        return fillThreshold;
    }

    public void setFillThreshold(Double fillThreshold) {
        this.fillThreshold = fillThreshold;
    }

    public Long getBinId() {
        return binId;
    }

    public void setBinId(Long binId) {
        this.binId = binId;
    }

    public Double getLevelValue() {
        return levelValue;
    }

    public void setLevelValue(Double levelValue) {
        this.levelValue = levelValue;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
}
