package vcluster.global;



public class Config {

	public static String configFile = "vcluster.conf";
	public static String CONDOR_IPADDR ;
	public static String ONE_IPADDR ;
	public static final int PORTNUM = 9734;
	
	public enum VMState {STOP, PENDING, RUNNING, SUSPEND, PROLOG, FAILED,NOT_DEFINED }; 
	public enum CloudType {PRIVATE, PUBLIC, NOT_DEFINED};
	public static final String xmlFile = "response.xml";
	
	public static boolean DEBUG_MODE = false;
}