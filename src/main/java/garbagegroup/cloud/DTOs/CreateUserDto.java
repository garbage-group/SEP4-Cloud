package garbagegroup.cloud.DTOs;

public class CreateUserDto {
    public String username;
    public String fullName;
    public String password;
    public String role;
    public String region;

    public CreateUserDto(String username, String fullName, String password, String role, String region) {
        this.username = username;
        this.fullName = fullName;
        this.password = password;
        this.role = role;
        this.region = region;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }
}
