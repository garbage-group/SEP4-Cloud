package garbagegroup.cloud.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Entity
public class Bin {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "longitude")
    private Double longitude;

    @Column(name = "latitude")
    private Double latitude;

    @Column(name = "capacity")
    private Double capacity;

    @Column(name = "emptied_last")
    private LocalDateTime emptiedLast;

    @Column(name = "pick_up_time")
    private LocalDateTime pickUpTime;

    @Column(name = "fill_threshold")
    private Double fillThreshold;

    @Column(name = "device_id")
    private int deviceId;

    @OneToMany(mappedBy = "bin", cascade = CascadeType.ALL, fetch= FetchType.EAGER)
    @JsonManagedReference
    @JsonIgnoreProperties("bin")
    private List<Humidity> humidity;

    @OneToMany(mappedBy = "bin", cascade = CascadeType.ALL, fetch= FetchType.EAGER)
    @JsonManagedReference
    @JsonIgnoreProperties("bin")
    private List<Level> fillLevels;

    @OneToMany(mappedBy = "bin", cascade = CascadeType.ALL, fetch= FetchType.EAGER)
    @JsonManagedReference
    @JsonIgnoreProperties("bin")
    private List<Temperature> temperatures;

    public Bin() {}

    public Bin(Double longitude, Double latitude, Double capacity, Double fillThreshold, LocalDateTime emptiedLast, LocalDateTime pickUpTime) {
        this.longitude = longitude;
        this.latitude = latitude;
        this.capacity = capacity;
        this.fillThreshold = fillThreshold;
        this.emptiedLast = emptiedLast;
        this.pickUpTime = pickUpTime;
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

    public List<Humidity> getHumidity() {
        return humidity;
    }

    public void setHumidity(List<Humidity> humidity) {
        this.humidity = humidity;
    }

    public List<Level> getFillLevels() {
        return fillLevels;
    }

    public void setFillLevels(List<Level> fillLevels) {
        this.fillLevels = fillLevels;
    }

    public List<Temperature> getTemperatures() {
        return temperatures;
    }

    public void setTemperatures(List<Temperature> temperatures) {
        this.temperatures = temperatures;
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