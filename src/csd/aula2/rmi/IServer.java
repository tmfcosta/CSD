package csd.aula2.rmi;

import java.io.File;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.Date;
import java.util.List;

public interface IServer
	extends Remote
{
	public void put(String directory, File value, String clientId) throws RemoteException, Exception;
	public File get(String directory, String name, String clientId) throws RemoteException;
	public File[] getAll(String directory, String clientId) throws RemoteException;
	public boolean checkDirectory(String directory) throws RemoteException;
	public void putDirectory(String directory, String clientId) throws RemoteException;
	public void delete(String directory, String name, String clientId) throws RemoteException;
}
