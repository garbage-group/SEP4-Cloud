package garbagegroup.cloud.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
public class Humidity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(columnDefinition = "bigint")
    private Long BinId;
    private double value;
    private LocalDateTime dateTime;

    public Humidity() {
    }

    public Long getBinId() {
        return BinId;
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
