package server;

import java.nio.channels.AlreadyBoundException;
import common.InterfazDeServer;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class RunServer {

    public static void main(String[] args) throws RemoteException, AlreadyBoundException, java.rmi.AlreadyBoundException { 
        InterfazDeServer server = new ServerImpl();
        Registry registry = LocateRegistry.createRegistry(1032);
        registry.bind("server", server);

        System.out.println("Servidor arriba!");
        //System.out.println(server.getAutos());
    }
}