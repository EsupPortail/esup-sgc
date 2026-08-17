package org.esupportail.sgc.services;

import org.esupportail.sgc.dao.PrinterDaoService;
import org.esupportail.sgc.domain.Printer;
import org.esupportail.sgc.services.ldap.GroupService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.Resource;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

@Service
public class PrinterService {

    Logger log = LoggerFactory.getLogger(getClass());

    @Resource
    GroupService groupService;

    @Resource
    EncodeAndPringLongPollService encodeAndPringLongPollService;

    @Resource
    PrinterDaoService printerDaoService;

    @Transactional
    @Async
    public void setMaintenanceInfo(String eppn, String maintenanceInfo, String ip) {
        Printer printer = null;
        List<Printer> printers = printerDaoService.findPrintersByEppn(eppn).getResultList();
        if(printers.size()!=0) {
            printer = printers.get(0);
        } else {
            printer = new Printer();
            printer.setEppn(eppn);
            printerDaoService.persist(printer);
        }
        printer.setIp(ip);
        printer.setMaintenanceInfo(maintenanceInfo);
        printer.setConnectionDate(LocalDateTime.now());
        log.trace("MaintenanceInfo for {} persisted : {}", eppn, maintenanceInfo);
    }

    /*
        Map de printers autorisés pour l'utilisateur : true si en ligne (disponible/connectée)
     */
    public SortedMap<Printer, Boolean> getPrinters(String eppn, List<String> groups) {
        Set<Printer> printers = new HashSet<>();
        Set<String> connectedEppnPrinters = encodeAndPringLongPollService.getManagersPrintEncodeEppns();
        printers.addAll(printerDaoService. findPrintersByEppnOrByEppnInPrinterUsersOryEppnInPrinterGroups(eppn, groups).getResultList());
        SortedMap<Printer, Boolean> printersMap = new TreeMap<Printer, Boolean>((a, b) -> a.getLabel().compareTo(b.getLabel()));
        for(Printer printer: printers) {
            printersMap.put(printer, connectedEppnPrinters.contains(printer.getEppn()));
        }
        return printersMap;
    }

    public boolean isPrinterConnected(String eppn) {
        Set<String> connectedEppnPrinters = encodeAndPringLongPollService.getManagersPrintEncodeEppns();
        return connectedEppnPrinters.contains(eppn);
    }

}
