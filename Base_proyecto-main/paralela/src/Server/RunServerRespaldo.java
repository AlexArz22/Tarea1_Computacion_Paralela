package Server;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

import Common.InterfazServidor;

public class RunServerRespaldo {
	

	public static void main(String[] args)
	{
        try 
        {
            // Crea la instancia del objeto que implementa la interfaz remota
            
	        Server backupServer = new Server();

            // Crea el registro ( es como un mapa) RMI
            Registry registry = LocateRegistry.createRegistry(1100);

            // Registra el objeto remoto en el registro bajo el nombre "RemoteInterface"
            registry.bind("ServidorRespaldo", backupServer);

            System.out.println("Servidor de respaldo listo.");      
        }
        
        catch (Exception e) 
        {
            System.err.println("Server exception: " + e.toString());
            e.printStackTrace();
        }
    }

}
