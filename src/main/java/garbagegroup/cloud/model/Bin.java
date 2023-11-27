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

    @Column(name = "location")
    private String location;

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

    public Bin() {}

    public Bin(String location, Double capacity, Double fillThreshold, LocalDateTime emptiedLast, LocalDateTime pickUpTime) {
        this.location = location;
        this.capacity = capacity;
        this.fillThreshold = fillThreshold;
        this.emptiedLast = emptiedLast;
        this.pickUpTime = pickUpTime;
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

    public String getLocation() {
        return location;
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

    public void setLocation(String location) {
        this.location = location;
    }

    public void setPickUpTime(LocalDateTime pickUpTime) {
        this.pickUpTime = pickUpTime;
    }
}