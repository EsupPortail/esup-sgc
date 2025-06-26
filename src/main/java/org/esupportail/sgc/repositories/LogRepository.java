package org.esupportail.sgc.repositories;

import org.esupportail.sgc.domain.Log;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;

public interface LogRepository extends PagingAndSortingRepository<Log, Long>, CrudRepository<Log, Long> {
    Page<Log> findAll(Example<Log> logSearchQuery, Pageable pageable);
}
