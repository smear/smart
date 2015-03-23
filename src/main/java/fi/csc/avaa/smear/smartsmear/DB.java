/**
 * 
 */
package fi.csc.avaa.smear.smartsmear;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Date;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import com.liferay.portal.kernel.util.StringPool;
//import fi.csc.smear.db.model.SmearVariableMetadata;
//import fi.csc.smear.db.service.SmearTableMetadataLocalServiceUtil;
//import fi.csc.smear.db.service.SmearVariableMetadataLocalServiceUtil;
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
	private final static String[] AVG = {CS, CS+GEOMETRIC, CS+"avg(", CS+"sum(", CS};
	private final static String[] LOPPUSULUT = {"", ")))", ")", ")", ""};
	public final static int NONE = 0;
	public final static int GEOM = 1;
	public final static int MEAN = 2;
	public final static int SUMM = 3;
	public static final int MEDIAN = 4;
	private int typeOfAVG; //some of above
	private int avg; //0, 30, 60min	
	Connection conn = null;
	Smearyhteys sy  = null;
	/**
	 * Connection
	 */
	public DB() {
		this.sy = new Smearyhteys();
		this.conn = sy.getConnection(true);
	}
	
	public ResultSet avg(Data data, List<String> variables, String kanta, long dstart, long dend, int min, int type) {
		this.avg = min;
		ResultSet rs = null;
		if (null != data) {
			variables = data.getV(kanta);
		}
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
		sb.append(" FROM " + kanta + " WHERE "+SAMPTIME+ " < ? AND "+SAMPTIME+ " >= ?");
		if (NONE != type) {
			sb.append(" GROUP BY floor(timestampdiff(minute, '1990-1-1', "+SAMPTIME+") /? )");
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
		if (null != data) {
			process(rs, data, kanta);
		}
		return rs;
	}
	
	/**
	 * Tallentaa ResultSetin Data olioon
	 * 
	 * @param rs ResultSet
	 * @param data Data
	 */
	 private void process(ResultSet rs, Data data, String kanta) {
	    	try {    		
	    		long t1=0, t2 = 0;
	    		ResultSetMetaData rsmd = rs.getMetaData();
	    		int cc = rsmd.getColumnCount();
	    		System.out.println("Sarakkeiden lkm: "+cc);
	    		data.addSarakelkm(cc-1);
	    		if (rs.next()) {
	    			ArrayList<String>labels = new ArrayList<String>();
	    			for (int i = 2; i <= cc; i++) {
	    				labels.add(rsmd.getColumnName(i));
	    				//System.out.println(kanta+" label added: "+rsmd.getColumnName(i));
	    			}
	    			data.addLabels(labels, kanta);
	    			data.setT1(rs.getTimestamp(1).getTime());
	    			if (rs.next()) {
	    				data.setT2(rs.getTimestamp(1).getTime());
	    			}
	    		} else {
	    			System.out.println("Haun tulos tyhjä");
	    		}
	    		rs.last();
				int rows = rs.getRow();
				String samptimes[] = new String[rows];
				Timestamp[] timestamps = new Timestamp[rows];
				String nws[] = new String[rows];
				float[][] fa = new float[cc][rows];  //column, rows
		    	rs.beforeFirst();
		    	Boolean ekarivi = true;
	    		int j=0; //row
			    int n = 0;
	    		while (rs.next()) {
	    			if (data.ekaTaulu()) {
	    				if (!ekarivi) {
	    					data.sb.append(StringPool.NEW_LINE);	    				
	    				} else {
	    					ekarivi = false;
	    				}
	    				timestamps[j] = rs.getTimestamp(1);
						data.sb.append(timestamps[j]);							
		    			samptimes[j] = rs.getString(1);		    			
	    			}
	    			//data.getFloat(columnIndex);
	    			for (int i = 2; i < cc; i++) {
	    				data.sb.append(Download.separator);
	    				try {
	    					String v = rs.getString(i);
	    					fa[i-2][j] = rs.getFloat(i);
	    					if (rs.wasNull()) {
	    						fa[i-2][j] = Float.NaN;
	    						v = "";
	    					}
	    					data.sb.append(v);
	    				} catch ( java.sql.SQLException e) {
	    					nws[j] = rs.getString(i);
	    					n=i;
	    					System.out.println("n= "+n);	    					
	    				} catch (  java.lang.ArrayIndexOutOfBoundsException e) {
	    					System.err.println("i="+i+" j="+j);
	    					e.printStackTrace();
	    					System.err.println("rows="+rows+" cols="+cc);
	    				}
	    			}
	    			j++;
	    		}
				data.setEka(false);					
	    		data.setTimestamps(timestamps);
	    		data.setSamptimes(samptimes);
	    		if (n > 0) {
	    			data.setNWS(nws);
	    			try {
	    				data.setNWSname(rsmd.getColumnName(n));
	    			} catch (java.sql.SQLException e) {
	    				System.err.println("NWS set failure");
	    			}
	    		}
	    		float[][] keskirarvoistettufa = keskiarvotus(fa, cc, rows, t2-t1 /(1000*60));
	    		data.addFtaulu(keskirarvoistettufa,kanta);
	    		
	    	} catch ( java.sql.SQLException e) {
	    		e.printStackTrace();
	    	}
	    	

		}
	 
	 private float[][] keskiarvotus(float[][] fa, int count, int size, long interval ) {
		    //Boolean geom = false; // findbug: täyttyy testata
			Boolean median = false;
			int average = 0;
			if ((avg > 0) && (interval > 0)) {
				median = MEDIAN == typeOfAVG ? true : false;
				average = (int) (avg/interval);
			}

			for (int i = 0 ; i < count ; i++) {
				if (average > 1) {
					int dims = size/average;
					float[] f =  new float[dims];
					int l = 0;
					for (int k = 0; k+average < size; k = k + average) {
						if (median) {
							float a[] = new float[average];
							int m = 0;
							for (int j = k; j < k+average; j++) {
								a[m++] = fa[i][j];
							}
							Arrays.sort(a);
							f[l++] = a[average/2];
						} 
					}
					fa[i] = f;
				}
			}	
			return fa;
		}
	 
/*	public ResultSet avgmulti(Set<String> variables, Set<String> kanta, long dstart, long dend, int min, int type) {
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
		String k0samptime = kannat[0]+"."+SAMPTIME;
		for (int ind=1; ind<kanta.size();ind++ ){
			sb.append(" LEFT OUTER JOIN "+kannat[ind]+" ON "+k0samptime+"="+kannat[ind]+"."+SAMPTIME);
		}
		sb.append(" WHERE "+k0samptime+ " < ? AND "+k0samptime+ " >= ?");
		if (NONE != type) {
			sb.append(" GROUP BY floor(timestampdiff(minute, '1990-1-1', "+k0samptime+") /? )");
		}
		try {
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
	}*/
}    
