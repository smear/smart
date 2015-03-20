/**
 * 
 */
package fi.csc.avaa.smear.smartsmear;

import java.sql.Timestamp;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
//import java.lang.reflect.Method;
//import java.lang.reflect.InvocationTargetException;




import com.liferay.portal.kernel.dao.orm.DynamicQuery;
import com.liferay.portal.kernel.dao.orm.DynamicQueryFactoryUtil;
import com.liferay.portal.kernel.dao.orm.RestrictionsFactoryUtil;
import com.liferay.portal.kernel.exception.SystemException;
import com.vaadin.addon.timeline.Timeline;
import com.vaadin.data.Item;
import com.vaadin.data.util.HierarchicalContainer;
import com.vaadin.data.util.IndexedContainer;
//import com.vaadin.server.Page;
//import com.vaadin.server.Sizeable.Unit;
//import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Image;
//import com.vaadin.ui.Notification;
import com.vaadin.ui.Tree;
import com.vaadin.addon.charts.model.style.SolidColor;

import fi.csc.smear.db.model.Hydemeta;
import fi.csc.smear.db.model.Kumpulameta;
import fi.csc.smear.db.model.Varriometa;
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
		timeline.setGraphOutlineColor(containerWS0, SolidColor.BLUE);
		timeline.setGraphOutlineColor(containerTower_WS_16m, SolidColor.BLACK);
		timeline.setGraphOutlineColor(containerWSU168, SolidColor.RED);
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
			timeline.setGraphOutlineColor(containerTDRY0, SolidColor.BLUE);
			timeline.setGraphOutlineColor(containerTower_T_16m, SolidColor.BLACK);
			timeline.setGraphOutlineColor(containerT168, SolidColor.RED);
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
			if (null == samptime) {
				//no debugging now
			} else {
				System.err.println(samptime + "was null: kesäaika");
			}
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
		    timeline.setGraphOutlineColor(containerCO2168, SolidColor.RED);
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
				timeline.setGraphOutlineColor(containerCO206, SolidColor.BLUE);
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
				timeline.setGraphOutlineColor(containerGlob, SolidColor.RED);
				timeline.setVerticalAxisLegendUnit(containerGlob, "W/m²");
			}
			if (data_k != null && !data_k.isEmpty()) {
				for (Kumpulameta content: data_k) {
					addItem(containerglob, content.getSamptime(), content.getGlob());
				}
				timeline.addGraphDataSource(containerglob, Timeline.PropertyId.TIMESTAMP, Timeline.PropertyId.VALUE);
				timeline.setGraphCaption(containerglob, "Kumpula");
				timeline.setGraphOutlineColor(containerglob, SolidColor.BLACK);
				timeline.setVerticalAxisLegendUnit(containerglob, "W/m²");
			}
			if (data_v != null && !data_v.isEmpty()) {
				for (Varriometa content: data_v) {
					addItem(containerGLOB, content.getSAMPTIME(), content.getGLOB());
				}
				timeline.addGraphDataSource(containerGLOB, Timeline.PropertyId.TIMESTAMP, Timeline.PropertyId.VALUE);
				timeline.setGraphCaption(containerGLOB, "Värriö");
				timeline.setGraphOutlineColor(containerGLOB, SolidColor.BLUE);
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
				timeline.setGraphOutlineColor(containerRHIRGA168, SolidColor.RED);
			}
			if (data_k != null && !data_k.isEmpty()) {
				for (Kumpulameta content: data_k) {
					addItem(containerRH, content.getSamptime(), content.getRh());
				}
				timeline.addGraphDataSource(containerRH, Timeline.PropertyId.TIMESTAMP, Timeline.PropertyId.VALUE);
				timeline.setGraphCaption(containerRH, "Kumpula");
				timeline.setGraphOutlineColor(containerRH, SolidColor.BLACK);
				timeline.setVerticalAxisLegendUnit(containerRH, "%");
			}
			if (data_v != null && !data_v.isEmpty()) {
				for (Varriometa content: data_v) {
					addItem(containerHUM_RH, content.getSAMPTIME(), content.getHUM_RH());
				}
				timeline.addGraphDataSource(containerHUM_RH, Timeline.PropertyId.TIMESTAMP, Timeline.PropertyId.VALUE);
				timeline.setGraphCaption(containerHUM_RH, "Värriö");
				timeline.setGraphOutlineColor(containerHUM_RH, SolidColor.BLUE);
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
				timeline.setGraphOutlineColor(containerO3168, SolidColor.RED);
				timeline.setVerticalAxisLegendUnit(containerO3168, "ppb");
			}
			if (data_k != null && !data_k.isEmpty()) {
				for (Kumpulameta content: data_k) {
					addItem(containerO_3, content.getSamptime(), content.getO_3());
				}
				timeline.addGraphDataSource(containerO_3, Timeline.PropertyId.TIMESTAMP, Timeline.PropertyId.VALUE);
				timeline.setGraphCaption(containerO_3, "Kumpula");
				timeline.setGraphOutlineColor(containerO_3, SolidColor.BLACK);
				timeline.setVerticalAxisLegendUnit(containerO_3, "ppb");
			}
			if (data_v != null && !data_v.isEmpty()) {
				for (Varriometa content: data_v) {
					addItem(containerO3_0, content.getSAMPTIME(), content.getO3_0());
				}
				timeline.addGraphDataSource(containerO3_0, Timeline.PropertyId.TIMESTAMP, Timeline.PropertyId.VALUE);
				timeline.setGraphCaption(containerO3_0, "Värriö");
				timeline.setGraphOutlineColor(containerO3_0, SolidColor.BLUE);
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
		tlSO.setGraphOutlineColor(containerSO2_0, SolidColor.BLUE);
		tlSO.setGraphOutlineColor(containerSO_2, SolidColor.BLACK);
		tlSO.setGraphOutlineColor(containerSO2168, SolidColor.RED);
			
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
			tlNO.setGraphOutlineColor(containerNO168, SolidColor.RED);
			tlNOx.setGraphOutlineColor(containerNOx168, SolidColor.RED);
			tlNO.setGraphOutlineColor(containerNO, SolidColor.BLACK);
			tlNOx.setGraphOutlineColor(containerNO_x, SolidColor.BLACK);
			tlNO.setGraphOutlineColor(containerNO_0, SolidColor.BLUE);
			tlNOx.setGraphOutlineColor(containerNOX_0, SolidColor.BLUE);
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
		HorizontalLayout[] rows = new HorizontalLayout[ROWS];
		for (int i = 0; i < ROWS; i++) {
			rows[i]= createVizLayout();
		}
		// now we have dl and there is the data too
		Data data = dl.getData();
		try{	
			int noc = data.getColumnCount(); //no of columns
			int rc = noc > MAXNOVIZ ? MAXNOVIZ : noc ;  //real colums 
			IndexedContainer[] ica = new IndexedContainer[rc];
			for (int i = 0; i < rc; i++) {
				ica[i] = createIndexedContainer();
			}
			String labels[] = new String[rc];
			int l = 0; // lkm laskuri koti rc:tä
			Iterator<String> iter = data.tableset.iterator();
			while( iter.hasNext() && (l < rc)) {
				String taulunnimi = iter.next();
				float[][] faa = data.getFtaulu(taulunnimi);
				Timestamp[] timestamps = data.getTimestamps();
				int sarakkeet = faa.length;
				int rivit = faa[0].length;
				int ehto = sarakkeet < rc-l ?  sarakkeet : rc-l;
				String Columnnames[] = data.getLabels(taulunnimi);
				if (null == Columnnames) {
					System.out.println("Ohitetaan ajanjaksolla tyhjä taulu: "+taulunnimi);
				} else {
					for (int i = 0; i < ehto ; i++) {
						try {
							labels[l+i] = Columnnames[i];
							for (int j = 0; j < rivit; j++) {
								addItem(ica[i], timestamps[j], faa[i][j]);
							}	
						} catch (  java.lang.ArrayIndexOutOfBoundsException e) {
							System.err.println("ArrayIndexOutOfBoundsException: "+e.getMessage());
							System.err.println("l="+l+", i="+i);
						}
					}
					l = l + ehto;
				}
			}
			Timeline[] tla = new Timeline[rc];
			for (int i = 0; i < rc; i++) {
				tla[i] = createTimeline(WIDTH, HEIGHT);
				// vrealname = muuttujan nimi. Puun alkiot ovat muuttuja:taulu muotoa
				String variab = labels[i];
				String vrealname = data.clean(variab);
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
				tla[i].setGraphOutlineColor(ica[i], SolidColor.RED);
				rows[i/VIZPERROW].addComponent(tla[i]);
				if (rc == 1){
					rows[0].getComponent(0).setWidth("600");
					rows[0].getComponent(0).setHeight("450");
				}
				if (rc == 2){
					rows[0].getComponent(0).setWidth("280");
					rows[0].getComponent(0).setHeight("220");
				}
			}
			if (rc == 2){
				rows[0].getComponent(0).setWidth("280");
				rows[0].getComponent(0).setHeight("220");
				rows[0].getComponent(1).setWidth("280");
				rows[0].getComponent(1).setHeight("220");
			}

		} catch (java.lang.NullPointerException e) {
			if (null == data) {
				System.err.println("Null pointer Exception because data was null");
			} else {
				System.err.println("datan rivit: "+ data.getRivienlkm());
				e.printStackTrace();
			}
		} catch (  java.lang.ArrayIndexOutOfBoundsException e) {
			System.err.println("ArrayIndexOutOfBoundsException: ");
			e.printStackTrace();
		}
	// } //else no dl
	return rows;
}

 
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
