package fi.csc.avaa.smear.smartsmear;

import java.io.File;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Hashtable;
import java.util.List;

import ncsa.hdf.hdf5lib.H5;
import ncsa.hdf.hdf5lib.HDF5Constants;
import ncsa.hdf.object.Attribute;
import ncsa.hdf.object.Dataset;
import ncsa.hdf.object.Datatype;
import ncsa.hdf.object.FileFormat;
import ncsa.hdf.object.Group;
import ncsa.hdf.object.h5.H5File;

import com.vaadin.ui.ComboBox;
/*import com.vaadin.ui.Tree;

import fi.csc.smear.db.model.Hydemeta;
import fi.csc.smear.db.model.Kumpulameta;*/
import fi.csc.smear.db.model.SmearVariableMetadata;
//import fi.csc.smear.db.model.Varriometa;
/**
 * write HDF5 file
 *  CLASSPATH jhdf5obj.jar:jhdf5.jar:jhdfobj.jar:
 *
 * Copyright (c) CSC 2012, 2013, 2014
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
	private Download dl;
	private ResultSet data;
	private int stationno;
	private List<SmearVariableMetadata> mdata;
	private File file;	
	H5File h5f;
	String stationname = "";
	private String filename = "";
	DateFormat formatter = new SimpleDateFormat(ISO8601);
	DateFormat fnf = new SimpleDateFormat(Download.FILENAMEFORMAT);
	//Columns columns = null;	
	Hashtable<String, String> httitle = new  Hashtable<String, String>();
    //private Station stations;
	private Hashtable<String, String> htunit = new  Hashtable<String, String>();
	private int avg;
	private ComboBox typeOfAVG;
	private float[][] keskirarvoistettufa;
	//private Tree tree;
	
	public HDF5(Download dl, Date start, Date end, int station, int avg, ComboBox typeOfAVG) {
		this.dl = dl;	
		this.data = dl.data;
		this.start = start;
		this.end = end;
		this.stationno = station;
		this.mdata = SmearViewUI.getMetadataInStation(station);
		//this.stations = stations;
		this.avg = avg;
		this.typeOfAVG = typeOfAVG;
		//this.tree = tree;
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
		for (int i = 0; i < mdata.size(); i++) {
			SmearVariableMetadata vm = mdata.get(i);
			httitle.put(vm.getVariable(), vm.getTitle());
			htunit.put(vm.getVariable(), vm.getUnit());
		}
		
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
		    ResultSetMetaData rsmd = data.getMetaData();
		    int cc = rsmd.getColumnCount();
		    long t1=0, t2 = 0;
		    Dataset dataset;
		    String samptimes[];
		    float fa[][];
		    String nws[];
		    long  dims[] = new long[1];
		    int rows;
		    int n = 0;
		    synchronized(dl.data) {
			data.last();
			rows = data.getRow();
			dims[0] =  rows ;
			samptimes = new String[rows+1];
			nws = new String[rows+1];
			fa = new float[cc][rows+1]; 
		    	data.beforeFirst();
		    	try {
		    		int j=0; //row
		    		while (data.next()) {
		    			samptimes[j++] = data.getString(1);
		    			if (1 == j) {
		    				t1 = data.getTimestamp(1).getTime();
		    			}
		    			if (2 == j) {
		    				t2 = data.getTimestamp(1).getTime();
		    			}
		    			//data.getFloat(columnIndex);
		    			for (int i = 2; i < cc; i++) {
		    				try {
		    					fa[i][j] = data.getFloat(i);
		    					if (data.wasNull()) {
		    						fa[i][j] = Float.NaN;
		    					}
		    				} catch ( java.sql.SQLException e) {
		    					nws[j] = data.getString(i);
		    					n=i;
		    				} catch (  java.lang.ArrayIndexOutOfBoundsException e) {
		    					System.err.println("i="+i+" j="+j);
		    					e.printStackTrace();
		    					System.err.println("rows="+rows+" cols="+cc);
		    				}
		    			}
		    		}
		    	} catch ( java.sql.SQLException e) {
		    		e.printStackTrace();
		    	}
		    }
		    dataset = h5f.createScalarDS(rsmd.getColumnName(1), station, isodate, dims, null, null, GZIPNO, samptimes);
		    if (null == dataset) { System.out.println("NULLdataset"); }
		    dataset.write(samptimes);
		    keskirarvoistettufa = keskiarvotus(fa, cc, rows, t2-t1 /(1000*60));		    
		    for (int i = 2; i < cc; i++) {		    	
				createAndWriteDS(rsmd.getColumnName(i), station, nativefloat, dims,  fa[i]);								
		    }
		    /*
					String[] c = columns.getNames();
			    	long interval = (hdata.get(1).getSamptime().getTime() - hdata.get(0).getSamptime().getTime())/(1000*60);
					fa = keskiarvotus(fa, columns.getCount(), data.size(), interval);
					dims[0] = fa[1].length;
					for (int i = 1 ; i < columns.getCount() ; i++) {
						createAndWriteDS(c[i], station, nativefloat, dims,  fa[i]);
					}
		     */
		    if (n > 0) {
		    	dataset = h5f.createScalarDS(rsmd.getColumnName(n), station, meteocode, dims, null, null, GZIPNO, nws);		    	
		    	//createAndWriteDS(rsmd.getColumnName(n), station, meteocode, dims,  nws);
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

   
	private float[][] keskiarvotus(float[][] fa, int count, int size, long interval ) {
	    //Boolean geom = false; // findbug: täyttyy testata
		Boolean median = false;
		int average = 0;
		if ((avg > 0) && (interval > 0)) {
			median = typeOfAVG.getValue().equals(Download.MEDIAN) ? true : false;
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
	
	private void createAndWriteDS(String name, Group station, Datatype nativefloat, long [] dims, float fa[]) {

			Dataset dataset;
			try {
				dataset = h5f.createScalarDS(name, station, nativefloat, dims,
						null, null, GZIPNO, fa);
				metawrite(dataset, clean(name));
				dataset.write(fa);
			} catch (ncsa.hdf.hdf5lib.exceptions.HDF5LibraryException e) {
				System.err.println(e+" HDF5LibraryException: "+name);
			} catch (Exception e) {
				e.printStackTrace();
			}

	}
	
    private String clean(String s) {
    	String clean = s; 
    	if (s.startsWith("avg(") || s.startsWith("sum(")) {
    		clean = s.substring(4, s.length()-1);
    	} else if (s.startsWith(DB.GEOMETRIC)) {
    		clean = s.substring(DB.GEOMETRIC.length(), s.length()-3); //lopusta poistetaan loppusulut
    	}
		return clean;
	}

	private void metawrite(Dataset dataset, String name) {
		String title = httitle.get(name);
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
		String unit = htunit.get(name);
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
	
	/**
	 * Tätä ei pidä kutsuman ennen kuin mediaani on laskettu, täytyisi varmaan lisätä synkrointia
	 * @return float[][]
	 */
	public float[][] getMedian() {
		return keskirarvoistettufa;
	}
	
}
