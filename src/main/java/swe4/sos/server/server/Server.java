package swe4.sos.server.server;

import swe4.sos.db.DBRepository;
import swe4.sos.gui.infrastructure.FakeRepository;
import swe4.sos.gui.infrastructure.Repository;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

public final class Server {
  private static final String CONNECTION_STRING = "jdbc:mysql://localhost/sos_db?autoReconnect=true&useSSL=false";
  private static final String USER_NAME         = "root";
  private static final String PASSWORD          = null;

  public static void main(String[] args) throws RemoteException, MalformedURLException {

    int registryPort      = Registry.REGISTRY_PORT;
    String serverHostName = "localhost";
    if (args.length > 0) {
      String[] hostAndPort = args[0].split(":");
      if (hostAndPort.length > 0) serverHostName = hostAndPort[0];
      if (hostAndPort.length > 1) registryPort = Integer.parseInt(hostAndPort[1]);
    }

    System.setProperty("java.rmi.server.hostname", serverHostName);

    String internalUrl = "rmi://localhost:%d/SOS".formatted(registryPort);
    String externalUrl = "rmi://%s:%d/SOS".formatted(serverHostName, registryPort);

    Repository repo = DBRepository.getInstance(CONNECTION_STRING, USER_NAME, PASSWORD);
    Remote repoStub = UnicastRemoteObject.exportObject(repo, registryPort);

    LocateRegistry.createRegistry(registryPort);
    Naming.rebind(internalUrl, repoStub);

    System.out.printf("SOS is running at '%s'%n", externalUrl);
  }
}
