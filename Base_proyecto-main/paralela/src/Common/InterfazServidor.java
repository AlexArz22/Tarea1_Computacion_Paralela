package Common;

import java.io.IOException;
import java.net.MalformedURLException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.ArrayList;

public interface InterfazServidor extends Remote
{
	public int generarComprobante(String nombreJugador, String nombreProducto, String tipoProducto, double monto) throws RemoteException;
    public Comprobante obtenerComprobante(int idComprobante, String name) throws RemoteException;
    public Skin obtenerPrecioSkin(String nombre) throws RemoteException;
	public ArrayList<Skin> obtenerSkinsPersonaje(String nombrePersonaje) throws RemoteException;
	public boolean registrarJugador(String nombre, int contraseña) throws RemoteException;
	public boolean iniciarSesion(String nombre, int contraseña) throws RemoteException;
	public ArrayList<RiotPoints> obtenerPreciosRp () throws RemoteException;
	public void cambiarContrasena(String nombre, int contraseña) throws RemoteException;
	public void cancelarCompra(int idComprobante, String name) throws RemoteException;
	public int heartbeat() throws RemoteException;
}