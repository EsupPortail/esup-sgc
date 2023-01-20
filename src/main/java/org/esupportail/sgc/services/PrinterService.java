package org.esupportail.sgc.services;

import org.esupportail.sgc.domain.Printer;
import org.esupportail.sgc.services.ldap.GroupService;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

@Service
public class PrinterService {

    @Resource
    GroupService groupService;

    @Resource
    EncodeAndPringLongPollService encodeAndPringLongPollService;

    @Transactional
    @Async
    public void setMaintenanInfo(String eppn, String maiuntenanceInfo, String ip) {
        Printer printer = null;
        List<Printer> printers = Printer.findPrintersByEppn(eppn).getResultList();
        if(printers.size()!=0) {
            printer = printers.get(0);
        } else {
            printer = new Printer();
            printer.setEppn(eppn);
            printer.persist();
        }
        printer.setIp(ip);
        printer.setMaintenanceInfo(maiuntenanceInfo);
        printer.setConnectionDate(new Date());
    }

    /*
        Map de printers autorisés pour l'utilisateur : true si en ligne (disponible/connectée)
     */
    public SortedMap<Printer, Boolean> getPrinters(String eppn) {
        List<String> groups = groupService.getGroupsForEppn(eppn);
        Set<Printer> printers = new HashSet<>();
        Set<String> connectedEppnPrinters = encodeAndPringLongPollService.getManagersPrintEncodeEppns();
        printers.addAll(Printer.findPrintersByEppn(eppn).getResultList());
        printers.addAll(Printer.findPrintersByEppnInPrinterUsers(eppn).getResultList());
        printers.addAll(Printer.findPrintersByEppnInPrinterGroups(groups).getResultList());
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
