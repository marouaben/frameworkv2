package ets.genielog.appwatcher_3;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

import android.util.Log;

import com.google.gson.Gson;

/**
 * Class used to run a strace command and Can run a ps or a strace command only.
 * 
 * @author trivh
 * 
 */
public class StraceScanCommand {

	/** Straced process id and name */
	private int pid = -1;
	private String name = "";
	/** Trace length */
	private int lentrace;
	/** boolean to set to false to stop strace */
	private boolean keepGoing;
	/** Filter with list of syscalls to ignore */
	private SysCallFilter sysCallFilter;

	/** Output and error stream threads. */
	public StreamToTrace fluxSortie;
	public StreamDisplay fluxErreur;
	/** Process */
	private java.lang.Process process;
	private int stracePid; // PID of strace process
	private TraceScanService context;

	private Model model;

	/**
	 * Constructor setups the name of the process to strace, and trace making
	 * parmeters.
	 * 
	 * @param name
	 *            : process name
	 * @param nbtrace
	 *            : number of traces to make
	 * @param lentrace
	 *            : length of traces to make
	 * */
	public StraceScanCommand(Model model, String name, int tracelen,
			SysCallFilter sysc, TraceScanService context) {
		this.name = name;
		this.lentrace = tracelen;
		keepGoing = true;
		this.model = model;
		sysCallFilter = sysc;
		this.context = context;

	}

	/**
	 * Used to run strace command. Will run a ps to get the pid from application
	 * name. Then runs strace with the pid found. Error stream is printed on
	 * console while Output stream is filtered and converted into a valid trace.
	 * Once enough traces have been created, the process is stopped.
	 */
	public void runStrace() {

		try {
			while ((pid <= 0) && (keepGoing)) {
				pid = getPid(name);
				Thread.sleep(3000);
			}
		} catch (ArrayIndexOutOfBoundsException e) {
			System.out
					.println("Stace Command error: can't retreive process PID.");
			System.out.println("Array Index out of bounds: application may "
					+ "be not running");
			e.printStackTrace();
			return;
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (pid > 0) {
			System.out.println("pid : " + pid);
			strace(pid);
			strace(pid);
			// Send extracted traces back to context
			System.out
					.println("Sequences extracted; traces are being processed. Please wait.");
		}

	}

	/**
	 * Gets the PID of process from its name. Do so by running ps and grep
	 * commands. If ps command fails, returns -1.
	 * 
	 * @param name
	 * @return
	 */
	private int getPid(String name) {
		String comm = "ps | /data/busybox/grep " + name
				+ " | /data/busybox/cut -c10-15";

		int read;
		byte[] buffer = new byte[4096];
		String erreur = new String();
		String line = "";
		String Getline = "";

		int res = -1;
		try {
			Process p = Runtime.getRuntime().exec("su");
			InputStream es = p.getErrorStream();
			DataOutputStream os = new DataOutputStream(p.getOutputStream());
			DataInputStream osRes = new DataInputStream(p.getInputStream());
			BufferedReader d = new BufferedReader(new InputStreamReader(osRes));

			os.writeBytes(comm + "\n");
			os.writeBytes("exit\n");
			os.flush();
			os.close();

			while ((line = d.readLine()) != null) {

				Getline = line;
				System.out.println("Getline= " + Getline);

			}

			while ((read = es.read(buffer)) > 0) {
				erreur += new String(buffer, 0, read);
			}

			p.waitFor();
			Log.e("log_tag", erreur.trim() + " (" + p.exitValue() + ")");

		} catch (IOException e) {
			Log.e("log_tag", e.getMessage());

		} catch (InterruptedException e) {
			Log.e("log_tag", e.getMessage());

		}

		if (((erreur.trim().isEmpty())) || (erreur == null)) {
			if (!(Getline.isEmpty()) || (Getline != null)) {
				String[] spl = Getline.split("\\s+"); // by space
				if ((spl[0].trim()) != "") {
					res = Integer.valueOf(spl[0].trim());
				}

				System.out.println("res:\n" + res);

			}

		} else {
			System.out.println("err:\n" + erreur);
		}

		return res;

	}

	/**
	 * Executes a strace command as root user. Attach error and output streams
	 * as class attribute for further interaction.
	 * 
	 * @param command
	 */
	private void strace(int pid) {

		try {
			System.out.println("Running strace on process " + pid);
			// run strace
			process = Runtime.getRuntime().exec("su -c strace -q -p " + pid);

			// Get Pid of process we just ran to be able to kill it later.
			stracePid = getPid("strace");

			// create output and error streams
			fluxSortie = new StreamToTrace(process.getInputStream());
			fluxErreur = new StreamDisplay(process.getErrorStream());

			// run streams on separate threads
			new Thread(fluxSortie).start();
			new Thread(fluxErreur).start();
			process.waitFor();

		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		TraceManager.rmStrace(name);

	}

	/**
	 * Stops an ongoing strace
	 */
	public void stop() {
		System.out
				.println("Stopping strace... make sure the loop actually ended");
		keepGoing = false;
	}

	/**
	 * Kills a root process. Necessary to kill strace we run.
	 * 
	 * @param Pid
	 *            : pid of strace to kill
	 */
	private void killSuProc(int Pid) {
		try {
			// Run command
			Process ps = Runtime.getRuntime().exec("su -c kill " + Pid);
			System.out.println("Killing process: " + Pid);
			// Create output and error streams
			StreamDisplay fluxSortie = new StreamDisplay(ps.getInputStream());
			StreamDisplay fluxErreur = new StreamDisplay(ps.getErrorStream());
			// Runs them on seperate threads
			new Thread(fluxSortie).start();
			new Thread(fluxErreur).start();
			// Wait for ps command to finish
			ps.waitFor();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Class used to read a stream. Implements runnable to be able to run error
	 * and output stream in different threads, as needed if executed program
	 * outputs on both streams. This class displays the stream in console.
	 * 
	 * @author trivh
	 * 
	 */
	class StreamDisplay implements Runnable {

		/** Stream to display */
		protected final InputStream inputStream;

		/** Constructor */
		public StreamDisplay(InputStream inputStream) {
			this.inputStream = inputStream;
		}

		/** Getter */
		protected BufferedReader getBufferedReader(InputStream is) {
			return new BufferedReader(new InputStreamReader(is));
		}

		/**
		 * When run, uses a buffer to read the stream and display it in console
		 * line per line.
		 */
		@Override
		public void run() {
			BufferedReader br = getBufferedReader(inputStream);
			String ligne = "";
			try {
				while ((ligne = br.readLine()) != null) {
					System.out.println(ligne);
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * Class used to convert a stream into a string.
	 * 
	 * @author trivh
	 * 
	 */
	class StreamStringConverter implements Runnable {

		// Stream converted to string.
		public String output = "";

		/** Stream to convert to string */
		protected final InputStream inputStream;

		/** Constructor */
		public StreamStringConverter(InputStream inputStream) {
			this.inputStream = inputStream;
		}

		/** Getter */
		protected BufferedReader getBufferedReader(InputStream is) {
			return new BufferedReader(new InputStreamReader(is));
		}

		/**
		 * When run, converts stream into a string using a buffer.
		 */
		@Override
		public void run() {
			BufferedReader br = getBufferedReader(inputStream);
			String ligne = "";
			try {
				while ((ligne = br.readLine()) != null) {
					System.out.println("line :" + ligne); // TODO
					output = output.concat(ligne);
				}
				System.out.println("total: " + output);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * This class converts received stream into traces. The number of traces to
	 * create and their length depends on the attributes of SysCallFilter. Same
	 * thing for possibly ignored syscalls. Once enough traces have been
	 * created, the process is stopped.
	 * 
	 * @author trivh
	 * 
	 */
	class StreamToTrace implements Runnable {
		/** List of created traces */
		public ArrayList<Trace> traces;

		/** Stream to convert to trace */
		protected final InputStream inputStream;

		/** Constructor */
		public StreamToTrace(InputStream inputStream) {
			this.inputStream = inputStream;
			traces = new ArrayList<Trace>();
		}

		/** Getter */
		protected BufferedReader getBufferedReader(InputStream is) {
			return new BufferedReader(new InputStreamReader(is));
		}

		/**
		 * Runs thread as described in class comment.
		 */
		@Override
		public void run() {
			BufferedReader br = getBufferedReader(inputStream);
			String line = "";
			MyHttpInsertTrace http = new MyHttpInsertTrace(name, "");
			Trace currentTrace = new Trace();
			ArrayList<Trace> scanned;

			try {
				/*
				 * Continue as long as there is something to read and not enough
				 * traces have been made.
				 */
				while ((line = br.readLine()) != null && keepGoing) {
					line = extractSysCall(line);
					String json;
					Gson gson = new Gson();
					// If we don't monitor this syscall, just skip it.
					if (sysCallFilter.isMonitored(line)) {
						// System.out.println("Adding trace line: " + line);
						// else add it to current trace
						currentTrace.add(line);

						/*
						 * When current trace reaches wanted trace size, add it
						 * to trace array.
						 */
						if (currentTrace.size() >= lentrace) {
							// System.out.println("Adding trace to list: " +
							// currentTrace);
							scanned = new ArrayList<Trace>();
							scanned.add(currentTrace);
							//model.scanTraces(scanned);
							// traces.add(currentTrace);

							json = gson.toJson(currentTrace);
							http.setTrace(json);
							http.run();
							currentTrace = new Trace();
							System.out.println("Trace count: " + traces.size());
						}
					}

				}
				// Once done, stop process
				killSuProc(stracePid);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		/**
		 * Extracts the syscall name from a line of strace return.
		 * 
		 * @param line
		 *            : line to be processed
		 * @return: extracted syscall.
		 */
		public String extractSysCall(String line) {

			String[] split = line.split("\\(");
			return split[0];

		}

	}

}
