package garbagegroup.cloud.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import garbagegroup.cloud.model.Humidity;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.List;

@Entity
public class BinDto {


    private Long id;

    private Double longitude;


    private Double latitude;


    private Double capacity;


    private LocalDateTime emptiedLast;


    private LocalDateTime pickUpTime;


    private Double fillThreshold;


    private int deviceId;

    private List<Humidity> humidity;

    public BinDto(){

    }
    public BinDto(Long id, Double longitude, Double latitude, Double capacity, LocalDateTime emptiedLast, LocalDateTime pickUpTime, Double fillThreshold, int deviceId, List<Humidity> humidity) {
        this.id = id;
        this.longitude = longitude;
        this.latitude = latitude;
        this.capacity = capacity;
        this.emptiedLast = emptiedLast;
        this.pickUpTime = pickUpTime;
        this.fillThreshold = fillThreshold;
        this.deviceId = deviceId;
        this.humidity = humidity;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
    public int getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(int deviceId) {
        this.deviceId = deviceId;
    }

    @OneToMany
    public List<Humidity> getHumidity() {
        return humidity;
    }

    public void setHumidity(List<Humidity> humidity) {
        this.humidity = humidity;
    }

    public Double getCapacity() {
        return capacity;
    }

    public Double getFillThreshold() {
        return fillThreshold;
    }

    public LocalDateTime getEmptiedLast() {
        return emptiedLast;
    }

    public LocalDateTime getPickUpTime() {
        return pickUpTime;
    }

    public void setCapacity(Double capacity) {
        this.capacity = capacity;
    }

    public void setEmptiedLast(LocalDateTime emptiedLast) {
        this.emptiedLast = emptiedLast;
    }

    public void setFillThreshold(Double fillThreshold) {
        this.fillThreshold = fillThreshold;
    }

    public void setPickUpTime(LocalDateTime pickUpTime) {
        this.pickUpTime = pickUpTime;
    }
}