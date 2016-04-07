package csd.aula2.rmi;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.UnicastRemoteObject;

import javax.rmi.ssl.SslRMIClientSocketFactory;
import javax.rmi.ssl.SslRMIServerSocketFactory;

public class SIFTBox extends UnicastRemoteObject implements IServer {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	// public Map<Integer, File> map= new HashMap<Integer,File>();
	private static String rootPath;
	private static String myAddress;

	protected SIFTBox() throws RemoteException {
		super(9000, new SslRMIClientSocketFactory(), new SslRMIServerSocketFactory());

	}

	/**
	 * Read the configuration file rootPath- path of the files myAddress -
	 * server address (not used)
	 */
	private static void readConfigFile() throws IOException {
		@SuppressWarnings("resource")
		BufferedReader br = new BufferedReader(new FileReader("config.txt"));

		rootPath = br.readLine();
		myAddress = br.readLine();
	}

	/**
	 * main - only responsible for starting the RMI server and invoke the
	 * readConfigFile method
	 * 
	 * HOW TO RUN: java csd.aula2.rmi.SIFTBox On the config file there is the
	 * address- do ifconfig the rootPath- our default test:
	 * /home/osboxes/SIFTBoxServerDirs
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			System.setProperty("java.security.policy", "src/csd/aula2/rmi/policy.all");
			System.setProperty("javax.net.ssl.keyStore", "server.ks");
			System.setProperty("javax.net.ssl.keyStorePassword", "123456");

			try { // start rmiregistry
				LocateRegistry.createRegistry(1099);
			} catch (RemoteException e) {
				// if not start it
				// do nothing - already started with rmiregistry
			}

			Naming.rebind("/siftBoxServer", new SIFTBox());
			readConfigFile();
			System.out.println("SIFTBox server secure RMI bound in registry");
		} catch (Throwable th) {
			th.printStackTrace();
		}
	}

	/**
	 * Method to put a new directory on Server Side Checks if the folder exists,
	 * if it doesn't create a new folder with the .shares file Used for
	 * starting, when the clients says which folders he wants to sync
	 * 
	 * @param directory
	 *            - name of the directory to be created
	 * @param clientId
	 *            - client that made the request
	 */
	@Override
	public void putDirectory(String directory, String clientId) throws RemoteException {
		File f = new File(rootPath, directory);
		if (!f.exists()) {
			f.mkdir();
			System.out.println("Creating directory "+ directory);
			File shareFile = new File(f, ".shares");
			try {
				shareFile.createNewFile();
				FileWriter fw = new FileWriter(shareFile);
				fw.write(clientId);
				fw.flush();
				fw.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	/**
	 * Method to put a file in a directory
	 * 
	 * @param directory
	 *            - directory of the file
	 * @param value
	 *            - file that is going to be created on ServerSide
	 * @param clientId
	 *            - the id of the client that made the request to check the
	 *            rights
	 */
	@Override
	public void put(String directory, File value, String clientId) throws RemoteException, Exception {
		if (hasRights(clientId, directory)) {
			System.out.println("Creating file "+ value.getName() + " in directory "+directory);
			byte[] data = new byte[Math.round(value.length())];
			BufferedInputStream bis = new BufferedInputStream(new FileInputStream(value));
			bis.read(data, 0, data.length);
			bis.close();

			File f = new File(rootPath + "/" + directory + "/" + value.getName());
			if (f.exists()) {
				f.delete();
			}
			System.out.println(f.createNewFile());
			FileOutputStream output = new FileOutputStream(f);
			output.write(data);
			output.close();
			f.setLastModified(value.lastModified());
		}
		// map.put(key, value);
	}

	/**
	 * File that return a file to the client
	 * 
	 * @param directory
	 *            - name of the directory where the file is
	 * @param name
	 *            - name of the file
	 * @param clientId
	 *            - the id of the client that made the request to check the
	 *            rights
	 */
	@Override
	public File get(String directory, String name, String clientId) throws RemoteException {
		if (hasRights(clientId, directory)) {
			System.out.println("Getting file "+ name + " in directory "+directory);
			File f = new File(rootPath + "/" + directory + name);
			return f;
		} else
			return null;
	}

	/**
	 * Method to delete a file on ServerSide
	 * 
	 * @param directory
	 *            - directory where the file is
	 * @param name
	 *            - name of the file
	 * @param clientId
	 *            - the id of the client that made the request to check the
	 *            rights
	 */
	@Override
	public void delete(String directory, String name, String clientId) throws RemoteException {
		File f = new File(rootPath + "/" + directory + name);
		System.out.println("Deleting file "+ name + " in directory "+directory);
		if (hasRights(clientId, directory) && f.exists()) {
			f.delete();
		}
	}

	/**
	 * Method to get all the files in a given directory
	 * 
	 * @param directory
	 *            - folder name where we will get all files
	 * @param clientId
	 *            - the id of the client that made the request to check the
	 *            rights
	 */
	@Override
	public File[] getAll(String directory, String clientId) throws RemoteException {
		if (hasRights(clientId, directory)) {
			System.out.println("GETTING ALL FILES FROM: "+directory);
			File f = new File(rootPath + "/" + directory);
			if (f.isDirectory()) {
				File[] temp = f.listFiles(new FileFilter() {
					public boolean accept(File pathname) {
						return !pathname.isHidden();
					}
				});
				return temp;
			}
		}
		return null;
	}

	/*
	 * @Override public boolean checkDirectory(String directory) throws
	 * RemoteException { File f = new File(rootPath + "/" + directory); if
	 * (f.exists() && f.isDirectory()) { return true; } else return false; }
	 */

	/**
	 * Checks if a client has rights to a certain directory
	 * 
	 * @param clientId
	 *            - the id of the client that made the request to check the
	 *            rights
	 * @param directory
	 *            - directory to check if the client has right
	 */
	private boolean hasRights(String clientID, String directory) throws RemoteException {
		boolean havePermissions = false;
		File f = new File(rootPath + "/" + directory + "/.shares");
		System.out.println("Checking permission on "+ directory+ " for client "+clientID );
		if (f.exists() && f.isFile()) {
			try {
				FileReader fileReader = new FileReader(f);
				BufferedReader bufferedReader = new BufferedReader(fileReader);
				String line;
				while ((line = bufferedReader.readLine()) != null) {
					if (line.equalsIgnoreCase(clientID)) {
						havePermissions = true;
					}
				}
				bufferedReader.close();
			} catch (IOException e) {
				System.err.println("no shares files");
			}
		}

		return havePermissions;
	}
}
