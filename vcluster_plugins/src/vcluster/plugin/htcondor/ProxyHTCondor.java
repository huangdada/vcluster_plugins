package vcluster.plugin.htcondor;

import java.io.File;

import vcluster.control.batchsysman.PoolStatus;
import vcluster.control.batchsysman.QStatus;
import vcluster.plugins.BatchInterface;

public class ProxyHTCondor implements BatchInterface{



	@Override
	public boolean ConnectTo(File conf) {
		// TODO Auto-generated method stub
		return false;
	}


	@Override
	public PoolStatus getPoolStatus() {
		// TODO Auto-generated method stub
		return new CheckCondor().getPool();
	}


	@Override
	public QStatus getQStatus() {
		// TODO Auto-generated method stub
		return new CheckCondor().getQ();
	}

}
