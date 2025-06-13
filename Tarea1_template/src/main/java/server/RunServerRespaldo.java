package server;

import java.nio.channels.AlreadyBoundException;
import common.InterfazDeServer;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class RunServerRespaldo {

    public static void main(String[] args) throws RemoteException, AlreadyBoundException, java.rmi.AlreadyBoundException { 
        InterfazDeServer server = new ServerImpl();
        Registry registry = LocateRegistry.createRegistry(1033);
        registry.bind("serverRespaldo", server);

        System.out.println("Servidor DE RESPALDO arriba!");
        //System.out.println(server.getAutos());
    }
}