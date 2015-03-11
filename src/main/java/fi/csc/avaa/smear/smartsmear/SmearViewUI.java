package fi.csc.avaa.smear.smartsmear;

//import java.util.Hashtable;
//import java.awt.Color;
//import java.io.ByteArrayInputStream;
import java.io.File;
//import java.io.InputStream;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;
import java.util.ArrayList;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.text.Format;
import java.util.Set;
/*import java.io.OutputStream;
import java.io.InputStream;
import java.io.IOException;
import java.net.Socket;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.UnknownHostException;

import com.liferay.portal.kernel.dao.orm.DynamicQuery;
import com.liferay.portal.kernel.dao.orm.DynamicQueryFactoryUtil;
import com.liferay.portal.kernel.dao.orm.RestrictionsFactoryUtil;
import com.liferay.portal.kernel.exception.SystemException;
import com.liferay.portal.kernel.util.StringPool; */
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.util.JavaConstants;
import com.vaadin.data.util.ObjectProperty;
import com.vaadin.data.util.PropertysetItem;
//import com.vaadin.ui.AbstractSelect.ItemCaptionMode;
import com.vaadin.ui.AbstractSelect;
/*import com.vaadin.addon.charts.Chart;
import com.vaadin.addon.charts.model.ChartType;
import com.vaadin.addon.charts.model.Configuration;
import com.vaadin.addon.charts.model.DataSeries;
import com.vaadin.addon.charts.model.DataSeriesItem;
import com.vaadin.addon.charts.model.YAxis;*/
import com.vaadin.addon.ipcforliferay.LiferayIPC;
/*import com.vaadin.addon.timeline.Timeline;
import com.vaadin.addon.timeline.Timeline.EventButtonClickEvent;
import com.vaadin.addon.timeline.Timeline.EventClickListener;*/
import com.vaadin.annotations.Theme;
import com.vaadin.annotations.Title;
import com.vaadin.annotations.VaadinServletConfiguration;
//import com.vaadin.annotations.Widgetset;
import com.vaadin.data.Item;
//import com.vaadin.data.util.IndexedContainer;
import com.vaadin.server.FileDownloader;
//import com.vaadin.server.StreamResource;
import com.vaadin.server.VaadinServlet;
//import com.vaadin.server.StreamResource.StreamSource;
import com.vaadin.server.VaadinRequest;
import com.vaadin.server.ThemeResource;
import com.vaadin.shared.ui.datefield.Resolution;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
//import com.vaadin.ui.CheckBox;
import com.vaadin.ui.HorizontalLayout;
//import com.vaadin.ui.HorizontalSplitPanel;
import com.vaadin.ui.Label;
import com.vaadin.ui.PopupDateField;
/*import com.vaadin.ui.TabSheet;
import com.vaadin.ui.Table;*/
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Tree;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.NativeSelect;
import com.vaadin.ui.Field;
import com.vaadin.data.validator.DateRangeValidator;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.data.Property.ValueChangeEvent;
//import com.vaadin.server.UserError;
import com.vaadin.ui.Notification;
import com.vaadin.server.Page;
import com.vaadin.ui.Alignment;
import com.vaadin.shared.ui.label.ContentMode;
//import com.vaadin.data.Item;
import com.vaadin.ui.AbstractSelect.ItemDescriptionGenerator;
import com.vaadin.ui.Component;
//import com.vaadin.data.Container;
import com.vaadin.data.util.HierarchicalContainer;
import com.vaadin.ui.TextField;
import com.vaadin.ui.Window;
import com.vaadin.ui.Window.CloseEvent;
import com.vaadin.event.ItemClickEvent;

/*import fi.csc.smear.db.model.Hydemeta;
import fi.csc.smear.db.service.HydemetaLocalServiceUtil;
import fi.csc.smear.db.model.Towermeta;
import fi.csc.smear.db.model.Hyde_eddy233;
import fi.csc.smear.db.model.Hyde_eddytow;
import fi.csc.smear.db.model.Varrio_tree;
import fi.csc.smear.db.service.TowermetaLocalServiceUtil;
import fi.csc.smear.db.model.SmearTableMetadata;*/
import fi.csc.smear.db.model.SmearVariableMetadata;
/*import fi.csc.smear.db.service.SmearTableMetadataLocalServiceUtil;
import fi.csc.smear.db.service.SmearVariableMetadataLocalServiceUtil;*/

import javax.portlet.PortletPreferences;
import javax.portlet.PortletRequest;
import javax.servlet.annotation.WebServlet;

import com.vaadin.data.Validator.InvalidValueException;
import com.vaadin.ui.Notification.Type;



/**
 * The Application's "main" class
 * 
 * As a Vaadin UI the init method is the main program of the portlet
 */
@SuppressWarnings("serial")
@Title("SmartSMEAR")
@Theme("liferay")
public class SmearViewUI extends UI {
	
	@WebServlet(value = "/VAADIN/*", asyncSupported = true)
    @VaadinServletConfiguration(productionMode = true, ui = SmearViewUI.class)
    public static class Servlet extends VaadinServlet {
    }
	
    private static final long serialVersionUID = 8532668582109303543L;
    private static Log log = LogFactoryUtil.getLog(SmearViewUI.class);
    public static final String CITE = "Cite: Junninen, H; Lauri, A; Keronen, P; Aalto, P; Hiltunen, V; Hari, P; Kulmala, M."+
	"Smart-SMEAR: on-line data exploration and visualization tool for SMEAR stations. BOREAL ENVIRONMENT RESEARCH (BER) Vol 14, Issue 4, pp.447-457";
    //private Vizualisation viz = new Vizualisation();
    private final DB db = new DB();
    private static final String LABELSTART = "<a href=\"/tmp/";
    private static final String LABELEND = "\">HDF5<a>";
    public final HierarchicalContainer treecontainer = new HierarchicalContainer();
    public static final String DESCRIPTION = "description";
    public static final String TITLE = "title";
    public static final String UNIT = "unit";
    public static final String SOURCE = "source";
    public static final String AVAILABLE = "available";
    public static final String CONTACT_EMAIL = "";
    public static final String CHECKED = "CHECKED";
    public static final String WIDTH = "130px";
    public static final String SMALLBUTTONWIDTH = "30px";
    public static List<SmearVariableMetadata> vmdata;
    public String contact_email = "Someone";
    //private Hashtable<String, Integer> tablestation = new Hashtable<String, Integer>();
    //private static final Date START = new Date(91,5,1);
    public boolean nohuman = false;
    public FileDownloader fd = null;
    public static int maxTimeWindow = 183;
    private static final String MAX_SCOPE = "maxScope";
    private HorizontalLayout[] oldrows = new HorizontalLayout[Vizualisation.ROWS]; //midselect visualization
    private static Date getModifiedDate(Date currentdate, int modifier) {
    	Calendar calendar = new GregorianCalendar();
    	calendar.setTime(currentdate);
    	if (modifier == 30){
	    calendar.add(Calendar.MONTH, 1);
    	}
    	if (modifier == -30) {
	    calendar.add(Calendar.MONTH, -1);
    	}
    	if (modifier != 30 && modifier != -30){
	    calendar.add(Calendar.DAY_OF_MONTH, modifier);
    	}
    	return calendar.getTime();
    }
    
    @Override
    protected void init(VaadinRequest request) {
	final PortletPreferences portletPreferences = ((PortletRequest) request.getAttribute(JavaConstants.JAVAX_PORTLET_REQUEST)).getPreferences();
	//main layout 
	final HorizontalLayout layout = new HorizontalLayout();
	layout.setMargin(true);
	setContent(layout);
	// leftside stationselection and tree
	final VerticalLayout variableselection = new VerticalLayout();
	variableselection.setMargin(true);
	final VerticalLayout middlesection = new VerticalLayout();
	middlesection.setMargin(true);
        //stationvariableselection.setMargin(true);
	final HorizontalLayout dayselection = new HorizontalLayout();
	//final Station stations = new Station(variableselection); //findbug 
        //variableselection.addComponent(stations.getStationselection());
	maxTimeWindow = Integer.parseInt(portletPreferences.getValue(MAX_SCOPE, ""));
	// Components
	final PopupDateField enddate = new PopupDateField("To:");
	final PopupDateField startdate = new PopupDateField("From:");
	Button prevbutton = new Button("<<");
	final NativeSelect timewindow = new NativeSelect("Shift:");
	Button nextbutton = new Button(">>");
	final ComboBox quality = new ComboBox("Quality Level:");
	final ComboBox aheight = new ComboBox("Arrival Height:");
	final ComboBox avaraging = new ComboBox("Averaging:");
	final ComboBox typeavaraging = new ComboBox("Averaging Type:");
	final Tree tree = new Tree("Variables:");
	final Button b = new Button("Download CSV");
		
		
	tree.setMultiSelect(true);
	tree.setNullSelectionAllowed(false);
	tree.setImmediate(true);
	tree.addItemClickListener(new ItemClickEvent.ItemClickListener() {

		public void itemClick(ItemClickEvent event) {
		    tree.expandItem(event.getItemId());
		    //tree.requestRepaint();
		    tree.markAsDirty();
		}
	    });
		
	for (int i = 0; i < Station.ASEMAT.length; i++){
	    Item asema = treecontainer.addItem(Station.ASEMAT[i]);
	    //System.out.println("Luotiin asema "+ Station.ASEMAT[i]);
	    for (int j = 0; j < Station.TAULUT[i].length; j++){
		String tablename = Station.TAULUT[i][j];
		//System.out.println("Tablename "+tablename);
		List<SmearVariableMetadata> mdata = null;
		try{
		    mdata = Station.getMetadataInStation(tablename);
		} catch(Exception mdataexc){
		    log.error("Exception in retrieving tablemetadata: "+tablename);
		    mdataexc.printStackTrace();		    
		}
		if (null == mdata || mdata.isEmpty()){
		    System.out.println("Could not retrieve metadata for station");
		}
		//System.out.println("Haettiin meta " +META[i]);
		if (j==0 && i==0){
		    vmdata=mdata;
		} else {
		    //read-only list must be looped
		    List<SmearVariableMetadata> mdata2 = new ArrayList<SmearVariableMetadata>(vmdata);
		    mdata2.addAll(mdata);
		    vmdata = mdata2;	
		}
				
		if (mdata != null && !mdata.isEmpty()) {
		    treecontainer.addContainerProperty(DESCRIPTION,String.class, "");
		    treecontainer.addContainerProperty(TITLE,String.class, "");
		    treecontainer.addContainerProperty(UNIT, String.class, "");
		    treecontainer.addContainerProperty(SOURCE, String.class, "");
		    treecontainer.addContainerProperty(AVAILABLE, String.class, "");
		    tree.setItemCaptionPropertyId(TITLE);
		    tree.setItemCaptionMode( AbstractSelect.ItemCaptionMode.PROPERTY );

		    /* Vaikeuksia, ajonaikanen null-poikkeus
		     */
		    try {
		    	asema.getItemProperty(DESCRIPTION).setValue(" ");
		    	asema.getItemProperty(TITLE).setValue(Station.ASEMAT[i]);
		    	asema.getItemProperty(UNIT).setValue(" ");
		    	asema.getItemProperty(SOURCE).setValue(" ");
		    	asema.getItemProperty(AVAILABLE).setValue(" ");						
		    } catch (Exception e) {
		    	asema = new PropertysetItem();
		    	asema.addItemProperty(DESCRIPTION, new ObjectProperty<String>(" ", String.class));
		    	asema.addItemProperty(TITLE, new ObjectProperty<String>(Station.ASEMAT[i], String.class));
		    	asema.addItemProperty(UNIT, new ObjectProperty<String>(" ", String.class));
		    	asema.addItemProperty(SOURCE, new ObjectProperty<String>(" ", String.class));
		    	asema.addItemProperty(AVAILABLE, new ObjectProperty<String>(" ", String.class));
		    }
		    //System.out.println("Aseman title on :"+asema.getItemProperty(TITLE));
		    //Item table = treecontainer.addItem(tablename);
		    //try{
		    //	table.getItemProperty(TITLE).setValue(tablename);
		    //} catch (Exception except) {
		    //	System.out.println("Nullpointer in table TITLE");
		    //table.addItemProperty(TITLE, new ObjectProperty<String>("  ", String.class));
		    //}
		    //table.addItemProperty(TITLE, new ObjectProperty<String>(tablename, String.class));
		    //treecontainer.setParent(tablename, Station.ASEMAT[i]);
		    for (SmearVariableMetadata meta: mdata) {
		    	String variable = meta.getVariable();
		    	String variable_id = variable+":"+tablename;
		    	//System.out.println("Variable "+variable_id);
		    	treecontainer.removeItem(variable_id);
		    	String description = meta.getDescription();
		    	String unit = meta.getUnit();
		    	String title = meta.getTitle();
		    	//System.out.println("Title "+title);
		    	String source = meta.getSource();
		    	Date started = meta.getPeriod_start();
		    	String available="";
		    	if (!String.valueOf(started.getTime()).equals("0000-00-00 00:00:00")){
		    		try{
		    			Format formatter = new SimpleDateFormat("yyyy-MM-dd");
		    			available = formatter.format(started);
		    		}catch (Exception edate) {
		    			System.out.println("Exception in "+variable+" available");
		    			available = "Not found";
		    		}
		    	} else {
		    		System.out.println("Variable has 0000-00-00 00:00:00 timestamp");
		    	}
		    	String cat = meta.getCategory();
		    	//System.out.println("Category "+cat);
		    	String categoryitem = Station.ASEMAT[i]+cat;
		    	//System.out.println("Muuttujalla " +variable+ " löytyy dataa alkaen " +started.getTime()+ " ja se kuuluu kategoriaan: "+cat);
		    	//System.out.println("Aikaikkunan aloitus on " +start.getTime());
		    	// Availeble 9999-09-09 is not supposed to be in tree
		    	if (!available.equals("9999-09-09")){
		    		//System.out.println("Dataa löytyy");

		    		Item variable_item = treecontainer.addItem(variable_id);
		    		Item category = treecontainer.addItem(categoryitem);
		    		//System.out.println("Luotiin kategoria " +categoryitem);
		    		try {
		    			category.getItemProperty(DESCRIPTION).setValue(" ");
		    			category.getItemProperty(TITLE).setValue(cat);
		    			category.getItemProperty(UNIT).setValue(" ");
		    			category.getItemProperty(SOURCE).setValue(" ");
		    			category.getItemProperty(AVAILABLE).setValue(" ");
		    		} catch (Exception e) {	
		    			category = new PropertysetItem();
		    			category.addItemProperty(DESCRIPTION, new ObjectProperty<String>(" ", String.class));
		    			category.addItemProperty(TITLE, new ObjectProperty<String>(cat, String.class));
		    			category.addItemProperty(UNIT, new ObjectProperty<String>(" ", String.class));
		    			category.addItemProperty(SOURCE, new ObjectProperty<String>(" ", String.class));
		    			category.addItemProperty(AVAILABLE, new ObjectProperty<String>(" ", String.class));
		    		}
		    		//category.getItemProperty(TITLE).setValue(cat);
		    		//treecontainer.setParent(categoryitem,tablename);
		    		treecontainer.setParent(categoryitem,Station.ASEMAT[i]);
		    		//System.out.println("Liitettiin kategoria "+categoryitem+" Asemaan "+ ASEMAT[i]);
		    		treecontainer.setParent(variable_id,categoryitem);
		    		treecontainer.setChildrenAllowed(variable_id, false);
		    		try{
		    			if (exist(description)) {
		    				variable_item.getItemProperty(DESCRIPTION).setValue(description);
		    			} else {
		    				variable_item.getItemProperty(DESCRIPTION).setValue("No meta");
		    			}
		    			if (exist(title)) { 
		    				variable_item.getItemProperty(TITLE).setValue(title);
		    			} else {
		    				variable_item.getItemProperty(TITLE).setValue("No title");
		    			}
		    			if (exist(unit)){
		    				variable_item.getItemProperty(UNIT).setValue(unit);
		    			} else {
		    				variable_item.getItemProperty(UNIT).setValue("No unit");
		    			}
		    			if (exist(source)) {
		    				variable_item.getItemProperty(SOURCE).setValue(source);
		    			} else {
		    				variable_item.getItemProperty(SOURCE).setValue("No source");
		    			}
		    			if (started != null || !String.valueOf(started).equals("0000-00-00 00:00:00")) {
		    				variable_item.getItemProperty(AVAILABLE).setValue(available);
		    			} else {
		    				variable_item.getItemProperty(AVAILABLE).setValue("No start");
		    			}
		    		} catch (Exception e) {
		    			System.out.println("Catched " + e);
		    		}
		    	}
			tree.setContainerDataSource(treecontainer);							
			//}         
			//else {
			//	//System.out.println("Muuttujalle ei ole dataa");
			//	}
		    }

		    //tree.expandItem(ASEMAT[i]);
		    tree.markAsDirty();
		    //variableselection.replaceComponent(oldComponent, newComponent);
		    variableselection.markAsDirty();
		} else {
		    System.err.println("No metadata");
		}
	    }
	} //for stations
	tree.setItemDescriptionGenerator(new ItemDescriptionGenerator() {                            
		public String generateDescription(Component tree, Object variable_id, Object description) {
		    Item item = treecontainer.getItem(variable_id);

		    Object desc = item.getItemProperty(DESCRIPTION).getValue();
		    //Object titl = item.getItemProperty(TITLE).getValue();
		    Object unit = item.getItemProperty(UNIT).getValue();
		    Object source = item.getItemProperty(SOURCE).getValue();
		    Object available = item.getItemProperty(AVAILABLE).getValue();
		    return "METADATA: " + String.valueOf(desc) + "  COLUMN: " + String.valueOf(variable_id) + "  UNIT: " + String.valueOf(unit) + "  SOURCE:  " + String.valueOf(source) + "  AVAILABLE:  " + String.valueOf(available);

		}});
	variableselection.addComponent(tree);

	dayselection.setMargin(true);
	final GregorianCalendar calendar = new GregorianCalendar();
	calendar.setTime(new Date());
		
	enddate.setWidth("65%");
	enddate.setDateFormat("yyyy-MM-dd");
	enddate.setResolution(Resolution.DAY);
	enddate.setInputPrompt("End date");
	//enddate.setWidth(WIDTH);
	enddate.setValue(calendar.getTime());
	enddate.setImmediate(true);
	enddate.setLocale(new Locale("en", "US"));
	enddate.setAssistiveText(CITE);
		
	startdate.setWidth("65%");
	startdate.setDateFormat("yyyy-MM-dd");
	startdate.setResolution(Resolution.DAY);
	startdate.setInputPrompt("Start date");
	//startdate.setWidth(WIDTH);
	startdate.setAssistiveText("University of Helsinki - Division of Atmospheric Sciences."); //sivun alareunassa "Arrow down key opens calendar element for choosing the date"
	startdate.setLocale(new Locale("en", "US"));
	final Date minValue = new GregorianCalendar(1996,1,1).getTime();
	startdate.setValue(getModifiedDate(calendar.getTime(), -1));
	Station.setFrom(startdate.getValue());
	startdate.setImmediate(true);
	startdate.setValidationVisible(true);
	enddate.setValidationVisible(true);
        
        final ValueChangeListener changeValueListener_to = new ValueChangeListener() {
		@Override
		public void valueChange(ValueChangeEvent event) {
		    try {
                	enddate.removeAllValidators();
                	enddate.addValidator(new DateRangeValidator("To-date must be set between 1991-01-01 and today",minValue,getModifiedDate(calendar.getTime(),1), Resolution.DAY));
			enddate.validate();
			enddate.setComponentError(null);
		    } catch (Exception e) {
                	new Notification("Invalid To-date",
					 "<br/>To-date should be set between 1991-01-01 and today",
					 Notification.Type.WARNING_MESSAGE, true)
			    .show(Page.getCurrent());
                	startdate.setValue(getModifiedDate(calendar.getTime(), -1));
                	enddate.setValue(calendar.getTime());
			//enddate.requestRepaint();
			enddate.markAsDirty();
		    }
		    try {
			enddate.removeAllValidators();
			enddate.addValidator(new DateRangeValidator("Query restricted to "+maxTimeWindow+" days maximum", minValue, getModifiedDate(startdate.getValue(),maxTimeWindow),Resolution.DAY));
			enddate.validate();
			enddate.setComponentError(null);
		    } catch (Exception e) {
                	new Notification("Timewindow exceeds "+maxTimeWindow+" days",
					 "<br/>From-date adjusted to allowed timewindow",
					 Notification.Type.WARNING_MESSAGE, true)
			    .show(Page.getCurrent());
                	if ((getModifiedDate(startdate.getValue(), -(maxTimeWindow-1))).before(minValue)){
			    startdate.setValue(minValue);
                	}
                	else {
			    startdate.setValue(getModifiedDate(enddate.getValue(), -(maxTimeWindow-1)));
                	}
			//startdate.requestRepaint();
                startdate.markAsDirty();   	
		    }
		    Station.setFrom(startdate.getValue());
		    tree.markAsDirty();
		    //tree.requestRepaint();
		    tree.markAsDirty();
		}
	    };
        final ValueChangeListener changeValueListener_from = new ValueChangeListener() {
		@Override
		public void valueChange(ValueChangeEvent event) {
		    try {
			startdate.removeAllValidators();
			startdate.addValidator(new DateRangeValidator("From-date must be set between 1991-01-01 and today", minValue,getModifiedDate(calendar.getTime(),1),Resolution.DAY));
			startdate.validate();
			startdate.setComponentError(null);
		    } catch (Exception e) {
                	new Notification("Invalid From-date",
					 "<br/>From-date should be set between 1991-01-01 and today",
					 Notification.Type.WARNING_MESSAGE, true)
			    .show(Page.getCurrent());
                	startdate.setValue(getModifiedDate(calendar.getTime(), -1));
                	enddate.setValue(calendar.getTime());
			startdate.markAsDirty();
		    }
		    try {
			startdate.removeAllValidators();
			startdate.addValidator(new DateRangeValidator("Query restricted to "+maxTimeWindow+" days maximum", getModifiedDate(enddate.getValue(),-maxTimeWindow) ,getModifiedDate(calendar.getTime(),1),Resolution.DAY));
			startdate.validate();
			startdate.setComponentError(null);
		    } catch (Exception e) {
                	new Notification("Timewindow exceeds "+maxTimeWindow+" days",
					 "<br/>To-date adjusted to allowed timewindow",
					 Notification.Type.WARNING_MESSAGE, true)
			    .show(Page.getCurrent());
                	if ((getModifiedDate(startdate.getValue(), 182)).before(calendar.getTime())){
			    enddate.setValue(getModifiedDate(startdate.getValue(), (maxTimeWindow-1)));
                	}
                	else {
			    enddate.setValue(calendar.getTime());
                	}
			enddate.markAsDirty();
		    }
		    Station.setFrom(startdate.getValue());
		    tree.markAsDirty();
		    tree.markAsDirty();
		}
	    };
        startdate.addValueChangeListener(changeValueListener_from);
        enddate.addValueChangeListener(changeValueListener_to);
        startdate.setImmediate(true);
        enddate.setImmediate(true);
        startdate.setHeight("100%");
        enddate.setHeight("100%");
        //dayselection.setWidth("700px");
        HorizontalLayout hsplit = new HorizontalLayout();
        hsplit.setWidth("250px");        
        hsplit.addComponent(startdate);
        hsplit.addComponent(enddate);
        hsplit.setComponentAlignment(startdate, Alignment.BOTTOM_LEFT);
        hsplit.setComponentAlignment(enddate, Alignment.BOTTOM_LEFT);
        dayselection.addComponent(hsplit);
        HorizontalLayout timesplit = new HorizontalLayout();
        
        timewindow.setHeight("100%");
        timewindow.addItem("Day");
        timewindow.addItem("Week");
        timewindow.addItem("Month");
        timewindow.setNullSelectionAllowed(false);
        timewindow.setValue("Day");
        timewindow.setImmediate(true);
        timewindow.setWidth("80px");
        
        
        
        prevbutton.setWidth(SMALLBUTTONWIDTH);
        
        timesplit.addComponent(prevbutton);
        timesplit.setExpandRatio(prevbutton, 1.0f);
        timesplit.addComponent(timewindow);
        timesplit.setExpandRatio(timewindow, 2.0f);
        
        nextbutton.setWidth(SMALLBUTTONWIDTH);
        
        timesplit.addComponent(nextbutton);
        timesplit.setExpandRatio(nextbutton, 1.0f);
        //timesplit.setWidth("200px");
        timesplit.setComponentAlignment(prevbutton, Alignment.BOTTOM_LEFT);
        timesplit.setComponentAlignment(timewindow, Alignment.BOTTOM_LEFT); 
        timesplit.setComponentAlignment(nextbutton, Alignment.BOTTOM_LEFT);
        timesplit.setHeight("100%");
        dayselection.addComponent(timesplit);
        
        
    	//Averaging
	//final ComboBox avaraging = new ComboBox("Averaging:");
	avaraging.addItem("NONE");
	avaraging.addItem("30MIN");
	avaraging.addItem("1HOUR");
	avaraging.setItemCaption("NONE", "None");
	avaraging.setItemCaption("30MIN", "30 min");
	avaraging.setItemCaption("1HOUR", "1 hour");
	avaraging.setNullSelectionAllowed(false);
	avaraging.setValue("NONE");
	avaraging.setImmediate(true);
	avaraging.setWidth(WIDTH);
	avaraging.setHeight("100%");
		
	//Type of avaraging
	//final ComboBox typeavaraging = new ComboBox("Averaging Type:");
	typeavaraging.addItem("NONE");
	typeavaraging.addItem("ARITHMETIC");
	typeavaraging.addItem(Download.GEOMETRIC);
	typeavaraging.addItem(Download.MEDIAN);
	typeavaraging.addItem(Download.SUMM);
	typeavaraging.setItemCaption("NONE", "None");
	typeavaraging.setItemCaption("ARITHMETIC", "Arithmetic");
	typeavaraging.setItemCaption(Download.GEOMETRIC, "Geometric");
	typeavaraging.setItemCaption(Download.MEDIAN, "Median");
	typeavaraging.setItemCaption(Download.SUMM, "Sum");
	typeavaraging.setNullSelectionAllowed(false);
	typeavaraging.setValue("NONE");
	typeavaraging.setImmediate(true);
	typeavaraging.setWidth(WIDTH);
	typeavaraging.setHeight("100%");
		
	// Arrival Height
        
	aheight.addItem("HEIGHT_100");
	aheight.addItem("HEIGHT_250");
	aheight.addItem("HEIGHT_500");
	aheight.setItemCaption("HEIGHT_100", "100m");
	aheight.setItemCaption("HEIGHT_250", "250m");
	aheight.setItemCaption("HEIGHT_500", "500m");
	aheight.setNullSelectionAllowed(false);
	aheight.setValue("HEIGHT_100");
	aheight.setImmediate(true);
	aheight.setWidth(WIDTH);
	aheight.setHeight("100%");
		
	//Qualitylevel
		
	quality.addItem("ANY");
	quality.addItem(CHECKED);
	quality.setItemCaption("ANY", "Any");
	quality.setItemCaption(CHECKED, "Quality Checked");
	quality.setNullSelectionAllowed(false);
	quality.setValue("ANY");
	quality.setImmediate(true);
	quality.setWidth(WIDTH);
	quality.setHeight("100%");

	final LiferayIPC liferayipc = new LiferayIPC();
	liferayipc.extend(this);
	final Button button = new Button();
		
	button.setIcon(new ThemeResource("../Query.png"));
	button.setWidth("115px");
	button.setDescription("Click to retrieve data visualization of selected variables");
	//button.setStyleName(Button.STYLE_LINK);
	button.addClickListener(new Button.ClickListener() {
		Button oldDownloadbutton;
		Label oldHDF5button; 
		public void buttonClick(ClickEvent event) {
		    if (null != fd){
			fd.remove();
			fd = null;
		    }
		    liferayipc.sendEvent("NewTrajectory", csvsoi(startdate.getValue())+","+aheight.getValue());
		    Tree t2 = (Tree)variableselection.getComponent(0);
		    final Set<String> set2 = (Set<String>)t2.getValue();
		    Download dl = null;
		    try {
			dl = new Download(startdate.getValue(), enddate.getValue(), db, variableselection,treecontainer, avaraging, typeavaraging, String.valueOf(quality.getValue()));
		    } catch (Exception e) {
			System.out.println("Catched " + e);
		    }
		    if (null != dl){
			if (!set2.isEmpty()){					
			    File hdf5 = dl.getHDF5();
			    //FileDownloader fd = dl.getCSV();
					
			    fd = dl.getCSV();
			    if (null != fd) {
				//Button b = new Button("Download CSV");
				b.setEnabled(true);
				b.setImmediate(true);
				fd.extend(b);

				if (dayselection.getComponentCount() > 3) { // there is 6 other component
				    dayselection.replaceComponent(oldDownloadbutton, b); //keyword is "replace"
				    dayselection.setComponentAlignment(b, Alignment.BOTTOM_RIGHT);
				} else {
				    dayselection.addComponent(b); // dl-button ilmaantuu vasta queryn jälkeen
				    dayselection.setComponentAlignment(b, Alignment.BOTTOM_RIGHT);
				}
				oldDownloadbutton=b;
			    }
			    if (null != hdf5) {
				Label b = new Label(LABELSTART+hdf5.getName()+LABELEND,ContentMode.HTML);
				if (dayselection.getComponentCount() > 4) { // there is 7 other component
				    dayselection.replaceComponent(oldHDF5button, b); //keyword is "replace"
				    dayselection.setComponentAlignment(b, Alignment.BOTTOM_RIGHT);
				} else {
				    dayselection.addComponent(b);
				    dayselection.setComponentAlignment(b, Alignment.BOTTOM_RIGHT); // HDF5-button ilmaantuu vasta queryn jälkeen
				}
				oldHDF5button=b;
			    }
			    else {
				new Notification("Could not produce HDF5",
						 "<br/>Could not produce HDF5", Notification.Type.WARNING_MESSAGE, true)
				    .show(Page.getCurrent());
			    } 
			}
			updateVizualisation(startdate, enddate, middlesection,variableselection, dl);
		    } //if dl!=null
		} 
	    });

        dayselection.addComponent(button);       
        dayselection.setComponentAlignment(button, Alignment.BOTTOM_LEFT);
        middlesection.addComponent(dayselection);
        
        //Options selection    
        HorizontalLayout optionselection = new HorizontalLayout();
        optionselection.setMargin(true);		
		
	Button mainview = new Button("Reload main view");
	mainview.addClickListener(new Button.ClickListener() {
		public void buttonClick(ClickEvent event) {
		    final Set<String> set3 = (Set<String>)tree.getValue();
		    for (String rm:set3){
			tree.unselect(rm);
		    }
		    enddate.setValue(calendar.getTime());
		    startdate.setValue(getModifiedDate(calendar.getTime(), -1));
		    timewindow.setValue("Day");
		    updateVizualisation(startdate,enddate, middlesection, variableselection, null); 
		}
	    });
	optionselection.addComponent(quality);
	optionselection.addComponent(avaraging);
	optionselection.addComponent(typeavaraging);
	optionselection.addComponent(aheight);
        middlesection.addComponent(optionselection);
        middlesection.removeComponent(mainview);
	middlesection.addComponent(mainview);
	middlesection.setComponentAlignment(mainview, Alignment.BOTTOM_RIGHT); 
	//listeners
	nextbutton.addClickListener(new Button.ClickListener() {
		public void buttonClick(ClickEvent event) {
		    if (timewindow.getValue() == "Day"){
            		startdate.setValue(getModifiedDate(startdate.getValue(), 1));
            		enddate.setValue(getModifiedDate(enddate.getValue(), 1));
		    }
		    if (timewindow.getValue() == "Week"){
            		startdate.setValue(getModifiedDate(startdate.getValue(), 7));
            		enddate.setValue(getModifiedDate(enddate.getValue(), 7));
		    }
		    if (timewindow.getValue() == "Month"){
            		startdate.setValue(getModifiedDate(startdate.getValue(), 30));
            		enddate.setValue(getModifiedDate(enddate.getValue(), 30));
		    }
		    Download dl = new Download(startdate.getValue(), enddate.getValue(), db, variableselection,treecontainer, avaraging, typeavaraging, String.valueOf(quality.getValue()));
		    //FileDownloader fd = dl.getCSV();
		    if (null != fd) {
            		fd.remove();
            		fd = null;
		    }
		    b.setEnabled(false);
		    updateVizualisation(startdate, enddate, middlesection, variableselection, dl);
		}
	    });
	prevbutton.addClickListener(new Button.ClickListener() {
		public void buttonClick(ClickEvent event) {
		    if (timewindow.getValue() == "Day"){
            		startdate.setValue(getModifiedDate(startdate.getValue(), -1));
            		enddate.setValue(getModifiedDate(enddate.getValue(), -1));
		    }
		    if (timewindow.getValue() == "Week"){
            		startdate.setValue(getModifiedDate(startdate.getValue(), -7));
            		enddate.setValue(getModifiedDate(enddate.getValue(), -7));
		    }
		    if (timewindow.getValue() == "Month"){
            		startdate.setValue(getModifiedDate(startdate.getValue(), -30));
            		enddate.setValue(getModifiedDate(enddate.getValue(), -30));
		    }
		    Download dl = new Download(startdate.getValue(), enddate.getValue(), db, variableselection,treecontainer, avaraging, typeavaraging, String.valueOf(quality.getValue()));
		    //FileDownloader fd = dl.getCSV();
		    if (null != fd) {
            		fd.remove();
            		fd = null;
		    }
		    b.setEnabled(false);
		    updateVizualisation(startdate, enddate, middlesection,variableselection, dl);
		}
	    });
		
	timewindow.addValueChangeListener(new Field.ValueChangeListener() {
		public void valueChange(ValueChangeEvent event) {
			if (timewindow.getValue() == "Day"){
				GregorianCalendar calendar = new GregorianCalendar();
				calendar.setTime(new Date());
				startdate.setResolution(Resolution.DAY);
				enddate.setResolution(Resolution.DAY);
				//startdate.setValue(getModifiedDate(enddate.getValue(), -1));
				if ((getModifiedDate(startdate.getValue(), 1)).before(calendar.getTime())){
					enddate.setValue(getModifiedDate(startdate.getValue(), 1));
				} else{
					enddate.setValue(calendar.getTime());
					startdate.setValue(getModifiedDate(enddate.getValue(), -1));
				}
			}
			if (timewindow.getValue() == "Week"){
				GregorianCalendar calendar = new GregorianCalendar();
				calendar.setTime(new Date());
				startdate.setResolution(Resolution.DAY);
				enddate.setResolution(Resolution.DAY); 
				//startdate.setValue(getModifiedDate(enddate.getValue(), -7));
				if ((getModifiedDate(startdate.getValue(), 7)).before(calendar.getTime())){
					enddate.setValue(getModifiedDate(startdate.getValue(), 7));
				} else{
					enddate.setValue(calendar.getTime());
					startdate.setValue(getModifiedDate(enddate.getValue(), -7));
				}
			}
			if (timewindow.getValue() == "Month"){
				GregorianCalendar calendar = new GregorianCalendar();
				calendar.setTime(new Date());
				startdate.setResolution(Resolution.DAY);
				enddate.setResolution(Resolution.DAY);
				//startdate.setValue(getModifiedDate(enddate.getValue(), -30));
				if ((getModifiedDate(startdate.getValue(), 30)).before(calendar.getTime())){
					enddate.setValue(getModifiedDate(startdate.getValue(), 30));
				} else{
					enddate.setValue(calendar.getTime());
					startdate.setValue(getModifiedDate(enddate.getValue(), -30));
				}
			}
			Download dl = new Download(startdate.getValue(), enddate.getValue(), db, variableselection,treecontainer, avaraging, typeavaraging, String.valueOf(quality.getValue()));
			b.setEnabled(false);
			//FileDownloader fd = dl.getCSV();
			if (null != fd) {
				fd.remove();
				fd = null;
			}
			updateVizualisation(startdate, enddate, middlesection,variableselection,dl);
		}
	}); 
	typeavaraging.addValueChangeListener(new Field.ValueChangeListener() {
		public void valueChange(ValueChangeEvent event) {
			if (typeavaraging.getValue() != "NONE" && nohuman == false){
				nohuman = true;
				avaraging.setValue("1HOUR");
				nohuman = false;
			}
		}
	});
	avaraging.addValueChangeListener(new Field.ValueChangeListener() {
		public void valueChange(ValueChangeEvent event) {
			if (avaraging.getValue() != "NONE" && nohuman == false){
				nohuman = true;
				typeavaraging.setValue("ARITHMETIC");
				nohuman = false;
			}
			if (avaraging.getValue() == "NONE" && nohuman == false){
				nohuman = true;
				typeavaraging.setValue("NONE");
				nohuman = false;
			}
		}
	});
	b.addClickListener(new Button.ClickListener() {
		public void buttonClick(ClickEvent event) {
		    if (contact_email == "Someone"){ 
			final Window subWindow = new Window("Contact");
			VerticalLayout subContent = new VerticalLayout();
			subContent.setMargin(true);
			subWindow.setContent(subContent);
			final TextField tf = new TextField("Email:");
			subContent.addComponent(new Label("Please leave your email, if you want to be informed about updates in the dataset you are downloading"));
			subContent.addComponent(tf);

			Button proceed = new Button("Proceed");
			proceed.addClickListener(new Button.ClickListener() {
				public void buttonClick(ClickEvent event) {
				    subWindow.close();
				}
			    });     
			subContent.addComponent(proceed);

			// Center it in the browser window
			subWindow.center();
			subWindow.addCloseListener(new Window.CloseListener() {
				public void windowClose(CloseEvent ce) {
					try{
						if ((String)tf.getValue() == ""){ 
							tf.setValue("Someone");
						}
						contact_email = (String)tf.getValue();
						Tree t2 = (Tree)variableselection.getComponent(0);
						final Set<String> set3 = (Set<String>)t2.getValue();
						portletPreferences.setValue(CONTACT_EMAIL,portletPreferences.getValue(CONTACT_EMAIL, "")+ "\n\n"+calendar.getTime()+"  "+contact_email+ " downloaded "+set3+ " Starting from: "+startdate.getValue()+ " To: "+enddate.getValue());
						portletPreferences.store();
					} catch(InvalidValueException ex){
						Notification.show(ex.getMessage(), Type.ERROR_MESSAGE);
					} catch(Exception e) {
						Notification.show("Failed to save preference value", Type.ERROR_MESSAGE);
					} 
				}
			    });
			// Open it in the UI
			UI.getCurrent().addWindow(subWindow);
			subWindow.bringToFront();
			subWindow.setVisible(true);
			//fd.remove();
    			//fd = null;
		    } else {
			try {
			    Tree t2 = (Tree)variableselection.getComponent(0);
			    final Set<String> set3 = (Set<String>)t2.getValue();
			    portletPreferences.setValue(CONTACT_EMAIL,portletPreferences.getValue(CONTACT_EMAIL, "")+ "\n\n"+calendar.getTime()+"  "+contact_email+ " downloaded "+set3+ " Starting from: "+startdate.getValue()+ " To: "+enddate.getValue());
			    portletPreferences.store();
			} catch(InvalidValueException ex){
			    Notification.show(ex.getMessage(), Type.ERROR_MESSAGE);
			} catch(Exception e) {
			    Notification.show("Failed to save preference value", Type.ERROR_MESSAGE);
			} 
		    }
		}
	    });
		
	//bottompart.addComponent(mainview);
		
		
        
	layout.addComponent(variableselection);
	updateVizualisation(startdate,enddate, middlesection, variableselection, null);
	//middlesection.addComponent(mainview);
	//middlesection.setComponentAlignment(mainview, Alignment.BOTTOM_RIGHT); 
	layout.addComponent(middlesection);
	//layout.addComponent(bottompart);
    }
    
    /**
     * Because called from HDF5, station is ignored, because vmdata is set by table
     *  
     * 
     * @return List<SmearVariableMetadata>
     */
    public static List<SmearVariableMetadata> getMetadata() {
    	return vmdata;
    }
    
    public void updateVizualisation(final PopupDateField start, final PopupDateField end, final VerticalLayout middlesection, final VerticalLayout variableselection, Download dl) { 
	//Vizualisation viz = new Vizualisation(start.getValue(), end.getValue());
	PopupDateField endt = new PopupDateField();
	endt.setValue(end.getValue());
	final Tree t = (Tree)variableselection.getComponent(0);
	Calendar cale = Calendar.getInstance();
	cale.setTime(start.getValue());
	cale.add(Calendar.DATE, 30); //minus number would decrement the days
	//endt.setValue(cale.getTime());
	//if (cale.getTime().before(end.getValue())){
	//	new Notification("Plotting only first month",
	//			"<br/>",
	//			Notification.TYPE_WARNING_MESSAGE, true)
	//	.show(Page.getCurrent());
	//	endt.setValue(getModifiedDate(start.getValue(), 30));
	//}
	Vizualisation viz = new Vizualisation(start.getValue(), endt.getValue());
	HorizontalLayout[] rows = new HorizontalLayout[Vizualisation.ROWS];
	final Set<String> set = (Set<String>)t.getValue();
	int norows = Vizualisation.ROWS; // turha alustus, vastattava todellisuutta tai tule null-osoitin-poikkeus
	if (set != null && set.isEmpty() == false){
	    rows = viz.plotVariables(set, t, treecontainer, dl);
	    norows = rows.length;
	} else { //if selection == null
	    rows[0] = viz.rivi1();
	    rows[1] = viz.rivi2();
	    rows[2] = viz.CSN(start.getValue(), endt.getValue(), Station.HYYTIÄLÄ);
	    rows[3] = viz.rivi3();
	    for (int i = 4; i<Vizualisation.ROWS; i++){
		rows[i] = viz.emptyrow();
	    }
	    norows = Vizualisation.ROWS;
	}
	if (middlesection.getComponentCount() > 3) { 
	    for (int i = 0; i < norows; i++) {		
		middlesection.replaceComponent(oldrows[i], rows[i]);
	    }
	} else {
	    for (int i = 0; i < norows; i++) {	
		middlesection.addComponent(rows[i]); 
	    }
	}
	oldrows = rows;
		
    }

    String csvsoi(Date ft) {
    	final String COMMASEPARATED = "yyyy,MM,dd,HH";
    	DateFormat fnf = new SimpleDateFormat(COMMASEPARATED);
    	try{
    		return fnf.format(ft);
    	} catch(java.lang.NullPointerException enu) {
    		System.err.println("Null pointer Exception");
    		return "No valid time";
    	}
    }

    /**
     * Tarkistetaan muuttujan olemassaolo ja että muuttujalla on sisältö.
     */  
    private boolean exist(String object) {
	if (null == object) {
	    return false;
	} 
	if (object.equals("")) {
	    return false;
	} 
	return true;
    }
	
}
