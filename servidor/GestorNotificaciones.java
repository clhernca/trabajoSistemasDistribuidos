package servidor;

import java.io.PrintWriter;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class GestorNotificaciones {

    private Map<String, PrintWriter> clientesConectados;
                                                         

    public GestorNotificaciones() {
        this.clientesConectados = new ConcurrentHashMap<>();
    }

    public void registrarCliente(String nombreUsuario, PrintWriter writer) {
        clientesConectados.put(nombreUsuario, writer);
        System.out.println("[NOTIF] Cliente registrado para notificaciones: " + nombreUsuario);
    }

    public void desregistrarCliente(String nombreUsuario) {
        clientesConectados.remove(nombreUsuario);
        System.out.println("[NOTIF] Cliente desregistrado: " + nombreUsuario);
    }

    public void notificarCambioLider(String usuarioAnterior, int idSubasta, String nuevoLider, double nuevoPrecio) {
        PrintWriter writer = clientesConectados.get(usuarioAnterior);
        if (writer != null) {
            try {
                String mensaje = String.format("NOTIF_ADELANTADO:%d:%s:%.2f",
                        idSubasta, nuevoLider, nuevoPrecio);
                                                             
                writer.println(mensaje);
                writer.flush();
                System.out.println("[NOTIF] Notificación enviada a " + usuarioAnterior + ": " + mensaje);
            } catch (Exception e) {
                System.err.println("[NOTIF] Error al enviar notificación a " + usuarioAnterior + ": " + e.getMessage());
                desregistrarCliente(usuarioAnterior);
            }
        }
    }

    public void notificarSubastaTerminada(String ganador, int idSubasta, String tituloSubasta, double precioFinal) {
        PrintWriter writer = clientesConectados.get(ganador);
        if (writer != null) {
            try {
                String mensaje = String.format("NOTIF_GANADOR:%d:%s:%.2f",
                        idSubasta, tituloSubasta, precioFinal); 
                                                               
                writer.println(mensaje);
                writer.flush();
                System.out.println("[NOTIF] Notificación de victoria enviada a " + ganador + ": " + mensaje);
            } catch (Exception e) {
                System.err.println("[NOTIF] Error al enviar notificación a " + ganador + ": " + e.getMessage());
                desregistrarCliente(ganador);
            }
        }
    }

    public boolean estaConectado(String nombreUsuario) { 
        return clientesConectados.containsKey(nombreUsuario);
    }

    public int getCantidadClientesConectados() {
        return clientesConectados.size();
    }

}
