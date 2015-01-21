/**
 * 
 */
package fi.csc.avaa.smear.smartsmear;

import java.util.Hashtable;
import java.util.List;
import java.util.Date;

import com.liferay.portal.kernel.dao.orm.DynamicQuery;
import com.liferay.portal.kernel.dao.orm.DynamicQueryFactoryUtil;
import com.liferay.portal.kernel.dao.orm.RestrictionsFactoryUtil;
import com.liferay.portal.kernel.exception.SystemException;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.Tree;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.AbstractSelect.ItemDescriptionGenerator;
import com.vaadin.ui.Component;
import com.vaadin.data.Container;
import com.vaadin.data.Item;
import com.vaadin.data.util.HierarchicalContainer;
import com.vaadin.data.util.ObjectProperty;
import com.vaadin.data.util.PropertysetItem;
import com.vaadin.ui.PopupDateField;

import fi.csc.smear.db.model.SmearTableMetadata;
import fi.csc.smear.db.model.SmearVariableMetadata;
import fi.csc.smear.db.service.SmearTableMetadataLocalServiceUtil;
import fi.csc.smear.db.service.SmearVariableMetadataLocalServiceUtil;

/**
 * @author pj
 *
 */
public class Station  {
	private static final long serialVersionUID = 4343017200085855933L;
	public static final int VÄRRIÖ = 2;
	public static final String Värriö = "Värriö SMEAR I";
	public static final int HYYTIÄLÄ = 0;
	public static final String Hyytiälä = "Hyytiälä SMEAR II";
	public static final String Kumpula = "Kumpula SMEAR III";
	public static final int KUMPULA = 1;
	public static final int SIIKANEVA1 = 4;
	public static final String Siikaneva1 = "Siikaneva 1";
	public static final int SIIKANEVA2 = 5;
	public static final String Siikaneva2 = "Siikaneva 2";
	public static final int TORNI = 6;
	public static final String Torni = "Helsinki Hotel Torni";
	public static final int STATIONS = 7; //bigest station no + 1
	//private static final String TABLES[] = {"", "VAR_META", "HYY_META", "", "KUM_META"};
	public static final String ASEMAT[] = {Hyytiälä, Kumpula, Värriö, Siikaneva1, Siikaneva2, Torni};	
	public static final String TAULUT[][] = {{"HYY_META", "HYY_EDDY233", "HYY_EDDYTOW", "HYY_EDDYSUB", "HYY_DMPS"}, 
		{"KUM_META", "KUM_EDDY", "KUM_DMPS"}, {"VAR_META", "VAR_TREE", "VAR_EDDY", "VAR_DMPS"}, 
		{"SII1_META", "SII1_EDDY"},{"SII2_META", "SII2_EDDY"},{"TOR_EDDY"}};
	public static final String DESCRIPTION = "description";
	public static final String TITLE = "title";
	public static final String UNIT = "unit";
	public static final String SOURCE = "source";
	private static final Date START = new Date(91,5,1);
	VerticalLayout stationselection;
	//private HorizontalLayout dayselection;
        //private VerticalLayout variableselection; //findbug
        //private static List<SmearVariableMetadata> vmdata; //findbug
	private static Hashtable<String, Integer> tablestation = new Hashtable<String, Integer>();
	private static String[] identifier = new String[STATIONS]; //bigest station no + 1  
	private static String[] geographicalCoverage = new String[STATIONS]; // 5 -"-
	public static HierarchicalContainer treecontainer = new HierarchicalContainer();
	public static Date startfrom = new Date(91,5,1);
	
	public Station( VerticalLayout vs ) {
	    //this.variableselection = vs;
		stationselection = new VerticalLayout();
			//stationselection.setMargin(true);
			/*final CheckBox hyde = new CheckBox(Hyytiälä);
			//hyde.setValue(true);
			hyde.addValueChangeListener(getValueChangeListener(TABLES[HYYTIÄLÄ], Hyytiälä));
			stationselection.addComponent(hyde);
			final CheckBox kumpula = new CheckBox(Kumpula);
			//kumpula.setEnabled(true);
			kumpula.addValueChangeListener(getValueChangeListener(TABLES[KUMPULA], Kumpula));
			stationselection.addComponent(kumpula);
			final CheckBox varrio = new CheckBox(Värriö);
			varrio.addValueChangeListener(getValueChangeListener(TABLES[VÄRRIÖ], Värriö));
			//varrio.setEnabled(true);
			stationselection.addComponent(varrio);*/
	}
	public static void setFrom(Date from){
		startfrom = from;
	}

	public VerticalLayout getStationselection() {
		return stationselection;
	}
	
	public int firstSelectedStation() { //use contructor order
		CheckBox cb = (CheckBox)stationselection.getComponent(0);
		if (cb.getValue()) { 
			System.out.println(cb.getCaption());
			return HYYTIÄLÄ;
		}
		cb = (CheckBox)stationselection.getComponent(1);
		if (cb.getValue()) { 
			System.out.println(cb.getCaption());
			return KUMPULA;
		}
		cb = (CheckBox)stationselection.getComponent(2);
		if (cb.getValue()) { 
			System.out.println(cb.getCaption());
			return VÄRRIÖ;
		}
		return 0;
	}
	/**
	 * Query from VariableMetadata table
	 * 
	 * @param tablename String Name of database table
	 * @return DynamicQuery result List run by liferay ServiceUtil
	 */
	@SuppressWarnings("unchecked")
	public static <T> List<T> getMetadataInStation(String tablename) {
		DynamicQuery query = DynamicQueryFactoryUtil.forClass(SmearVariableMetadata.class)
				.add(RestrictionsFactoryUtil.eq("tableID", tablename2ID(tablename)));
		try { 
			return SmearVariableMetadataLocalServiceUtil.dynamicQuery(query);
		} catch (SystemException e) {
			System.err.println("SystemException: "+e);	
		}
		return null;
	}
	
	/**
	 * Query from TableMetadata table with name of database table as input 
	 
	 * @param entityClass liferay service builder generated class describing the table 
	 * @param tablename String Name of database table
	 * @return DynamicQuery result List run by liferay ServiceUtil
	 */
	@SuppressWarnings("unchecked")
	public static <T> List<T> getTablefromName(Class<T> entityClass, String  tablename) {
		DynamicQuery query = DynamicQueryFactoryUtil.forClass(entityClass)
				.add(RestrictionsFactoryUtil.eq("name", tablename));
		try { 
			return SmearTableMetadataLocalServiceUtil.dynamicQuery(query);
		} catch (SystemException e) {
			System.err.println("SystemException: "+e);	
		}
		return null;
	}
	/**
	 * https://confluence.csc.fi/display/SMEAR/newSchema has smearmetadata SVG, PDF, PNG
	 * drawing describing the metadata tables
	 * 
	 * @param tablename String Name of the database table 
	 * @return long TableID 
	 */
	private static long tablename2ID(String tablename) {
		List<SmearTableMetadata> tablemetadata =  getTablefromName(SmearTableMetadata.class, tablename);
		if (tablemetadata != null && !tablemetadata.isEmpty()) {
			identifier[resolve(tablename)] = tablemetadata.get(0).getIdentifier();
			geographicalCoverage[resolve(tablename)] = tablemetadata.get(0).getSpatial_coverage();
			return tablemetadata.get(0).getTableID();
		}
		return -1; //error
	}

	private static int resolve(String tablename) {
		//System.out.println("Resolve tablename "+tablename+ "palautetaan " +tablestation.get(tablename));
		return tablestation.get(tablename);
	}

	
	/**
	 * Smear database TableMetadata table column identifier
	 * @param station int  from public static final int VÄRRIÖ ... of this
	 * @return TableMetadata table identifier column
	 */
	public static String getIdentifier(int station) {
		return identifier[station];
	}
	
	/**
	 * 
	 * @param station int from public static final int VÄRRIÖ ... of this
	 * @return TableMetadata table Coverage of the station
	 */
	public static String getGeographicalCoverage(int station) {
		return geographicalCoverage[station];
	}
	
    static {
		for (int i = 0; i < ASEMAT.length; i++){
			for (int j = 0; j < Station.TAULUT[i].length; j++){
				tablestation.put(TAULUT[i][j], i);
			}
		}

	}

}
