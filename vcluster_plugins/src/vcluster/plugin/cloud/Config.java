package vcluster.plugin.cloud;



public class Config {

	public static String configFile = "vcluster.conf";
	public static final String xmlFile = "response.xml";
	
	public static String CONDOR_IPADDR = "150.183.233.60";
	public static String ONE_IPADDR = "150.183.233.60";
	public static final int PORTNUM = 9734;
	
	/* interval for checking q in minutes */
	public static int MAX_PROBE_INTERVAL = 30;  
	
	public enum VMState {STOP, PENDING, RUNNING, SUSPEND, PROLOG, NOT_DEFINED }; 
	public enum CloudType {PRIVATE, PUBLIC, NOT_DEFINED};

	//public static CloudManager cloudMan = null;
	
	public static boolean DEBUG_MODE = false;
	
	public static int DEFAULT_SLEEP_SEC = 30;
	public static int SLEEP_SEC_INC = 30;
	public static int MAX_SLEEP_SEC = 60*30; /* 30 mins */
	

	//public static String ProxyExecutor_plugin = "";
	
	
	
}