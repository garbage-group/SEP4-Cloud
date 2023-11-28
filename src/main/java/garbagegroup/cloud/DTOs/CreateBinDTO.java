package garbagegroup.cloud.DTOs;

public class CreateBinDTO {
    private String location;
    private double capacity;
    private double fillThreshold;

    public CreateBinDTO(String location, double capacity, double fillThreshold) {
        this.location = location;
        this.capacity = capacity;
        this.fillThreshold = fillThreshold;
    }

    public String getLocation() {
        return location;
    }

    public double getCapacity() {
        return capacity;
    }

    public double getFillThreshold() {
        return fillThreshold;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public void setCapacity(double capacity) {
        this.capacity = capacity;
    }

    public void setFillThreshold(double fillThreshold) {
        this.fillThreshold = fillThreshold;
    }
}
