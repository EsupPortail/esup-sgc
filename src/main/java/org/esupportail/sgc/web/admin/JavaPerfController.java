package org.esupportail.sgc.web.admin;

import org.apache.commons.dbcp.BasicDataSource;
import org.apache.commons.lang3.StringUtils;
import org.esupportail.sgc.domain.SgcHttpSession;
import org.esupportail.sgc.domain.User;
import org.esupportail.sgc.security.SgcHttpSessionsListenerService;
import org.esupportail.sgc.services.AppliConfigService;
import org.esupportail.sgc.services.userinfos.UserInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.ldap.core.support.LdapContextSource;
import org.springframework.ldap.pool.factory.PoolingContextSource;
import org.springframework.security.core.session.SessionInformation;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.annotation.Resource;
import javax.naming.ldap.LdapContext;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

@RequestMapping("/admin/javaperf")
@Controller
public class JavaPerfController {

	@Resource
	AppliConfigService appliConfigService;

	@Resource
	List<BasicDataSource> basicDataSources;

	@Autowired(required = false)
	List<PoolingContextSource> poolingContextSources = new ArrayList<>();
	
	@ModelAttribute("active")
	public String getActiveMenu() {
		return "javaperf";
	}
	
	@ModelAttribute("help")
	public String getHelp() {
		return appliConfigService.getHelpAdmin();
	}
	
	@ModelAttribute("footer")
	public String getFooter() {
		return appliConfigService.pageFooter();
	}  

	
	@RequestMapping
	public String getJavaPerf(Model uiModel) throws IOException {

		Runtime runtime = Runtime.getRuntime();
		long maxMemoryInMB = runtime.maxMemory() / 1024 / 1024;
		long totalMemoryInMB = runtime.totalMemory() / 1024 / 1024;
		long freeMemoryInMB = runtime.freeMemory() / 1024 / 1024;
		long usedMemoryInMB = totalMemoryInMB - freeMemoryInMB;
		uiModel.addAttribute("maxMemoryInMB", maxMemoryInMB);
		uiModel.addAttribute("totalMemoryInMB", totalMemoryInMB);
		uiModel.addAttribute("freeMemoryInMB", freeMemoryInMB);
		uiModel.addAttribute("usedMemoryInMB", usedMemoryInMB);

		uiModel.addAttribute("basicDataSources", basicDataSources);

		Map<PoolingContextSource, String> ldapContextSources = new HashMap<>();
		for(PoolingContextSource p : poolingContextSources) {
			LdapContextSource ldapContextSource = (LdapContextSource) p.getContextSource();
			String urls = StringUtils.join(ldapContextSource.getUrls(), ", ");
			ldapContextSources.put(p, urls);
		}
		uiModel.addAttribute("ldapContextSources", ldapContextSources);

		ThreadMXBean threadMXBean = ManagementFactory.getThreadMXBean();
		List<ThreadInfo> threadInfos = Arrays.asList(threadMXBean.dumpAllThreads(true, true));
		Collections.sort(threadInfos, (ThreadInfo o1, ThreadInfo o2) -> o1.getThreadState().compareTo(o2.getThreadState()));
		Map<String, Long> threadStateCount = new HashMap<>();
		for (ThreadInfo threadInfo : threadInfos) {
			String threadState = threadInfo.getThreadState().toString();
			if (threadStateCount.containsKey(threadState)) {
				threadStateCount.put(threadState, threadStateCount.get(threadState) + 1);
			} else {
				threadStateCount.put(threadState, 1L);
			}
		}
		uiModel.addAttribute("threadMXBean", threadMXBean);
		uiModel.addAttribute("threadInfos", threadInfos);
		uiModel.addAttribute("threadStateCount", threadStateCount);
		long currentThreadId = Thread.currentThread().getId();
		uiModel.addAttribute("currentThreadId", currentThreadId);



		return "admin/javaperf";
	}

}
