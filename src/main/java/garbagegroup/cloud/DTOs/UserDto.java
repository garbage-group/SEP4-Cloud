package garbagegroup.cloud.DTOs;


public class UserDto {

    private String username;
    private String password;
    private String fullname;
    private String role;
    private String region;

    public UserDto(String username, String password, String fullname, String role, String region) {
        this.username = username;
        this.password = password;
        this.fullname = fullname;
        this.role = role;
        this.region = region;
    }

    public UserDto() {

    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

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

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }
}
