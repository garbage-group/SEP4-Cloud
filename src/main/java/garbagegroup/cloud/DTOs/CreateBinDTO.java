package garbagegroup.cloud.DTOs;

public class CreateBinDTO {
    private double longitude;
    private double latitude;
    private double capacity;
    private double fillThreshold;

    public CreateBinDTO(double longitude, double latitude, double capacity, double fillThreshold) {
        this.longitude = longitude;
        this.latitude = latitude;
        this.capacity = capacity;
        this.fillThreshold = fillThreshold;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public double getCapacity() {
        return capacity;
    }

    public double getFillThreshold() {
        return fillThreshold;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public void setCapacity(double capacity) {
        this.capacity = capacity;
    }

    public void setFillThreshold(double fillThreshold) {
        this.fillThreshold = fillThreshold;
    }
}
