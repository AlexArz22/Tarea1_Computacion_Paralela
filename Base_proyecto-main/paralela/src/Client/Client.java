package Client;

import java.io.IOException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ArrayList;

import Common.InterfazServidor;
import Common.RiotPoints;
import Common.Skin;
import Common.Comprobante;

public class Client {
    private static InterfazServidor stub;
    private String host = "localhost";
    private int primaryPort = 1099;
    private int backupPort = 1100;
    private boolean connectedToPrimary = true;
    private boolean running = true;
    
    public Client() throws RemoteException, NotBoundException {
        conectarSvPrincipal();
        startHeartbeat();
    }

    private void startHeartbeat() {
        new Thread(() -> {
            while (running) {
                try {
                	// Se manda un heartbeat cada 5 segundos
                    Thread.sleep(1000);
                    // Si estamos conectados al sv principal le mandamos una señal
                    if (connectedToPrimary) {
                        stub.heartbeat();
                    }
                    // En caso de no estar conectados al prinicpal le mandamos una señal al respaldo
                    else {
                        // Comprobar conexión al servidor de respaldo
                        stub.heartbeat();
                    }
                } catch (RemoteException e) {
                    if (connectedToPrimary) {
                        System.err.println("Heartbeat fallido, cambiando al servidor de respaldo...");
                        cambiarSvRespaldo();
                    } else {
                        System.err.println("Heartbeat fallido en el servidor de respaldo. Terminando ejecución...");
                        terminarEjecucion();
                    }
                } catch (InterruptedException e) {
                    System.err.println("Heartbeat interrumpido: " + e.getMessage());
                }
            }
        }).start();
    }

    private void conectarSvPrincipal() throws RemoteException, NotBoundException {
        stub = establecerConexion(host, primaryPort, "ServidorPrincipal");

        if (stub == null) {
            System.out.println("Conectando al servidor de respaldo...");
            cambiarSvRespaldo();
        } else {
            connectedToPrimary = true;
        }

        if (stub == null) {
            throw new RemoteException("No se pudo conectar a ningún servidor.");
        }
    }

    private void cambiarSvRespaldo() {
        stub = establecerConexion(host, backupPort, "ServidorRespaldo");
        if (stub == null) {
            System.err.println("No se pudo conectar al servidor de respaldo.");
            terminarEjecucion();
        } else {
            System.out.println("Conectado al servidor de respaldo.");
            connectedToPrimary = false;
        }
    }

    private InterfazServidor establecerConexion(String host, int port, String bindingName) {
        try {
            Registry registry = LocateRegistry.getRegistry(host, port);
            return (InterfazServidor) registry.lookup(bindingName);
        } catch (Exception e) {
            System.err.println("Excepción conectando a " + bindingName + ": " + e.toString());
            return null;
        }
    }

    private void terminarEjecucion() {
        // Lógica para terminar la ejecución del programa
        running = false;
        System.err.println("Terminando ejecución debido a la falta de respuesta del servidor de respaldo.");
        System.exit(1);
    }


    private void comprobarConexion() {
        if (connectedToPrimary) {
            System.err.println("Perdida la conexión con el servidor principal, reconectando al servidor de respaldo...");
            cambiarSvRespaldo();
        }
    }

    public Comprobante obtenerComprobante(int id, String name) throws RemoteException {
        try {
            return stub.obtenerComprobante(id, name);
        } catch (RemoteException e) {
            comprobarConexion();
            return stub != null ? stub.obtenerComprobante(id, name) : null;
        }
    }
    
    public ArrayList<Skin> obtenerSkins(String nombre) throws RemoteException {
        try {
            return stub.obtenerSkinsPersonaje(nombre);
        } catch (RemoteException e) {
            comprobarConexion();
            return stub != null ? stub.obtenerSkinsPersonaje(nombre) : null;
        }
    }
    
    public Comprobante verComprobante(int idComprobante, String name) throws RemoteException {
        try {
            return stub.obtenerComprobante(idComprobante, name);
        } catch (RemoteException e) {
            comprobarConexion();
            return stub != null ? stub.obtenerComprobante(idComprobante, name) : null;
        }
    }
    
    public boolean iniciarSesion(String nombre, int contrasena) throws RemoteException {
        try {
            return stub.iniciarSesion(nombre, contrasena);
        } catch (RemoteException e) {
            comprobarConexion();
            return stub != null ? stub.iniciarSesion(nombre, contrasena) : false;
        }
    }
    
    public boolean registrarse(String nombre, int contrasena) throws RemoteException {
        try {
            return stub.registrarJugador(nombre, contrasena);
        } catch (RemoteException e) {
            comprobarConexion();
            return stub != null ? stub.registrarJugador(nombre, contrasena) : false;
        }
    }
    
    public ArrayList<Skin> obtenerSkinPersonaje(String nombrePersonajeSkin) throws RemoteException {
        try {
            return stub.obtenerSkinsPersonaje(nombrePersonajeSkin);
        } catch (RemoteException e) {
            comprobarConexion();
            return stub != null ? stub.obtenerSkinsPersonaje(nombrePersonajeSkin) : null;
        }
    }
    
    public Skin obtenerPrecioSkin(String name) throws RemoteException {
        try {
            return stub.obtenerPrecioSkin(name);
        } catch (RemoteException e) {
            comprobarConexion();
            return stub != null ? stub.obtenerPrecioSkin(name) : null;
        }
    }
    
    public int generarComprobante(String name, String nameProd, String tipoProducto, double monto) throws RemoteException {
        try {
            return stub.generarComprobante(name, nameProd, tipoProducto, monto);
        } catch (RemoteException e) {
            comprobarConexion();
            return stub != null ? stub.generarComprobante(name, nameProd, tipoProducto, monto) : -1;
        }
    }
    
    public ArrayList<RiotPoints> obtenerPrecioRp() throws RemoteException {
        try {
            return stub.obtenerPreciosRp();
        } catch (RemoteException e) {
            comprobarConexion();
            return stub != null ? stub.obtenerPreciosRp() : null;
        }
    }

    public void cambiarContrasena(String nombre, int nuevaContrasena) throws RemoteException {
        try {
            stub.cambiarContrasena(nombre, nuevaContrasena);
        } catch (RemoteException e) {
            comprobarConexion();
            if (stub != null) {
                stub.cambiarContrasena(nombre, nuevaContrasena);
            }
        }
    }

    public void cancelarCompra(int idComprobante, String name) throws RemoteException {
        try {
            stub.cancelarCompra(idComprobante, name);
        } catch (RemoteException e) {
            comprobarConexion();
            if (stub != null) {
                stub.cancelarCompra(idComprobante, name);
            }
        }
    }
}
