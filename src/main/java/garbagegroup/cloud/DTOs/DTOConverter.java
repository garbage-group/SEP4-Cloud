package garbagegroup.cloud.DTOs;

import garbagegroup.cloud.model.*;

public class DTOConverter {

    private DTOConverter() {}

    public static BinDto convertToBinDto(Bin bin){
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

    public static User createUserDtoToUser(CreateUserDto createUserDto) {
        return new User(
                createUserDto.getUsername(),
                createUserDto.getPassword(),
                createUserDto.getFullName(),
                createUserDto.getRole(),
                createUserDto.getRegion());
    }

    public static UserDto convertToUserDto(User user){
        UserDto dto = new UserDto();
        dto.setUsername(user.getUsername());
        dto.setPassword(user.getPassword());
        dto.setFullname(user.getFullname());
        dto.setRole(user.getRole());
        dto.setRegion(user.getRegion());
        return dto;
    }
}
