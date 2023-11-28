package garbagegroup.cloud.repository;

import garbagegroup.cloud.model.Bin;
import garbagegroup.cloud.tcpserver.ServerSocketHandler;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IBinRepository extends JpaRepository<Bin, Long> {
    @Query("SELECT b.id FROM Bin b")
    List<Integer> findAllBinIds();
}
