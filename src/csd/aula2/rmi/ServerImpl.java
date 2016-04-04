package csd.aula2.rmi;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.UnicastRemoteObject;

import javax.rmi.ssl.SslRMIClientSocketFactory;
import javax.rmi.ssl.SslRMIServerSocketFactory;
import javax.swing.plaf.synth.SynthSeparatorUI;

public class ServerImpl extends UnicastRemoteObject implements IServer {
	// public Map<Integer, File> map= new HashMap<Integer,File>();
	private static String rootPath;
	private static String myAddress;

	protected ServerImpl() throws RemoteException {
		super(9000, new SslRMIClientSocketFactory(), new SslRMIServerSocketFactory());

	}

	private static void readConfigFile() throws IOException {
		BufferedReader br = new BufferedReader(new FileReader("config.txt"));

		rootPath = br.readLine();
		myAddress = br.readLine();
	}

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

			Naming.rebind("/myServer", new ServerImpl());
			readConfigFile();
			System.out.println("myServer bound in registry");
		} catch (Throwable th) {
			th.printStackTrace();
		}
	}

	@Override
	public void putDirectory(String directory, String clientId) throws RemoteException {
		File f = new File(rootPath, directory);
		if (!f.exists()) {
			f.mkdir();

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

	@Override
	public void put(String directory, File value, String clientId) throws RemoteException, Exception {
		if (hasRights(clientId, directory)) {
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

	@Override
	public File get(String directory, String name, String clientId) throws RemoteException {
		if (hasRights(clientId, directory)) {
			File f = new File(rootPath + "/" + directory + name);
			return f;
		} else
			return null;
	}

	@Override
	public void delete(String directory, String name, String clientId) throws RemoteException {
		File f = new File(rootPath + "/" + directory + name);
		f.delete();
	}

	@Override
	public File[] getAll(String directory, String clientId) throws RemoteException {
		if (hasRights(clientId, directory)) {
			System.out.println("I GOT ALL");
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

	@Override
	public boolean checkDirectory(String directory) throws RemoteException {
		File f = new File(rootPath + "/" + directory);
		if (f.exists() && f.isDirectory()) {
			return true;
		} else
			return false;
	}

	private boolean hasRights(String clientID, String directory) throws RemoteException {
		boolean havePermissions = false;
		File f = new File(rootPath + "/" + directory + "/.shares");
		if (f.exists() && f.isFile()) {
			System.out.println(f.getName() + "   " + f.isHidden());
			System.out.println(clientID + "   " + directory);
			try {
				FileReader fileReader = new FileReader(f);
				BufferedReader bufferedReader = new BufferedReader(fileReader);
				String line;
				while ((line = bufferedReader.readLine()) != null) {
					if (line.equalsIgnoreCase(clientID)) {
						havePermissions = true;
						System.out.println(line + " " + line.equalsIgnoreCase(clientID));
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
