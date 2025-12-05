package servidor;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.List;

import compartido.Mensaje;
import compartido.Subasta;

public class ManejadorCliente implements Runnable {

    private Socket socket;
    private GestorSubastas gestor;
    private String nombreUsuario;
    private BufferedReader in;
    private PrintWriter out;

    public ManejadorCliente(Socket socket, GestorSubastas gestor) {
        this.socket = socket;
        this.gestor = gestor;
    }

    @Override
    public void run() {

        try {
            // Crear streams de entrada/salida
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);

            // Pedir nombre de usuario
            out.println("NOMBRE_REQUERIDO");
            nombreUsuario = in.readLine();

            if (nombreUsuario == null || nombreUsuario.trim().isEmpty()) {
                nombreUsuario = "Usuario_" + System.currentTimeMillis();
            }

            System.out.println("[SERVIDOR] Se conectó: " + nombreUsuario);
            out.println("BIENVENIDA:" + nombreUsuario);

            String linea;
            while ((linea = in.readLine()) != null) {
                Mensaje mensaje = Mensaje.parsear(linea);

                if (mensaje == null) {
                    out.println("ERROR:Mensaje inválido");
                    continue;
                }

                procesarComando(mensaje);
            }

        } catch (IOException e) {
            System.err.println("[ERROR] Desconexión de: " + nombreUsuario);
        } finally {
            // cerrarConexion();
        }
    }

    private void procesarComando(Mensaje mensaje) {
        String comando = mensaje.getComando();

        switch (comando) {
            case "LIST":
                manejarListar();
                break;

            case "BID":
                System.out.println("[" + nombreUsuario + "] Pidió hacer una puja");
                manejarPuja(mensaje);
                break;

            case "INFO":
                System.out.println("[" + nombreUsuario + "] Pidió información de una subasta");
                manejarInfo(mensaje);
                break;

            default:
                out.println("ERROR:Comando desconocido: " + comando);
        }
    }

    private void manejarPuja(Mensaje mensaje) {
        int idSubasta = mensaje.getParametroInt(0);
        double cantidad = mensaje.getParametroDouble(1);

        if (idSubasta == -1 || cantidad == -1.0) {          // Por qué -1? Porque getParametroInt y getParametroDouble devuelven -1 en caso de error
            out.println("BID_ERROR:Parámetros inválidos");
            return;
        }

        Subasta subasta = gestor.buscarSubasta(idSubasta);

        if (subasta == null) {
            out.println("BID_ERROR:Subasta no encontrada");
            return;
        }

        if (!subasta.estaActiva()) {
            out.println("BID_ERROR:Subasta cerrada");
            return;
        }

        boolean exitosa = gestor.procesarPuja(idSubasta, nombreUsuario, cantidad); // Llama a pujar en Subasta que mira si la cantidad es mayor que el precio actual

        if (exitosa) {
            System.out.println("[" + nombreUsuario + "] Pujo €" + String.format("%.2f", cantidad) + " en subasta #" + idSubasta);
            out.println("BID_OK:" + idSubasta + ":" + String.format("%.2f", cantidad));
        } else {
            out.println("BID_ERROR:Cantidad debe ser > €" +
                    String.format("%.2f", subasta.getPrecioActual()));
        }
    }

    private void manejarInfo(Mensaje mensaje) {

        int idSubasta = mensaje.getParametroInt(0);

        System.out.println("[" + nombreUsuario + "] Pidió INFO de subasta #" + idSubasta);

        Subasta subasta = gestor.buscarSubasta(idSubasta);

        if (subasta == null) {
            out.println("ERROR:Subasta no encontrada");
            return;
        }

        out.println("INFO:" + subasta.toString());
    }

    private void manejarListar() {
        System.out.println("[" + nombreUsuario + "] Pidió listar subastas");
        List<Subasta> subastas = gestor.obtenerSubastas();

        if (subastas.isEmpty()) {
            out.println("LISTA_VACIA");
            return;
        }

        StringBuilder sb = new StringBuilder("LISTA:");
        for (Subasta s : subastas) {
            sb.append(s.getId()).append("|")
                    .append(s.getTitulo()).append("|")
                    .append(String.format("%.2f", s.getPrecioActual())).append("|")
                    .append(s.getPujadorLider()).append("|")
                    .append(s.getTiempoRestante()).append(";");
        }

        out.println(sb.toString()); // LISTA:id|titulo|precioActual|pujadorLider|tiempo;id|titulo|precioActual|pujadorLider|tiempo;...

    }
}

/*
 * 
 * private void cerrarConexion() {
 * try {
 * if (in != null) in.close();
 * if (out != null) out.close();
 * if (socket != null) socket.close();
 * System.out.println("[SERVIDOR] Desconectado: " + nombreUsuario);
 * } catch (IOException e) {
 * System.err.println("[ERROR] Al cerrar conexión: " + e.getMessage());
 * }
 * }
 * 
 * 
 */
