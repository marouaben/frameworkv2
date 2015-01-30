package ets.genielog.appwatcher_3;


import java.util.ArrayList;
import java.util.List;

/**
 * Class used to filter a sequence of system calls. It is able to take a sequence and
 * return a sequence with only the syscalls we want to monitor.
 * @author trivh
 *
 */
public class SysCallFilter {
	
	/** List of syscall we don't want to monitor. **/
	private List<String> sysCallList;
	
	/** Constructor only setups attribute*/
	public SysCallFilter(ArrayList<String> sysCallList){
		this.sysCallList = sysCallList;
	}
	
	/** Checks if a syscall is in the list of ignored syscalls.
	 * @param syscall: syscall to check
	 * @return true is is not in the list  */
	public boolean isMonitored(String syscall){
		for(int i = 0; i < sysCallList.size(); i++){
			if(syscall.equals(sysCallList.get(i))){
				return false;
			}
		}
		return true;	
	}

}
