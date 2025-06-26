package org.esupportail.sgc.repositories;

import org.esupportail.sgc.domain.CrousSmartCard;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;

public interface CrousSmartCardRepository extends PagingAndSortingRepository<CrousSmartCard, Long>, CrudRepository<CrousSmartCard, Long> {
}
