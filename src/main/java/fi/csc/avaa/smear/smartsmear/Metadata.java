/**
 * 
 */
package fi.csc.avaa.smear.smartsmear;

import java.util.Hashtable;
import java.util.List;

import fi.csc.smear.db.model.SmearVariableMetadata;

/**
 * Store metadata of the variables as Hashtable by variable
 * 
 * @author pj
 *
 */
public class Metadata {

	private Hashtable<String, String> httitle = new  Hashtable<String, String>();
	private Hashtable<String, String> htunit = new  Hashtable<String, String>();
	private Hashtable<String, String> htdescription = new  Hashtable<String, String>();
	private Hashtable<String, String> htsource = new  Hashtable<String, String>();
	private Hashtable<String, Integer> htindex = new  Hashtable<String, Integer>();
	
	public Metadata(List<SmearVariableMetadata> mdata ) {
		// from HDF5
		for (int i = 0; i < mdata.size(); i++) {
			SmearVariableMetadata vm = mdata.get(i);
			httitle.put(vm.getVariable(), vm.getTitle());
			htunit.put(vm.getVariable(), vm.getUnit());
			htdescription.put(vm.getVariable(), vm.getDescription());
			htsource.put(vm.getVariable(), vm.getSource());
			htindex.put(vm.getVariable(), i);
		}		
	}

	public Hashtable<String, Integer> getHTindex() {
		return htindex;
	}
	
	public Hashtable<String, String> getHTtitle() {
		return httitle;
	}

	public Hashtable<String, String>  getHTunit() {
		return htunit;
	}

	/**
	 * Getter for htdescription
	 * 
	 * @return htdescription Hashtable<String, String>
	 */
	public Hashtable<String, String> getHTdescription() {
		return htdescription;
	}
	
	/**
	 * Getter for htsource
	 * 
	 * @return htsource Hashtable<String, String>
	 */
	public Hashtable<String, String>  getHTsource () {
		return htsource ;
	}


}
