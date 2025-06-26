package org.esupportail.sgc.repositories;

import org.esupportail.sgc.domain.CardActionMessage;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;

public interface CardActionMessageRepository extends PagingAndSortingRepository<CardActionMessage, Long>, CrudRepository<CardActionMessage, Long> {

}
