package org.esupportail.sgc.repositories;

import org.esupportail.sgc.domain.PayboxTransactionLog;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;

public interface PayboxTransactionLogRepository extends PagingAndSortingRepository<PayboxTransactionLog, Long>, CrudRepository<PayboxTransactionLog, Long> {
    Page<PayboxTransactionLog> findAll(Example<PayboxTransactionLog> logSearchQuery, Pageable pageable);
}
