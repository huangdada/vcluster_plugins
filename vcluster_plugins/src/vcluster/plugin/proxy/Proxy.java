package vcluster.plugin.proxy;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.List;

import vcluster.plugman.CloudInterface;

public class Proxy implements CloudInterface {
	
	public static void main(String[] arg){
	    String cmdLine = "";
	    Proxy proxy = new Proxy();
	    /* prompt */
	   do{
		    System.out.print("vcluter > ");
			
		    InputStreamReader input = new InputStreamReader(System.in);
		    BufferedReader reader = new BufferedReader(input);
		    
		    try {
			    /* get a command string */
		    	cmdLine = reader.readLine(); 
		    }
		    catch(Exception e){e.printStackTrace();}	
		    
		    String[] cmds = cmdLine.split(" ");
			if(cmds[1].equalsIgnoreCase("list")){
				proxy.listVMs();
			}else if(cmds[1].equalsIgnoreCase("create")){
				int nums = Integer.parseInt(cmds[2]);
				proxy.createVM(nums);
			}else if(cmds[1].equalsIgnoreCase("destroy")){
						proxy.destroyVM(cmds[2]);
			}else{
				proxy.socketToproxy(cmdLine);
			}
	   }while(!cmdLine.equals("quit"));
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
	
	private boolean socketToproxy(String cmd){
		String cmdLine=cmd;
		 Socket socket = null;
	        BufferedReader in = null;
	        DataOutputStream out = null;

	        try {
	        	socket = new Socket(addr, port);
	        	
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
	    	        
	    	        if (!str.equals(temp))
	    	        	System.out.println(str);
	            	cbuf[0] = '\0';
	            	temp = str;
	            }
	            
	        } catch (UnknownHostException e) {
	    		System.out.print("ERROR: " +e.getMessage());
	            closeStream(in, out, socket);
	        } catch (IOException e) {
	    		System.out.print("ERROR: " +e.getMessage());
	            closeStream(in, out, socket);
	        }
	        
	        closeStream(in, out, socket);
	        return true;
	}
	
	
	@Override
	public boolean RegisterCloud(List<String> configurations) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean createVM(int maxCount) {
		// TODO Auto-generated method stub
		String cmdLine="onevm create OpenNebula/vcluster.one -m "+ maxCount;		
			return this.socketToproxy(cmdLine);
	}

	@Override
	public boolean listVMs() {
		// TODO Auto-generated method stub
		String cmdLine="onevm list";
		 		
			return this.socketToproxy(cmdLine);
	}

	@Override
	public boolean destroyVM(String id) {
		// TODO Auto-generated method stub
		String cmdLine = "onevm delete "+id;
	//	System.out.println(cmdLine);
		return this.socketToproxy(cmdLine);
		 
	}

	@Override
	public boolean startVM(String id) {
		// TODO Auto-generated method stub
		String cmdLine="onevm resume "+id;
 		
		return this.socketToproxy(cmdLine);
	}

	@Override
	public boolean suspendVM(String id) {
		// TODO Auto-generated method stub
		String cmdLine = "onevm suspend "+id;
		this.socketToproxy(cmdLine);
		return false;
	}

	@Override
	public String getInfo() {
		// TODO Auto-generated method stub
		return null;
	}
	
	private String addr = "fcl301.fnal.gov";
	private int port = 9734;

}
