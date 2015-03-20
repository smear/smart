/**
 * 
 */
package fi.csc.avaa.smear.smartsmear;

//import java.sql.ResultSetMetaData;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Set;

/**
 * Data
 * @author pj
 *
 */
public class Data {
	private Hashtable<String, ArrayList<String>> labels = new Hashtable<String, ArrayList<String>>();
	private Hashtable<String, float[][]> fdata = new Hashtable<String, float[][]>();
	private Hashtable<String, Integer> htindex;
	private Set<String> finalset;
	public Set<String> tableset;
	private boolean eka = true;
	private int nocolums = 0; //number of columns
	private int rivit;
	private String[] samptimes;
	private String[] nws; 
	private long t1, t2; // datan aikaväli lasketaan t2-t1
	public StringBuffer sb;
	private String nwsname;
	private Timestamp[] timestamps;
	
	public Data(Set<String> finalset, Set<String> tableset, Hashtable<String, Integer>  htindex ) {
		this.finalset = finalset;
		this.tableset = tableset;
		this.sb = new StringBuffer();
		this.htindex = htindex; //from metadata
	}

	/**
	 * Variables
	 * 
	 * @param table String table name
	 * @return sorted list of variables
	 */
	public List<String> getV(String table) {
		//System.out.println("Taulu="+table);
		String[] sa = new String[2000]; //muuttujien lkm
		finalset.stream().filter(v -> v.endsWith(table)).forEach(v ->
			sa[indexsoi(muuttuja(v))] = muuttuja(v)); // sorting
		ArrayList<String> l = new ArrayList<String>();
		for (int i = 0; i < sa.length; i++) {
			if (null != sa[i]) {
				l.add(sa[i]);
			}
		}
		return l;
	}

	private String muuttuja(String tm) {
		return tm.substring(0,tm.indexOf(":"));
	}
	private int indexsoi(String v) {
		//System.out.println("v= "+v+" i="+htindex.get(v));
		return htindex.get(v);
	}

	public boolean ekaTaulu() {
		return eka;
	}

	public void setEka(Boolean f) {
		eka = f;
	}

	public int getRivienlkm() {
		return rivit;
	}

	public String[] getLabels(String table) {
		ArrayList<String> al = labels.get(table);
		if (null == al) {
			System.err.println("No labels; table: "+table);
			return null;
		}
		return (String[]) al.toArray(new String[al.size()]);
	}

	public int getColumnCount() {
		return nocolums;
	}

	public void addSarakelkm(int i) {
		this.nocolums += i; 		
	}

	public void addLabels(ArrayList<String> cn, String table) {
		labels.put(table, cn);		
	}

	public void addFtaulu(float[][] fa, String taulu) {
		fdata.put(taulu, fa);
		rivit = fa[0].length;
	}
	
	public float[][] getFtaulu(String taulu) {
		return fdata.get(taulu);
	}

	public void setSamptimes(String[] samptimes) {
		this.samptimes = samptimes;		
	}
	public String[] getSamptimes() {
		return this.samptimes;
	}

	/**
	 * @return the t2
	 */
	public long getT2() {
		return t2;
	}

	/**
	 * @param t2 long toisen datapisteen aika
	 */
	public void setT2(long t2) {
		this.t2 = t2;
	}

	/**
	 * @return the t1
	 */
	public long getT1() {
		return t1;
	}

	/**
	 * @param t1 long ensimmäisen  datapisteen aika 
	 */
	public void setT1(long t1) {
		this.t1 = t1;
	}

	public void setNWS(String[] nws) {
		this.nws = nws;		
	}
	
	public String[] getNWS() {
		return this.nws;
	}

	public void setNWSname(String columnName) {
		this.nwsname = columnName;		
	}
	
	public String getNWSname() {
		return this.nwsname;
	}

	public void setTimestamps(Timestamp[] timestamps) {
		this.timestamps = timestamps;		
	}
	
	public Timestamp[] getTimestamps() {
		return this.timestamps;
	}
	
	public String clean(String s) {
	    	String clean = s;
	    	if (null != s) {
	    		if (s.startsWith("avg(") || s.startsWith("sum(")) {
	    			clean = s.substring(4, s.length()-1);
	    		} else if (s.startsWith(DB.GEOMETRIC)) {
	    			clean = s.substring(DB.GEOMETRIC.length(), s.length()-3); //lopusta poistetaan loppusulut
	    		}
	    	}
			return clean;
		}
	
/*	
private String clean(String variab) {
	if (variab.startsWith("avg(") || variab.startsWith("sum(")) {
		return variab.substring(4, variab.length()-1);			
	} else if (variab.startsWith(DB.GEOMETRIC)) {
		//System.out.println(variab.substring(DB.GEOMETRIC.length(), variab.length()-3));
		return variab.substring(DB.GEOMETRIC.length(), variab.length()-3);
	}
	return variab;
}*/


}
