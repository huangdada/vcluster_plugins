package vcluster.plugin.proxy;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import vcluster.control.VMelement;
import vcluster.global.Config.VMState;
import vcluster.plugman.CloudInterface;

public class Proxy implements CloudInterface {
	
	public static void main(String[] arg){
	    String cmdLine = "";
	    Proxy proxy = new Proxy();
	    proxy.RegisterCloud(new ArrayList<String>());
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
			//proxy.socketToproxy(cmdLine);
		   
			if(cmds[1].equalsIgnoreCase("list")){
				proxy.listVMs();
			}else if(cmds[1].equalsIgnoreCase("create")){
				int nums = Integer.parseInt(cmds[2]);
				proxy.createVM(nums);
			}else if(cmds[1].equalsIgnoreCase("destroy")){
						proxy.destroyVM(cmds[2]);
			}else if(cmds[1].equalsIgnoreCase("suspend")){
				proxy.suspendVM(cmds[2]);
			}
			else if(cmds[1].equalsIgnoreCase("start")){
				proxy.startVM(cmds[2]);
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
	
	private ArrayList<String> socketToproxy(String cmd){
		String cmdLine=cmd;
		ArrayList<String> feedBack = new ArrayList<String>();
		 Socket socket = null;
	        BufferedReader in = null;
	        DataOutputStream out = null;
	        //System.out.println(cmdLine);
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
	
	
	@Override
	public boolean RegisterCloud(List<String> configurations) {
		// TODO Auto-generated method stub
		//configurations.add("username=amol");
		//configurations.add("endpoint=fcl301.fnal.gov");
		//configurations.add("port=9734");
		//configurations.add("template = OpenNebula/clean.one");
		for(String aLine : configurations){
			
			StringTokenizer st = new StringTokenizer(aLine, "=");
			
			if (!st.hasMoreTokens()) return false;
			
			/* get a keyword */
			String aKey = st.nextToken().trim();
		
			/* get a value */
			if (!st.hasMoreTokens()) return false;

			String aValue = st.nextToken().trim();
			
			if (aKey.equalsIgnoreCase("username"))
				this.username = aValue;
			else if (aKey.equalsIgnoreCase("endpoint"))
				this.addr = aValue;
			else if (aKey.equalsIgnoreCase("port"))
				this.port = Integer.parseInt(aValue);
			else if (aKey.equalsIgnoreCase("template")){
				this.template = aValue;
			}
			
		}
		return true;
	}

	@Override
	public ArrayList<VMelement> createVM(int maxCount) {
		// TODO Auto-generated method stub
		String cmdLine="onevm create "+template +" -m "+maxCount;	
		ArrayList<VMelement> vmList = new ArrayList<VMelement>();
		ArrayList<String> feedBack = socketToproxy(cmdLine);
		if(feedBack!=null&&!feedBack.isEmpty()&&feedBack.get(0).contains("ID:")){
			for(int i = 0;i<feedBack.size();i++){
				
				String [] vmEle = feedBack.get(i).split("\\s+");
				VMelement vm = new VMelement();
				vm.setId(vmEle[1]);
				vm.setState(VMState.PROLOG);
				vmList.add(vm);				
			}
		}else{
			System.out.println(feedBack.get(0));
			return null;
		}
			return vmList;
	}

	@Override
	public ArrayList<VMelement> listVMs() {
		// TODO Auto-generated method stub
		String cmdLine="onevm list "+username;
		ArrayList<VMelement> vmList = new ArrayList<VMelement>();
		ArrayList<String> feedBack = socketToproxy(cmdLine);
		if(feedBack!=null&&!feedBack.isEmpty()&&feedBack.get(0).contains("ID")){
			for(int i = 1;i<feedBack.size();i++){
				
				String [] vmEle = feedBack.get(i).split("\\s+");
				if(vmEle.length<10){
					continue;
				}
				VMelement vm = new VMelement();
				try{
					vm.setId(vmEle[0]);
					//vm.setState(vmEle[4]);
					if(vmEle[4].equalsIgnoreCase("runn")){
						vm.setState(VMState.RUNNING);
					}else if(vmEle[4].equalsIgnoreCase("stop")){
						vm.setState(VMState.STOP);
					}else if(vmEle[4].equalsIgnoreCase("Pend")){
						vm.setState(VMState.PENDING);
					}else if(vmEle[4].equalsIgnoreCase("Prol")){
						vm.setState(VMState.PROLOG);
					}else if(vmEle[4].equalsIgnoreCase("Susp")){
						vm.setState(VMState.SUSPEND);
					}else{
						vm.setState(VMState.NOT_DEFINED);
					}
				
				}catch(Exception e){
					continue;
				}
				vmList.add(vm);				
			}
		}else{
			System.out.println(feedBack.get(0));
			return null;
		}
		
		return vmList;
	}

	@Override
	public ArrayList<VMelement> destroyVM(String id) {
		// TODO Auto-generated method stub
		String cmdLine = "onevm delete "+id;
		ArrayList<VMelement> vmList = new ArrayList<VMelement>();
		ArrayList<String> feedBack = socketToproxy(cmdLine);
		if(feedBack!=null&&!feedBack.isEmpty()){
			System.out.println(feedBack.get(0));
			return null;
		}
		VMelement vm = new VMelement();
		vm.setId(id);
		vm.setState(VMState.STOP);
		vmList.add(vm);
		return vmList; 
	}

	@Override
	public ArrayList<VMelement> startVM(String id) {
		// TODO Auto-generated method stub
		String cmdLine="onevm resume "+id;
		ArrayList<VMelement> vmList = new ArrayList<VMelement>();
		ArrayList<String> feedBack = socketToproxy(cmdLine);
		if(feedBack!=null&&!feedBack.isEmpty()){
			System.out.println(feedBack.get(0));
			return null;
		}
		VMelement vm = new VMelement();
		vm.setId(id);
		vm.setState(VMState.PROLOG);
		vmList.add(vm);
		return vmList;
	}

	@Override
	public ArrayList<VMelement> suspendVM(String id) {
		// TODO Auto-generated method stub
		String cmdLine = "onevm suspend "+id;
		ArrayList<VMelement> vmList = new ArrayList<VMelement>();
		ArrayList<String> feedBack = socketToproxy(cmdLine);
		if(feedBack!=null&&!feedBack.isEmpty()){
			System.out.println(feedBack.get(0));
			return null;
		}
		VMelement vm = new VMelement();
		vm.setId(id);
		vm.setState(VMState.SUSPEND);
		vmList.add(vm);
		return vmList;
	}

	



	private String addr;
	private int port;
	private String username;
	private String template;


}
