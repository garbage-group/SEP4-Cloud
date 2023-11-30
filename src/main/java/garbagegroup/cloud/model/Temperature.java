package garbagegroup.cloud.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

import java.time.LocalDateTime;

@Entity
public class Temperature extends SensorData {
    @Id
    @ManyToOne
    @JoinColumn(name = "bin_id", nullable = false)
    @JsonManagedReference
    @JsonIgnore()
    private Bin bin;

    private double value;
    @Id
    private LocalDateTime dateTime;

    public Temperature() {
    }

    public Temperature(Bin bin, double value, LocalDateTime dateTime) {
        super(dateTime);
        this.bin = bin;
        this.value = value;
        this.dateTime = dateTime;
    }

    public Bin getBin() {
        return bin;
    }

    public double getValue() {
        return value;
    }

    public void setValue(double value) {
        this.value = value;
    }

    public LocalDateTime getDateTime() {
        return dateTime;
    }

    public void setDateTime(LocalDateTime dateTime) {
        this.dateTime = dateTime;
    }
}
