package compartido;

public class Subasta {
    private int id;
    private String titulo; // Nombre del producto
    private double precioActual;
    private String pujadorLider;
    private boolean activa; // Para saber si la subasta sigue activa o ha finalizado
    private long tiempoFinal;

    private String ganador;
    private double precioFinal;

    public Subasta() {
        // Constructor vacío para XML
    }

    public Subasta(int id, String titulo, double precioInicial) {
        this.id = id;
        this.titulo = titulo;
        this.precioActual = precioInicial;
        this.pujadorLider = "Ninguno";
        this.activa = true;
        this.tiempoFinal = System.currentTimeMillis() + (5 * 60 * 1000); // 5 minutos

        this.ganador = null;
        this.precioFinal = 0;
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

    public synchronized void cerrar(String ganador) {
        activa = false;
        this.ganador = ganador;
        this.precioFinal = precioActual;
    }

    public synchronized boolean estaActiva() {
        return activa && System.currentTimeMillis() < tiempoFinal;
    }

    // @XmlElement
    public int getId() {
        return id;
    }

    // @XmlElement
    public String getTitulo() {
        return titulo;
    }

    // @XmlElement
    public synchronized double getPrecioActual() {
        return precioActual;
    }

    public void setPrecioActual(double precioActual) {
        this.precioActual = precioActual;
    }

    // @XmlElement
    public synchronized String getPujadorLider() {
        return pujadorLider;
    }

    public void setPujadorLider(String pujadorLider) {
        this.pujadorLider = pujadorLider;
    }

    // @XmlElement
    public boolean isActiva() {
        return activa;
    }

    public void setActiva(boolean activa) {
        this.activa = activa;
    }

    // @XmlElement
    public long getTiempoFinal() {
        return tiempoFinal;
    }

    public void setTiempoFinal(long tiempoFinal) {
        this.tiempoFinal = tiempoFinal;
    }

    // @XmlElement
    public String getGanador() {
        return ganador;
    }

    public void setGanador(String ganador) {
        this.ganador = ganador;
    }

    // @XmlElement
    public double getPrecioFinal() {
        return precioFinal;
    }

    public void setPrecioFinal(double precioFinal) {
        this.precioFinal = precioFinal;
    }

    public long getTiempoRestante() {
        long restante = tiempoFinal - System.currentTimeMillis();
        return restante > 0 ? restante / 1000 : 0; // en segundos
    }

    @Override
    public synchronized String toString() {
        return id + "|" + titulo + "|" + precioActual + "|" + pujadorLider + "|" + getTiempoRestante(); // Actualizar
                                                                                                        // con nuevos
                                                                                                        // atributos?
    }

    public String toStringDetallado() {
        StringBuilder sb = new StringBuilder();
        sb.append("=== Subasta #").append(id).append(" ===\n")
                .append("Título: ").append(titulo).append("\n")
                .append("Precio: €").append(String.format("%.2f", precioActual)).append("\n")
                .append("Pujador Líder: ").append(pujadorLider).append("\n")
                .append("Tiempo Restante: ").append(getTiempoRestante()).append("s\n");

        if (!activa) {
            sb.append("ESTADO: CERRADA\n");
            if (ganador != null) {
                sb.append("Ganador: ").append(ganador).append("\n");
                sb.append("Precio Final: €").append(String.format("%.2f", precioFinal));
            }
        } else {
            sb.append("Estado: ACTIVA");
        }

        return sb.toString();
    }

}
