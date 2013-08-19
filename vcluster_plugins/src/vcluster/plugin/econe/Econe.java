package vcluster.plugin.econe;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;

import vcluster.control.VMelement;
import vcluster.plugman.CloudInterface;
import com.sun.xml.bind.StringInputStream;




public class Econe implements CloudInterface{

	public static void main(String[] arg){
				
	    String cmdLine = "";
	    Econe ec = new Econe();
	    ec.cloud.setAccessKey("dada");
	    ec.cloud.setCloudType("public");
	    ec.cloud.setEndPoint("http://150.183.233.60:4567/");
	    ec.cloud.setImageName("ami-0000002");
	    ec.cloud.setInstanceType("m1.small");
	    ec.cloud.setSecretKey("fedd1d1122aa65028c81e16ceb85d9c73790a2fa");
	    ec.cloud.setSignatureMethod("HmacSHA256");
	    ec.cloud.setSignatureVersion("2");
	    ec.cloud.setVersion("2011-05-15");
	    
	    
	    /* prompt */
	   do{
		    System.out.print("EconeTesting > ");
			
		    InputStreamReader input = new InputStreamReader(System.in);
		    BufferedReader reader = new BufferedReader(input);
		    
		    try {
			    /* get a command string */
		    	cmdLine = reader.readLine(); 
		    }
		    catch(Exception e){e.printStackTrace();}	
		    
		    String[] cmds = cmdLine.split(" ");
			if(cmds[1].equalsIgnoreCase("list")){
				ec.listVMs();
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
	
	
	
	CloudElement cloud = new CloudElement();


	
	private static String makeGETQuery(CloudElement cloud, QueryInfo ci) 
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
		        String signature = GetSignature.calculateRFC2104HMAC(new String(stringToSign), 
		        		cloud.getSecretKey(), cloud.getSignatureMethod());

				return (queryString + "&Signature=" + URLEncoder.encode(signature, "UTF-8") 
						+ "&AWSAccessKeyId="+cloud.getAccessKey());
			}
			

	public static ArrayList<VMelement> executeQuery(Command command,String fullURL, String httpQuery)
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

	private static ArrayList<VMelement> doHttpQuery(Command command,URL endPoint) throws Exception {
		HttpURLConnection con = (HttpURLConnection) endPoint.openConnection();
		ArrayList<VMelement> vmList = null;
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
		cloud = new CloudElement(conf);
		return true;

	}

	@Override
	public ArrayList<VMelement> createVM(int maxCount) {
		
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
	public ArrayList<VMelement> listVMs() {
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

		
    	return executeQuery(Command.DESCRIBE_INSTANCE,cloud.getEndPoint(), query);

	}

	@Override
	public ArrayList<VMelement> destroyVM(String id) {
		// TODO Auto-generated method stub

		QueryInfo qi = new QueryInfo();

		qi.putValue("Action", "TerminateInstances");
		qi.putValue("InstanceId.1", id);

		/* fill the default values */
		qi.putValue("SignatureVersion", cloud.getSignatureVersion());
		qi.putValue("SignatureMethod", cloud.getSignatureMethod());

		
		String query = null;
		try {
			query = makeGETQuery(cloud, qi);
		} catch (Exception e) {
			e.printStackTrace();
		}

        return 	executeQuery(Command.TERMINATE_INSTANCE, cloud.getEndPoint(), query);

	}

	@Override
	public ArrayList<VMelement> startVM(String id) {
		// TODO Auto-generated method stub
		QueryInfo qi = new QueryInfo();

		qi.putValue("Action", "StartInstances");
		qi.putValue("InstanceId.1", id);

		/* fill the default values */
		qi.putValue("SignatureVersion", cloud.getSignatureVersion());
		qi.putValue("SignatureMethod", cloud.getSignatureMethod());

		
		String query = null;
		try {
			query = makeGETQuery(cloud, qi);
		} catch (Exception e) {
			e.printStackTrace();
		}

    	return executeQuery(Command.TERMINATE_INSTANCE, cloud.getEndPoint(), query);
	
	}

	@Override
	public ArrayList<VMelement> suspendVM(String id) {
		// TODO Auto-generated method stub
		QueryInfo qi = new QueryInfo();

		qi.putValue("Action", "StopInstances");
		qi.putValue("InstanceId.1", id);

		/* fill the default values */
		qi.putValue("SignatureVersion", cloud.getSignatureVersion());
		qi.putValue("SignatureMethod", cloud.getSignatureMethod());

		
		String query = null;
		try {
			query = makeGETQuery(cloud, qi);
		} catch (Exception e) {
			e.printStackTrace();
		}

    	return executeQuery(Command.TERMINATE_INSTANCE, cloud.getEndPoint(), query);
		
	}

	
}
