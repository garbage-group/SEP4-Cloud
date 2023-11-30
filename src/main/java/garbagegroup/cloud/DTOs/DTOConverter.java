package garbagegroup.cloud.DTOs;

import garbagegroup.cloud.model.Bin;

public class DTOConverter {

    public DTOConverter() {
    }
    public BinDto convertToBinDto(Bin bin){
        BinDto dto = new BinDto();
        dto.setId(bin.getId());
        dto.setLongitude(bin.getLongitude());
        dto.setLatitude(bin.getLatitude());
        dto.setCapacity(bin.getCapacity());
        dto.setEmptiedLast(bin.getEmptiedLast());
        dto.setPickUpTime(bin.getPickUpTime());
        dto.setFillThreshold(bin.getFillThreshold());
        dto.setDeviceId(bin.getDeviceId());
        dto.setHumidity(bin.getHumidity());
        dto.setFillLevels(bin.getFillLevels());
        dto.setTemperatures(bin.getTemperatures());
        return dto;
    }
}
