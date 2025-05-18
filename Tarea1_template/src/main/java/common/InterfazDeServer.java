package common;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;


public interface InterfazDeServer extends Remote{
    public void conectarBD () throws RemoteException;

    public ArrayList <Auto> getAutos() throws RemoteException;
    public Auto Auto(String patente, String conductor, String tipoCombustible) throws RemoteException;

    public String getToken() throws RemoteException;
    public ArrayList<Estacion> getDataFromApi() throws RemoteException;

    public void agregarAuto() throws IOException, RemoteException;
	public void eliminarAuto() throws IOException, RemoteException;

    public ArrayList<Estacion> getBencinerasPorComunaYMarca(String comuna, String marca) throws RemoteException, JsonMappingException, JsonProcessingException;
    public ArrayList<Estacion> getPrecioxComuna(String tipoDeCombustible, String comuna) throws JsonMappingException, JsonProcessingException, RemoteException;
    public ArrayList<RegistroCompra> getHistorialCompras(String patente) throws RemoteException;
}