package garbagegroup.cloud.model;

import jakarta.persistence.*;

@Entity
public class Humidity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(columnDefinition = "bigint")
    private Long id;
    private Long value; // [%]

    // Need to add the other attributes regarding humidity

    /**
     * Empty constructor for springboot, it uses it when creating the repository
     * Please never delete/modify this, just create a new constructor if you need one
     */
    public Humidity () {}

    public Humidity (Long value) {
        this.value = value;
    }


    public void setId(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }

    public Long getValue() {
        return value;
    }
}
