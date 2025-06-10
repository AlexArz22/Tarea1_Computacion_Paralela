package Client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.rmi.RemoteException;
import java.util.ArrayList;

import Common.Comprobante;
import Common.RiotPoints;
import Common.Skin;

public class RunClient2 {
    
    static BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
    static String name;
    
    
    public static void main(String[] args) {
        try {
            Client c = new Client();
            boolean valido = false;
            int op;
            do {
                do {
                    mostrarMenuUsuario();
                    op = Integer.parseInt(reader.readLine());
                } while (op < 1 || op > 3);

                switch (op) {
                    case 1:
                        valido = iniciarSesion(c);
                        break;
                    case 2:
                        valido = registrarse(c);
                        break;
                    case 3:
                        System.out.println("Saliendo del programa...");
                        System.exit(0);
                        return;
                    default:
                        System.out.println("Opción inválida. Por favor, seleccione una opción válida.");
                }

                if (!valido) {
                    System.out.println("\nLa operación no fue exitosa. Intente nuevamente.");
                }

            } while (!valido);
            
            while (true) {
                mostrarMenuPrincipal();
                op = Integer.parseInt(reader.readLine());
                switch (op) {
                    case 1:
                        verSkin(c);
                        break;
                    case 2:
                        verPrecioSkin(c);
                        break;
                    case 3:    
                        comprarSkin(c);
                        break;
                    case 4:
                        comprarRp(c);
                        break;
                    case 5:
                        verComprobante(c);
                        break;
                    case 6:
                        cambiarContrasena(c);
                        break;
                    case 7:
                        cancelarCompra(c);
                        break;
                    case 0:
                        System.out.println("Saliendo del programa...");
                        System.exit(0);
                        return;
                    default:
                        System.out.println("Opción inválida. Por favor, seleccione una opción válida.");
                }
                
            }
            
        } catch (Exception e) {
            System.err.println("Client exception: " + e.toString());
            e.printStackTrace();
        }
    }    
    
    public static void mostrarMenuUsuario() {
        System.out.println("\nMenu Inicio Sesión:");
        System.out.println("1. Iniciar sesión");
        System.out.println("2. Registrarse");
        System.out.println("3. Salir");
    }
    
    public static void mostrarMenuPrincipal() {
        System.out.println("\nMenu Principal:");
        System.out.println("1. Ver skins");
        System.out.println("2. Ver precio de una skin");
        System.out.println("3. Comprar skin");
        System.out.println("4. Comprar RP");
        System.out.println("5. Ver recibo de compra");
        System.out.println("6. Cambiar contraseña");
        System.out.println("7. Cancelar compra");
        System.out.println("0. Salir");
    }
    
    public static boolean iniciarSesion(Client c) throws NumberFormatException, IOException {
        System.out.println("Ingrese su nombre de usuario:");
        String usuario = reader.readLine();
        // verificar que el usuario ingrese un entero antes de hacer el parse
        System.out.println("Ingrese su contraseña, solo caracteres numéricos:");
        int contrasena = Integer.parseInt(reader.readLine());
        
        if (c.iniciarSesion(usuario, contrasena) == false) {
            System.out.println("Este usuario no existe o la contraseña es incorrecta.");
            return false;
        }
        
        name = usuario;
        System.out.println("\nBienvenido(a) " + usuario);
        return true;
    }
    
    public static boolean registrarse(Client c) throws NumberFormatException, IOException {
        System.out.println("Ingrese su nombre de usuario:");
        String usuario = reader.readLine();
        // verificar que el usuario ingrese un entero antes de hacer el parse
        System.out.println("Ingrese su contraseña, solo caracteres numéricos:");
        int contrasena = Integer.parseInt(reader.readLine());
        
        if (c.registrarse(usuario, contrasena) == false) {
            System.out.println("Este usuario ya existe.");
            return false;
        }
        name = usuario;
        System.out.println("\nBienvenido(a) " + usuario);
        return true;
    }
    
    public static void verSkin(Client c) throws IOException {
        System.out.println("\nIngrese el nombre del personaje que desea ver las skin:");
        String nombrePersonajeSkin = reader.readLine();
        ArrayList<Skin> skins = c.obtenerSkinPersonaje(nombrePersonajeSkin);
        if(skins == null) {
            System.out.println("No se encontraron skins para el personaje especificado.");

        } else {
            System.out.println("Skins disponibles:");
            for (Skin skin : skins) {
                System.out.println("Nombre: " + skin.getNombre());
            }  
        }
    }
    
    public static void verPrecioSkin(Client c) throws IOException {
        System.out.println("Ingrese el nombre de la skin:");
        String nombrePersonajeSkin = reader.readLine();
        Skin skin = c.obtenerPrecioSkin(nombrePersonajeSkin);
        if(skin == null) {
            System.out.println("\nLa skin ingresada no existe:");

        } else {
            System.out.println("\nNombre: " + skin.getNombre() + 
                                "\nPrecio: " + skin.getPrecio());
        }
    }
    
    public static void verComprobante(Client c) throws IOException {
        System.out.println("Ingrese el id de su comprobante: ");
        int idComprobante = Integer.parseInt(reader.readLine());
        Comprobante comprobante = c.verComprobante(idComprobante, name);
        if (comprobante == null) {
            System.out.println("La id proporcionada no corresponde a ningún comprobante");
        } else {
            System.out.println("\nDatos del comprobante:");
            
            System.out.println("ID: " + comprobante.getId() +
                    "\nTipo de producto: " + comprobante.getTipoProd() +
                    "\nNombre Jugador: " + comprobante.getNombreJugador() +
                    "\nTotal CLP: " + comprobante.getTotalCLP() +
                    "\nProducto: " + comprobante.getProducto() +
                    "\nFecha Compra: " + comprobante.getFechaCompra());
        }
    }
    
    public static void mostrarDatosRiotPoints(ArrayList<RiotPoints> rps) {
        for (RiotPoints rp : rps) {
            System.out.println("Cantidad: " + rp.getCantidad());
            System.out.println("Precio en USD: " + rp.getPrecioUSD());
            System.out.println("Precio en CLP: " + rp.getPrecioCLP());
            System.out.println();
        }
    }
    
    public static double obtenerPrecioRp(ArrayList<RiotPoints> rps, int cantidad) {
        double price = 0;
        for (RiotPoints rp : rps) {
            if (rp.getCantidad() == cantidad) price = (double) rp.getPrecioCLP();
        }
        return price;
    }
    
    public static void realizarCompra(Client c, String nameProd, String tipo, double precio) throws RemoteException {
        int res = c.generarComprobante(name, nameProd, tipo, precio);
        if (res == -1) {
            System.out.println("\nYa posees este producto.");
        } else if (res == 0) {
            System.out.println("\nHubo un error al realizar la compra.");
        } else {
            System.out.println("\nCompra realizada de forma exitosa!");
            
            System.out.println("\nComprobante de venta");
            // Obtener los detalles del comprobante
            Comprobante comprobante = c.obtenerComprobante(res, name);
            // Imprimir los detalles del comprobante
            System.out.println("ID del comprobante: " + comprobante.getId());
            System.out.println("Nombre del producto: " + comprobante.getProducto());
            System.out.println("Tipo de producto: " + comprobante.getTipoProd());
            System.out.println("Fecha de la compra: " + comprobante.getFechaCompra());
            System.out.println("Precio: " + comprobante.getTotalCLP());
        }
    }
    
    public static void comprarSkin (Client c) throws IOException {
        System.out.println("\nIngrese el nombre de la skin:");
        String skinName = reader.readLine();
        // verificar que el usuario ingrese un entero antes de hacer el parse
        Skin skin = c.obtenerPrecioSkin(skinName);
        if (skin == null) {
            System.out.println("La skin ingresada no existe.");
            return;
        }
        
        System.out.println("\nConfirme la compra.");
        String op;
        do {
            System.out.println("Escriba Confirmar o Cancelar");
            op = reader.readLine();
        } while (!op.equals("Confirmar") && !op.equals("Cancelar"));

        if (op.equals("Cancelar") == true) {
            System.out.println("\nCompra cancelada");
            return;
        }
        realizarCompra(c, skin.getNombre(), "skin", (double) skin.getPrecio());
    }
    
    public static void comprarRp (Client c) throws IOException {
        System.out.println("\nIngrese la cantidad de Riot Points que desea comprar: ");
        mostrarDatosRiotPoints(c.obtenerPrecioRp());
        String qty;
        do {
            qty = reader.readLine();
        } while (!qty.equals("650") && !qty.equals("1380") && !qty.equals("2800") && !qty.equals("5000") && !qty.equals("7200") && !qty.equals("15000"));
        
        System.out.println("\nConfirme la compra.");
        String op;
        do {
            System.out.println("Escriba Confirmar o Cancelar");
            op = reader.readLine();
        } while (!op.equals("Confirmar") && !op.equals("Cancelar"));
        
        if (op.equals("Cancelar") == true) {
            System.out.println("\nCompra cancelada");
            return;
        }
        
        String nombre = qty + " Riot Points";
        int precio = (int)obtenerPrecioRp(c.obtenerPrecioRp(), Integer.parseInt(qty));
        realizarCompra(c, nombre, "riotPoints", (double) precio);
    }

    public static void cambiarContrasena(Client c) throws IOException {
        System.out.println("Ingrese su nueva contraseña, solo caracteres numéricos:");
        int nuevaContrasena = Integer.parseInt(reader.readLine());
        
        try {
            c.cambiarContrasena(name, nuevaContrasena);
            System.out.println("Contraseña cambiada exitosamente.");
        } catch (RemoteException e) {
            System.err.println("Error al cambiar la contraseña: " + e.getMessage());
        }
    }
    
    public static void cancelarCompra(Client c) throws IOException {
        System.out.println("Ingrese el ID del comprobante que desea cancelar:");
        int idComprobante = Integer.parseInt(reader.readLine());
        
        try {
            c.cancelarCompra(idComprobante, name);
            System.out.println("Compra cancelada exitosamente.");
        } catch (RemoteException e) {
            System.err.println("Error al cancelar la compra: " + e.getMessage());
        }
    }
}
