package garbagegroup.cloud.repository;

import garbagegroup.cloud.model.Humidity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BinRepository extends JpaRepository<Humidity, Long> {
    /* In terms of the types that are currently included in the JpaRepository (Humidity - model class, Long - type of the primary key),
        we have to do some research on how to include more classes, not only humidity, as we will have many more models. */
}
