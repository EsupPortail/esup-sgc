package org.esupportail.sgc.repositories;

import org.esupportail.sgc.domain.CrousPatchIdentifier;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;

public interface CrousPatchIdentifierRepository extends PagingAndSortingRepository<CrousPatchIdentifier, Long>, CrudRepository<CrousPatchIdentifier, Long> {
}
