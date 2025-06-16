package server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection; 
import javax.net.ssl.HttpsURLConnection; 
import java.net.URL;
import java.net.URL;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.Normalizer;
import java.util.ArrayList;

import java.sql.PreparedStatement;
import java.util.Collections;
import java.util.Comparator;
import java.util.zip.GZIPInputStream;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;


import common.Auto;
import common.Estacion;
import common.InterfazDeServer; 
import common.RegistroCompra;

public class ServerImpl implements InterfazDeServer{
	private Lock lock = new ReentrantLock();
	private long lastReleaseTime = 0;
	private final long MIN_BLOCK_TIME_MS = 8000;  
	
	public ServerImpl() throws RemoteException {
		conectarBD();
		UnicastRemoteObject.exportObject(this, 0);
	}
	
	//PRUEBA 8 SEGUNDOS
	private boolean requestMutex(String nombreCliente) {
		lock.lock();
		try {
			long now = System.currentTimeMillis();
			long timeSinceLastRealease = now-lastReleaseTime;
			if(timeSinceLastRealease<MIN_BLOCK_TIME_MS) {
				long waitTime = MIN_BLOCK_TIME_MS-timeSinceLastRealease;
				System.out.println(nombreCliente+" espera "+waitTime+" ms para bloqueo ");
				Thread.sleep(waitTime);
			}
			System.out.println(nombreCliente+" obtuvo recurso");
			return true;
		}catch(InterruptedException e){
			System.err.println("error al querer obtener recurso");
			return false;
		}
	}
	
	private void releaseMutex(String nombreCliente) {
		lastReleaseTime=System.currentTimeMillis();
		System.out.println(nombreCliente+" libera recurso");
		lock.unlock();
	}

	private ArrayList<Auto> BD_copia = new ArrayList<>();
	
	@Override
	public void conectarBD () throws RemoteException {
		if(!requestMutex("BASE DE DATOS")) {
			throw new RemoteException("no se pudo hacer bloqueo BASE DATOS");
		}
		Connection connection = null;
		Statement query = null;
		ResultSet resultados = null;
		//PreparedStatement test = null
		BD_copia.clear();
		try {
			String url = "jdbc:mysql://localhost:3306/empresa_colectivos";
			String username = "root";
			String password_BD = "";
			
			connection = DriverManager.getConnection(url, username, password_BD);
			
			query = connection.createStatement();
			String sql = "SELECT * FROM auto";

			resultados = query.executeQuery(sql);
			
			while(resultados.next()) {
				String patente = resultados.getString("patente");
				String conductor = resultados.getString("conductor");
				String tipoCombustible = resultados.getString("tipo_combustible");
				
				
				Auto newAuto = new Auto(patente, conductor, tipoCombustible);
				
				BD_copia.add(newAuto);
				
				//System.out.println("" + patente + "" + conductor + "" + tipoCombustible);
			}
			
			//System.out.println(resultados);
			
			connection.close();	
		} catch (SQLException e) {
			e.printStackTrace();
			System.out.println("No se pudo conectar a la BD");
		}finally {
			releaseMutex("BASE DE DATOS");
		}
	}
	
	@Override
	public ArrayList<Auto> getAutos() throws RemoteException {
		return BD_copia;
	}
	
	@Override
	public long tiempoRestanteBloqueo() throws RemoteException {
	    long now = System.currentTimeMillis();
	    long timeSinceLastRelease = now - lastReleaseTime;

	    if (timeSinceLastRelease < MIN_BLOCK_TIME_MS) {
	        return MIN_BLOCK_TIME_MS - timeSinceLastRelease;
	    } else {
	        return 0;
	    }
	}
	
	@Override
	public Auto Auto(String patente, String conductor, String tipoCombustible) throws RemoteException{
		Auto auto = new Auto(patente, conductor, tipoCombustible);
		return auto;
	}
	
	@Override
	public boolean estaPatente(String patente)throws RemoteException{
		for(Auto auto : BD_copia) {
	    	if (auto.getPatente().equals(patente)) return true;
		}
		return false;
	}
	
	
	@Override
	public boolean agregarAuto(Auto auto) throws RemoteException{
		if(!requestMutex(auto.getPatente())) {
			throw new RemoteException("no se pudo hacer bloqueo AgregarAuto");
		}
		
		try {
			if (insertar_BD(auto)) {
				BD_copia.add(auto);
				return true;
			}else return false;
		}finally {
			releaseMutex(auto.getPatente());
		}
	}
	
	public boolean insertar_BD(Auto auto) {
		Connection connection = null;
		PreparedStatement ps = null;
	    try {
	        String url = "jdbc:mysql://localhost:3306/empresa_colectivos";
	        String username = "root";
	        String password_BD = "";

	        connection = DriverManager.getConnection(url, username, password_BD);

	        String sql = "INSERT INTO auto (patente, conductor, tipo_combustible) VALUES (?, ?, ?)";
	        ps = connection.prepareStatement(sql);
	        ps.setString(1, auto.getPatente());
	        ps.setString(2, auto.getConductor());
	        ps.setString(3, auto.getTipoCombustible());

	        int filas = ps.executeUpdate();

	        if (filas > 0) {
	            System.out.println("Auto insertado correctamente.\n");
	            return true;
	        } else {
	            System.out.println("No se insertó el auto.");
	            return false;
	        }
	    } catch (SQLException e) {
			e.printStackTrace();
			System.out.println("No se pudo conectar a la BD");
			return false;
		}
	}
	
	public boolean eliminarAuto(Auto auto) throws RemoteException {
	    String patente = auto.getPatente();
	    if (!requestMutex(patente)) {
	        throw new RemoteException("No se pudo bloquear para eliminar auto");
	    }
	    try {
	        if (eliminar_BD(patente)) {
	            BD_copia.removeIf(a -> a.getPatente().equals(patente));
	            return true;
	        } else {
	            return false; 
	        }
	    } finally {
	        releaseMutex(patente);
	    }
	}


	public boolean eliminar_BD(String patente) {
	    Connection connection = null;
	    PreparedStatement ps = null;
	    try {
	        String url = "jdbc:mysql://localhost:3306/empresa_colectivos";
	        String username = "root";
	        String password_BD = "";

	        connection = DriverManager.getConnection(url, username, password_BD);

	        String sql = "DELETE FROM auto WHERE patente = ?";
	        ps = connection.prepareStatement(sql);
	        ps.setString(1, patente);

	        int filas = ps.executeUpdate();

	        if (filas == 0) {
	            System.out.println("No se encontró un auto con esa patente en la base de datos.");
	            return false;
	        }
	    } catch (SQLException e) {
	        e.printStackTrace();
	        System.out.println("Error al conectar o eliminar en la base de datos.");
	    }
	    return true;
	}
	
	@Override
	public boolean modificarConductor(String patente, String nuevoConductor) throws RemoteException {
		if(!requestMutex(patente)) {
			throw new RemoteException("no se pudo hacer bloqueo getHistorialCompras");
		}
	    Connection connection = null;
	    PreparedStatement ps = null;
	    boolean exito = false;

	    try {
	        String url = "jdbc:mysql://localhost:3306/empresa_colectivos";
	        String username = "root";
	        String password_BD = "";

	        connection = DriverManager.getConnection(url, username, password_BD);

	        String sql = "UPDATE auto SET conductor = ? WHERE patente = ?";
	        ps = connection.prepareStatement(sql);
	        ps.setString(1, nuevoConductor);
	        ps.setString(2, patente);

	        int filas = ps.executeUpdate();

	        if (filas > 0) {
	            System.out.println("Conductor modificado correctamente para la patente: " + patente);
	            exito = true;
	        } else {
	            System.out.println("No se encontró un auto con la patente indicada.");
	        }

	    } catch (SQLException e) {
	        e.printStackTrace();
	        System.out.println("Error al modificar el conductor en la BD.");
	    } finally {
	    	releaseMutex(patente);
	        try {
	            if (ps != null) ps.close();
	            if (connection != null) connection.close();
	        } catch (SQLException e) {
	            e.printStackTrace();
	        }
	    }
	    
	    //desconectarse y volver a conectar para actualizar arreglo local de autos
	    if (exito) {
	        conectarBD(); 
	    }
	    return exito;
	}

	
	@Override
	public String getToken() throws RemoteException {
	    String email = "davidm2201@hotmail.com";
	    String password = "proyectoparalela1";
	    String urlString = "https://api.cne.cl/api/login?email=" + email + "&password=" + password;

	    try {
	        URL url = new URL(urlString);
	        HttpsURLConnection con = (HttpsURLConnection) url.openConnection();
	        con.setRequestMethod("POST");

	        int status = con.getResponseCode();
	        if (status != 200) {
	            System.out.println("Error al autenticar. Código HTTP: " + status);
	            return null;
	        }

	        BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream(), "UTF-8"));
	        StringBuilder content = new StringBuilder();
	        String inputLine;
	        while ((inputLine = in.readLine()) != null) {
	            content.append(inputLine);
	        }
	        in.close();
	        con.disconnect();

	        String response = content.toString();

	        ObjectMapper mapper = new ObjectMapper();
	        JsonNode root = mapper.readTree(response);
	        String token = root.path("token").asText(null);  

	        return token;

	    } catch (Exception e) {
	        System.out.println("Error al obtener token: " + e.getMessage());
	        return null;
	    }
	}

	@Override
	public ArrayList<Estacion> getDataFromApi() throws RemoteException {
	    String token = getToken();
	    if (token == null) {
	        throw new RemoteException("No se pudo obtener el token de autenticación.");
	    }

	    ArrayList<Estacion> estaciones = new ArrayList<>();

	    try {
	        URL apiUrl = new URL("https://api.cne.cl/api/v4/estaciones");
	        HttpURLConnection connection = (HttpURLConnection) apiUrl.openConnection();

	        connection.setRequestMethod("GET");
	        connection.setRequestProperty("Authorization", "Bearer " + token);
	        connection.setRequestProperty("Accept-Encoding", "gzip");

	        int responseCode = connection.getResponseCode();

	        if (responseCode == HttpURLConnection.HTTP_OK) {
	            InputStream inputStream;
	            String encoding = connection.getContentEncoding();

	            if ("gzip".equalsIgnoreCase(encoding)) {
	                inputStream = new GZIPInputStream(connection.getInputStream());
	            } else {
	                inputStream = connection.getInputStream();
	            }

	            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
	            StringBuilder responseBuilder = new StringBuilder();
	            String line;
	            while ((line = reader.readLine()) != null) {
	                responseBuilder.append(line);
	            }
	            reader.close();

	            String json = responseBuilder.toString();

	            ObjectMapper objectMapper = new ObjectMapper();
	            JsonNode root = objectMapper.readTree(json);

	            for (JsonNode estacion : root) {
	                String comunaActual = estacion.path("ubicacion").path("nombre_comuna").asText();
	                String direccion = estacion.path("ubicacion").path("direccion").asText();
	                String marcaActual = estacion.path("distribuidor").path("marca").asText();
	                String razonSocial = estacion.path("razon_social").asText();
	                
	                String precio93 = estacion.path("precios").path("93").path("precio").asText(null);
	                String precio95 = estacion.path("precios").path("95").path("precio").asText(null);
	                String precio97 = estacion.path("precios").path("97").path("precio").asText(null);
	                String precioDi  = estacion.path("precios").path("DI").path("precio").asText(null);
	                String precioKe  = estacion.path("precios").path("KE").path("precio").asText(null);
	                
	                Estacion estacionObjeto = new Estacion(
	                    marcaActual, comunaActual, direccion,
	                    precio93, precio95, precio97, precioDi, precioKe,razonSocial
	                );
	                estaciones.add(estacionObjeto);
	            }
	        } else {
	            throw new RemoteException("Error al conectar a la API. Código de respuesta: " + responseCode);
	        }

	    } catch (Exception e) {
	        e.printStackTrace();
	        throw new RemoteException("Error al obtener o procesar los datos de la API.", e);
	    }

	    return estaciones;
	}
	
	private String normalize(String input) {
		if (input == null) return "";
        String normalized = Normalizer.normalize(input, Normalizer.Form.NFD);
        return normalized.replaceAll("\\p{M}", "").toLowerCase();
	}

	@Override
	public ArrayList<Estacion> getBencinerasPorComunaYMarca(String comuna, String marca) throws RemoteException, JsonProcessingException {
		
		ArrayList<Estacion> todasLasEstaciones = getDataFromApi();
	    ArrayList<Estacion> resultado = new ArrayList<>();

	    for (Estacion estacion : todasLasEstaciones) {
	    	if (normalize(estacion.getComunaActual()).equals(normalize(comuna)) && normalize(estacion.getMarcaActual()).equals(normalize(marca))) {
	    		resultado.add(estacion);
	        }
	    }
	    return resultado;
	}

	@Override
	public ArrayList<Estacion> getPrecioxComuna(String tipoDeCombustible, String comuna) throws RemoteException {
	    ArrayList<Estacion> todasLasEstaciones = getDataFromApi();
	    ArrayList<Estacion> resultado = new ArrayList<>();

	    for (Estacion estacion : todasLasEstaciones) {
	        if (normalize(estacion.getComunaActual()).equalsIgnoreCase(normalize(comuna))) {
	        	String precio = estacion.getPrecio(tipoDeCombustible);
	            if (precio != null && !precio.isEmpty() && !precio.equals("0")) {
	                resultado.add(estacion);
	            }
	        }
	    }

	    Collections.sort(resultado, new Comparator<Estacion>() {
	        @Override
	        public int compare(Estacion e1, Estacion e2) {
	            try {
	                String p1 = e1.getPrecio(tipoDeCombustible);
	                String p2 = e2.getPrecio(tipoDeCombustible);

	                if (p1 == null || p1.isEmpty()) p1 = "0";
	                if (p2 == null || p2.isEmpty()) p2 = "0";

	                Double precio1 = Double.parseDouble(p1);
	                Double precio2 = Double.parseDouble(p2);

	                return precio1.compareTo(precio2);
	            } catch (NumberFormatException e) {
	                return 0;
	            }
	        }
	    });

	    return resultado;
	}
	
	@Override
	public ArrayList<RegistroCompra> getHistorialCompras(String patente) throws RemoteException {
	    Connection connection = null;
	    PreparedStatement ps = null;
	    ResultSet resultados = null;
	    ArrayList<RegistroCompra> historial = new ArrayList<>();
	    
	    try {
	        String url = "jdbc:mysql://localhost:3306/empresa_colectivos";
	        String username = "root";
	        String password_BD = "";
	        
	        connection = DriverManager.getConnection(url, username, password_BD);
	        
	        String sql = "SELECT * FROM historial_compras WHERE patente = ? ORDER BY fecha DESC";
	        ps = connection.prepareStatement(sql);
	        ps.setString(1, patente);
	        
	        resultados = ps.executeQuery();
	        
	        while(resultados.next()) {
	            int idCompra = resultados.getInt("id");
	            String patenteAuto = resultados.getString("patente");
	            float litros = resultados.getFloat("litros");
	            float gastoTotal = resultados.getFloat("costo");
	            String fechaCompra = resultados.getString("fecha");
	            
	            RegistroCompra registro = new RegistroCompra(idCompra, patenteAuto, litros, gastoTotal, fechaCompra);
	            historial.add(registro);
	        }
	        
	        connection.close();
	        
	    } catch (SQLException e) {
	        e.printStackTrace();
	        System.out.println("No se pudo conectar a la BD o hubo un error en la consulta");
	    }
	    
	    return historial;
	}
	
	@Override
	public void agregarCompra(RegistroCompra compra) throws RemoteException, IOException, SQLException{
		if(!requestMutex(compra.getPatente())) {
			throw new RemoteException("no se pudo hacer bloqueo agregarCompra");
		}
		
	    Connection connection = null;
	    PreparedStatement ps = null;

	    try {
	        String url = "jdbc:mysql://localhost:3306/empresa_colectivos";
	        String username = "root";
	        String password_BD = "";

	        connection = DriverManager.getConnection(url, username, password_BD);

	        String insertSql = "INSERT INTO historial_compras (patente, litros, costo, fecha) VALUES (?, ?, ?, ?)";
	        ps = connection.prepareStatement(insertSql);
	        ps.setString(1, compra.getPatente());
	        ps.setFloat(2, compra.getLitros());
	        ps.setFloat(3, compra.getCosto());
	        ps.setString(4, compra.getFecha());

	        ps.executeUpdate();

	    } finally {
	    	releaseMutex(compra.getPatente());
	        if (ps != null) ps.close();
	        if (connection != null) connection.close();
	    }
	}
	
	public int heartbeat() throws RemoteException {
		return 0;
	}
	
}
