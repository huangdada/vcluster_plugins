package vcluster.plugin.ec2gcloud;

import vcluster.global.Config;


public class PrintMsg {
	
	public static void print(vcluster.plugin.ec2gcloud.PrintMsg.DMsgType error, String msg) {
		if (error == DMsgType.ERROR)
			System.out.println(msgtype(error) + msg);
		else if (Config.DEBUG_MODE == true)
			System.out.println(msgtype(error) + msg);
	}
	
	private static String msgtype(vcluster.plugin.ec2gcloud.PrintMsg.DMsgType error) {
		switch (error) {
		case ERROR: return "\t[ERROR]\t: ";
		case INFO: return "\t[INFO]\t: ";
		case MSG: return "\t[MSG]\t: ";
		}
		/* default */
		return "[MSG]\t: ";
	}
	
	public enum DMsgType {ERROR, INFO, MSG};

}
