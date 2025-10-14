package org.esupportail.sgc.repositories;

import org.esupportail.sgc.domain.LogMail;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;

public interface LogMailRepository extends PagingAndSortingRepository<LogMail, Long>, CrudRepository<LogMail, Long> {
    Page<LogMail> findAll(Example<LogMail> logSearchQuery, Pageable pageable);
}
