package compartido;

public class Subasta {
    private int id;
    private String titulo; // Nombre del producto
    private double precioActual;
    private String pujadorLider;
    private boolean activa; // Para saber si la subasta sigue activa o ha finalizado
    private long tiempoFinal;

    public Subasta(int id, String titulo, double precioInicial) {
        this.id = id;
        this.titulo = titulo;
        this.precioActual = precioInicial;
        this.pujadorLider = "Ninguno";
        this.activa = true;
        this.tiempoFinal = System.currentTimeMillis() + (5 * 60 * 1000); // 5 minutos
    }

    public synchronized boolean pujar(String usuario, double cantidad) {
        if (!activa) {
            return false;
        }

        if (cantidad > precioActual) {
            precioActual = cantidad;
            pujadorLider = usuario;
            return true;
        }
        return false;
    }

    public synchronized void cerrar() {
        activa = false;
    }

    public synchronized boolean estaActiva() {
        return activa && System.currentTimeMillis() < tiempoFinal;
    }

    public int getId() {
        return id;
    }

    public String getTitulo() {
        return titulo;
    }

    public synchronized double getPrecioActual() {
        return precioActual;
    }

    public synchronized String getPujadorLider() {
        return pujadorLider;
    }

    public boolean isActiva() {
        return activa;
    }

    public long getTiempoRestante() {
        long restante = tiempoFinal - System.currentTimeMillis();
        return restante > 0 ? restante / 1000 : 0; // en segundos
    }

    @Override
    public synchronized String toString() {
        return id + "|" + titulo + "|" + precioActual + "|" + pujadorLider + "|" + getTiempoRestante();
    }

    public String toStringDetallado() {
        return "=== Subasta #" + id + " ===\n" +
                "Título: " + titulo + "\n" +
                "Precio Actual: €" + String.format("%.2f", precioActual) + "\n" +
                "Pujador Líder: " + pujadorLider + "\n" +
                "Tiempo Restante: " + getTiempoRestante() + "s\n" +
                "Estado: " + (estaActiva() ? "ACTIVA" : "CERRADA");
    }

}
