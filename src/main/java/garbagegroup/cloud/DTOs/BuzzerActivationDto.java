package garbagegroup.cloud.DTOs;

public class BuzzerActivationDto {
    private Long binId;

    public BuzzerActivationDto() {
    }

    public BuzzerActivationDto(Long binId) {
        this.binId = binId;
    }

    public Long getBinId() {
        return binId;
    }

    public void setBinId(Long binId) {
        this.binId = binId;
    }

}
