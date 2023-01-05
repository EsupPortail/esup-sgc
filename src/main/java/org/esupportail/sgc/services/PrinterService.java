package org.esupportail.sgc.services;

import org.apache.commons.lang3.StringUtils;
import org.esupportail.sgc.domain.Printer;
import org.esupportail.sgc.services.ldap.GroupService;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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

    public List<Printer> getPrinters(String eppn) {
        List<String> groups = groupService.getGroupsForEppn(eppn);
        Set<Printer> printers = new HashSet<>();
        Set<String> connectedEppnPrinters = encodeAndPringLongPollService.getManagersPrintEncodeEppns();
        if(connectedEppnPrinters.size()>0) {
            // Récupération des imprimantes enregistrées en base et actuellement connectées
            if (connectedEppnPrinters.contains(eppn)) {
                printers.addAll(Printer.findPrintersByEppn(eppn).getResultList());
            }
            printers.addAll(Printer.findPrintersByEppnInPrinterUsers(eppn, connectedEppnPrinters).getResultList());
            printers.addAll(Printer.findPrintersByEppnInPrinterGroups(groups, connectedEppnPrinters).getResultList());
        }
        List<Printer> printersList = new ArrayList<>(printers);
        printersList.sort((a, b) -> a.getLabel().compareTo(b.getLabel()));
        return printersList;
    }

    public boolean canHandleCard(String eppn) {
        if(StringUtils.isEmpty(eppn)) {
            return false;
        }
        return getPrinters(eppn).size()>0;
    }

}
