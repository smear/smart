/**
 * 
 */
package fi.csc.smear.smartsmear;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Set;

import java.util.List;
import java.util.HashSet;

import com.liferay.portal.kernel.util.StringPool;
import com.vaadin.server.FileDownloader;
import com.vaadin.server.FileResource;
import com.vaadin.server.Resource;
import com.vaadin.server.StreamResource;
import com.vaadin.server.StreamResource.StreamSource;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Tree;
import com.vaadin.data.Item;

import com.vaadin.ui.VerticalLayout;
import com.vaadin.data.util.HierarchicalContainer;
import com.vaadin.ui.Notification;
import com.vaadin.server.Page;
import fi.csc.smear.db.model.Hydemeta;
import fi.csc.smear.db.model.Kumpulameta;
import fi.csc.smear.db.model.SmearVariableMetadata;
import fi.csc.smear.db.model.Varriometa;
import fi.csc.smear.db.model.Towermeta;
import fi.csc.smear.db.model.Hyde_eddy233;
import fi.csc.smear.db.model.Hyde_eddytow;
import fi.csc.smear.db.model.Varrio_tree;

/**
 * @author pj
 *
 */
public class Download {
	private static final long serialVersionUID = 4343017200185855932L;

	final static String FILENAMEFORMAT = "yyyyMMdd'T'HHmm";
	public static final String GEOMETRIC = "GEOMETRIC";
	public static final String MEDIAN = "MEDIAN";
	public static final String SUMM = "SUMM";
	private static final String FILENAME = "export"; 
	private static final String CSV = ".csv";
	private Tree tree;
	public ResultSet data;
	public ResultSet qset;
	public String[] metadata_desc;
	public String[] metadata_sour;
	public String[] metadata_unit;
	public String[] metadata_var;
	ComboBox avg, typeOfAVG;
	private static String separator;
	private Station stations;
	private int station;
	private Date start;
	private Date end;
	//private String quality;
	DateFormat fnf = new SimpleDateFormat(FILENAMEFORMAT);
	private int qnocolumns;
	//private HierarchicalContainer container
	private String stationstr;
	private String tablestr;
	private HierarchicalContainer container;
	//Download dl = new Download(startdate.getValue(), enddate.getValue(), variableselection, avaraging, typeavaraging);
	
	public Download(Date start, Date end, DB db, VerticalLayout vs, HierarchicalContainer container, ComboBox avg, ComboBox typeOfAVG, String quality) {
		//this.station = station;
		this.start = start;
		this.end = end;
		//this.stations = stations; //findbug
		this.tree = (Tree)vs.getComponent(0); //Variable selection tree
		this.separator = StringPool.COMMA;
		this.avg = avg;
		this.typeOfAVG = typeOfAVG;
		this.container = container;
		
		
		System.out.println("QUALITY SET AS: "+quality);
		Set<String> finalset = new HashSet<String>();
		Set<String> emep = new HashSet<String>();
		Set<String> set = (Set<String>)this.tree.getValue();
		Set<String> tableset = new HashSet<String>();
		Boolean setok = true;
		metadata_desc = new String[set.size()];
		metadata_sour = new String[set.size()];
		metadata_unit = new String[set.size()];
		metadata_var = new String[set.size()];
		
		for (String varsta:set){
			String[] parts = varsta.split(":");
			String var = parts[0];
			tablestr = parts[1];
			//System.out.println("Muuttuja "+var+" taulusta "+tablestr);
			stationstr = (String)this.tree.getParent((String)this.tree.getParent(varsta));
			//System.out.println("Asemalta "+stationstr);
			//tableset.add(tablestr);
		    
			if (quality.equals(SmearViewUI.CHECKED)){
				emep.add(var+"_EMEP");
				emep.add(var);
				this.qset = db.avg(emep, tablestr, start.getTime(), end.getTime(), readAVG(this.avg), readAVGtype(typeOfAVG));					
				emep.clear();
				if (null == this.qset){
					//System.out.println("Not Checked: "+sta);
					new Notification("Variable "+var+" is not quality checked in this timeframe","",
                		    Notification.TYPE_WARNING_MESSAGE, true)
                		    .show(Page.getCurrent());
				} else {
				    try {
					while (this.qset.next()){
					    if (this.qset.getString(3) != null) {
						if ((this.qset.getString(2) == null) || 
						    (this.qset.getInt(2) < 2 )) {
						    setok = false;
						}
					    }
					}	
					if (setok){
						finalset.add(var);
						tableset.add(tablestr);
					} else {
						new Notification("Variable "+var+" is not quality checked in this timeframe","",
			                		    Notification.TYPE_WARNING_MESSAGE, true)
			                		    .show(Page.getCurrent());
						setok = true;
					}
				//} //next
				
				} catch(java.sql.SQLException exc) {
					System.out.println("SQL Exception "+exc);
				}	
				}				
			} else{
				finalset.add(var);
				tableset.add(tablestr);
			}
		}
		if (!finalset.isEmpty()){
			this.data = db.avgmulti(finalset, tableset, start.getTime(), end.getTime(), readAVG(this.avg), readAVGtype(typeOfAVG));
			//System.out.println("Retrieving data");
			this.qnocolumns = finalset.size();
		} else {
			System.out.println("No variables in query");
		}
		int i=0;
		for (String sta2:set){
		Item v = container.getItem(sta2);
		//stationstr = (String)this.tree.getParent((String)this.tree.getParent(varsta));
		Object uni = null;
		Object des = null;
		Object sou = null;
		//variablename needed for sorting
		String[] parts2 = sta2.split(":");
		String variablename = parts2[0];
		try{
			//System.out.println("Item "+sta2);
			uni = v.getItemProperty(Station.UNIT).getValue();
			//System.out.println("Unit "+uni);
			des = v.getItemProperty(Station.DESCRIPTION).getValue();
			//System.out.println("Des "+des);
			sou = v.getItemProperty(Station.SOURCE).getValue();
			//System.out.println("sou "+sou);
			String adddesc = ""+des;
			//adddesc.replace(',','t');
			metadata_desc[i] = adddesc.replace(',',' ');
			String addsour = "From: "+sou;
			//addsour.replace(',','t');
			metadata_sour[i] = addsour.replace(',',' ');
			String addunit = "Unit: "+uni;
			//addunit.replace(',','t');
			metadata_unit[i] = addunit.replace(',',' ');
			metadata_var[i] = variablename;
			i++;
		} catch(java.lang.NullPointerException enu) {
			System.err.println("Null pointer Exception in download add metadata");
		}
		}
		System.out.println("Download OK");
	}
	
	
	private int readAVGtype(ComboBox typeOfAVG2) {
		String v = typeOfAVG2.getValue().toString();
		if (v.equals(GEOMETRIC)) {
			return DB.GEOM;
		}
		if (v.equals("ARITHMETIC")) {
			return DB.MEAN;
		}
		return DB.NONE;
	}

	public File getHDF5() {
		if (data != null) {
			HDF5 hdf5 = new HDF5(this, start, end, station, this.stations, readAVG(this.avg), this.typeOfAVG);
			return hdf5.getFile();
		} else {
			return null;
		}
		
	}
	
	public FileDownloader getCSV() {

		
		final int average = readAVG(this.avg);
		//final String separator = StringPool.COMMA;
		if (data != null ) {
			FileDownloader fd = new FileDownloader(new StreamResource(new StreamSource() {
				private static final long serialVersionUID = 4343017200085855921L;
				@Override
				public InputStream getStream() {
					StringBuilder sb = new StringBuilder();
					
					
					try {						
						ResultSetMetaData rsmd = data.getMetaData();	
						
						int [] order = new int[metadata_var.length];
						for (int i = 2; i < qnocolumns+2; i++) {
							sb.append(separator);
							sb.append(rsmd.getColumnLabel(i));
							// find order to match variable with metadata
							for (int varmet = 0; varmet < metadata_var.length; varmet++){
								//System.out.println("Verrataan datasta "+rsmd.getColumnLabel(i)+" ja metasta "+metadata_var[varmet]);
								if (rsmd.getColumnLabel(i).equals(metadata_var[varmet])){
									//System.out.println("Löytyi");
									order[i-2] = varmet;
								}
							}
							
						}
						sb.append(StringPool.NEW_LINE);
						
						for (int met = 0;met < metadata_desc.length;met++){
							sb.append(separator);
							sb.append(metadata_desc[order[met]]);
						}
						sb.append(StringPool.NEW_LINE);
						for (int met = 0;met < metadata_sour.length;met++){
							sb.append(separator);
							sb.append(metadata_sour[order[met]]);
						}
						sb.append(StringPool.NEW_LINE);
						sb.append(DB.SAMPTIME);
						for (int met = 0;met < metadata_unit.length;met++){
							sb.append(separator);
							sb.append(metadata_unit[order[met]]);
						}
						sb.append(StringPool.NEW_LINE);
						data.beforeFirst();
						while (data.next()) {
							sb.append(StringPool.NEW_LINE);
							sb.append(data.getTimestamp(1));
							//data.getFloat(columnIndex);
							for (int i = 2; i < qnocolumns+2; i++) {
								sb.append(separator);
								sb.append(read(i));							
							}
						}
					} catch ( java.sql.SQLException e) {
						e.printStackTrace();
					}
					return new ByteArrayInputStream(sb.toString().getBytes());
				}
			}, "Smeardata"+FILENAME+fnf.format(start)+CSV));
			return fd;
		}else {
			System.err.println("Query return NO data");
			return null;
		}
	}

	/**
	 * Read Non NULL value from resultset
	 * @param i int column to read
	 * @return String column value or "" case of NULL  
	 */
	public String read(int i) {
		String v = "";
		try {
			v = data.getString(i);
			if (data.wasNull()) {
				v = "";
			}
		} catch ( java.sql.SQLException e) {
			/*if (data.wasNull()) {
				v = "";
				System.err.println("SQLException was null");
			} else {*/
				System.err.println("SQLException: "+e);
			//}
		} catch (java.lang.NullPointerException e) {
			System.err.println("NullPointerException: "+i);
		}
		return v;
	}


	private int readAVG(ComboBox avg2) {
		try {
			String v = avg2.getValue().toString();
			if (v.equals("30MIN")) {
				return 30;
			}
			if (v.equals("1HOUR")) {
				return 60;
			}
		} catch (java.lang.NullPointerException e) {
			System.out.println("NO average selected");
		}
		return 0;
	}

	public ResultSet getResultSet() {
		return this.data;
	}

	/*protected void append(StringBuilder sb, String variable, float value) {
		if (this.tree.isSelected(variable)) {
			sb.append(this.separator);
			sb.append(value);
		}		
	}	
	protected void append(StringBuilder sb, String variable, String value) {
		if (this.tree.isSelected(variable)) {
			sb.append(this.separator);
			sb.append(value);
		}		
	}*/	

}
