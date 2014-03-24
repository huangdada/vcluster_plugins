package vcluster.plugin.blancer;

import java.util.TreeMap;

import vcluster.control.cloudman.Cloud;
import vcluster.control.cloudman.Host;
import vcluster.control.vmman.Vm;
import vcluster.plugins.LoadBalancer;
import vcluster.ui.Api;

public class BalancerDemo implements LoadBalancer{
	public static void main(String [] arg){
		//String str0 = String.format("%-18-s","dada");
		String str1 = String.format("%-18s","dada");
		System.out.println(str1);
	}
	

	@Override
	public void activate() {
		// TODO Auto-generated method stub
		System.out.println("--------------Load Gcloud-------------------");
		Api.loadCloud("Gcloud");
		System.out.println();
		System.out.println();
		System.out.println();
		try {
			Thread.sleep(3000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("--------------Print DataStructure-------------------");
		System.out.println();
		System.out.println();
		TreeMap<String,Cloud> ds = Api.getDataStructure();
		//String line =  "---------------------------------------------------------------------------------------";
		for(Cloud c : ds.values()){
			if(!c.isLoaded())continue;
			System.out.println("|-"+String.format("%-10s",c.getCloudName())+"-|---------------------------------------------------");
			for(Host h : c.getHostList().values()){
				System.out.println("|            |-"+String.format("%-9s",h.getId())+"-|--------------------------------------");
				for(Vm vm : h.getVmList().values()){
					String fStat = String.format("%-10s", vm.getState());
					String fPrivateIp =  String.format("%-18s", vm.getPrivateIP());
					String fActivity =  String.format("%-3s", vm.isIdle());
					String fInternalID =  String.format("%-12s", vm.getId());
					System.out.print("|            |           ");
					System.out.println("|-"+fInternalID+fStat+fActivity+fPrivateIp);							
				}
				
			}
			System.out.println("|----------------------------------------------------------------");
		}
		System.out.println("Demostartion is ending................");
		try {
			Thread.sleep(3000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

}
