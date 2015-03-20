package fi.csc.avaa.smear.smartsmear;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;

import ncsa.hdf.hdf5lib.H5;
import ncsa.hdf.hdf5lib.HDF5Constants;
import ncsa.hdf.object.Attribute;
import ncsa.hdf.object.Dataset;
import ncsa.hdf.object.Datatype;
import ncsa.hdf.object.FileFormat;
import ncsa.hdf.object.Group;
import ncsa.hdf.object.h5.H5File;

import com.vaadin.ui.ComboBox;
//import fi.csc.smear.db.model.Varriometa;
/**
 * write HDF5 file
 *  CLASSPATH jhdf5obj.jar:jhdf5.jar:jhdfobj.jar:
 *
 * Copyright (c) CSC 2012, 2013, 2014, 2015
 * @author Pekka Järveläinen
 */
public class HDF5 implements Runnable{

	final static String ISO8601 = "yyyy-MM-dd'T'HH:mm:ssz";	
	final private static  String DIR = "/var/www/html/tmp/";
	final static int GZIPNO = 0;
	final static int GZIPYES = 1; //ei toiminut v 2012
	final static int CLSTRING =  Datatype.CLASS_STRING;
	final static int SAMTIMELENGTH = 19; //second accurance, 21 with .0 seconds
	//private static FileFormat fileFormat = null;
	private Thread thr; 
	private Date start; 
	private Date end;
	//private Download dl;
	private Data data;
	private int stationno;
	private Metadata mdata;
	private File file;	
	H5File h5f;
	String stationname = "";
	private String filename = "";
	DateFormat formatter = new SimpleDateFormat(ISO8601);
	DateFormat fnf = new SimpleDateFormat(Download.FILENAMEFORMAT);
	//Columns columns = null;	
	
	private ComboBox typeOfAVG;
		
	public HDF5(Download dl, Date start, Date end, int station, int avg, ComboBox typeOfAVG) {
		//this.dl = dl;	
		this.data = dl.data;
		this.start = start;
		this.end = end;
		this.stationno = station;
		this.mdata = SmearViewUI.getMetadata();	
		//this.avg = avg;
		this.typeOfAVG = typeOfAVG;
		thr = new Thread(this);
		/*if (null == fileFormat) {
			fileFormat = FileFormat.getFileFormat(FileFormat.FILE_TYPE_HDF5);
		}*/
		
		/*if (this.stationno == Station.KUMPULA) {
			stationname = Station.Kumpula;
		}
		if (this.stationno == Station.HYYTIÄLÄ) {
			stationname = Station.Hyytiälä;
		}
		if (this.stationno == Station.VÄRRIÖ) {
			stationname = Station.Värriö;
		}
		*/
		stationname = "Smeardata";
		this.filename = DIR + stationname +typeOfAVG.getValue()+fnf.format(start)+".hdf5"; 
		this.file = new File(filename); // cann't be in the thread, used after constructor 
		if (null == file) {
			System.err.println("file was null!" + filename);
		}	
		thr.start();
	}
	
	public void run() {	
		
		try {
			FileFormat fileFormat = FileFormat.getFileFormat(FileFormat.FILE_TYPE_HDF5);
			if (null == fileFormat) {
			    System.err.println("Failed to getFileFormat: "+filename);				//homma kaatuu null-pointeriin seuraavalla rivillä 
			}
			h5f = (H5File)fileFormat.createFile(filename, FileFormat.FILE_CREATE_DELETE );
			if (h5f  == null) {
				System.err.println("Failed to create file: "+filename);
				return;
			}
			if (-1 == h5f.open()) {
				System.err.println("Failed to open file: "+filename);
			}
			//file = new File(filename);
			Group root = (Group)((javax.swing.tree.DefaultMutableTreeNode)h5f.getRootNode()).getUserObject();
			Group station = h5f.createGroup(stationname, root);
			station.writeMetadata(attribute("metadata identifier", CLSTRING, "URN:NBN:fi-fe201207066171"));
			Date date = new Date();
			station.writeMetadata(attribute("metadata date of modification", CLSTRING, formatter.format(date)));		
			station.writeMetadata(attribute("identifier", CLSTRING, Station.getIdentifier(stationno)));
			station.writeMetadata(attribute("date of modification", CLSTRING,  formatter.format(end)));
			station.writeMetadata(attribute("typeOfAVG", CLSTRING, (String)typeOfAVG.getValue()));
			station.writeMetadata(attribute("isPartOf", CLSTRING, "http://www.atm.helsinki.fi/SMEAR/"));
			h5f.writeAttribute(root, attribute("distributor contact", CLSTRING,
					"University of Helsinki, Departement of Atmospheric Sciences, Departement of Physical Sciences, atm-data@helsinki.fi"),
					false);
			h5f.writeAttribute(root, attribute("Cite", CLSTRING, SmearViewUI.CITE), false);
			station.writeMetadata(attribute("title", CLSTRING, stationname + " SMEAR "+
					formatter.format(start)+"-"+formatter.format(end)));
			station.writeMetadata(attribute("geographicalCoverage", CLSTRING, Station.getGeographicalCoverage(stationno)));
			h5f.writeAttribute(root, attribute("access rights", CLSTRING,
					"<RightsDeclaration RIGHTSCATEGORY=”LICENSED”>http://creativecommons.org/licenses/by-sa/3.0/</RightsDeclaration>"), false);
			h5f.writeAttribute(root, attribute("project", CLSTRING, "http://www.atm.helsinki.fi/SMEAR/"), false);
		    Datatype isodate =  h5f.createDatatype(CLSTRING, SAMTIMELENGTH, Datatype.NATIVE, Datatype.NATIVE);
		    Datatype meteocode =  h5f.createDatatype(CLSTRING, 3, Datatype.NATIVE, Datatype.NATIVE);
		    Datatype nativefloat = h5f.createDatatype(Datatype.CLASS_FLOAT, 4, Datatype.NATIVE, Datatype.NATIVE);
		    
		    long  dims[] = new long[1];
		    Dataset dataset = h5f.createScalarDS(DB.SAMPTIME, station, isodate, dims, null, null, GZIPNO, data.getSamptimes());
		    if (null == dataset) { System.out.println("NULLdataset"); }
		    dataset.write(data.getSamptimes());
		   
		    int rows = data.getSamptimes().length;
			dims[0] =  rows ;
		    Iterator<String> iter = data.tableset.iterator();
		    while( iter.hasNext()) {
		    	String taulunnimi = (String)iter.next();
		    	float[][] fa = data.getFtaulu(taulunnimi);
		    	//int cc = fa.length;
		    	String[] ColumnNames =  data.getLabels(taulunnimi);
		    	if (null != ColumnNames) {
		    		int cc = ColumnNames.length;
		    		System.out.println("HDFsarakkeet: "+cc);
		    		for (int i = 0; i < cc; i++) {		    	
		    			createAndWriteDS(ColumnNames[i], station, nativefloat, dims,  fa[i]);								
		    		}
		    	}
		    }
		    String nws[] = data.getNWS();
		    if (null != nws) {
		    	dataset = h5f.createScalarDS(data.getNWSname(), station, meteocode, dims, null, null, GZIPNO, nws);		    			    
		    	dataset.write(nws);
		    }
		    h5f.close();
		} catch (Exception e) {
			if (null == file) {
			System.err.println("file was null: " + filename);
			} else {
				System.err.println("HDF5 luontivirhe: ");
				e.printStackTrace();
			}
		}
		if (null == file) {
			file = new File(filename);
		}
	}

   
	
	/**
	 * Kirjoittaa Vektorin HDF5 tiedostoon.
	 * Taulu parametrin voisi lisätä.
	 * 
	 * @param name	Tietokantasarakkeen nimi
	 * @param station	Group (HDF5)
	 * @param nativefloat Datatype (HDF5)
	 * @param dims 	long [] yksiulotteinen: dims[0] =  rows ;
	 * @param fa	float[] Tiedostoon kirjoitettava data
	 */
	private void createAndWriteDS(String name, Group station, Datatype nativefloat, long [] dims, float fa[]) {

			Dataset dataset;
			try {
				dataset = h5f.createScalarDS(name, station, nativefloat, dims,
						null, null, GZIPNO, fa);
				metawrite(dataset, data.clean(name));
				dataset.write(fa);
			} catch (ncsa.hdf.hdf5lib.exceptions.HDF5LibraryException e) {
				System.err.println(e+" HDF5LibraryException: "+name);
			} catch (Exception e) {
				e.printStackTrace();
			}

	}
	
   
	private void metawrite(Dataset dataset, String name) {
		String title = mdata.getHTtitle().get(name);
		if (null != title) {
			try {
				dataset.writeMetadata(attribute("title", CLSTRING, title));
			} catch (Exception e) {
				System.out.println("Type Exception HD5 title metawrite");
				e.printStackTrace();
			}
		} else {
			System.err.println("There is no title for name "+name);
		}
		String unit = mdata.getHTunit().get(name);
		if (null != unit) {
			try {
				dataset.writeMetadata(attribute("unit", CLSTRING, unit));
			} catch (Exception e) {
				System.out.println("Type Exception HD5 unit metawrite");
				e.printStackTrace();
			}
		} else {
			System.err.println("There is no unit for name "+name);
		}
	}

	/**
     * Create single 1 dimension Attribute and set value
     */
     Attribute attribute(String name, int type, String value) {
        long[] dims = { 1 };
        String values[] = new String[1];
        values[0] = value;
        Datatype datatype = null;
        int dtype = 0;
        try {
            //+4 sallii pari ä-kirjainta, jotka ovat pitempiä UTF-8 koodauksessa
            //varmaan jomman kumman Datatype.NATIVE tilalla pitäisi olla CharacterEncoding.UTF8
            //JNI sucks. Mutta NCSA erikseen sanoo, etteivät aio tehdä puhdasta java toteutusta.
            datatype = h5f.createDatatype(type,  value.length()+4,  Datatype.NATIVE, Datatype.NATIVE);
            dtype = datatype.toNative();
            H5.H5Tset_cset(dtype, HDF5Constants.H5T_CSET_UTF8); 
            //System.out.println("Onnistui luoda merkkijono"+dtype);
        } catch ( ncsa.hdf.hdf5lib.exceptions.HDF5LibraryException e )  {
            System.out.println("Error to createDatatype"+e.getMajorErrorNumber() +e.getMessage());
        } catch ( Exception e )  {
            System.out.println("Other error to createDatatype"+e.toString());
        }
        Attribute result = new Attribute(name, datatype, dims);
        result.setValue(values);
        datatype.close(dtype);
        return result;
    }

	/**
	 * HDF5 file was written to DIR = "/var/www/html/tmp/";
	 * @return File
	 */
	public File getFile() {
		return file;
	}
		
}
