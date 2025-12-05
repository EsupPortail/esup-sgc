package org.esupportail.sgc.repositories;

import org.esupportail.sgc.services.crous.CrousErrorLog;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.List;

public interface CrousErrorLogRepository extends PagingAndSortingRepository<CrousErrorLog, Long>, CrudRepository<CrousErrorLog, Long> {
    Page<CrousErrorLog> findAll(Example<CrousErrorLog> searchCrousErrorLogQuery, Pageable pageable);

    @Query("SELECT DISTINCT c.code FROM CrousErrorLog c WHERE c.code IS NOT NULL order by c.code")
    List<String> findDistinctCodes();

    @Query("SELECT DISTINCT c.message FROM CrousErrorLog c WHERE c.message IS NOT NULL order by c.message")
    List<String> findDistinctMessages();
}
