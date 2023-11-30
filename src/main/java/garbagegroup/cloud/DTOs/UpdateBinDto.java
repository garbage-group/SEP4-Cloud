package garbagegroup.cloud.DTOs;

public class UpdateBinDto {
    private Long id;
    private Double longitude;
    private Double latitude;
    private Double fillthreshold;


    public UpdateBinDto() {
    }

    public UpdateBinDto(Long id, Double longitude, Double latitude, Double fillthreshold) {
        this.id = id;
        this.longitude = longitude;
        this.latitude = latitude;
        this.fillthreshold = fillthreshold;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public Double getFillthreshold() {
        return fillthreshold;
    }

    public void setFillthreshold(Double fillthreshold) {
        this.fillthreshold = fillthreshold;
    }
}
