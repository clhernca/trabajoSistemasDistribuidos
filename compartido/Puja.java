package compartido;

public class Puja {
    private String usuario;
    private double cantidad;
    private long timestamp;

    public Puja(String usuario, double cantidad) {
        this.usuario = usuario;
        this.cantidad = cantidad;
        this.timestamp = System.currentTimeMillis();
    }

}
