package ets.genielog.appwatcher_3.profiling.core;

import java.io.File;

import android.app.ActivityManager;
import android.content.Context;
import android.os.CountDownTimer;
import android.os.Debug.MemoryInfo;
import android.util.Log;
import ets.genielog.appwatcher_3.profiling.ipc.IpcMessage.ipcAction;
import ets.genielog.appwatcher_3.profiling.ipc.IpcMessage.ipcData;
import ets.genielog.appwatcher_3.profiling.ipc.IpcMessage.ipcMessage;
import ets.genielog.appwatcher_3.profiling.ipc.IpcService;
import ets.genielog.appwatcher_3.profiling.ipc.IpcService.ipcClientListener;
import ets.genielog.appwatcher_3.profiling.model.NetworkInfo.networkInfo;
import ets.genielog.appwatcher_3.profiling.model.OsInfo.osInfo;
import ets.genielog.appwatcher_3.profiling.model.ProcessInfo.processInfo;
import ets.genielog.appwatcher_3.profiling.util.Config;
import ets.genielog.appwatcher_3.profiling.model.Process;

/**
 * This is the Profiler manager. Through this class you communicate with the
 * core engine define in cc (see jni). It implements the
 * {@link ipcClientListener}. The {@link IpcService} class let us add request
 * and manage them in order to send them. This class contains all the profiling
 * data dealing with {@link Battery}, {@link Processes}, {@link Memory},
 * {@link Network} and {@link Storage}.
 * 
 * @author alexisdet
 * 
 */
public class State implements ipcClientListener {

	private static final String TAG = "State - ";

	/** The current context */
	private Context mContext;

	/** The ipc client */
	private IpcService ipcService;

	/** The battery manager */
	private Battery mBatteryManager;
	/** The connection manager */
	private Network mNetworkManager;
	/** The memory manager */
	private Memory mMemoryManager;
	/** The cpu manager */
	private Processes mProcessManager;

	/** PID of the process which will be profiled */
	public static int PID = -1;

	/** Set all the profiling action */
	public static final ipcAction allAction[] = new ipcAction[] {
			ipcAction.PROCESS, ipcAction.OS, ipcAction.NETWORK, ipcAction.CPU };

	/** Set the process profiling action */
	public static final ipcAction processAction[] = new ipcAction[] {
			ipcAction.PROCESS, ipcAction.OS, ipcAction.CPU };

	/**
	 * Constructor
	 */
	public State() {
	}

	/**
	 * Constructor
	 * 
	 * @param context
	 *            - the current context
	 */
	public State(Context context) {
		this.mContext = context;
	}
	
	public String send() {

		return null;
	}

	/**
	 * Initialize the ipc client and the data
	 */
	public void initProfiler() {
		Log.d(Config.TAG_APP, TAG + "initProfiler");

		// IPC service
		IpcService.Initialize(mContext);
		this.ipcService = IpcService.getInstance();
		// Battery
		this.mBatteryManager = Battery.getInstance();
		// Network
		this.mNetworkManager = Network.getInstance();
		// RAM
		this.mMemoryManager = Memory.getInstance();
		// CPU
		this.mProcessManager = Processes.getInstance();

		// Create folder
		new File(Config.APP_PATH).mkdir();
		

		// Start the count down until which the profile engine is collecting
		// data for the framework, and the specify process, when noticed
		/**new CountDownTimer(10000000, 1000) {
0
			public void onFinish() {
			}

			@Override
			public void onTick(long millisUntilFinished) {
				if ((millisUntilFinished & 1) == 0)
					requestProfileStateForProcess(State.processAction,
							android.os.Process.myPid());
				else if (PID != -1)
					requestProfileStateForProcess(State.processAction, PID);
			}
		}.start();**/
	}

	/**
	 * This method stop the profiler process
	 */
	public void stopProfiler() {
		Log.d(TAG, "stopProfiler");
		// disconnect the ipc client socket
		IpcService.getInstance().disconnect();
		// kill the process
		//android.os.Process.killProcess(android.os.Process.myPid());
	}

	/**
	 * Add a request to the ipc service command queue
	 * 
	 * @param cmd
	 *            - array of ipcAction to perform
	 * @param pid
	 *            - a process id
	 */
	public boolean requestProfileStateForProcess(ipcAction cmd[], int pid) {
		Log.d(Config.TAG_APP, TAG + "request");
		return ipcService.addRequest(cmd, pid, 0, this);
	}

	@Override
	public void onRecvData(ipcMessage result) {

		// if attempt fail, add request again
		if (result == null) {
			// do something
			return;
		}

		clearDataSet();

		for (int index = 0; index < result.getDataCount(); index++) {
			try {
				// get the data
				ipcData rawData = result.getData(index);

				// process the OS data
				if (rawData.getAction() == ipcAction.OS) {
					mMemoryManager.setOsData(osInfo.parseFrom(rawData
							.getPayload(0)));
				}
				// process the Network data
				if (rawData.getAction() == ipcAction.NETWORK) {
					for (int count = 0; count < rawData.getPayloadCount(); count++) {
						networkInfo nwInfo = networkInfo.parseFrom(rawData
								.getPayload(count));

						String tp = mNetworkManager.getInterfaceStatus(nwInfo
								.getFlags());

						if (!tp.contains("lo"))
							if (tp.contains("up"))
								if (tp.contains("running"))
									mNetworkManager.add(nwInfo);
					}
					if (mNetworkManager.getNetworkData().isEmpty())
						networkDisconnected();
				}

				// process the Process data
				if (rawData.getAction() == ipcAction.PROCESS) {
					for (int count = 0; count < rawData.getPayloadCount(); count++) {

						processInfo psInfo = processInfo.parseFrom(rawData
								.getPayload(count));
					
						//
						// String path = Config.PROCESS_PATH + psInfo.getName()
						// + ".csv";
						//
						// File file = new File(path);
						// BufferedWriter out = new BufferedWriter(new
						// FileWriter(
						// file, true));
						// out.write(""
						// + Processes
						// .convertToUsage(psInfo.getCpuUsage())
						// .replace(",", ".")
						// + ","
						// + Memory.convertToSize(
						// (psInfo.getRss() * 1024), true)
						// .replace(",", ".") + "\n");
						// out.close();

						// check whether or not it is a native process
						if (psInfo.getUid() == 0
								|| psInfo.getName().contains("/system/")
								|| psInfo.getName().contains("/sbin/")) {
							mProcessManager
									.addNativeUsage(psInfo.getCpuUsage());
						} else {
							mProcessManager.addUserUsage(psInfo.getCpuUsage());

							// get memory information
							MemoryInfo memInfo = Memory
									.getMemoryInfoPerPID(
											((ActivityManager) mContext
													.getSystemService(Context.ACTIVITY_SERVICE)),
											psInfo.getPid());
							String memoryData = Memory.convertToSize(
									(psInfo.getRss() * 1024), true)
									+ " /  "
									+ Memory.convertToSize(
											memInfo.getTotalPss() * 1024, true)
									+ " / "
									+ Memory.convertToSize(
											memInfo.getTotalPrivateDirty() * 1024,
											true);

							mProcessManager.addProcess(new Process(psInfo
									.getName(), psInfo.getPid(), memoryData,
									Processes.convertToUsage(psInfo
											.getCpuUsage())));
						}
					}
				}
			} catch (Exception e) {
				Log.d(Config.TAG_APP, TAG + e.getMessage());
				e.printStackTrace();
			}
		}
	}

	private void networkDisconnected() {
		Log.d(Config.TAG_APP, TAG + "network disconnected");
	}



	/**
	 * Clear the data remained by the objects
	 */
	private void clearDataSet() {
		this.mNetworkManager.clearDataSet();
		this.mProcessManager.clearDataSet();
		this.mMemoryManager.clearDataSet();
	}

	/**
	 * @return the ipcService
	 */
	public IpcService getIpcService() {
		return ipcService;
	}

	/**
	 * @return the mBatteryManager
	 */
	public Battery getBatteryManager() {
		return mBatteryManager;
	}

	/**
	 * @return the mNetworkManager
	 */
	public Network getNetworkManager() {
		return mNetworkManager;
	}

	/**
	 * @return the mMemoryManager
	 */
	public Memory getMemoryManager() {
		return mMemoryManager;
	}

	/**
	 * @return the mProcessManager
	 */
	public Processes getProcessManager() {
		return mProcessManager;
	}

}