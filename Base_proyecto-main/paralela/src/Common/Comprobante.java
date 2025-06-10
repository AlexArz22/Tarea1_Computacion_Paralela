package Common;

import java.io.Serializable;
import java.util.Date;

public class Comprobante implements Serializable
{
	private static final long serialVersionUID = 1L;
	private int id;
	private String nombreJugador;
	private Float total;
	private String producto;
	String tipoProd;
	Date fechaCompra;
	
	public Comprobante(int id, String nombre, Float total, String prod, String tipoProd, Date fecha)
	{
		this.id = id;
		this.nombreJugador = nombre;
		this.total = total;
		this.producto = prod;
		this.tipoProd = tipoProd;
		this.fechaCompra = fecha;
	}
	
	public String getTipoProd() {
		return tipoProd;
	}

	public void setTipoProd(String tipoProd) {
		this.tipoProd = tipoProd;
	}

	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getNombreJugador() {
		return nombreJugador;
	}
	public void setNombreJugador(String nombreJugador) {
		this.nombreJugador = nombreJugador;
	}
	public Float getTotalCLP() {
		return total;
	}
	public void setTotalCLP(Float totalCLP) {
		this.total = totalCLP;
	}
	public String getProducto() {
		return producto;
	}
	public void setProducto(String producto) {
		this.producto = producto;
	}
	public Date getFechaCompra() {
		return fechaCompra;
	}
	public void setFechaCompra(Date fechaCompra) {
		this.fechaCompra = fechaCompra;
	}
}
