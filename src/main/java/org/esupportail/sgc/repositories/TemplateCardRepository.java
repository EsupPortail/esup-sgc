package org.esupportail.sgc.repositories;

import org.esupportail.sgc.domain.TemplateCard;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;

public interface TemplateCardRepository extends PagingAndSortingRepository<TemplateCard, Long>, CrudRepository<TemplateCard, Long> {
}
