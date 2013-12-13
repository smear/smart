/**
 * 
 */
package fi.csc.smear.smartsmear;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
/*import java.util.Properties;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;*/
import javax.sql.DataSource;

import fi.csc.smear.db.service.VarriometaLocalServiceUtil;
import fi.csc.smear.db.service.persistence.VarriometaPersistence;
import fi.csc.smear.db.service.persistence.VarriometaUtil;

/**
 * @author pj
 *
 */
public class Smearyhteys {
	static final String AJURI= "com.mysql.jdbc.Driver";
	String DATASOURCE_CONTEXT = "smear";
	//static final String PORTALEXTPROPERTIES = "/opt/avaa/liferay-portal/tomacat/webapps/ROOT/WEB-INF/classes/portal-ext.properties";
	static final String PROPERTIES = "db.properties";
	static int port=3320;
	static final String URL = "jdbc:mysql://db5.csc.fi:"+port+"/smear";
	private String user = "AVAA-read"; // database has no secrets
	private String passwd = "qry881iU"; // I tried JNDI and property reading before this
	
	public Smearyhteys() {
		/*Properties prop = new Properties();
		try {
			//ClassLoader classLoader = Thread.currentThread().getContextClassLoader();		
			prop.load(this.getClass().getResourceAsStream("PROPERTIES"));
			user = prop.getProperty("jdbc.smear.username");
			passwd = prop.getProperty("jdbc.smear.password");
		} catch (IOException ex) {
    		ex.printStackTrace();
        }*/
	}
	
	public Connection getConnection(boolean tuotanto) {
		Connection connection = null;
		
		VarriometaPersistence varriometaPersistence = VarriometaUtil.getPersistence();
		      DataSource datasource = varriometaPersistence.getDataSource();
		      if (datasource != null) {
		        try {
					connection = datasource.getConnection();
				} catch (SQLException e) {
					e.printStackTrace();
				}
		      }
/*		try {
			connection = DriverManager.getConnection(URL, user, passwd);
		} catch (SQLException e) {
			e.printStackTrace();
		}*/
		return connection;
	}

}
