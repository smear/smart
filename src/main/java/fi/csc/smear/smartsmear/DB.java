/**
 * 
 */
package fi.csc.smear.smartsmear;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Date;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.Set;
import java.util.List;

import fi.csc.smear.db.model.SmearTableMetadata;
import fi.csc.smear.db.model.SmearVariableMetadata;
import fi.csc.smear.db.service.SmearTableMetadataLocalServiceUtil;
import fi.csc.smear.db.service.SmearVariableMetadataLocalServiceUtil;
/**
 * SQL Queries
 * @author pj
 *
 */
public class DB {
	
	private final static String SELECT = "SELECT ";
	public final static String SAMPTIME =  "samptime";
	public final static String GEOMETRIC = "exp(avg(ln(";
	private final static String CS = ", ";
	private final static String[] AVG = {CS, CS+GEOMETRIC, CS+"avg(", CS+"sum("};
	private final static String[] LOPPUSULUT = {"", ")))", ")", ")"};
	public final static int NONE = 0;
	public final static int GEOM = 1;
	public final static int MEAN = 2;
	public final static int SUMM = 3;
	Connection conn = null;
	Smearyhteys sy  = null;
	/**
	 * Connection
	 */
	public DB() {
		this.sy = new Smearyhteys();
		this.conn = sy.getConnection(true);
	}
	
	public ResultSet avg(Set<String> variables, String kanta, long dstart, long dend, int min, int type) {
		ResultSet rs = null;
		StringBuilder sb = new StringBuilder();
		Date start = new Date(dstart);
		Date end = new Date(dend);
		sb.append(SELECT);
		sb.append(SAMPTIME);
		Iterator<String> i = variables.iterator();
		while (i.hasNext()) {
			sb.append(AVG[type]);
			sb.append(i.next());
			sb.append(LOPPUSULUT[type]);
		}
		sb.append(" FROM " + kanta + " WHERE "+SAMPTIME+ " < ? AND "+SAMPTIME+ " > ?");
		if (NONE != type) {
			sb.append(" GROUP BY floor(timestampdiff(minute, '1990-1-1',  samptime) /? )");
		}
		try {
			//runtime abstract method error
			/*if (!this.conn.isValid(1)) {
				this.conn.close();
				this.conn = sy.getConnection(true);	
			}*/
			PreparedStatement ps = this.conn.prepareStatement(sb.toString());
			ps.setDate(1, end);
			ps.setDate(2, start);
			if (NONE != type) {
				if (min < 30) { //danger!! fix if set new averaging time
					ps.setInt(3, 30); //use 30 as default when user NOT select avraging time
				} else {
					ps.setInt(3, min);
				}
			}
			rs = ps.executeQuery();
		} catch (SQLException e) {
			e.printStackTrace();
			System.err.println(sb.toString());
		}
		return rs;
	}
	public ResultSet avgmulti(Set<String> variables, Set<String> kanta, long dstart, long dend, int min, int type) {
		int counter = 0;
		int [] periods = new int[kanta.size()];
		String [] kannat = new String[kanta.size()];
		for (String kan:kanta){
			kannat[counter]=kan;
			List<SmearTableMetadata> tableperiod = Station.getTablefromName(SmearTableMetadata.class,kan);
			if (tableperiod != null && !tableperiod.isEmpty()) {
				periods[counter] = tableperiod.get(0).getPeriod();
				counter++;
			} else{
				System.out.println("No period set for "+kan);
			}
		}
		// We need to sort tables according to periods  
		String holdkanta = "";
		int holdperiod = 0;
		for(int index = 0; index<kanta.size();index++)
        {
            for(int index1 = 0; index1<kanta.size()-1;index1++)
            {
                if(periods[index1]>periods[index1+1])
                {
                    holdperiod = periods[index1];
                    holdkanta = kannat[index1];
                    periods[index1] = periods[index1+1];
                    kannat[index1] = kannat[index1+1];
                    periods[index1+1] = holdperiod;
                    kannat[index1+1] = holdkanta; 
                }
            }
        }
		
		ResultSet rs = null;
		StringBuilder sb = new StringBuilder();
		Date start = new Date(dstart);
		Date end = new Date(dend);
		sb.append(SELECT);
		sb.append(kannat[0]+"."+SAMPTIME);
		Iterator<String> i = variables.iterator();
		while (i.hasNext()) {
			sb.append(AVG[type]);
			sb.append(i.next());
			sb.append(LOPPUSULUT[type]);
		}
		sb.append(" FROM "+kannat[0]);
		//Iterator<String> ik = kanta.iterator();
		//while (ik.hasNext()) {
		//	sb.append(ik.next());
		//	if (ik.hasNext()){
		//	sb.append(", ");
		//	}
		//}
		for (int ind=1; ind<kanta.size();ind++ ){
			sb.append(" LEFT OUTER JOIN "+kannat[ind]+" ON "+kannat[0]+"."+SAMPTIME+"="+kannat[ind]+"."+SAMPTIME);
		}
		sb.append(" WHERE "+kannat[0]+"."+SAMPTIME+ " < ? AND "+kannat[0]+"."+SAMPTIME+ " > ?");
		if (NONE != type) {
			sb.append(" GROUP BY floor(timestampdiff(minute, '1990-1-1',  samptime) /? )");
		}
		try {
			//runtime abstract method error
			/*if (!this.conn.isValid(1)) {
				this.conn.close();
				this.conn = sy.getConnection(true);	
			}*/
			PreparedStatement ps = this.conn.prepareStatement(sb.toString());
			ps.setDate(1, end);
			ps.setDate(2, start);
			if (NONE != type) {
				if (min < 30) { //danger!! fix if set new averaging time
					ps.setInt(3, 30); //use 30 as default when user NOT select avraging time
				} else {
					ps.setInt(3, min);
				}
			}
			rs = ps.executeQuery();
		} catch (SQLException e) {
			e.printStackTrace();
			System.err.println(sb.toString());
		}
		return rs;
	}
}    