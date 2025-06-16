package client;

import java.io.IOException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ArrayList;
import java.util.Random;
import java.util.Scanner;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.io.BufferedReader;
import java.io.InputStreamReader;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;

import common.InterfazDeServer;
import common.RegistroCompra;
import common.Auto;
import common.Estacion;

public class Client {

	private InterfazDeServer server;
    private String host = "localhost";
    private int primaryPort = 1032;
    private int backupPort = 1033;
    private boolean connectedToPrimary = true;
    private boolean running = true;
    
    public Client() {};

    public void startClient() throws RemoteException, NotBoundException {
        conectarServidorPrincipal();
        startHeartbeat();
    }

    private void conectarServidorPrincipal() {
        server = establecerConexion(host, primaryPort, "server");
        if (server == null) {
            System.out.println("No se pudo conectar al servidor principal. Intentando servidor de respaldo...");
            cambiarAServidorRespaldo();
        } else {
            connectedToPrimary = true;
            System.out.println("Conectado al servidor principal.");
        }
    }

    private void cambiarAServidorRespaldo() {
        server = establecerConexion(host, backupPort, "serverRespaldo");
        if (server == null) {
            System.err.println("No se pudo conectar al servidor de respaldo.");
            terminarEjecucion();
        } else {
            connectedToPrimary = false;
            System.out.println("Conectado al servidor de respaldo.");
        }
    }

    private InterfazDeServer establecerConexion(String host, int port, String bindingName) {
        try {
            Registry registry = LocateRegistry.getRegistry(host, port);
            return (InterfazDeServer) registry.lookup(bindingName);
        } catch (java.rmi.ConnectException e) {
            System.out.println("Servidor " + bindingName + " no está disponible. Intentando otro servidor...");
            return null;
        } catch (Exception e) {
            System.out.println("Error al conectar a " + bindingName + ": " + e.getMessage());
            return null;
        }
    }


    public void startHeartbeat() {
        new Thread(() -> {
            while (running) {
                try {
                    Thread.sleep(1000); 
                    if (server != null) {
                        if (server.heartbeat() != 0) {
                            throw new RemoteException("Heartbeat inválido");
                        }
                    }
                } catch (RemoteException e) {
                    if (connectedToPrimary) {
                        System.err.println("Fallo el servidor principal. Cambiando a servidor de respaldo...");
                        cambiarAServidorRespaldo();
                    } else {
                        System.err.print("Fallo el servidor de respaldo.");
                        terminarEjecucion();
                    }
                } catch (InterruptedException e) {
                    System.err.println("Heartbeat interrumpido: " + e.getMessage());
                }
            }
        }).start();
    }


    private void terminarEjecucion() {
    	System.err.println("Terminando cliente...");
        running = false;
        System.exit(1);
    }

    public void mostrarAutos() throws RemoteException {
    	long esperaMs = server.tiempoRestanteBloqueo();
        if (esperaMs > 0) {
            System.out.println("Recurso ocupado, esperando " + (esperaMs / 1000.0) + " segundos...");
            try {
                Thread.sleep(esperaMs);
            } catch (InterruptedException e) {
                System.out.println("Espera interrumpida.");
                return; 
            }
        }
        ArrayList<Auto> autos = server.getAutos();
        
        if(autos.isEmpty()) {
        	System.out.println("No hay autos registrados");
        	return;
        }
        
        int contador = 1;

        System.out.println("Autos:");

        for(Auto auto : autos) {

            System.out.println(contador + ". Patente: " + auto.getPatente() + ", Conductor: " + auto.getConductor() + ", Combustible: " + auto.getTipoCombustible());
            contador++;
        }
        System.out.println("");
    }
    
    public boolean validarPatente(String patente)throws RemoteException{
		// Que no sea nula o vacía
		if (patente == null || patente.trim().isEmpty()) {
			return false;
	    }

		String formato1 = "^[A-Z]{2}\\d{4}$";   // AB1234
	    String formato2 = "^[A-Z]{4}\\d{2}$";   // ABCD12
		
	    
	    if (!patente.matches(formato1) && !patente.matches(formato2)) return false;
	    long esperaMs = server.tiempoRestanteBloqueo();
        if (esperaMs > 0) {
            System.out.println("Verificando patente favor espere" + (esperaMs / 1000.0) + " segundos...");
            try {
                Thread.sleep(esperaMs);
            } catch (InterruptedException e) {
                System.out.println("Espera interrumpida.");
            }
        }
	    if(server.estaPatente(patente)) return false;
	    return true;
	}

    public void agregarAuto() throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

        System.out.println("Ingrese la patente del vehículo: (FORMATO: ABCD34 o AB1234)");
        String patente = reader.readLine();

        boolean validadorPatente = false;

        while (!validadorPatente) {
            if (validarPatente(patente)) {
                validadorPatente = true;
            } else {
                System.out.println("Formato inválido o Patente repetida, ingrese nuevamente");
                patente = reader.readLine();
            }
        }

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
            System.out.print("Ingrese el número de la opción: ");
            String entrada = reader.readLine().trim();

            switch (entrada) {
                case "1":
                    tipoCombustible = "93";
                    entradaValida = true;
                    break;
                case "2":
                    tipoCombustible = "95";
                    entradaValida = true;
                    break;
                case "3":
                    tipoCombustible = "97";
                    entradaValida = true;
                    break;
                case "4":
                    tipoCombustible = "KE";
                    entradaValida = true;
                    break;
                case "5":
                    tipoCombustible = "DI";
                    entradaValida = true;
                    break;
                default:
                    System.out.println("Opción inválida. Intente nuevamente.\n");
            }
        }

        long esperaMs = server.tiempoRestanteBloqueo();
        if (esperaMs > 0) {
            System.out.println("Recurso ocupado, esperando " + (esperaMs / 1000.0) + " segundos...");
            try {
                Thread.sleep(esperaMs);
            } catch (InterruptedException e) {
                System.out.println("Espera interrumpida.");
                return; 
            }
        }

        if (server.agregarAuto(new Auto(patente, conductor, tipoCombustible))) {
            System.out.println("Auto agregado correctamente.");
        } else {
            System.out.println("No se pudo agregar el auto.");
        }
    }



    public void quitarAuto() throws IOException {
    	
    	ArrayList<Auto> autos= server.getAutos();
    	BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

	    if (autos.isEmpty()) {
	        System.out.println("No hay autos registrados para eliminar.");
	        return;
	    }

	    System.out.println("Lista de autos registrados:");
	    for (int i = 0; i < autos.size(); i++) {
	        Auto auto = autos.get(i);
	        System.out.println((i + 1) + ". Patente: " + auto.getPatente() + " | Conductor: " + auto.getConductor() + " | Combustible: " + auto.getTipoCombustible());
	    }

	    System.out.print("\nIngrese el número del auto que desea eliminar: ");
	    String entrada = reader.readLine().trim();

	    try {
	        int seleccion = Integer.parseInt(entrada);

	        if (seleccion < 1 || seleccion > autos.size()) {
	            System.out.println("Número fuera de rango.");
	            return;
	        }
	        
	        Auto autoSeleccionado = autos.get(seleccion - 1);
	        
	        long esperaMs = server.tiempoRestanteBloqueo();
	        if (esperaMs > 0) {
	            System.out.println("Recurso ocupado, esperando " + (esperaMs / 1000.0) + " segundos...");
	            try {
	                Thread.sleep(esperaMs);
	            } catch (InterruptedException e) {
	                System.out.println("Espera interrumpida.");
	                return; 
	            }
	        }
	        
	        
	        if (server.eliminarAuto(autoSeleccionado)) {
	            System.out.println("Auto eliminado correctamente.");
	        } else {
	            System.out.println("No se pudo eliminar el auto. Es posible que ya no exista.");
	        }
	        
	    } catch (NumberFormatException e) {
	        System.out.println("Entrada inválida. Debe ingresar un número.");
	    }
    	
    }
    
    public void getDataFromApi() throws RemoteException {
        
        ArrayList<Estacion> estaciones = server.getDataFromApi();

        for (Estacion estacion : estaciones) {
            System.out.println("╔════════════════════════════════════════════════════════╗");
            System.out.println("║ "+ estacion.getRazonSocial());
            System.out.println("╠ Marca    : "+ estacion.getMarcaActual());
            System.out.println("╠ Dirección: "+ estacion.getDireccion());
            System.out.println("╠ Comuna   : "+ estacion.getComunaActual());
            System.out.println("╠ Precios:");
            System.out.println("║   93: " + (estacion.getPrecio93() != null ? estacion.getPrecio93() : "Precio no disponible"));
            System.out.println("║   95: " + (estacion.getPrecio95() != null ? estacion.getPrecio95() : "Precio no disponible"));
            System.out.println("║   97: " + (estacion.getPrecio97() != null ? estacion.getPrecio97() : "Precio no disponible"));
            System.out.println("║   DI: "  + (estacion.getPrecioDi()  != null ? estacion.getPrecioDi()  : "Precio no disponible"));
            System.out.println("║   KE: "  + (estacion.getPrecioKe()  != null ? estacion.getPrecioKe()  : "Precio no disponible"));
            System.out.println("╚════════════════════════════════════════════════════════╝\n");
        }

        System.out.println("Total de estaciones: "+ estaciones.size());
    }



    
    
	public void seleccionarAuto() throws RemoteException, JsonMappingException, JsonProcessingException {
		long esperaMs = server.tiempoRestanteBloqueo();
        if (esperaMs > 0) {
            System.out.println("Recurso ocupado, esperando " + (esperaMs / 1000.0) + " segundos...");
            try {
                Thread.sleep(esperaMs);
            } catch (InterruptedException e) {
                System.out.println("Espera interrumpida.");
                return; 
            }
        }
		
		ArrayList<Auto> autos = server.getAutos();
		
        if(autos.isEmpty()) {
        	System.out.println("No hay autos registrados");
        	return;
        }
		
		System.out.println("Seleccione un auto:");
	    for (int i = 0; i < autos.size(); i++) {
	        Auto auto = autos.get(i);
	        System.out.println((i + 1) + ". Patente: " + auto.getPatente());
	    }
	    
	    Scanner scanner = new Scanner(System.in);
	    System.out.print("Ingrese el número del auto que desea seleccionar: ");
	    int seleccion = scanner.nextInt();
	    
	    Auto autoSeleccionado = null;

	    if (seleccion < 1 || seleccion > autos.size()) {
	        System.out.println("Selección inválida.");
	        return;
	    } else {
	        autoSeleccionado = autos.get(seleccion - 1);
	        System.out.println("Auto seleccionado: Patente " + autoSeleccionado.getPatente());
	    }
	    
	    menuSeleccion(autoSeleccionado);
}

	private void menuSeleccion(Auto autoSeleccionado) throws JsonMappingException, JsonProcessingException, RemoteException {

		Scanner scanner = new Scanner(System.in);
	    boolean salir = false;
	    
	    while (!salir) {
	        System.out.println("1. Registrar una compra");
	        System.out.println("2. Buscar bencineras por comuna");
	        System.out.println("3. Ver historial de compras");
	        System.out.println("4. Modificar conductor");
	        System.out.println("5. Salir");
	        System.out.print("Seleccione una opción: ");
	        
	        int opcion = scanner.nextInt();
	        
	        switch (opcion) {
	            case 1:
	                registrarCompra(autoSeleccionado);
	                break;
	                
	            case 2:
	                buscarBencinerasPorComuna(autoSeleccionado);
	                break;
	                
	            case 4:
	            	modificarConductor(autoSeleccionado);
	                break;
	            case 5:
	                salir = true;
	                System.out.println("Saliendo...");
	                break;
	            case 3:
	            	verHistorialCompras(autoSeleccionado);
	            	break;
	                
	            default:
	                System.out.println("Opción inválida, por favor seleccione nuevamente.");
	        }
	    }
	}
	
	
    public boolean validarFecha(String fecha) {
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            LocalDate.parse(fecha, formatter);
            return true;
        } catch (DateTimeParseException e) {
            return false;
        }
    }
	

	private void registrarCompra(Auto autoSeleccionado) throws JsonMappingException, JsonProcessingException, RemoteException {

		System.out.println("Registrar compra para el auto con patente: " + autoSeleccionado.getPatente());
	    Scanner scanner = new Scanner(System.in);
	    System.out.print("Ingrese la comuna donde realizó la compra: ");
	    String comuna = scanner.nextLine();
	    System.out.print("Ingrese la marca de la estación de servicio donde compró: ");
	    String marca = scanner.nextLine();

	    ArrayList<Estacion> estaciones = server.getBencinerasPorComunaYMarca(comuna, marca);
	    
	    if (estaciones.isEmpty())  System.out.println("No se encontraron estaciones para esa comuna y marca.");
	    
	    System.out.println("Estaciones disponibles en " + comuna + " de la marca " + marca + ":");

	    for (int i = 0; i < estaciones.size(); i++) {
	        System.out.println((i + 1) + ". " + estaciones.get(i).getDireccion());
	    }

	    System.out.print("Seleccione una estación ingresando el número correspondiente: ");
	    int seleccion = scanner.nextInt();
	    scanner.nextLine(); 

	    if (seleccion < 1 || seleccion > estaciones.size()) {
	        System.out.println("Selección inválida.");
	        return;
	    }

	    Estacion estacionSeleccionada = estaciones.get(seleccion - 1);
	    System.out.println("Has seleccionado la estación en: " + estacionSeleccionada.getDireccion());
	    
	    String tipoBencinaAuto = autoSeleccionado.getTipoCombustible();
	    String precioSeleccionado = "";

	    switch (tipoBencinaAuto) {
	        case "93":
	            precioSeleccionado = estacionSeleccionada.getPrecio93();
	            break;
	        case "95":
	            precioSeleccionado = estacionSeleccionada.getPrecio95();
	            break;
	        case "97":
	            precioSeleccionado = estacionSeleccionada.getPrecio97();
	            break;
	        case "DI":
	            precioSeleccionado = estacionSeleccionada.getPrecioDi();
	            break;
	        case "KE":
	            precioSeleccionado = estacionSeleccionada.getPrecioKe();
	            break;
	        default:
	            System.out.println("Tipo de bencina no válido.");
	            return;
	            
	    }
	    
	    float gastoTotal = 0;
	    boolean entradaValida = false;

	    while (!entradaValida) {
	        System.out.println("Ingrese el gasto total en bencina (en moneda): ");
	        String entrada = scanner.nextLine();
	        try {
	            gastoTotal = Float.parseFloat(entrada);
	            entradaValida = true;
	        } catch (NumberFormatException e) {
	            System.out.println("El ingreso no corresponde a un número válido. Intente nuevamente.");
	        }
	    }
	    
	    entradaValida = false;

	    float precioPorLitro = Float.parseFloat(precioSeleccionado);
	    float litros = gastoTotal / precioPorLitro;

        System.out.println("La cantidad de litros comprados es: " + litros);
        
        
        String fechaCompra;

        while (true) {
            System.out.println("Ingrese la fecha de la compra. FORMATO: Año-Mes-Día, Ejemplo: 2000-02-28");
            fechaCompra = scanner.nextLine();

            if (validarFecha(fechaCompra)) {
                break;
            } else {
                System.out.println("Fecha inválida. Intente nuevamente.");
            }
        }
        
        RegistroCompra registroCompra = new RegistroCompra(0,autoSeleccionado.getPatente(), litros, gastoTotal, fechaCompra);
        
        long esperaMs = server.tiempoRestanteBloqueo();
        if (esperaMs > 0) {
            System.out.println("Recurso ocupado, esperando " + (esperaMs / 1000.0) + " segundos...");
            try {
                Thread.sleep(esperaMs);
            } catch (InterruptedException e) {
                System.out.println("Espera interrumpida.");
                return; 
            }
        }
        
        try {
            server.agregarCompra(registroCompra);
            System.out.println("Compra registrada en la comuna '" + comuna + "' en la estación de servicio '" + marca + "'.");
        } catch (Exception e) {
            System.out.println("NO se pudo registrar la compra");
        }
	}

	private void buscarBencinerasPorComuna(Auto autoSeleccionado) throws JsonMappingException, JsonProcessingException, RemoteException {
	    System.out.println("Buscar bencineras por comuna...");
	    Scanner scanner = new Scanner(System.in);
	    
	    System.out.println("Ingrese comuna");
	    String comuna = scanner .nextLine();

	    String tipoDeCombustible = autoSeleccionado.getTipoCombustible();
	    ArrayList<Estacion> bencineras = server.getPrecioxComuna(tipoDeCombustible,  comuna);
	    
	    if (bencineras == null || bencineras.isEmpty()) {
	        System.out.println("No se encontraron bencineras que cumplan con los requerimientos");
	        return;
	    }
	    
	    for(int i = 0 ; i < bencineras.size() ; i++) {
	    	Estacion bencineraActual = bencineras.get(i);
	    	String ubicacion = bencineraActual.getDireccion();
	    	String precio = bencineraActual.getPrecio(tipoDeCombustible);
	    	String marca = bencineraActual.getMarcaActual();
	    	System.out.println("Precio: " + precio + " | Marca: " + marca + " | Ubicación: " + ubicacion);
	    }
	}
	
	public void verHistorialCompras(Auto autoSeleccionado) throws RemoteException {
	    String patente = autoSeleccionado.getPatente();
	    long esperaMs = server.tiempoRestanteBloqueo();
        if (esperaMs > 0) {
            System.out.println("Recurso ocupado, esperando " + (esperaMs / 1000.0) + " segundos...");
            try {
                Thread.sleep(esperaMs);
            } catch (InterruptedException e) {
                System.out.println("Espera interrumpida.");
                return; 
            }
        }
	    ArrayList<RegistroCompra> historial = server.getHistorialCompras(patente);
	    
	    if (historial.isEmpty()) {
	        System.out.println("No hay registros de compra para el auto con patente: " + patente);
	        return;
	    }
	    
	    System.out.println("Historial de compras para el auto con patente: " + patente);
	    System.out.println("--------------------------------------------------");
	    System.out.println("ID | Fecha | Litros | Gasto Total");
	    System.out.println("--------------------------------------------------");
	    
	    for (RegistroCompra registro : historial) {
	        System.out.println(registro.getId() + " | " + 
	                          registro.getFecha() + " | " + 
	                          registro.getLitros() + " | " + 
	                          registro.getCosto());
	    }
	    System.out.println("--------------------------------------------------");
	}
	
	private void modificarConductor(Auto autoSeleccionado) throws RemoteException {
	    Scanner scanner = new Scanner(System.in);

	    System.out.print("Ingrese el nuevo nombre del conductor: ");
	    String nuevoConductor = scanner.nextLine();
	    
	    long esperaMs = server.tiempoRestanteBloqueo();
        if (esperaMs > 0) {
            System.out.println("Recurso ocupado, esperando " + (esperaMs / 1000.0) + " segundos...");
            try {
                Thread.sleep(esperaMs);
            } catch (InterruptedException e) {
                System.out.println("Espera interrumpida.");
                return; 
            }
        }

	    boolean exito = server.modificarConductor(autoSeleccionado.getPatente(), nuevoConductor);

	    if (exito) {
	        System.out.println("Conductor actualizado correctamente.");
	    } else {
	        System.out.println("No se pudo actualizar el conductor.");
	    }
	}
	
	
}
