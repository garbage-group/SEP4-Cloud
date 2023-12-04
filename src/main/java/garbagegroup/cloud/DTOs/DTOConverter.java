package garbagegroup.cloud.DTOs;

import garbagegroup.cloud.model.Bin;
import garbagegroup.cloud.model.User;

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

    public UserDto convertToUserDto(User user){
        UserDto dto = new UserDto();
        dto.setUsername(user.getUsername());
        dto.setPassword(user.getPassword());
        dto.setFullname(user.getFullname());
        dto.setRole(user.getRole());
        dto.setRegion(user.getRegion());
        return dto;
    }
}
