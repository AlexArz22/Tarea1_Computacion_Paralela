package server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
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

import common.Auto;
import common.Estacion;
import common.InterfazDeServer; 
import common.RegistroCompra;

public class ServerImpl implements InterfazDeServer{
	
	public ServerImpl() throws RemoteException {
		conectarBD();
		UnicastRemoteObject.exportObject(this, 0);
	}
	
	private ArrayList<Auto> BD_copia = new ArrayList<>();
	
	@Override
	public void conectarBD () throws RemoteException {
		Connection connection = null;
		Statement query = null;
		ResultSet resultados = null;
		//PreparedStatement test = null
		
		try {
			String url = "jdbc:mysql://localhost:3306/empresa_colectivos";
			String username = "root";
			String password_BD = "";
			
			connection = DriverManager.getConnection(url, username, password_BD);
			
			//Met
			query = connection.createStatement();
			String sql = "SELECT * FROM auto";
			//INSERT para agregar datos a la BD, PreparedStatement
			//Delete
			//UPDATE
			resultados = query.executeQuery(sql);
			
			while(resultados.next()) {
				//Revisar VALORES de BD
				String patente = resultados.getString("patente");
				String conductor = resultados.getString("conductor");
				String tipoCombustible = resultados.getString("tipo_combustible");
				
				
				Auto newAuto = new Auto(patente, conductor, tipoCombustible);
				
				BD_copia.add(newAuto);
				
				System.out.println("" + patente + "" + conductor + "" + tipoCombustible);
			}
			
			System.out.println(resultados);
			
			connection.close();
			
			
			
			
		} catch (SQLException e) {
			e.printStackTrace();
			System.out.println("No se pudo conectar a la BD");
		}
	}
	
	@Override
	public ArrayList<Auto> getAutos() throws RemoteException {
		return BD_copia;
	}
	
	@Override
	public Auto Auto(String patente, String conductor, String tipoCombustible) throws RemoteException{
		Auto auto = new Auto(patente, conductor, tipoCombustible);
		return auto;
	}
	
	@Override
	public void agregarAuto() throws IOException, RemoteException {
		
		BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
		
		System.out.println("Ingrese la patente del vehículo:");
		String patente = reader.readLine();
		System.out.println("");
		
		System.out.println("Ingrese el conductor del vehículo: ");
		String conductor = reader.readLine();
		System.out.println("");
		
		String tipoCombustible = "";
	    boolean entradaValida = false;

	    while (!entradaValida) {
	        System.out.println("Seleccione el tipo de combustible del vehículo:");
	        System.out.println("1. 93");
	        System.out.println("2. 95");
	        System.out.println("3. 97");
	        System.out.println("4. Kerosene");
	        System.out.println("5. Diesel");
	        System.out.print("Ingrese el número o nombre de la opción: ");
	        String entrada = reader.readLine().trim();

	        switch (entrada) {
	            case "1": tipoCombustible = "93"; entradaValida = true; break;
	            case "2": tipoCombustible = "95"; entradaValida = true; break;
	            case "3": tipoCombustible = "97"; entradaValida = true; break;
	            case "4": tipoCombustible = "KE"; entradaValida = true; break;
	            case "5": tipoCombustible = "DI"; entradaValida = true; break;
	            default:
	                System.out.println("Opción inválida. Intente nuevamente.\n");
	        }
	    }
		
		Auto auto = new Auto(patente, conductor, tipoCombustible);
		
		BD_copia.add(auto);
		insertar_BD(auto);

	}
	
	public void insertar_BD(Auto auto) {
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
	            System.out.println("Auto insertado correctamente.");
	        } else {
	            System.out.println("No se insertó el auto.");
	        }
	    } catch (SQLException e) {
			e.printStackTrace();
			System.out.println("No se pudo conectar a la BD");
		}
	}
	
	public void eliminarAuto() throws IOException, RemoteException {
	    BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

	    if (BD_copia.isEmpty()) {
	        System.out.println("No hay autos registrados para eliminar.");
	        return;
	    }

	    System.out.println("Lista de autos registrados:");
	    for (int i = 0; i < BD_copia.size(); i++) {
	        Auto auto = BD_copia.get(i);
	        System.out.println((i + 1) + ". Patente: " + auto.getPatente() + " | Conductor: " + auto.getConductor() + " | Combustible: " + auto.getTipoCombustible());
	    }

	    System.out.print("\nIngrese el número del auto que desea eliminar: ");
	    String entrada = reader.readLine().trim();

	    try {
	        int seleccion = Integer.parseInt(entrada);

	        if (seleccion < 1 || seleccion > BD_copia.size()) {
	            System.out.println("Número fuera de rango.");
	            return;
	        }

	        Auto autoSeleccionado = BD_copia.get(seleccion - 1);
	        BD_copia.remove(seleccion - 1);
	        eliminar_BD(autoSeleccionado.getPatente());

	        System.out.println("Auto eliminado correctamente: " + autoSeleccionado.getPatente());
	    } catch (NumberFormatException e) {
	        System.out.println("Entrada inválida. Debe ingresar un número.");
	    }
	}

	public void eliminar_BD(String patente) {
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
	        }
	    } catch (SQLException e) {
	        e.printStackTrace();
	        System.out.println("Error al conectar o eliminar en la base de datos.");
	    }
	}

	
	@Override
	public String getToken() throws RemoteException {
	    String email = "davidm2201@hotmail.com";
	    String password = "proyectoparalela1";
	    String urlString = "https://api.cne.cl/api/login?email=" + email + "&password=" + password;

	    try {
	        URL url = new URL(urlString);
	        HttpURLConnection con = (HttpURLConnection) url.openConnection();
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
	        String token = root.path("token").asText(null);  // Ajusta "token" si la clave es distinta en la respuesta

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

	                String precio93 = estacion.path("precios").path("93").path("precio").asText(null);
	                String precio95 = estacion.path("precios").path("95").path("precio").asText(null);
	                String precio97 = estacion.path("precios").path("97").path("precio").asText(null);
	                String precioDi  = estacion.path("precios").path("DI").path("precio").asText(null);
	                String precioKe  = estacion.path("precios").path("KE").path("precio").asText(null);

	                Estacion estacionObjeto = new Estacion(
	                    marcaActual, comunaActual, direccion,
	                    precio93, precio95, precio97, precioDi, precioKe
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
	        
	        String sql = "SELECT * FROM registro_compra WHERE patente = ? ORDER BY fecha_compra DESC";
	        ps = connection.prepareStatement(sql);
	        ps.setString(1, patente);
	        
	        resultados = ps.executeQuery();
	        
	        while(resultados.next()) {
	            int idCompra = resultados.getInt("id_compra");
	            String patenteAuto = resultados.getString("patente");
	            float litros = resultados.getFloat("litros");
	            float gastoTotal = resultados.getFloat("gasto_total");
	            String fechaCompra = resultados.getString("fecha_compra");
	            
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
	
}
