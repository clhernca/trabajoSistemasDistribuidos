package servidor;

import compartido.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.List;

public class ManejadorCliente implements Runnable {

    private Socket socket;
    private GestorSubastas gestorSubastas;
    private GestorUsuarios gestorUsuarios;
    private GestorNotificaciones gestorNotificaciones;
    private Usuario usuarioActual;
    private BufferedReader in;
    private PrintWriter out;
    private boolean conexion;

    public ManejadorCliente(Socket socket, GestorSubastas gestorSubastas, GestorUsuarios gestorUsuarios,
            GestorNotificaciones gestorNotificaciones) {
        this.socket = socket;
        this.gestorSubastas = gestorSubastas;
        this.gestorUsuarios = gestorUsuarios;
        this.gestorNotificaciones = gestorNotificaciones;
    }

    @Override
    public void run() {

        try {
            // Crear streams de entrada/salida
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);

            boolean autenticado = false;

            while (!autenticado) {
                String lineaAut = in.readLine();
                Mensaje mensajeAut = Mensaje.parsear(lineaAut);

                if (mensajeAut == null) {
                    out.println("ERROR:Formato inválido");
                    return;
                }

                String comando = mensajeAut.getComando();

                if (comando.equals("LOGIN")) {
                    if (manejarLogin(mensajeAut.getParametro(0), mensajeAut.getParametro(1))) {
                        out.println("LOGIN_OK");
                        autenticado = true;
                    } else {
                        out.println("LOGIN_ERROR:Usuario o contraseña incorrectos");
                    }

                } else if (comando.equals("REGISTER")) {
                    if (manejarRegistro(mensajeAut.getParametro(0), mensajeAut.getParametro(1))) {
                        // Registro exitoso, automáticamente lo logueamos
                        // NO ME GUSTA AQUÍ LO DE USUARIO ACTUAL, CAMBIAR A DENTRO DE MANEJAREGISTRO
                        usuarioActual = gestorUsuarios.login(mensajeAut.getParametro(0),
                                mensajeAut.getParametro(1));
                        out.println("REGISTER_OK");
                        autenticado = true;
                    } else {
                        out.println("REGISTER_ERROR:El usuario ya existe");
                    }

                } else {
                    out.println("ERROR:Se esperaba LOGIN o REGISTER");
                }
            }

            if (usuarioActual != null) { // Lo he puesto aqui, no se si está perfecto
                gestorNotificaciones.registrarCliente(usuarioActual.getNombre(), out);
            }

            conexion = true;
            String linea;
            while (conexion) {
                linea = in.readLine();
                Mensaje mensaje = Mensaje.parsear(linea);

                if (mensaje == null) {
                    out.println("ERROR:Mensaje inválido");
                    continue; // Qué hace esto?
                }

                procesarComando(mensaje);
            }

        } catch (IOException e) {
            System.err.println("[ERROR] Desconexión de: " + usuarioActual.getNombre());
        } finally {
            cerrarConexion();
        }
    }

    private void cerrarConexion() {
        try {
            if (usuarioActual != null) {
                gestorNotificaciones.desregistrarCliente(usuarioActual.getNombre());
            }
            if (out != null)
                out.close();
            if (in != null)
                in.close();
            if (socket != null && !socket.isClosed())
                socket.close();
            System.out.println("Conexión cerrada");
        } catch (Exception e) {
            System.err.println("Error al cerrar conexión: " + e.getMessage());
        }
    }

    private boolean manejarRegistro(String usuario, String contrasena) {

        boolean registrado = gestorUsuarios.registrar(usuario, contrasena);

        if (registrado) {
            System.out.println("[SERVIDOR] Nuevo usuario registrado: " + usuario);
        }

        return registrado;
    }

    private boolean manejarLogin(String usuario, String contrasena) {
        Usuario logeado = gestorUsuarios.login(usuario, contrasena);
        if (logeado != null) {
            usuarioActual = logeado;
            return true;
        }
        return false;
    }

    private void procesarComando(Mensaje mensaje) {
        String comando = mensaje.getComando();

        switch (comando) {
            case "LIST":
                manejarListar();
                break;

            case "BID":
                System.out.println("[" + usuarioActual.getNombre() + "] Pidió hacer una puja");
                manejarPuja(mensaje);
                break;

            case "INFO":
                System.out.println("[" + usuarioActual.getNombre() + "] Pidió información de una subasta");
                manejarInfo(mensaje);
                break;

            case "CONSULT":
                System.out.println("[" + usuarioActual.getNombre() + "] Pidió información propia");
                consultar(mensaje.getParametro(0));
                break;

            case "SALIR":
                System.out.println("[" + usuarioActual.getNombre() + "] Terminó");
                conexion = false;
                break;

            default:
                out.println("ERROR:Comando desconocido: " + comando);
        }
    }

    private void manejarPuja(Mensaje mensaje) {
        int idSubasta = mensaje.getParametroInt(0);
        double cantidad = mensaje.getParametroDouble(1);

        if (idSubasta == -1 || cantidad == -1.0) { // Por qué -1? Porque getParametroInt y getParametroDouble devuelven
                                                   // -1 en caso de error
            out.println("BID_ERROR:Parámetros inválidos");
            return;
        }

        Subasta subasta = gestorSubastas.buscarSubasta(idSubasta);

        if (subasta == null) {
            out.println("BID_ERROR:Subasta no encontrada");
            return;
        }

        if (!subasta.estaActiva()) {
            out.println("BID_ERROR:Subasta cerrada");
            return;
        }

        if (!usuarioActual.puedePujar(cantidad)) {

            out.println("BID_ERROR:Saldo insuficiente. Disponible: €"
                    + String.format("%.2f", usuarioActual.getSaldoDisponible())
                    + " (Bloqueado: €" + String.format("%.2f", usuarioActual.getSaldoBloqueado()) + ")");
            return;
        }

        String pujadorAnterior = subasta.getPujadorLider();
        double cantidadAnterior = subasta.getPrecioActual();
        boolean exitosa = gestorSubastas.procesarPuja(idSubasta, usuarioActual.getNombre(), cantidad); // Llama a pujar
                                                                                                       // en Subasta que
                                                                                                       // mira si la
                                                                                                       // cantidad es
                                                                                                       // mayor que el
                                                                                                       // precio actual
                                                                                                       // y realiza la
                                                                                                       // puja

        if (exitosa) {

            if (!pujadorAnterior.equals("Ninguno")) {
                Usuario usuarioAnterior = gestorUsuarios.obtenerUsuario(pujadorAnterior);
                if (usuarioAnterior != null) {
                    gestorNotificaciones.notificarCambioLider(
                            usuarioAnterior.getNombre(),
                            idSubasta,
                            usuarioActual.getNombre(),
                            cantidad);
                    usuarioAnterior.liberarDinero(cantidadAnterior);
                }
            }

            usuarioActual.bloquearDinero(cantidad);

            System.out.println("[" + usuarioActual.getNombre() + "] Pujó €" + String.format("%.2f", cantidad)
                    + " en subasta #" + idSubasta
                    + " (Bloqueado: €" + String.format("%.2f", usuarioActual.getSaldoBloqueado()) + ")");

            usuarioActual.registrarPuja(new Puja(usuarioActual.getNombre(), idSubasta, cantidad));
            out.println("BID_OK:" + idSubasta + ":" + String.format("%.2f", cantidad));

        } else {
            out.println("BID_ERROR:Cantidad debe ser > €"
                    + String.format("%.2f", subasta.getPrecioActual()));
        }
    }

    private void manejarInfo(Mensaje mensaje) {

        int idSubasta = mensaje.getParametroInt(0);

        System.out.println("[" + usuarioActual.getNombre() + "] Pidió INFO de subasta #" + idSubasta);

        Subasta subasta = gestorSubastas.buscarSubasta(idSubasta);

        if (subasta == null) {
            out.println("ERROR:Subasta no encontrada");
            return;
        }

        out.println("INFO:" + subasta.toString());
    }

    private void consultar(String param) {
        if (param.equals("credit")) {
            out.println("SALDO:Tu saldo total: €" + String.format("%.2f", usuarioActual.getSaldo())
                    + "{{NL}}Bloqueado en pujas: €" + String.format("%.2f", usuarioActual.getSaldoBloqueado())
                    + "{{NL}}Disponible: €" + String.format("%.2f", usuarioActual.getSaldoDisponible()));

        } else if (param.equals("history")) {
            String historial = usuarioActual.mostrarHistorial();
            String historialEscapado = historial.replace("\n", "{{NL}}");
            out.println("HISTORIAL:" + historialEscapado);
        } else {
            out.println("ERROR:Comando de consulta desconocido");
        }
    }

    private void manejarListar() {
        System.out.println("[" + usuarioActual.getNombre() + "] Pidió listar subastas");
        List<Subasta> subastas = gestorSubastas.obtenerSubastas();

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
 * System.out.println("[SERVIDOR] Desconectado: " + usuarioActual.getNombre());
 * } catch (IOException e) {
 * System.err.println("[ERROR] Al cerrar conexión: " + e.getMessage());
 * }
 * }
 * 
 * 
 */
