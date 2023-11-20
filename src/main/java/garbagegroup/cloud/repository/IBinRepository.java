package garbagegroup.cloud.repository;

import garbagegroup.cloud.model.Bin;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IBinRepository extends JpaRepository<Bin, Long> {

}
