package common;
import java.io.Serializable;
public class Estacion implements Serializable{
    private String marca;
    private String comuna;
    private String direccion;
    private String precio93;
    private String precio95;
    private String precio97;
    private String precioDi;
    private String precioKe;

    public Estacion(String marcaActual, String comunaActual, String direccion,
                        String precio93, String precio95, String precio97,
                        String precioDi, String precioKe) {
        this.marca = marcaActual;
        this.comuna = comunaActual;
        this.direccion = direccion;
        this.precio93 = precio93;
        this.precio95 = precio95;
        this.precio97 = precio97;
        this.precioDi = precioDi;
        this.precioKe = precioKe;
    }

    public String getMarcaActual() { return marca; }
    public String getComunaActual() { return comuna; }
    public String getDireccion() { return direccion; }
    public String getPrecio93() { return precio93; }
    public String getPrecio95() { return precio95; }
    public String getPrecio97() { return precio97; }
    public String getPrecioDi() { return precioDi; }
    public String getPrecioKe() { return precioKe; }
    
    public String getPrecio(String tipoDeCombustible) {
        switch (tipoDeCombustible) {
            case "93":
                return this.precio93;
            case "95":
                return this.precio95;
            case "97":
                return this.precio97;
            case "DI":
                return this.precioDi;
            case "KE":
                return this.precioKe;
            default:
                return "0"; 
        }
    }
    
    public void setMarcaActual(String marca) {
        this.marca = marca;
    }

    public void setComunaActual(String comuna) {
        this.comuna = comuna;
    }

    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }

    public void setPrecio93(String precio93) {
        this.precio93 = precio93;
    }

    public void setPrecio95(String precio95) {
        this.precio95 = precio95;
    }

    public void setPrecio97(String precio97) {
        this.precio97 = precio97;
    }

    public void setPrecioDi(String precioDi) {
        this.precioDi = precioDi;
    }

    public void setPrecioKe(String precioKe) {
        this.precioKe = precioKe;
    }

}