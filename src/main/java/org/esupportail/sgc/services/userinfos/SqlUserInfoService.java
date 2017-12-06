package org.esupportail.sgc.services.userinfos;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.sql.DataSource;

import org.esupportail.sgc.domain.User;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.roo.addon.javabean.RooJavaBean;

@RooJavaBean
public class SqlUserInfoService implements ExtUserInfoService {
	
	private String sqlQuery;
	
	private JdbcTemplate jdbcTemplate;
	
	private Long order = Long.valueOf(0);
	
	private String eppnFilter = ".*";

	public void setDataSource(DataSource datesource) {
		this.jdbcTemplate = new JdbcTemplate(datesource);
	}

	@Override
	public Map<String, String> getUserInfos(User user, HttpServletRequest request, final Map<String, String> userInfosInComputing) {

		Map<String, String> userInfos = (Map<String, String>) jdbcTemplate.query(sqlQuery, new Object[] {user.getEppn()}, new ResultSetExtractor() {
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
