package org.esupportail.sgc.repositories;

import org.esupportail.sgc.domain.EsupNfcSgcJwsDevice;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;

public interface EsupNfcSgcJwsDeviceRepository extends PagingAndSortingRepository<EsupNfcSgcJwsDevice, Long>, CrudRepository<EsupNfcSgcJwsDevice, Long> {

}
