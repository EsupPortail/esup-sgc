package org.esupportail.sgc.repositories;

import org.esupportail.sgc.domain.Prefs;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;

public interface PrefsRepository extends PagingAndSortingRepository<Prefs, Long>, CrudRepository<Prefs, Long> {
}
