/**
 * 
 */
package fi.csc.smear.smartsmear;


import java.awt.Color;
import java.sql.SQLException;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;

import com.liferay.portal.kernel.dao.orm.DynamicQuery;
import com.liferay.portal.kernel.dao.orm.DynamicQueryFactoryUtil;
import com.liferay.portal.kernel.dao.orm.RestrictionsFactoryUtil;
import com.liferay.portal.kernel.exception.SystemException;

import com.vaadin.addon.timeline.Timeline;
import com.vaadin.data.Item;
import com.vaadin.data.util.HierarchicalContainer;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.server.Page;
import com.vaadin.server.Sizeable.Unit;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Image;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Tree;

import fi.csc.smear.db.model.Hydemeta;
import fi.csc.smear.db.model.Kumpulameta;
import fi.csc.smear.db.model.Varriometa;
import fi.csc.smear.db.model.Towermeta;
import fi.csc.smear.db.model.Hyde_eddy233;
import fi.csc.smear.db.model.Hyde_eddytow;
import fi.csc.smear.db.model.Kumpula_eddy;
import fi.csc.smear.db.model.Varrio_tree;
import fi.csc.smear.db.service.HydemetaLocalServiceUtil;
import fi.csc.smear.db.service.KumpulametaLocalServiceUtil;
import fi.csc.smear.db.service.VarriometaLocalServiceUtil;



/**
 * @author pj
 *
 */
public class Vizualisation  implements java.io.Serializable {
	private static final long serialVersionUID = 8532668582109303532L;
	public static final int MAXNOVIZ = 18; //visualisointien määrä sivulla
	public static final int VIZPERROW = 3; //visualisointien määrä rivillä
	public static final int ROWS = MAXNOVIZ/VIZPERROW;
	public static final int WIDTH = 200;
	public static final int HEIGHT = 150;
	private Date start;
	private Date end;
	
	
	Vizualisation(Date start, Date end) {
		this.start = start;
		this.end = end;
	}
	
	HorizontalLayout rivi1() {
		
		HorizontalLayout hl1rivi = createVizLayout();
		hl1rivi.addComponent(temperature(start, end, Station.HYYTIÄLÄ));
	    //hl1rivi.addComponent(co(start, end, Station.KUMPULA+Station.VÄRRIÖ+Station.HYYTIÄLÄ));
	    hl1rivi.addComponent(co(start,end));
	    //hl1rivi.addComponent(windspeed(start, end));
	    hl1rivi.addComponent(dpmsViz());
	    return hl1rivi;
	}
	
	HorizontalLayout rivi2() {
		HorizontalLayout hl2rivi = createVizLayout();;
	    hl2rivi.addComponent(glob(start, end, Station.KUMPULA+Station.VÄRRIÖ+Station.HYYTIÄLÄ));
	    hl2rivi.addComponent(RH(start, end, Station.KUMPULA+Station.VÄRRIÖ+Station.HYYTIÄLÄ));
	    hl2rivi.addComponent(O3(start, end, Station.KUMPULA+Station.VÄRRIÖ+Station.HYYTIÄLÄ));
	    return hl2rivi;
	}
	HorizontalLayout rivi3() {
		HorizontalLayout hl3rivi = createVizLayout();;
	    hl3rivi.addComponent(windspeed(start, end));
	    return hl3rivi;
	}
	public HorizontalLayout dpmsViz(){
		HorizontalLayout dpmslayout = createVizLayout();
		DPMS dpms = new DPMS(this);
		Image dpmsviz = new Image("Size Distribution [m]", dpms.getHyde(start, end));
		dpmslayout.addComponent(dpmsviz);
		return dpmslayout;
	}
	HorizontalLayout emptyrow(){
		HorizontalLayout empty_row = createVizLayout();
		return empty_row;
	}
	
	public Timeline windspeed(Date startdate, Date enddate) {
		Timeline timeline = createTimeline(WIDTH, HEIGHT);
		timeline.setCaption("Windspeed 15-16m");
		IndexedContainer containerWSU168 = createIndexedContainer();
		IndexedContainer containerTower_WS_16m = createIndexedContainer();
		IndexedContainer containerWS0 = createIndexedContainer();
		List<Hydemeta> data_h = getDataInRange(Hydemeta.class, startdate, enddate, "WSU168");
		List<Kumpulameta> data_k = getDataInRange(Kumpulameta.class, startdate, enddate, "Tower_WS_16m");
		List<Varriometa> data_v = getDataInRange(Varriometa.class, startdate, enddate, "WS0");
		if (data_h != null && !data_h.isEmpty()) {
			for (Hydemeta hydemeta: data_h) {
				addItem(containerWSU168, hydemeta.getSamptime(), hydemeta.getWSU168());	
			}
		} else {
			System.err.println("No Hyde data");
		}
		if (data_k != null && !data_k.isEmpty()) {
			for (Kumpulameta kumpulameta: data_k) {
				addItem(containerTower_WS_16m, kumpulameta.getSamptime(), kumpulameta.getTower_WS_16m());
			}
		} else {
			System.out.println("No Kumpula data");
		}
		if (data_v != null && !data_v.isEmpty()) {
			for (Varriometa varriometa: data_v) {
				addItem(containerWS0, varriometa.getSAMPTIME(), varriometa.getWS0());
			}
		} else {
			System.out.println("No Värriö data");
		}
		timeline.addGraphDataSource(containerWSU168, Timeline.PropertyId.TIMESTAMP, Timeline.PropertyId.VALUE);
		timeline.addGraphDataSource(containerTower_WS_16m, Timeline.PropertyId.TIMESTAMP, Timeline.PropertyId.VALUE);
		timeline.addGraphDataSource(containerWS0, Timeline.PropertyId.TIMESTAMP, Timeline.PropertyId.VALUE);
		timeline.setGraphCaption(containerWSU168, "Hyytiälä");
		timeline.setGraphCaption(containerTower_WS_16m, "Kumpula");
		timeline.setGraphCaption(containerWS0, "Värriö 15m");
		timeline.setGraphOutlineColor(containerWS0, Color.BLUE);
		timeline.setGraphOutlineColor(containerTower_WS_16m, Color.BLACK);
		timeline.setGraphOutlineColor(containerWSU168, Color.RED);
		return timeline;
	}
		
	/**
	 * 
	 * @param startdate java.util.Date
	 * @param enddate java.util.Date
	 * @return Timeline from vaadin addon chart 
	 * */
	
	public Timeline temperature(Date startdate, Date enddate, int stations) {
			Timeline timeline = createTimeline(WIDTH, HEIGHT);
			timeline.setCaption("Temperature 15-16m");
			IndexedContainer containerT168 = createIndexedContainer();
			IndexedContainer containerTower_T_16m = createIndexedContainer();
			IndexedContainer containerTDRY0 = createIndexedContainer();
			List<Hydemeta> data_h = getDataInRange(Hydemeta.class, startdate, enddate, "T672");
			List<Kumpulameta> data_k = getDataInRange(Kumpulameta.class, startdate, enddate, "Tower_T_16m");
			List<Varriometa> data_v = getDataInRange(Varriometa.class, startdate, enddate, "TDRY0");
			if (data_h != null && !data_h.isEmpty()) {
				for (Hydemeta hydemeta: data_h) {
					addItem(containerT168, hydemeta.getSamptime(), hydemeta.getT168());
				}
			} else {
				System.err.println("No Hyde data");
			}
			if (data_k != null && !data_k.isEmpty()) {
				for (Kumpulameta kumpulameta: data_k) {
					addItem(containerTower_T_16m, kumpulameta.getSamptime(), kumpulameta.getTower_T_16m());
				}
			} else {
				System.out.println("No Kumpula data");
			}
			if (data_v != null && !data_v.isEmpty()) {
				for (Varriometa varriometa: data_v) {
					addItem(containerTDRY0, varriometa.getSAMPTIME(), varriometa.getTDRY0());
				}
			} else {
				System.out.println("No Värriö data");
			}
			timeline.addGraphDataSource(containerT168, Timeline.PropertyId.TIMESTAMP, Timeline.PropertyId.VALUE);
			timeline.addGraphDataSource(containerTower_T_16m, Timeline.PropertyId.TIMESTAMP, Timeline.PropertyId.VALUE);
			timeline.addGraphDataSource(containerTDRY0, Timeline.PropertyId.TIMESTAMP, Timeline.PropertyId.VALUE);
			timeline.setGraphCaption(containerT168, "Hyytiälä");
			timeline.setGraphCaption(containerTower_T_16m, "Kumpula");
			timeline.setGraphCaption(containerTDRY0, "Värriö 15m");
			timeline.setGraphOutlineColor(containerTDRY0, Color.BLUE);
			timeline.setGraphOutlineColor(containerTower_T_16m, Color.BLACK);
			timeline.setGraphOutlineColor(containerT168, Color.RED);
		return timeline;
	}

	/**
	 * Creates an indexed container with two properties: value and timestamp.
	 * 
	 * @return a container with "value, timestamp" items.
	 */
	public IndexedContainer createIndexedContainer() {
		IndexedContainer container = new IndexedContainer();
		container.addContainerProperty(Timeline.PropertyId.VALUE, Float.class,
				new Float(0));
		container.addContainerProperty(Timeline.PropertyId.TIMESTAMP,
				java.util.Date.class, null);
		return container;
	}

	/**
	 * 
	 * @param width int
	 * @param height int
	 * @return vaadin chart addon timeline
	 */
	Timeline createTimeline(int width, int height) {
		Timeline timeline = new Timeline();
		timeline.setWidth(width + "px");
		timeline.setHeight(height + "px");
		timeline.setZoomLevelsCaption(null);
		timeline.setChartModesVisible(false);
		timeline.setZoomLevelsVisible(false);
		timeline.setDateSelectEnabled(false);
		timeline.setDateSelectVisible(false); 
		timeline.setBrowserVisible(false);
		return timeline;
	}
	
	/**
	 * add one point to graph data
	 * 
	 * @param c IndexedContainer to store data
	 * @param samptime java.util.Date
	 * @param value float
	 */
	void addItem(IndexedContainer c, java.util.Date samptime, float value){
		try {
			Item item = c.addItem(samptime);
			item.getItemProperty(Timeline.PropertyId.TIMESTAMP).setValue(samptime);
			item.getItemProperty(Timeline.PropertyId.VALUE).setValue(value);
		} catch (java.lang.NullPointerException e) {
			System.err.println(samptime + "was null: kesäaika");
		}
	}

	/**
	 * 
	 * @param entityClass database table as liferay service builder generated
	 * @param start java.util.Date
	 * @param end java.util.Date
	 * @param property String Database columm name: only IS NOT NULL values returned
	 * @return List of data from database
	 */
	@SuppressWarnings("unchecked")
	public <T> List<T> getDataInRange(Class<T> entityClass, Date start, Date end, String property) {
		if (entityClass.equals(Varriometa.class)) {
			DynamicQuery query = DynamicQueryFactoryUtil.forClass(entityClass)
					.add(RestrictionsFactoryUtil.ge("SAMPTIME", start))
					.add(RestrictionsFactoryUtil.le("SAMPTIME", end));
			if (null != property) {
				query.add(RestrictionsFactoryUtil.isNotNull(property));
			}
			try {
				return VarriometaLocalServiceUtil.dynamicQuery(query);
			} catch (SystemException e) {
				System.err.println("SystemException: "+e);	
			}
		} else { 	
			DynamicQuery query = DynamicQueryFactoryUtil.forClass(entityClass)
					.add(RestrictionsFactoryUtil.ge("samptime", start))
					.add(RestrictionsFactoryUtil.le("samptime", end));
			if (null != property) {
				query.add(RestrictionsFactoryUtil.isNotNull(property));
			}
			try {
				if (entityClass.equals(Kumpulameta.class)) {
					return KumpulametaLocalServiceUtil.dynamicQuery(query);
				} else {		
					return HydemetaLocalServiceUtil.dynamicQuery(query);
				}
			} catch (SystemException e) {
				System.err.println("SystemException: "+e);	
			}
		}
		return null;
	}

	/**
	 * 
	 * @param start Date
	 * @param end Date
	 * @param stations  int VÄRRIÖ = 1; HYYTIÄLÄ = 2; KUMPULA = 4;
	 * @return Timeline vaadin chart addon
	 */
	public Timeline co(Date start, Date end) {
		Timeline timeline = createTimeline(WIDTH, HEIGHT);
		timeline.setCaption("CO₂");
		Float min = 390.0f;
		Float max = 391.0f;
		IndexedContainer containerCO2168 = createIndexedContainer();
		List<Hydemeta> data_h = getDataInRange(Hydemeta.class, start, end, "CO2168");
		if (data_h != null && !data_h.isEmpty()) {
		    for (Hydemeta hydemeta: data_h) {
			float f = hydemeta.getCO2168();
			addItem(containerCO2168, hydemeta.getSamptime(), f);
			min = Math.min(min, f);
			max = Math.max(max, f);
		    }
		    min = (float)Math.floor(min);
		    max = (float)Math.round(max+0.5);
		    timeline.setVerticalAxisRange(min, max); 
		    timeline.addGraphDataSource(containerCO2168, Timeline.PropertyId.TIMESTAMP, Timeline.PropertyId.VALUE);
		    timeline.setGraphCaption(containerCO2168, "Hyytiälä 16m");
		    timeline.setVerticalAxisLegendUnit(containerCO2168, "ppm");
		    timeline.setGraphOutlineColor(containerCO2168, Color.RED);
		}
					// No co2 in kumpula?
			//}
		//if ((stations & Station.KUMPULA) > 0) {
			//IndexedContainer containerCO2 = createIndexedContainer();
			//List<Kumpulameta> data = getDataInRange(Kumpulameta.class, start, end, "p");
			//if (data != null && !data.isEmpty()) {
			//	for (Kumpulameta content: data) {
			//		float f = content.getP();
			//		if (f < min) {
			//			min = f;
			//		}
			//		if (f > max) {
			//			max =  f;
			//		}
			//		addItem(container, content.getSamptime(), f); 
			//	}
			//	timeline.addGraphDataSource(container, Timeline.PropertyId.TIMESTAMP, Timeline.PropertyId.VALUE);
			//	timeline.setGraphCaption(container, "");
			//	timeline.setVerticalAxisLegendUnit(container, "");
			//}
		//}
		//if ((stations & Station.VÄRRIÖ) > 0) {
			IndexedContainer containerCO206 = createIndexedContainer();
			List<Varriometa> data_v = getDataInRange(Varriometa.class, start, end, "CO206");
			if (data_v != null && !data_v.isEmpty()) {
				for (Varriometa content: data_v) {
					float f = content.getCO206();
					//if (f < min) {
					//	min = f;
					//}
					//if (f > max) {
					//	max =  f;
					//}
					addItem(containerCO206, content.getSAMPTIME(), f); 
				}
				timeline.addGraphDataSource(containerCO206, Timeline.PropertyId.TIMESTAMP, Timeline.PropertyId.VALUE);
				timeline.setGraphCaption(containerCO206, "Värriö 0.6m");
				timeline.setGraphOutlineColor(containerCO206, Color.BLUE);
				timeline.setVerticalAxisLegendUnit(containerCO206, "ppm");
			}
		//}
		//timeline.setVerticalAxisRange(min, max);
		return timeline;
	}
	
	public Timeline glob(Date start, Date end, int stations) {
		Timeline timeline = createTimeline(WIDTH, HEIGHT);
		timeline.setCaption("Global shortwave radiation");
		//if ((stations & Station.HYYTIÄLÄ) > 0) {
			IndexedContainer containerGlob = createIndexedContainer();
			IndexedContainer containerglob = createIndexedContainer();
			IndexedContainer containerGLOB = createIndexedContainer();
			List<Hydemeta> data_h = getDataInRange(Hydemeta.class, start, end, "Glob");
			List<Kumpulameta> data_k = getDataInRange(Kumpulameta.class, start, end, "glob");
			List<Varriometa> data_v = getDataInRange(Varriometa.class, start, end, "GLOB");
			//List<Hydemeta> data = getDataInRange(Hydemeta.class, start, end, "Glob");
			if (data_h != null && !data_h.isEmpty()) {
				for (Hydemeta content: data_h) {
					addItem(containerGlob, content.getSamptime(), content.getGlob());
				}
				timeline.addGraphDataSource(containerGlob, Timeline.PropertyId.TIMESTAMP, Timeline.PropertyId.VALUE);
				timeline.setGraphCaption(containerGlob, "Hyytiälä");
				timeline.setGraphOutlineColor(containerGlob, Color.RED);
				timeline.setVerticalAxisLegendUnit(containerGlob, "W/m²");
			}
			if (data_k != null && !data_k.isEmpty()) {
				for (Kumpulameta content: data_k) {
					addItem(containerglob, content.getSamptime(), content.getGlob());
				}
				timeline.addGraphDataSource(containerglob, Timeline.PropertyId.TIMESTAMP, Timeline.PropertyId.VALUE);
				timeline.setGraphCaption(containerglob, "Kumpula");
				timeline.setGraphOutlineColor(containerglob, Color.BLACK);
				timeline.setVerticalAxisLegendUnit(containerglob, "W/m²");
			}
			if (data_v != null && !data_v.isEmpty()) {
				for (Varriometa content: data_v) {
					addItem(containerGLOB, content.getSAMPTIME(), content.getGLOB());
				}
				timeline.addGraphDataSource(containerGLOB, Timeline.PropertyId.TIMESTAMP, Timeline.PropertyId.VALUE);
				timeline.setGraphCaption(containerGLOB, "Värriö");
				timeline.setGraphOutlineColor(containerGLOB, Color.BLUE);
				timeline.setVerticalAxisLegendUnit(containerGLOB, "W/m²");
			}
		//}
		return timeline;
	}
	
	public Timeline RH(Date start, Date end, int stations) {
		Timeline timeline = createTimeline(WIDTH, HEIGHT);
		timeline.setCaption("Relative humidity");
		//if ((stations & Station.HYYTIÄLÄ) > 0) {
			IndexedContainer containerRHIRGA168 = createIndexedContainer();
			IndexedContainer containerRH = createIndexedContainer();
			IndexedContainer containerHUM_RH = createIndexedContainer();
			//List<Hydemeta> data = getDataInRange(Hydemeta.class, start, end, "RHIRGA42");
			List<Hydemeta> data_h = getDataInRange(Hydemeta.class, start, end, "RHIRGA168");
			List<Kumpulameta> data_k = getDataInRange(Kumpulameta.class, start, end, "rh");
			List<Varriometa> data_v = getDataInRange(Varriometa.class, start, end, "HUM_RH");

			if (data_h != null && !data_h.isEmpty()) {
				for (Hydemeta content: data_h) {
					addItem(containerRHIRGA168, content.getSamptime(), content.getRHIRGA168());
				}
				timeline.addGraphDataSource(containerRHIRGA168, Timeline.PropertyId.TIMESTAMP, Timeline.PropertyId.VALUE);
				timeline.setGraphCaption(containerRHIRGA168, "Hyytiälä");
				timeline.setVerticalAxisLegendUnit(containerRHIRGA168, "%");
				timeline.setGraphOutlineColor(containerRHIRGA168, Color.RED);
			}
			if (data_k != null && !data_k.isEmpty()) {
				for (Kumpulameta content: data_k) {
					addItem(containerRH, content.getSamptime(), content.getRh());
				}
				timeline.addGraphDataSource(containerRH, Timeline.PropertyId.TIMESTAMP, Timeline.PropertyId.VALUE);
				timeline.setGraphCaption(containerRH, "Kumpula");
				timeline.setGraphOutlineColor(containerRH, Color.BLACK);
				timeline.setVerticalAxisLegendUnit(containerRH, "%");
			}
			if (data_v != null && !data_v.isEmpty()) {
				for (Varriometa content: data_v) {
					addItem(containerHUM_RH, content.getSAMPTIME(), content.getHUM_RH());
				}
				timeline.addGraphDataSource(containerHUM_RH, Timeline.PropertyId.TIMESTAMP, Timeline.PropertyId.VALUE);
				timeline.setGraphCaption(containerHUM_RH, "Värriö");
				timeline.setGraphOutlineColor(containerHUM_RH, Color.BLUE);
				timeline.setVerticalAxisLegendUnit(containerHUM_RH, "%");
			}
		//}
		return timeline;
	}
	
	public Timeline O3(Date start, Date end, int stations) {
		Timeline timeline = createTimeline(WIDTH, HEIGHT);
		timeline.setCaption("Ozone concentration");
		//if ((stations & Station.HYYTIÄLÄ) > 0) {
			IndexedContainer containerO3168 = createIndexedContainer();
			IndexedContainer containerO_3 = createIndexedContainer();
			IndexedContainer containerO3_0 = createIndexedContainer();
			//List<Hydemeta> data = getDataInRange(Hydemeta.class, start, end, "O3672");
			List<Hydemeta> data_h = getDataInRange(Hydemeta.class, start, end, "O3168");
			List<Kumpulameta> data_k = getDataInRange(Kumpulameta.class, start, end, "O_3");
			List<Varriometa> data_v = getDataInRange(Varriometa.class, start, end, "O3_0");
			if (data_h != null && !data_h.isEmpty()) {
				for (Hydemeta content: data_h) {
					addItem(containerO3168, content.getSamptime(), content.getO3168());
				}
				timeline.addGraphDataSource(containerO3168, Timeline.PropertyId.TIMESTAMP, Timeline.PropertyId.VALUE);
				timeline.setGraphCaption(containerO3168, "Hyytiälä");
				timeline.setGraphOutlineColor(containerO3168, Color.RED);
				timeline.setVerticalAxisLegendUnit(containerO3168, "ppb");
			}
			if (data_k != null && !data_k.isEmpty()) {
				for (Kumpulameta content: data_k) {
					addItem(containerO_3, content.getSamptime(), content.getO_3());
				}
				timeline.addGraphDataSource(containerO_3, Timeline.PropertyId.TIMESTAMP, Timeline.PropertyId.VALUE);
				timeline.setGraphCaption(containerO_3, "Kumpula");
				timeline.setGraphOutlineColor(containerO_3, Color.BLACK);
				timeline.setVerticalAxisLegendUnit(containerO_3, "ppb");
			}
			if (data_v != null && !data_v.isEmpty()) {
				for (Varriometa content: data_v) {
					addItem(containerO3_0, content.getSAMPTIME(), content.getO3_0());
				}
				timeline.addGraphDataSource(containerO3_0, Timeline.PropertyId.TIMESTAMP, Timeline.PropertyId.VALUE);
				timeline.setGraphCaption(containerO3_0, "Värriö");
				timeline.setGraphOutlineColor(containerO3_0, Color.BLUE);
				timeline.setVerticalAxisLegendUnit(containerO3_0, "ppb");
			}
		//}
		return timeline;
	}
	
	/**
	 * DPMS Size Disribution png image and 2 timelines
	 * @param start Date
	 * @param end Date
	 * @param stations ready to future real use
	 * @return HorizontalLayout with Hyytiälä CO₂ SO₂ and Hyytiälä NOx NO timeline and DPMS Size Disribution
	 */
	public HorizontalLayout CSN(Date start, Date end, int stations) {
		HorizontalLayout chartsHL = createVizLayout();
        Timeline tlSO, tlNO, tlNOx;
        tlSO = createTimeline(WIDTH, HEIGHT);
        tlNO = createTimeline(WIDTH, HEIGHT);
        tlNOx = createTimeline(WIDTH,HEIGHT);
        tlSO.setCaption("SO₂ 15-16m");
        tlNO.setCaption("NO 15-16m");
        tlNOx.setCaption("NOx 15-16m");
        //IndexedContainer containerCO2 = createIndexedContainer();
        IndexedContainer containerSO_2 = createIndexedContainer();
        IndexedContainer containerSO2168 = createIndexedContainer();
        IndexedContainer containerSO2_0 = createIndexedContainer();
        IndexedContainer containerNO168 = createIndexedContainer();
        IndexedContainer containerNOx168 = createIndexedContainer();
        IndexedContainer containerNO = createIndexedContainer();
        IndexedContainer containerNO_x = createIndexedContainer();
        IndexedContainer containerNO_0 = createIndexedContainer();
        IndexedContainer containerNOX_0 = createIndexedContainer();
		List<Hydemeta> data_h = getDataInRange(Hydemeta.class, start, end, "SO2168");
		List<Kumpulameta> data_k = getDataInRange(Kumpulameta.class, start, end, "SO_2");
		List<Varriometa> data_v = getDataInRange(Varriometa.class, start, end, "SO2_0");
		if (data_h != null && !data_h.isEmpty()) {
			for (Hydemeta hydemeta: data_h) {
				//addItem(containerCO2, hydemeta.getSamptime(), hydemeta.getCO2672());
				addItem(containerSO2168, hydemeta.getSamptime(), hydemeta.getSO2168());
				addItem(containerNO168, hydemeta.getSamptime(), hydemeta.getNO168());
				addItem(containerNOx168, hydemeta.getSamptime(), hydemeta.getNOx168());
			}
		} else {
			System.err.println("No Hyde data");
		}
		if (data_k != null && !data_k.isEmpty()) {
			for (Kumpulameta kumpulameta: data_k) {
				addItem(containerSO_2, kumpulameta.getSamptime(), kumpulameta.getSO_2());
				addItem(containerNO, kumpulameta.getSamptime(), kumpulameta.getNO());
				addItem(containerNO_x, kumpulameta.getSamptime(), kumpulameta.getNO_x());
			}
		} else {
			System.out.println("No Kumpula data");
		}
		if (data_v != null && !data_v.isEmpty()) {
			for (Varriometa varriometa: data_v) {
				addItem(containerSO2_0, varriometa.getSAMPTIME(), varriometa.getSO2_0());
				addItem(containerNO_0, varriometa.getSAMPTIME(), varriometa.getNO_0());
				addItem(containerNOX_0, varriometa.getSAMPTIME(), varriometa.getNOX_0());
			}
		} else {
			System.out.println("No Värriö data");
		}
		tlSO.addGraphDataSource(containerSO2168, Timeline.PropertyId.TIMESTAMP, Timeline.PropertyId.VALUE);
		tlSO.addGraphDataSource(containerSO_2, Timeline.PropertyId.TIMESTAMP, Timeline.PropertyId.VALUE);
		tlSO.addGraphDataSource(containerSO2_0, Timeline.PropertyId.TIMESTAMP, Timeline.PropertyId.VALUE);
		tlSO.setGraphCaption(containerSO2168, "Hyytiälä");
		tlSO.setGraphCaption(containerSO_2, "Kumpula");
		tlSO.setGraphCaption(containerSO2_0, "Värriö");
		tlSO.setGraphOutlineColor(containerSO2_0, Color.BLUE);
		tlSO.setGraphOutlineColor(containerSO_2, Color.BLACK);
		tlSO.setGraphOutlineColor(containerSO2168, Color.RED);
			
			//tlCO2.addGraphDataSource(containerCO2, Timeline.PropertyId.TIMESTAMP, Timeline.PropertyId.VALUE);
			tlNO.addGraphDataSource(containerNO168, Timeline.PropertyId.TIMESTAMP, Timeline.PropertyId.VALUE);
			tlNOx.addGraphDataSource(containerNOx168, Timeline.PropertyId.TIMESTAMP, Timeline.PropertyId.VALUE);
			tlNO.addGraphDataSource(containerNO, Timeline.PropertyId.TIMESTAMP, Timeline.PropertyId.VALUE);
			tlNOx.addGraphDataSource(containerNO_x, Timeline.PropertyId.TIMESTAMP, Timeline.PropertyId.VALUE);
			tlNO.addGraphDataSource(containerNO_0, Timeline.PropertyId.TIMESTAMP, Timeline.PropertyId.VALUE);
			tlNOx.addGraphDataSource(containerNOX_0, Timeline.PropertyId.TIMESTAMP, Timeline.PropertyId.VALUE);
			//tlCO2.setGraphCaption(containerCO2, "CO₂");
			tlSO.setGraphCaption(containerSO2168, "Hyytiälä");
			tlSO.setGraphCaption(containerSO_2, "Kumpula");
			tlSO.setGraphCaption(containerSO2_0, "Värriö");
			tlNO.setGraphCaption(containerNO168, "Hyytiälä");
			tlNOx.setGraphCaption(containerNOx168, "Hyytiälä");
			tlNO.setGraphCaption(containerNO, "Kumpula");
			tlNOx.setGraphCaption(containerNO_x, "Kumpula");
			tlNO.setGraphCaption(containerNO_0, "Värriö");
			tlNOx.setGraphCaption(containerNOX_0, "Värriö");
			//tlSO.setVerticalAxisLegendUnit(containerCO2, "ppm");
			tlSO.setVerticalAxisLegendUnit(containerSO2168, "ppb");
			tlSO.setVerticalAxisLegendUnit(containerSO_2, "ppb");
			tlSO.setVerticalAxisLegendUnit(containerSO2_0, "ppb");
			tlNO.setVerticalAxisLegendUnit(containerNO168, "ppb");
			tlNOx.setVerticalAxisLegendUnit(containerNOx168, "ppb");
			tlNO.setVerticalAxisLegendUnit(containerNO, "ppb");
			tlNOx.setVerticalAxisLegendUnit(containerNO_x, "ppb");
			tlNO.setVerticalAxisLegendUnit(containerNO_0, "ppb");
			tlNOx.setVerticalAxisLegendUnit(containerNOX_0, "ppb");
			tlNO.setGraphOutlineColor(containerNO168, Color.RED);
			tlNOx.setGraphOutlineColor(containerNOx168, Color.RED);
			tlNO.setGraphOutlineColor(containerNO, Color.BLACK);
			tlNOx.setGraphOutlineColor(containerNO_x, Color.BLACK);
			tlNO.setGraphOutlineColor(containerNO_0, Color.BLUE);
			tlNOx.setGraphOutlineColor(containerNOX_0, Color.BLUE);
			tlSO.setVerticalAxisRange(0f, 1f);
			//tlCO2.setGraphOutlineColor(containerCO2, Color.RED);
			//tlCO2.setCaption("Hyytiälä CO₂ SO₂");
			//tlNO.setCaption("Hyytiälä NOx NO");
			chartsHL.addComponent(tlSO);
			chartsHL.addComponent(tlNO);
			chartsHL.addComponent(tlNOx);
		//} else {
		//	System.err.print("No SO2 data during"+start);
		//	System.err.println("-"+end);
		//}
		//DPMS dpms = new DPMS(this);
		//Image dpmsviz = new Image("Size Disribution [m]", dpms.getHyde(start, end));
		//chartsHL.addComponent(dpmsviz);		
		return chartsHL;
	}

	HorizontalLayout[] plotVariables(Set<String> set, Tree t, HierarchicalContainer treecontainer,  Download dl) {
		int ch = 0;
		String stationstr = "";
		HorizontalLayout[] rows = new HorizontalLayout[ROWS];
		for (int i = 0; i < ROWS; i++) {
			rows[i]= createVizLayout();
		}
		/*if (null == dl ) 
		loop:{
			for (String variabsta:set){
				String[] parts = variabsta.split(":");
				String variab = parts[0];
				Item v = treecontainer.getItem(variabsta);
				Object u = null;
				try{
					u = v.getItemProperty(Station.UNIT).getValue();
				} catch(java.lang.NullPointerException enu) {
					System.err.println("Null pointer Exception");
				}
					stationstr = (String)t.getParent((String)t.getParent(variabsta));
					System.out.println("Station "+stationstr);
				if (ch < 3){
					rows[0].addComponent(plotVariable(variab,stationstr, String.valueOf(u)));
				}
				if (ch > 2 && ch < 6){
					rows[1].addComponent(plotVariable(variab,stationstr, String.valueOf(u)));
				}
				if (ch > 5 && ch < 9){
					rows[2].addComponent(plotVariable(variab,stationstr, String.valueOf(u)));
				}
				if (ch > 8) {
					new Notification("Plotting only " +MAXNOVIZ+ " first variables",
							"<br/>", Notification.TYPE_WARNING_MESSAGE, true)
					.show(Page.getCurrent());
					break loop;
				}
				ch++;
			}
			if (ch == 1){
				rows[0].getComponent(0).setWidth("600");
				rows[0].getComponent(0).setHeight("450");
			}
		} else { //loop */
		
			// now we have dl and there is the data too
			ResultSet data = dl.getResultSet();
			try{
			try {
				ResultSetMetaData rsmd = data.getMetaData();
				int noc = rsmd.getColumnCount(); //no of columns
				int rc = noc > MAXNOVIZ ? MAXNOVIZ + 1 : noc + 1;  //real colums 
				IndexedContainer[] ica = new IndexedContainer[rc];
				for (int i = 0; i < rc; i++) {
					ica[i] = createIndexedContainer();
				}
				//rsmd.getColumnType(column);
				synchronized(dl.data) { 
					data.beforeFirst();
					while (data.next()) {
						for (int i = 2; i < rc; i++) {
						    try {
							float f = data.getFloat(i);
							if (!data.wasNull()){
								addItem(ica[i-2], data.getTimestamp(1), f);
							}
						    } catch ( java.sql.SQLException e) {
							if (e.toString().startsWith("Invalid value for getFloat() - 'NUL'")) 
							    System.err.println(e.toString());
							else
							    e.printStackTrace();
						    }
						}
					}
				}
				Timeline[] tla = new Timeline[rc];
				for (int i = 0; i < rc-2; i++) {
					tla[i] = createTimeline(WIDTH, HEIGHT);
					// vrealname = muuttujan nimi. Puun alkiot ovat muuttuja:taulu muotoa
					String variab = rsmd.getColumnLabel(i+2);
					String vrealname = clean(variab);
					String stati = "";
					Item v = null;
					for (String variabsta:set){
						String[] parts = variabsta.split(":");
						String vartree = parts[0];
						if (vartree.equals(vrealname)){
							v = treecontainer.getItem(variabsta);
							stati = parts[1].substring(0,3);
						}
					}
					Object u = null;
					try {
					  u = v.getItemProperty(Station.UNIT).getValue();
					} catch (java.lang.NullPointerException e) {
						System.err.println("Null pointer Exception " + variab);
					}
					//stationstr = (String)t.getParent((String)t.getParent(vrealname));
					//tla[i].setCaption(stationstr + "  " + vrealname); //variable
					tla[i].setCaption(vrealname+":"+stati);
					tla[i].addGraphDataSource(ica[i], Timeline.PropertyId.TIMESTAMP, Timeline.PropertyId.VALUE);
					tla[i].setVerticalAxisLegendUnit(ica[i], String.valueOf(u));
					tla[i].setGraphCaption(ica[i], variab);
					tla[i].setGraphOutlineColor(ica[i], Color.RED);
					rows[i/VIZPERROW].addComponent(tla[i]);
					if (rc-2 == 1){
						rows[0].getComponent(0).setWidth("600");
						rows[0].getComponent(0).setHeight("450");
					}
					if (rc-2 == 2){
						rows[0].getComponent(0).setWidth("280");
						rows[0].getComponent(0).setHeight("220");
					}
				}
				if (rc-2 == 2){
					rows[0].getComponent(0).setWidth("280");
					rows[0].getComponent(0).setHeight("220");
					rows[0].getComponent(1).setWidth("280");
					rows[0].getComponent(1).setHeight("220");
				}
			} catch ( java.sql.SQLException e) {
				e.printStackTrace();
			} catch (java.lang.NullPointerException e) {
				if (null == data) {
					System.err.println("Null pointer Exception because data was null");
				} else {
					System.err.println("data: "+ data);
				}
			}
			} catch (java.lang.NullPointerException exx) {
					System.err.println("Null pointer Exception");
			}
		// } //else no dl
		return rows;
	}

	private String clean(String variab) {
		if (variab.startsWith("avg(") || variab.startsWith("sum(")) {
			return variab.substring(4, variab.length()-1);			
		} else if (variab.startsWith(DB.GEOMETRIC)) {
			//System.out.println(variab.substring(DB.GEOMETRIC.length(), variab.length()-3));
			return variab.substring(DB.GEOMETRIC.length(), variab.length()-3);
		}
		return variab;
	}

    /*	public Timeline plotVariable(String variable, String stationstr, String unit){ 
		
	    Timeline variabletimeline = createTimeline(WIDTH, HEIGHT);
	    variabletimeline.setCaption(stationstr + "  " + variable);
	    IndexedContainer container_variable = createIndexedContainer();
	    String datatype = "float";
	    Float floatval = null;
	    Integer intval = null;
	    Method m = null;
	    //String wantedmethod = "";
	    if (stationstr.equals(Station.Hyytiälä)){
	        	List<Hydemeta> data = getDataInRange(Hydemeta.class, start, end, variable);
	        	//Method[] methods = Hydemeta.getClass().getMethods();
	        	String met = Character.toUpperCase(variable.charAt(0)) + variable.substring(1);
	        	
	        	if (data != null && !data.isEmpty()) {
					for (Hydemeta content: data) {
						try{				
						try{
							//System.out.println("Hydestä "+variable);
						//	for (String str:methods){
						//		if (str.getName().toUpperCase().equals(("get"+variable).toUpperCase())){
						//			wantedmethod = str;
						//		}
						//	}
							m = content.getClass().getMethod(("get" + met));
							m.setAccessible(true);
						}catch (NoSuchMethodException x) {
						    x.printStackTrace();
						}
						try {
							if (datatype == "float"){
								try{
									floatval = (Float)m.invoke(content);
								} catch (ClassCastException x) {
									datatype = "int";
									try{
										intval = (Integer)m.invoke(content);
									} catch (ClassCastException xs) {
										System.out.println("A string");
										datatype = "string";
									}
								}
							} else {
								if (datatype == "int"){
									try{
									    intval = (Integer)m.invoke(content);
										} catch (ClassCastException x) {
											datatype = "float";
											try{
												floatval = (Float)m.invoke(content);
											} catch (ClassCastException xs) {
												System.out.println("A string");
												datatype = "string";
											}
										}
								} //if
							} //else
						} catch (InvocationTargetException x) {
						    x.printStackTrace();
						} 
					//	System.out.println("Yritetään metodia: "+wantedmethod.getName());
						if (datatype == "float"){
							addItem(container_variable, content.getSamptime(), floatval.floatValue());
						}
						if (datatype == "int") {
							addItem(container_variable, content.getSamptime(), intval.intValue());
						}
						if (datatype == "string"){
							//System.out.println("Will not plot a string");
						}
						} catch (IllegalAccessException x){
							x.printStackTrace();
						}
					}
	        	}
	        }
	    if (stationstr.equals("Kumpula")){
	        	List<Kumpulameta> data = getDataInRange(Kumpulameta.class, start, end, variable);
	        	//Method[] methods = Kumpulameta.getClass().getMethods();
	        	String met = Character.toUpperCase(variable.charAt(0)) + variable.substring(1);
	        	
	        	if (data != null && !data.isEmpty()) {
					for (Kumpulameta content: data) {
						try{
						try{
							m = content.getClass().getMethod("get" + met);
							m.setAccessible(true);
						}catch (NoSuchMethodException x) {
							x.printStackTrace();
						}
						try {
							if (datatype == "float"){
								try{
									floatval = (Float)m.invoke(content);
								} catch (ClassCastException x) {
									datatype = "int";
									try{
										intval = (Integer)m.invoke(content);
									} catch (ClassCastException xs) {
										System.out.println("A string");
										datatype = "string";
									}
								}
							} else {
								if (datatype == "int"){
									try{
									    intval = (Integer)m.invoke(content);
										} catch (ClassCastException x) {
											datatype = "float";
											try{
												floatval = (Float)m.invoke(content);
											} catch (ClassCastException xs) {
												System.out.println("A string");
												datatype = "string";
											}
										}
								} //if
							} //else
						} catch (InvocationTargetException x) {
						    x.printStackTrace();
						} 
						
						//System.out.println("Kumpulasta "+variable);
						//for (Method str:methods){
						//	if (str.getName().toUpperCase().equals(("get"+variable).toUpperCase())){
						//		wantedmethod = str.getName();
						//	}
						//}
						//System.out.println("Yritetään metodia: "+wantedmethod);
						if (datatype == "float"){
							addItem(container_variable, content.getSamptime(), floatval.floatValue());
						}
						if (datatype == "int") {
							addItem(container_variable, content.getSamptime(), intval.intValue());
						}
						if (datatype == "string"){
							//System.out.println("Will not plot a string");
						}
						}catch (IllegalAccessException x) {
							x.printStackTrace();
						}
					}
	        	}
	        }
	    if (stationstr.equals("Värriö")){
	        	List<Varriometa> data = getDataInRange(Varriometa.class, start, end, variable);
	        	//Method[] methods = Varriometa.getClass().getMethods();
	        	String met = Character.toUpperCase(variable.charAt(0)) + variable.substring(1);
	        	
	        	if (data != null && !data.isEmpty()) {
					for (Varriometa content: data) {
						try{
						try{
							m = content.getClass().getMethod("get" + met);
							m.setAccessible(true);
						} catch (NoSuchMethodException x) {
						    x.printStackTrace();
						}
						try {
							if (datatype == "float"){
								try{
									floatval = (Float)m.invoke(content);
								} catch (ClassCastException x) {
									datatype = "int";
									try{
										intval = (Integer)m.invoke(content);
									} catch (ClassCastException xs) {
										System.out.println("A string");
										datatype = "string";
									}	
								}
							} else {
								if (datatype == "int"){
									try{
									    intval = (Integer)m.invoke(content);
										} catch (ClassCastException x) {
											datatype = "float";
											try{
												floatval = (Float)m.invoke(content);
											} catch (ClassCastException xs) {
												System.out.println("A string");
												datatype = "string";
											}
										}
								} //if
							} //else
						} catch (InvocationTargetException x) {
						    x.printStackTrace();
						} 
						
						//System.out.println("Värriöstä "+variable);
						//for (Method str:methods){
						//	if (str.getName().toUpperCase().equals(("get"+variable).toUpperCase())){
						//		wantedmethod = str.getName();
						//	}
						//}
						//System.out.println("Yritetään metodia: "+wantedmethod);
						if (datatype == "float"){
							addItem(container_variable, content.getSAMPTIME(), floatval.floatValue());
						}
						if (datatype == "int") {
							addItem(container_variable, content.getSAMPTIME(), intval.intValue());
						}
						if (datatype == "string"){
							//System.out.println("Will not plot a string");
						}
						} catch (IllegalAccessException x) {
							x.printStackTrace();
						}
					}
	        	}
	        }
	        variabletimeline.addGraphDataSource(container_variable, Timeline.PropertyId.TIMESTAMP, Timeline.PropertyId.VALUE);
	        variabletimeline.setGraphCaption(container_variable, variable);
	        variabletimeline.setVerticalAxisLegendUnit(container_variable, unit);
	        variabletimeline.setGraphOutlineColor(container_variable, Color.RED);	
	        return variabletimeline;	
		} */ //findbug
 
	/**
	 * HorizontalLayout with Margin and Spacing
	 * @return new HorizontalLayout 
	 */
	public HorizontalLayout createVizLayout() {
		HorizontalLayout chartsHL = new HorizontalLayout();
		chartsHL.setMargin(true);
		chartsHL.setSpacing(true);
		return chartsHL;		
	}
}
