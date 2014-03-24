package vcluster.plugin.econe;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;

import javax.net.ssl.HttpsURLConnection;

import vcluster.control.vmman.Vm;
import vcluster.plugins.CloudInterface;

import com.sun.xml.bind.StringInputStream;




public class Econe implements CloudInterface{

	public static void main(String[] arg){
				
	    String cmdLine = "";
	    Econe ec = new Econe();
	    ec.cloud.setAccessKey("dada915");
	    ec.cloud.setCloudType("public");
	    ec.cloud.setEndPoint("http://fermicloud.fnal.gov:8444/");
	    ec.cloud.setImageName("ami-00000054");
	    ec.cloud.setInstanceType("t1.micro");
	    ec.cloud.setSignatureMethod("HmacSHA256");
	    ec.cloud.setSignatureVersion("2");
	    ec.cloud.setVersion("2011-05-15");
	    
	  
	    /* prompt */
	   do{
		    System.out.print("EconeTesting2 > ");
			
		    InputStreamReader input = new InputStreamReader(System.in);
		    BufferedReader reader = new BufferedReader(input);
		    
		    try {
			    /* get a command string */
		    	cmdLine = reader.readLine(); 
		    }
		    catch(Exception e){e.printStackTrace();}	
		    
		    String[] cmds = cmdLine.split(" ");
		    System.out.println(cmds[1]);
			if(cmds[1].equalsIgnoreCase("list")){
				System.out.println("testtesttest");
				//ec.listVMs();
				ec.rqstHttps();
			}else if(cmds[1].equalsIgnoreCase("create")){
				int nums = Integer.parseInt(cmds[2]);
				ec.createVM(nums);
			}else if(cmds[1].equalsIgnoreCase("destroy")){
						ec.destroyVM(cmds[2]);
			}else if(cmds[1].equalsIgnoreCase("suspend")){
				ec.suspendVM(cmds[2]);
			}else if(cmds[1].equalsIgnoreCase("start")){
				ec.startVM(cmds[2]);						
			}
			else{
				//ec.socketToproxy(cmdLine);
			}
	   }while(!cmdLine.equals("quit"));
	}
	
	
	
	Cloud cloud = new Cloud();
	private void rqstHttps(){
      
      //  final String KEYSTORE_FILE     = "dada.pfx";  
        
  //      final String KEYSTORE_PASSWORD = null;  
  
      //  final String KEYSTORE_ALIAS    = "alias";  
		
        try {
        	  /* KeyStore ks = KeyStore.getInstance("PKCS12");  
            
            FileInputStream fis = new FileInputStream(KEYSTORE_FILE); 
            char[] nPassword = null;
            ks.load(fis, nPassword);
        	fis.close();
            System.out.println("keystore type=" + ks.getType());  
            Enumeration enum1 = ks.aliases();  
            
            String keyAlias = null;  
            
            if (enum1.hasMoreElements()) // we are readin just one certificate.   
            	  
            {  
  
                keyAlias = (String)enum1.nextElement();  
  
                System.out.println("alias=[" + keyAlias + "]");  
  
            }  
  
   
  
            // Now once we know the alias, we could get the keys.   
  
            System.out.println("is key entry=" + ks.isKeyEntry(keyAlias));  
  
            PrivateKey prikey = (PrivateKey) ks.getKey(keyAlias, nPassword);  
  
            java.security.cert.Certificate cert = ks.getCertificate(keyAlias);  
  
            PublicKey pubkey = cert.getPublicKey();  
  
   
  
            System.out.println("cert class = " + cert.getClass().getName());  
  
            System.out.println("cert = " + cert);  
  
            System.out.println("public key = " + pubkey);  
  
            System.out.println("private key = " + prikey);  
            */
			URL myURL = new URL("https://fermicloud.fnal.gov:8444/?Action=DescribeInstances&Timestamp=2014-01-16T00%3A15%3A14.632Z"); 
			HttpsURLConnection httpsConn = (HttpsURLConnection) myURL.openConnection(); 
			InputStreamReader insr = new InputStreamReader(httpsConn.getInputStream());  
			int respInt = insr.read();
			while (respInt != -1) {
			    System.out.print((char) respInt);
			    respInt = insr.read();
			}
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	
	private static String makeGETQuery(Cloud cloud, QueryInfo ci) 
			throws UnsupportedEncodingException, InvalidKeyException, NoSuchAlgorithmException
			{
				List<String> allKeyNames = new ArrayList<String>(ci.getKeySet());
				Collections.sort(allKeyNames, String.CASE_INSENSITIVE_ORDER);

				String queryString = "";
				
				StringBuffer stringToSign = new StringBuffer("GET\n"+cloud.getShortEndPoint()+"\n"+"/"+"\n");
				stringToSign.append("AWSAccessKeyId="+cloud.getAccessKey() + "&");

				boolean first = true;
				for (String keyName : allKeyNames) {
		            if (first)
		                first = false;
		            else
		            	queryString += "&";

		            if(ci.getAttrValue(keyName) == null) {
		            	System.out.println("Keyname = "+ keyName+" is null");
		            }
		            
		   			queryString += keyName+"="+URLEncoder.encode(ci.getAttrValue(keyName).toString(), "UTF-8");
				}
				//System.out.println("after 22222");
				stringToSign.append(queryString);
		        String signature = GetSignature.calculateRFC2104HMAC(new String(stringToSign), cloud.getSecretKey(), cloud.getSignatureMethod());
		        
				String str = (queryString + "&Signature=" + URLEncoder.encode(signature, "UTF-8") 
						+ "&AWSAccessKeyId="+cloud.getAccessKey());
				//System.out.println(str);
				return str;
			}
			

	
	
	
	
	public static ArrayList<Vm> executeQuery(Command command,String fullURL, String httpQuery)
	{

		
		try {            
			URL endPoint = new URL(fullURL+"?"+httpQuery);
			return doHttpQuery(command,endPoint);
		} catch (Exception e)
		{
			e.printStackTrace();
			
		}
		return null;
	}

	private static ArrayList<Vm> doHttpQuery(Command command,URL endPoint) throws Exception {
		HttpURLConnection con = (HttpURLConnection) endPoint.openConnection();
		ArrayList<Vm> vmList = null;
		con.setRequestMethod("GET");
		con.setDoOutput(true);
		con.connect();
		String respStr;
	//	System.out.println(endPoint.toString());
		if (con.getResponseCode() != HttpURLConnection.HTTP_OK) {
			ResponseDataHandler.handlError(con.getErrorStream());
		} else {
			//System.out.println("MARK 2");
			respStr = getResponseString(con.getInputStream());
			//System.out.println(respStr);
			vmList = ResponseDataHandler.handleResponse(command, new StringInputStream(respStr));
		}
		con.disconnect();
		return vmList;

	}

	
	private static String getResponseString(InputStream queryResp) throws Exception 
	{
		final InputStreamReader inputStreamReader = new InputStreamReader(queryResp);
		BufferedReader buffReader = new BufferedReader(inputStreamReader);

		String line = new String();
		StringBuffer stringBuff = new StringBuffer();

		while ((line = buffReader.readLine()) != null) {
			stringBuff.append(line);
		}

		return stringBuff.toString();
	}

	@Override
	public boolean RegisterCloud(List<String> conf) {
		// TODO Auto-generated method stub
		cloud = new Cloud(conf);
		return true;

	}

	@Override
	public ArrayList<Vm> createVM(int maxCount) {
		
		//System.out.println("Under devoleping.......");
		QueryInfo qi = new QueryInfo();
		
		qi.putValue("Action", Command.RUN_INSTANCE.getCommand());
		qi.putValue("ImageId", cloud.getImageName());

		String timestamp = Util.getTimestampFromLocalTime(Calendar.getInstance().getTime());

		/* fill the default values */
		qi.putValue("MinCount", "1");
		qi.putValue("MaxCount", Integer.toString(maxCount));
		qi.putValue("InstanceType", cloud.getInstaceType());

		/* fill the default values */
        qi.putValue("Timestamp", timestamp);
		qi.putValue("Version", cloud.getVersion());
		
		if (cloud.getKeyName() != null) 
			qi.putValue("KeyName", cloud.getKeyName());
		
		
		qi.putValue("SignatureVersion", cloud.getSignatureVersion());
		qi.putValue("SignatureMethod", cloud.getSignatureMethod());
		
		//System.out.println("before");
		String query = null;
		try {
			query = makeGETQuery(cloud, qi);
			//System.out.println("before2");
		} catch (Exception e) {
			e.printStackTrace();
		}
		//System.out.println(cloud.getEndPoint());
	
		return executeQuery(Command.RUN_INSTANCE, cloud.getEndPoint(), query);

		// TODO Auto-generated method stub
		/*Thread t = new Thread() {
			public void run() {
				PrintMsg.print(DMsgType.MSG, "launch vm in a separated thread");
				Config.vmMan.launchVM(1, cloud);
				PrintMsg.print(DMsgType.MSG, "launching is done in a separated thread");
			}
		};
		t.start();
*/
	}

	@Override
	public ArrayList<Vm> listVMs() {
		// TODO Auto-generated method stub
		//System.out.println("MARK 1...................................");
		//ArrayList<VMelement> vmList = null;
		QueryInfo qi = new QueryInfo();

		//qi.putValue("Action", "DescribeInstances");
		qi.putValue("Action", "DescribeInstances");
		String timestamp = Util.getTimestampFromLocalTime(Calendar.getInstance().getTime());

		/* fill the default values */
        qi.putValue("Timestamp", timestamp);
		qi.putValue("Version", cloud.getVersion());

		qi.putValue("SignatureVersion", cloud.getSignatureVersion());
		qi.putValue("SignatureMethod", cloud.getSignatureMethod());


		String query = null;
		try {
			query = makeGETQuery(cloud, qi);
		} catch (Exception e) {
			e.printStackTrace();
		}
		//System.out.println(query);
		try {
			Thread.sleep(5000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	return executeQuery(Command.DESCRIBE_INSTANCE,cloud.getEndPoint(), query);
		//return new ArrayList<Vm> ();
	}

	@Override
	public ArrayList<Vm> destroyVM(String id) {
		// TODO Auto-generated method stub

		QueryInfo qi = new QueryInfo();
		String timestamp = Util.getTimestampFromLocalTime(Calendar.getInstance().getTime());
		qi.putValue("Action", "TerminateInstances");
		qi.putValue("InstanceId.1", id);

		/* fill the default values */
		qi.putValue("Version", cloud.getVersion());
		qi.putValue("SignatureVersion", cloud.getSignatureVersion());
		qi.putValue("SignatureMethod", cloud.getSignatureMethod());
        qi.putValue("Timestamp", timestamp);
		
		String query = null;
		try {
			query = makeGETQuery(cloud, qi);
		} catch (Exception e) {
			e.printStackTrace();
		}

        return 	executeQuery(Command.TERMINATE_INSTANCE, cloud.getEndPoint(), query);

	}

	@Override
	public ArrayList<Vm> startVM(String id) {
		// TODO Auto-generated method stub
		QueryInfo qi = new QueryInfo();

		qi.putValue("Action", "StartInstances");
		qi.putValue("InstanceId.1", id);

		/* fill the default values */
		qi.putValue("SignatureVersion", cloud.getSignatureVersion());
		qi.putValue("SignatureMethod", cloud.getSignatureMethod());

		String timestamp = Util.getTimestampFromLocalTime(Calendar.getInstance().getTime());
		qi.putValue("Timestamp", timestamp);
		qi.putValue("Version", cloud.getVersion());
		String query = null;
		try {
			query = makeGETQuery(cloud, qi);
		} catch (Exception e) {
			e.printStackTrace();
		}

    	return executeQuery(Command.TERMINATE_INSTANCE, cloud.getEndPoint(), query);
	
	}

	@Override
	public ArrayList<Vm> suspendVM(String id) {
		// TODO Auto-generated method stub
		QueryInfo qi = new QueryInfo();

		qi.putValue("Action", "StopInstances");
		qi.putValue("InstanceId.1", id);

		/* fill the default values */
		qi.putValue("SignatureVersion", cloud.getSignatureVersion());
		qi.putValue("SignatureMethod", cloud.getSignatureMethod());
		String timestamp = Util.getTimestampFromLocalTime(Calendar.getInstance().getTime());
		qi.putValue("Timestamp", timestamp);
		qi.putValue("Version", cloud.getVersion());
		
		String query = null;
		try {
			query = makeGETQuery(cloud, qi);
		} catch (Exception e) {
			e.printStackTrace();
		}
    	return executeQuery(Command.TERMINATE_INSTANCE, cloud.getEndPoint(), query);    	
	}


	@Override
	public boolean hoston(String ipmiID) {
		// TODO Auto-generated method stub
		return false;
	}


	@Override
	public boolean hostoff(String ipmiID) {
		// TODO Auto-generated method stub
		return false;
	}


	@Override
	public boolean migrate(String vmid, String hostid) {
		// TODO Auto-generated method stub
		return false;
	}	
	
}
