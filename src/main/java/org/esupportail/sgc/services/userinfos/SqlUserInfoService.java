package org.esupportail.sgc.services.userinfos;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jakarta.servlet.http.HttpServletRequest;
import javax.sql.DataSource;

import org.apache.commons.io.IOUtils;
import org.esupportail.sgc.domain.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;


public class SqlUserInfoService implements ExtUserInfoService {

	private static final Logger log = LoggerFactory.getLogger(SqlUserInfoService.class);

	private String sqlQuery;
	
	private JdbcTemplate jdbcTemplate;
	
	private Long order = Long.valueOf(0);
	
	private String eppnFilter = ".*";

	String beanName;

	@Override
	public String getBeanName() {
		return beanName;
	}
	@Override
	public void setBeanName(String beanName) {
		this.beanName = beanName;
	}

	public void setDataSource(DataSource datesource) {
		this.jdbcTemplate = new JdbcTemplate(datesource);
	}

	public Long getOrder() {
		return order;
	}

	public void setOrder(Long order) {
		this.order = order;
	}

	public String getEppnFilter() {
		return eppnFilter;
	}

	public void setEppnFilter(String eppnFilter) {
		this.eppnFilter = eppnFilter;
	}

	public void setSqlQuery(String sqlQuery) {
		this.sqlQuery = sqlQuery;
	}

	@Override
	public Map<String, String> getUserInfos(User user, HttpServletRequest request, final Map<String, String> userInfosInComputing) {

		String sqlQueryWithParams = sqlQuery.replace("?", "'" + user.getEppn() + "'");
		if(userInfosInComputing != null) {
			for (String key : userInfosInComputing.keySet()) {
				if (sqlQueryWithParams.contains("{" + key + "}")) {
					sqlQueryWithParams = sqlQueryWithParams.replace("{" + key + "}", "'" + userInfosInComputing.get(key) + "'");
				}
			}
		}
		log.trace("SQL query for user {}: {}", user.getEppn(), sqlQueryWithParams);

		Map<String, String> userInfos = (Map<String, String>) jdbcTemplate.query(sqlQueryWithParams, new ResultSetExtractor() {
	        @Override
	        public Map<String, String> extractData(ResultSet rs) throws SQLException, DataAccessException {
	        	Map<String, String> userInfos = new HashMap<String, String>();
	        	List<String> columnNames = new ArrayList<String>();
	            ResultSetMetaData rsmd = rs.getMetaData();
	            int columnCount = rsmd.getColumnCount();
	            for(int i = 1 ; i <= columnCount ; i++){
	                columnNames.add(rsmd.getColumnLabel(i));
	            }
	            while (rs.next()) {
	            	for(int i = 1 ; i <= columnCount ; i++){
	            		Object value = rs.getObject(i);
	            		String columnName = columnNames.get(i-1);
	            		if(value instanceof byte[]) {
	            			value = new String((byte[])value);
	            		}
	            		if(value!=null) {
	            			userInfos.put(columnName, value.toString());
	            		} else {
	            			userInfos.put(columnName, "");
	            		}
	            	}
	            }
	            return userInfos;
	        }
	    });
		
		return userInfos;
	}

}
