package Common;

import java.io.Serializable;

public class Skin implements Serializable
{
	private static final long serialVersionUID = 1L;
	private int precio;
	private int descuento;
	private String nombre;
	
	public Skin (int precio, int descuento, String nombre)
	{
		this.precio = precio;
		this.descuento = descuento;
		this.nombre = nombre;
	}
	
	public int getPrecio()
	{
		return precio;
	}
	
	public void setPrecio(int precio) {
		this.precio = precio;
	}
	
	public int getDescuento()
	{
		return descuento;
	}
	
	public void setDescuento(int descuento) {
		this.descuento = descuento;
	}

	public String getNombre() {
	    return nombre;
	}

	public void setNombre(String nombre) {
	    this.nombre = nombre;
	}
}
