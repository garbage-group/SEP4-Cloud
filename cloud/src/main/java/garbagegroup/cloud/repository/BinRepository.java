package garbagegroup.cloud.repository;

import garbagegroup.cloud.model.Bin;
import garbagegroup.cloud.model.Humidity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BinRepository extends JpaRepository<Bin, Long> {
    //Optional<Humidity> findTopByBinIdOrderByDateTimeDesc(Long binId);
}
