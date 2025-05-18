package common;

import java.io.Serializable;
public class RegistroCompra implements Serializable {
    private int id;
    private String patente;
    private float litros;
    private float costo;
    private String fecha;


    public RegistroCompra(int id, String patente, float litros, float costo, String fecha) {
        setId(id);
        setPatente(patente);
        setFecha(fecha);
        setLitros(litros);
        setCosto(costo);
    }

    private void setId(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    private void setPatente(String patente) {
        this.patente = patente;
    }

    public String getPatente() {
        return patente;
    }

    private void setFecha(String fecha) {
        this.fecha = fecha;
    }

    public String getFecha() {
        return fecha;
    }

    private void setLitros(float litros) {
        this.litros = litros;
    }

    public float getLitros() {
        return litros;
    }

    private void setCosto(float costo) {
        this.costo = costo;
    }

    public float getCosto() {
        return costo;
    }
}