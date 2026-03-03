package org.esupportail.sgc.repositories;

import org.esupportail.sgc.domain.CrousSmartCard;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;

public interface CrousSmartCardRepository extends PagingAndSortingRepository<CrousSmartCard, Long>, CrudRepository<CrousSmartCard, Long> {
    Page<CrousSmartCard> findAll(Example<CrousSmartCard> crousSmartCardSearchQuery, Pageable pageable);
}
