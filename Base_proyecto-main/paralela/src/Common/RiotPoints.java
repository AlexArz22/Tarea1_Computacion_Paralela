package Common;

import java.io.Serializable;

public class RiotPoints implements Serializable
{
	private static final long serialVersionUID = 1L;
	private int cantidad;
    private int precioUSD;
    private double precioCLP;

    public RiotPoints(int cantidad, int precioUSD, double precioCLP2) {
        this.cantidad = cantidad;
        this.precioUSD = precioUSD;
        this.precioCLP = precioCLP2;
    }

    public int getCantidad() {
        return cantidad;
    }

    public double getPrecioCLP() {
        return precioCLP;
    }

	public int getPrecioUSD() {
		return precioUSD;
	}

	public void setPrecioUSD(int precioUSD) {
		this.precioUSD = precioUSD;
	}

	public void setCantidad(int cantidad) {
		this.cantidad = cantidad;
	}

	public void setPrecioCLP(double precioCLP) {
		this.precioCLP = precioCLP;
	}

}
