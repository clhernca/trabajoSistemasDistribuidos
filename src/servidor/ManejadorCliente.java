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

                        out.println("REGISTER_OK");
                        autenticado = true;
                    } else {
                        out.println("REGISTER_ERROR:El usuario ya existe");
                    }

                } else {
                    out.println("ERROR:Se esperaba LOGIN o REGISTER");
                }
            }

            if (usuarioActual != null) {
                gestorNotificaciones.registrarCliente(usuarioActual.getNombre(), out);
            }

            conexion = true;
            String linea;
            while (conexion) {
                linea = in.readLine();
                Mensaje mensaje = Mensaje.parsear(linea);

                if (mensaje == null) {
                    out.println("ERROR:Mensaje inválido");
                    continue;
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
            System.out.println("[MANEJADOR] Conexión cerrada");
        } catch (Exception e) {
            System.err.println("[MANEJADOR] Error al cerrar conexión: " + e.getMessage());
        }
    }

    private boolean manejarRegistro(String usuario, String contrasena) {

        boolean registrado = gestorUsuarios.registrar(usuario, contrasena);

        if (registrado) {
            System.out.println("[SERVIDOR] Nuevo usuario registrado: " + usuario);
            usuarioActual = gestorUsuarios.login(usuario, contrasena);
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

            case "ADD":
                System.out.println("[" + usuarioActual.getNombre() + "] Pidió agregar una subasta");
                manejarAgregarSubasta(mensaje);
                break;

            case "DEP":
                System.out.println("[" + usuarioActual.getNombre() + "] Pidió ingresar dinero");
                manejarIngreso(mensaje);
                break;

            case "SALIR":
                System.out.println("[" + usuarioActual.getNombre() + "] Terminó");
                conexion = false;
                break;

            default:
                out.println("ERROR:Comando desconocido: " + comando);
        }
    }

    private void manejarIngreso(Mensaje mensaje) {
        double cantidad = mensaje.getParametroDouble(0);
        if (cantidad <= 0) {
            out.println("DEP_ERROR:Cantidad inválida");
            return;
        }
        usuarioActual.sumarSaldo(cantidad);
        System.out.println("[" + usuarioActual.getNombre() + "] Ingresó €" + String.format("%.2f", cantidad)
                + " | Nuevo saldo: €" + String.format("%.2f", usuarioActual.getSaldo()));
        out.println("DEP_OK:" + String.format("%.2f", usuarioActual.getSaldo()));
    }

    private void manejarAgregarSubasta(Mensaje mensaje) {
        String titulo = mensaje.getParametro(0);
        double precioInicial = mensaje.getParametroDouble(1);
        int duracion = mensaje.getParametroInt(2);

        if (titulo == null || precioInicial == -1.0) {
            out.println("ADD_ERROR:Parámetros inválidos");
            return;
        }

        int nuevoId = gestorSubastas.obtenerSubastas().size() + 1;
        Subasta nuevaSubasta = new Subasta(nuevoId, titulo, precioInicial, duracion);
        gestorSubastas.agregarSubasta(nuevaSubasta);

        System.out.println("[" + usuarioActual.getNombre() + "] Agregó nueva subasta: #" + nuevoId + " - " + titulo
                + " (Precio inicial: €" + String.format("%.2f", precioInicial) + ")");

        out.println("ADD_OK:" + nuevoId);
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
            out.println("INFO_ERROR: Subasta no encontrada");
            return;
        }

        out.println("INFO_OK:" + subasta.toString());
    }

    private void consultar(String param) {
        if (param.equals("credit")) {

            out.println("SALDO:" + usuarioActual.getSaldo() + ":"
                    + usuarioActual.getSaldoBloqueado() + ":"
                    + usuarioActual.getSaldoDisponible());

            System.out.println("[" + usuarioActual.getNombre() + "] Consultó saldo");

        } else if (param.equals("history")) {

            if (usuarioActual.getHistorialPujas() == null || usuarioActual.getHistorialPujas().isEmpty()) {
                out.println("HISTORIAL_VACIO");
                System.out.println("[" + usuarioActual.getNombre() + "] Consultó historial (vacío)");
            } else {
                StringBuilder sb = new StringBuilder("HISTORIAL:");

                for (Puja p : usuarioActual.getHistorialPujas()) {
                    sb.append(p.getIdSubasta()).append("|")
                            .append(String.format("%.2f", p.getCantidad())).append("|")
                            .append(p.getFechaFormato()).append(";");
                }

                if (sb.charAt(sb.length() - 1) == ';') {
                    sb.setLength(sb.length() - 1);
                }

                out.println(sb.toString());
                System.out.println("[" + usuarioActual.getNombre() + "] Consultó historial ("
                        + usuarioActual.getHistorialPujas().size() + " pujas)");
            }

        } else {
            out.println("CONSULT_ERROR:Comando de consulta desconocido");
            System.out.println("[" + usuarioActual.getNombre() + "] Consultó parámetro inválido: " + param);
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

        out.println(sb.toString());

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
