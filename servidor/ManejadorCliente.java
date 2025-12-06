package servidor;

import compartido.Mensaje;
import compartido.Subasta;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.List;

public class ManejadorCliente implements Runnable {

    private Socket socket;
    private GestorSubastas gestor;
    private String nombreUsuario;
    private BufferedReader in;
    private PrintWriter out;
    private boolean conexion;

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

            String lineaLogin = in.readLine();
            Mensaje mensajeLogin = Mensaje.parsear(lineaLogin);

            if (mensajeLogin == null || !mensajeLogin.getComando().equals("LOGIN")) {
                out.println("LOGIN_ERROR:Formato de login inválido");
                socket.close();
                return;
            }


            if (manejarLogin(mensajeLogin.getParametro(0), mensajeLogin.getParametro(1))){
                out.println("LOGIN_OK");
                conexion = true;
            }
            else {
                out.println("LOGIN_ERROR");
                socket.close();
                return;
            }

            //El cliente al principio manda LOGIN:USER:CONTRASEÑA
            //Se lee ese mensaje y se hace una función de check LOGIN
            //Se devuelve un mensaje de cómo ha ido el login
/*



            String mensajeLogin = in.readLine();
            String [] splitLogin = mensajeLogin.split(":");

            if (manejarLogin(splitLogin[1], splitLogin[2])){
                out.println("LOGIN_OK");
                //Se podría mandar el mensaje con la infor del usuario (saldo...)
                nombreUsuario = splitLogin[1];
            }
            else {
                out.println("LOGIN_ERROR");
                //Si da error, qué hacemos? No se debería de seguir con el código
            } */

            String linea;
            while (conexion) {
                linea = in.readLine();
                Mensaje mensaje = Mensaje.parsear(linea);

                if (mensaje == null) {
                    out.println("ERROR:Mensaje inválido");
                    continue; //Qué hace esto?
                }

                procesarComando(mensaje);
            }

        } catch (IOException e) {
            System.err.println("[ERROR] Desconexión de: " + nombreUsuario);
        } finally {
            // cerrarConexion();
        }
    }

    private boolean manejarLogin(String usuario, String contrasena){
        nombreUsuario = usuario;
        return true; //ver si en verdad está y devolver otra cosa si no
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

            case "SALIR":
                System.out.println("[" + nombreUsuario + "] Terminó");
                conexion = false;
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

        boolean exitosa = gestor.procesarPuja(idSubasta, nombreUsuario, cantidad); // Llama a pujar en Subasta que mira si la cantidad es mayor que el precio actual y realiza la puja

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
