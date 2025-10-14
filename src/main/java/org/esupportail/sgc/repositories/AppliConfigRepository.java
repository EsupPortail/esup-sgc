package org.esupportail.sgc.repositories;

import org.esupportail.sgc.domain.AppliConfig;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;

public interface AppliConfigRepository extends PagingAndSortingRepository<AppliConfig, Long>, CrudRepository<AppliConfig, Long> {
    Page<AppliConfig> findByType(AppliConfig.TypeConfig typeConfig, Pageable pageable);
}
