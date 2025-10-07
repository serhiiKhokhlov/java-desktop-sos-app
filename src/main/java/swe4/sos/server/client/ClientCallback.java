package swe4.sos.server.client;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface ClientCallback extends Remote {
  void notifyChange() throws RemoteException;
}
