package vcluster.plugin.ec2gcloud;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.Socket;
import java.net.URL;
import java.net.URLEncoder;
import java.net.UnknownHostException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;

import vcluster.control.vmman.Vm;
import vcluster.plugins.CloudInterface;

import com.sun.xml.bind.StringInputStream;




public class Ec2gcloud implements CloudInterface{

	public static void main(String[] arg){
				
	    String cmdLine = "";
	    Ec2gcloud ec = new Ec2gcloud();
	    ec.cloud.setAccessKey("AKIAISFW7SIGWGQSQI5A");
	    ec.cloud.setCloudType("public");
	    ec.cloud.setEndPoint("https://ap-northeast-1.ec2.amazonaws.com/");
	    ec.cloud.setImageName("ami-25158f24");
	    ec.cloud.setInstanceType("t1.micro");
	    ec.cloud.setSecretKey("NKM1lXWLWIeK/233BiW06iw0AHl9cTpKiIPog6yF");
	    ec.cloud.setSignatureMethod("HmacSHA256");
	    ec.cloud.setSignatureVersion("2");
	    ec.cloud.setVersion("2011-05-15");
	    ec.cloud.setKeyName("dada");
	    
	  
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
	
	
	
	Cloud cloud = new Cloud();


	
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
		        String signature = GetSignature.calculateRFC2104HMAC(new String(stringToSign), 
		        		cloud.getSecretKey(), cloud.getSignatureMethod());
		        
				return (queryString + "&Signature=" + URLEncoder.encode(signature, "UTF-8") 
						+ "&AWSAccessKeyId="+cloud.getAccessKey());
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
		
    	ArrayList<Vm> vmList = executeQuery(Command.DESCRIBE_INSTANCE,cloud.getEndPoint(), query);
		ArrayList<String> feedback = socketToproxy("onevm list");
		if(feedback!=null&&!feedback.isEmpty()&&feedback.get(0).contains("ID")){
			for(String str : feedback){
				String[] line = str.split("\\s+");
				for(Vm vm : vmList){
					if(vm.getId().contains(line[0])){
						vm.setHostname(line[7].trim());
					}
				}
				//System.out.println(line[0]+"    "+line[7]);
			}
		}
    	
    	return vmList;

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
	private static ArrayList<String> socketToproxy(String cmd){
		String cmdLine=cmd;
		ArrayList<String> feedBack = new ArrayList<String>();
		 Socket socket = null;
	        BufferedReader in = null;
	        DataOutputStream out = null;
	        //System.out.println(cmdLine);
	        try {
	        	socket = new Socket("150.183.233.60", 9734);
	        	
	            in = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"));
	            out = new DataOutputStream(socket.getOutputStream());
	            out.flush();
	            /* make an integer to unsigned int */
	            int userInput = 5;
	            userInput <<= 8;
	            userInput |=  1;
	            userInput &= 0x7FFFFFFF;

	            String s = Integer.toString(userInput);
	            byte[] b = s.getBytes();
	            
	            out.write(b, 0, b.length);
	            out.write(cmdLine.getBytes(), 0, cmdLine.getBytes().length);
	            
	            char[] cbuf = new char[1024];
	        	String temp = null;
	        	while (in.read(cbuf, 0, 1024) != -1) {
	            	String str = new String(cbuf);
	    	        str = str.trim();	    	        
	    	        if (!str.equals(temp)){
	    	        	//System.out.println(str);
	    	        	 feedBack.add(str);
	    	        }
	    	        
	    	        cbuf[0] = '\0';
	            	temp = str;
	            }
	        	
	        } catch (UnknownHostException e) {
	    		System.out.print("ERROR: " +e.getMessage());
	            closeStream(in, out, socket);
	            return feedBack;
	        } catch (IOException e) {
	    		System.out.print("ERROR: " +e.getMessage());
	            closeStream(in, out, socket);
	            return feedBack;
	        }
	        
	        closeStream(in, out, socket);
	        return feedBack;
	}
	
	private static void closeStream(BufferedReader in, DataOutputStream out, Socket socket)
	{
		try {
	        if (in != null) in.close();
	        if (out != null) out.close();
	        if (socket != null) socket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
}
