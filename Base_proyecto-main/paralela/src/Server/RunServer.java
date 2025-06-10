package Server;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

import Common.InterfazServidor;

public class RunServer 
{
	
	public static void main(String[] args)
	{
        try 
        {
            // Crea la instancia del objeto que implementa la interfaz remota
            
            Server sv = new Server();

            // Crea el registro ( es como un mapa) RMI
            Registry registry = LocateRegistry.createRegistry(1099);

            // Registra el objeto remoto en el registro bajo el nombre "RemoteInterface"
            registry.bind("ServidorPrincipal", sv);

            System.out.println("Server funcionando");      
        }
        
        catch (Exception e) 
        {
            System.err.println("Server exception: " + e.toString());
            e.printStackTrace();
        }
    }
}