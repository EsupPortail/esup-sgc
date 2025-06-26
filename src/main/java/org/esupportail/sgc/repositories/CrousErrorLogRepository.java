package org.esupportail.sgc.repositories;

import org.esupportail.sgc.services.crous.CrousErrorLog;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;

public interface CrousErrorLogRepository extends PagingAndSortingRepository<CrousErrorLog, Long>, CrudRepository<CrousErrorLog, Long> {
    Page<CrousErrorLog> findAll(Example<CrousErrorLog> searchCrousErrorLogQuery, Pageable pageable);
}
