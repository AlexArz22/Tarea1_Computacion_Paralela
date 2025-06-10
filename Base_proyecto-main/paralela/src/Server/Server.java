package Server;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URL;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import Common.Comprobante;
import Common.InterfazServidor;
import Common.RiotPoints;
import Common.Skin;

public class Server implements InterfazServidor {
    static private String UrlPj = "http://localhost:5000/skins?character=";
    static private String UrlSkin = "http://localhost:5000/skins?name=";
    private InterfazServidor stub;
    private Lock lock;

    public Server() throws RemoteException {
        stub = (InterfazServidor) UnicastRemoteObject.exportObject(this, 0);
        this.lock = new ReentrantLock();
    }

    private boolean requestMutex(String clientName) {
        try {
            System.out.println(clientName + " intentando acceder a recurso...");
            // Intenta adquirir el bloqueo dentro de 5 segundos
            boolean acquired = lock.tryLock(60, TimeUnit.SECONDS);
            if (acquired) {
                System.out.println(clientName + " obtiene el recurso");
            }
            return acquired;
        } catch (InterruptedException e) {
            System.err.println("Error al intentar adquirir el bloqueo: " + e.getMessage());
            return false;
        }
    }

    private void releaseMutex(String clientName) {
        System.out.println(clientName + " libera el recurso");
        lock.unlock();
    }

    @Override
    public Skin obtenerPrecioSkin(String nombre) throws RemoteException {
        try {
            URL url = new URL(UrlSkin + nombre.replace(" ", "%20"));
            String response = HttpUtil.fetchHttpResponse(url);
            if (response == null) return null;

            JSONObject jsonObject = new JSONObject(response.toString());
            Skin skin = new Skin(jsonObject.getInt("precioNormal"), jsonObject.getInt("precioConDescuento"), nombre);
            return skin;
        } catch (Exception e) {
            System.err.println("Error al conectar con la API: " + e.getMessage());
        }
        return null;
    }

    @Override
    public ArrayList<Skin> obtenerSkinsPersonaje(String nombrePersonaje) throws RemoteException {
        ArrayList<Skin> skins = new ArrayList<>();
        try {
            URL url = new URL(UrlPj + nombrePersonaje.replace(" ", "%20"));
            String response = HttpUtil.fetchHttpResponse(url);
            if (response == null) return null;

            JSONArray skinsArray = new JSONArray(response);

            for (int i = 0; i < skinsArray.length(); i++) {
                JSONObject skinObject = skinsArray.getJSONObject(i);
                String nombreSkin = skinObject.getString("nombreSkin");
                JSONObject descripcion = skinObject.getJSONObject("descripcion");
                int precio = descripcion.getInt("precio");
                int descuento = descripcion.getInt("valorDescuento");

                skins.add(new Skin(precio, descuento, nombreSkin));
            }
            return skins;
        } catch (Exception e) {
            System.err.println("Error en la conexión a la API: " + e.getMessage());
        }
        return null;
    }

    public double obtenerTasaCambioUSDaCLP() throws Exception {
        try {
            URL url = new URL("https://api.exchangerate-api.com/v4/latest/USD");
            String response = HttpUtil.fetchHttpResponse(url);
            if (response == null) return -1;

            JSONObject jsonObject = new JSONObject(response.toString());
            JSONObject rates = jsonObject.getJSONObject("rates");
            return rates.getDouble("CLP");
        } catch (Exception e) {
            System.err.println("Error en la conexión a la API: " + e.getMessage());
            return -1;
        }
    }

    public ArrayList<RiotPoints> obtenerRiotPoints() throws Exception {
        try {
            URL url = new URL("http://localhost:5000/rp");
            String response = HttpUtil.fetchHttpResponse(url);
            if (response == null) return null;

            JSONObject jsonObject = new JSONObject(response.toString());
            JSONArray rpArray = jsonObject.getJSONArray("riotPoints");
            ArrayList<RiotPoints> riotPoints = new ArrayList<>();
            for (int i = 0; i < rpArray.length(); i++) {
                JSONObject rp = rpArray.getJSONObject(i);
                riotPoints.add(new RiotPoints(rp.getInt("cantidad"), rp.getInt("precioUSD"), 0));
            }
            return riotPoints;
        } catch (Exception e) {
            System.err.println("Error en la conexión a la API: " + e.getMessage());
            return null;
        }
    }

    @Override
    public int generarComprobante(String nombreJugador, String nombreProducto, String tipoProducto, double monto) throws RemoteException {
        String clientName = "Cliente " + nombreJugador;
        if (!requestMutex(clientName)) {
            throw new RemoteException("No se pudo adquirir el bloqueo para generar el comprobante.");
        }
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        String sql;
        try {

            conn = DataBaseUtil.getConnection();
            if (conn == null) {
                return 0;
            }
            conn.setAutoCommit(false);

            if (tipoProducto.equals("skin")) {
                sql = "SELECT * FROM comprobante WHERE nombreJugador = ? AND nombreProducto = ? AND tipoProducto = 'skin';";
                stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
                stmt.setString(1, nombreJugador);
                stmt.setString(2, nombreProducto);
                rs = stmt.executeQuery();

                if (rs.next()) {
                    conn.rollback();
                    return -1;
                }
            }

            sql = "INSERT INTO comprobante (nombreJugador, nombreProducto, tipoProducto, fecha, precioTotal) VALUES (?, ?, ?, CURDATE(), ?)";
            stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            stmt.setString(1, nombreJugador);
            stmt.setString(2, nombreProducto);
            stmt.setString(3, tipoProducto);
            stmt.setDouble(4, monto);
            int affectedRows = stmt.executeUpdate();

            if (affectedRows > 0) {
                rs = stmt.getGeneratedKeys();
                if (rs.next()) {
                    int generatedId = rs.getInt(1);
                    conn.commit();
                    return generatedId;
                } else {
                    conn.rollback();
                    return 0;
                }
            }
            conn.rollback();
            return 0;
        } catch (SQLException e) {
            System.err.println("SQLException: " + e.getMessage());
            throw new RemoteException("Error al generar el comprobante: " + e.getMessage());
        } finally {
            releaseMutex(clientName);
            try {
                if (rs != null) rs.close();
                if (stmt != null) stmt.close();
                if (conn != null) conn.close();
            } catch (SQLException ex) {
                System.err.println("Error al cerrar los recursos de la base de datos: " + ex.getMessage());
            }
        }
    }

    @Override
    public Comprobante obtenerComprobante(int idComprobante, String name) throws RemoteException {
        String clientName = "Cliente " + name;
        if (!requestMutex(clientName)) {
            throw new RemoteException("No se pudo adquirir el bloqueo para obtener el comprobante.");
        }
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        Comprobante comprobante = null;
        try {

            conn = DataBaseUtil.getConnection();
            if (conn == null) {
                return null;
            }
            String sql = "SELECT * FROM comprobante WHERE idComprobante = ?";
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, idComprobante);
            rs = stmt.executeQuery();

            if (rs.next()) {
                comprobante = new Comprobante(rs.getInt("idComprobante"), rs.getString("nombreJugador"), rs.getFloat("precioTotal"), rs.getString("nombreProducto"), rs.getString("tipoProducto"), rs.getDate("fecha"));
            }
        } catch (SQLException e) {
            System.err.println("SQLException: " + e.getMessage());
            throw new RemoteException("Error al obtener el comprobante: " + e.getMessage());
        } finally {
            releaseMutex(clientName);
            try {
                if (rs != null) rs.close();
                if (stmt != null) stmt.close();
                if (conn != null) conn.close();
            } catch (SQLException ex) {
                System.err.println("Error al cerrar los recursos de la base de datos: " + ex.getMessage());
            }
        }
        return comprobante;
    }

    @Override
    public boolean registrarJugador(String nombre, int contraseña) throws RemoteException {
        String clientName = "Cliente " + nombre;
        if (!requestMutex(clientName)) {
            throw new RemoteException("No se pudo adquirir el bloqueo para registrar el jugador.");
        }
        Connection conn = null;
        PreparedStatement checkStmt = null;
        PreparedStatement insertStmt = null;
        ResultSet rs = null;

        try {

            conn = DataBaseUtil.getConnection();
            conn.setAutoCommit(false);

            String checkSql = "SELECT nombre FROM Jugador WHERE nombre = ?";
            checkStmt = conn.prepareStatement(checkSql);
            checkStmt.setString(1, nombre);
            rs = checkStmt.executeQuery();

            if (rs.next()) {
                conn.rollback();
                return false;
            } else {
                String insertSql = "INSERT INTO Jugador (nombre, clave) VALUES (?, ?)";
                insertStmt = conn.prepareStatement(insertSql);
                insertStmt.setString(1, nombre);
                insertStmt.setInt(2, contraseña);
                int affectedRows = insertStmt.executeUpdate();

                if (affectedRows > 0) {
                    conn.commit();
                    return true;
                } else {
                    conn.rollback();
                    return false;
                }
            }
        } catch (SQLException  e) {
            System.err.println("SQLException: " + e.getMessage());
            throw new RemoteException("Error en el registro del jugador: " + e.getMessage());
        } finally {
            releaseMutex(clientName);
            try {
                if (rs != null) rs.close();
                if (checkStmt != null) checkStmt.close();
                if (insertStmt != null) checkStmt.close();
                if (conn != null) conn.close();
            } catch (SQLException ex) {
                System.err.println("Error al cerrar los recursos de la base de datos: " + ex.getMessage());
            }
        }
    }

    @Override
    public boolean iniciarSesion(String nombre, int contraseña) throws RemoteException {
        String clientName = "Cliente " + nombre;
        if (!requestMutex(clientName)) {
            throw new RemoteException("No se pudo adquirir el bloqueo para iniciar sesión.");
        }
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
        	
            conn = DataBaseUtil.getConnection();
            String sql = "SELECT clave FROM Jugador WHERE nombre = ?";
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, nombre);
            rs = stmt.executeQuery();

            if (rs.next()) {
                int storedPassword = rs.getInt("clave");
                if (storedPassword == contraseña) {
                    return true;
                }
            }
            return false;
        } catch (SQLException e) {
            System.err.println("SQLException: " + e.getMessage());
            throw new RemoteException("Error al iniciar sesión: " + e.getMessage());
        } finally {
            releaseMutex(clientName);
            try {
                if (rs != null) rs.close();
                if (stmt != null) stmt.close();
                if (conn != null) conn.close();
            } catch (SQLException ex) {
                System.err.println("Error al cerrar los recursos de la base de datos: " + ex.getMessage());
            }
        }
    }

    @Override
    public ArrayList<RiotPoints> obtenerPreciosRp() throws RemoteException {
        ArrayList<RiotPoints> precios = new ArrayList<>();
        try {
            ArrayList<RiotPoints> riotPoints = obtenerRiotPoints();
            double tasaCambio = obtenerTasaCambioUSDaCLP();
            for (RiotPoints rp : riotPoints) {
                double precioCLP = rp.getPrecioUSD() * tasaCambio;
                precios.add(new RiotPoints(rp.getCantidad(), rp.getPrecioUSD(), precioCLP));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return precios;
    }

    @Override
    public void cambiarContrasena(String nombre, int nuevaContrasena) throws RemoteException {
        String clientName = "Cliente " + nombre;
        if (!requestMutex(clientName)) {
            throw new RemoteException("No se pudo adquirir el bloqueo para cambiar la contraseña.");
        }
        Connection conn = null;
        PreparedStatement stmt = null;
        try {
            conn = DataBaseUtil.getConnection();
            if (conn == null) {
                throw new RemoteException("No se pudo establecer la conexión con la base de datos.");
            }
            String sql = "UPDATE Jugador SET clave = ? WHERE nombre = ?";
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, nuevaContrasena);
            stmt.setString(2, nombre);
            int affectedRows = stmt.executeUpdate();

            if (affectedRows > 0) {
                conn.commit();
            } else {
                conn.rollback();
                throw new RemoteException("No se pudo actualizar la contraseña.");
            }
        } catch (SQLException e) {
            System.err.println("SQLException: " + e.getMessage());
            throw new RemoteException("Error al cambiar la contraseña: " + e.getMessage());
        } finally {
            releaseMutex(clientName);
            try {
                if (stmt != null) stmt.close();
                if (conn != null) conn.close();
            } catch (SQLException ex) {
                System.err.println("Error al cerrar los recursos de la base de datos: " + ex.getMessage());
            }
        }
    }

    @Override
    public void cancelarCompra(int idComprobante, String name) throws RemoteException {
        String clientName = "Cliente " + name;
        if (!requestMutex(clientName)) {
            throw new RemoteException("No se pudo adquirir el bloqueo para cancelar la compra.");
        }
        Connection conn = null;
        PreparedStatement stmt = null;
        try {
            conn = DataBaseUtil.getConnection();
            if (conn == null) {
                throw new RemoteException("No se pudo establecer la conexión con la base de datos.");
            }
            String sql = "DELETE FROM comprobante WHERE idComprobante = ?";
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, idComprobante);
            int affectedRows = stmt.executeUpdate();

            if (affectedRows > 0) {
                conn.commit();
            } else {
                conn.rollback();
                throw new RemoteException("No se pudo cancelar la compra.");
            }
        } catch (SQLException e) {
            System.err.println("SQLException: " + e.getMessage());
            throw new RemoteException("Error al cancelar la compra: " + e.getMessage());
        } finally {
            releaseMutex(clientName);
            try {
                if (stmt != null) stmt.close();
                if (conn != null) conn.close();
            } catch (SQLException ex) {
                System.err.println("Error al cerrar los recursos de la base de datos: " + ex.getMessage());
            }
        }
    }

	@Override
	public int heartbeat() throws RemoteException {
		// TODO Auto-generated method stub
		return 0;
	}
}
