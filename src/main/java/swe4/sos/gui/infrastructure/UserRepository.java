package swe4.sos.gui.infrastructure;

import swe4.sos.gui.model.User;

import java.rmi.RemoteException;
import java.rmi.Remote;

public interface UserRepository extends Remote {
  User getUser(int id) throws RemoteException;
  User getUserByUsername(String username) throws RemoteException;
  User getUserByEmail(String email) throws RemoteException;
  void addUser(String username, String password, String email) throws RemoteException;
}
