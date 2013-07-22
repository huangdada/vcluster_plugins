package vcluster.plugin.cloud;

import java.io.BufferedReader;
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
import vcluster.plugman.CloudInterface;

import com.sun.xml.bind.StringInputStream;




public class Econe implements CloudInterface{
	
	CloudElement cloud = null;
	@Override
	public String getInfo() {
		// TODO Auto-generated method stub
		return null;
	}
	
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

				stringToSign.append(queryString);
		        String signature = GetSignature.calculateRFC2104HMAC(new String(stringToSign), 
		        		cloud.getSecretKey(), cloud.getSignatureMethod());

				return (queryString + "&Signature=" + URLEncoder.encode(signature, "UTF-8") 
						+ "&AWSAccessKeyId="+cloud.getAccessKey());
			}
			

	public static void executeQuery(Command command,String fullURL, String httpQuery)
	{
		try {
			URL endPoint = new URL(fullURL+"?"+httpQuery);
			doHttpQuery(command,endPoint);
		} catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	private static void doHttpQuery(Command command,URL endPoint) throws Exception {
		HttpURLConnection con = (HttpURLConnection) endPoint.openConnection();

		con.setRequestMethod("GET");
		con.setDoOutput(true);
		con.connect();
		String respStr;
		//System.out.println(endPoint.toString());
		if (con.getResponseCode() != HttpURLConnection.HTTP_OK) {
			ResponseDataHandler.handlError(con.getErrorStream());
		} else {
			System.out.println("MARK 2");
			respStr = getResponseString(con.getInputStream());
			ResponseDataHandler.handleResponse(command, new StringInputStream(respStr));
		}
		con.disconnect();
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
	public boolean createVM() {
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
		System.out.println("Under devoleping.......");
		return true;
	}

	@Override
	public boolean listVMs() {
		// TODO Auto-generated method stub
		System.out.println("MARK 1...................................");
		QueryInfo qi = new QueryInfo();

		String id = null;

		qi.putValue("Action", "DescribeInstances");

		if (id != null)
			qi.putValue("InstanceId.1", id);
		
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

		
    	executeQuery(Command.DESCRIBE_INSTANCE,cloud.getEndPoint(), query);

		
    	
    	return true;
	}

	@Override
	public boolean destroyVM(String id) {
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

    	executeQuery(Command.TERMINATE_INSTANCE, cloud.getEndPoint(), query);
		
		
    	return true;
	}

	@Override
	public boolean startVM(String para) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean suspendVM(String para) {
		// TODO Auto-generated method stub
		return false;
	}


	
}
