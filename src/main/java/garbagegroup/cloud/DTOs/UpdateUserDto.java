package garbagegroup.cloud.DTOs;

public class UpdateUserDto {
    private String username;
    private String password;
    private String fullname;
    private String region;

    public UpdateUserDto(String username, String password, String fullname, String region) {
        this.username = username;
        this.password = password;
        this.fullname = fullname;
        this.region = region;
    }

    public UpdateUserDto() {}

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getFullname() {
        return fullname;
    }

    public void setFullname(String fullname) {
        this.fullname = fullname;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }
}