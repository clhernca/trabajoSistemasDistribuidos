
package cliente;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class ClienteSubastasMejorado {

    private static final String HOST = "localhost";
    private static final int PUERTO = 5000;

    private static Socket socket;
    private static BufferedReader in;
    private static PrintWriter out;
    private static Scanner scanner;
    private static String nombreUsuario;
    private static boolean conexion;

    // Cola para comunicación entre hilos
    private static BlockingQueue<String> colaRespuestas = new LinkedBlockingQueue<>();

    private static Thread hiloLector;
    private static final Object consoleLock = new Object();

    public static void main(String[] args) {
        try {
            socket = new Socket(HOST, PUERTO);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);
            scanner = new Scanner(System.in);

            imprimirConsola("[CLIENT] Conectado al server en " + HOST + ":" + PUERTO);

            // Iniciar hilo lector ANTES de autenticación
            iniciarHiloLector();

            boolean autenticado = false;
            while (!autenticado) {
                imprimirConsola("======= BIENVENIDO =======");
                imprimirConsola("1. Registrarse");
                imprimirConsola("2. Iniciar sesión");
                imprimirConsola("3. Salir");
                System.out.print("Opción: ");

                int opcion = scanner.nextInt();
                scanner.nextLine();

                switch (opcion) {
                    case 1:
                        autenticado = registro();
                        break;
                    case 2:
                        autenticado = login();
                        break;
                    case 3:
                        imprimirConsola("[CLIENT] Saliendo...");
                        return;
                    default:
                        imprimirConsola("[CLIENT] Opción inválida. Vuelve a intentarlo.");
                        break;
                }
            }

            conexion = true;

            while (conexion) {
                mostrarMenu();
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            cerrarConexion();
        }
    }

    /**
     * Hilo que lee TODAS las respuestas del servidor
     * Distingue entre notificaciones y respuestas normales
     */
    private static void iniciarHiloLector() {
        hiloLector = new Thread(() -> {
            try {
                String linea;
                while ((linea = in.readLine()) != null) {
                    if (linea.startsWith("NOTIF_")) {
                        // Notificación: mostrar inmediatamente
                        procesarNotificacion(linea);
                    } else {
                        // Respuesta normal: añadir a cola para que el hilo principal la procese
                        colaRespuestas.offer(linea);
                    }
                }
            } catch (IOException e) {
                if (conexion) {
                    imprimirConsola("[ERROR] Conexión con servidor perdida: " + e.getMessage());
                }
            }
        });
        hiloLector.setDaemon(true);
        hiloLector.start();
        imprimirConsola("[SYSTEM] Sistema de comunicación activado");
    }

    /**
     * Procesa notificaciones del servidor y las muestra inmediatamente
     */
    private static void procesarNotificacion(String notificacion) {
        String[] partes = notificacion.split(":");

        if (partes[0].equals("NOTIF_ADELANTADO")) {
            String idSubasta = partes[1];
            String nuevoLider = partes[2];
            String nuevoPrecio = partes[3];

            imprimirConsola("\n╔════════════════════════════════════════╗");
            imprimirConsola("║          ⚠️  NOTIFICACIÓN  ⚠️          ║");
            imprimirConsola("╠════════════════════════════════════════╣");
            imprimirConsola("  Has sido adelantado en subasta #" + idSubasta);
            imprimirConsola("  Nuevo líder: " + nuevoLider);
            imprimirConsola("  Nueva puja: " + nuevoPrecio + "€");
            imprimirConsola("╚════════════════════════════════════════╝\n");

        } else if (partes[0].equals("NOTIF_GANADOR")) {
            String idSubasta = partes[1];
            String tituloSubasta = partes[2];
            String precioFinal = partes[3];

            imprimirConsola("\n╔════════════════════════════════════════╗");
            imprimirConsola("║          🎉  ¡FELICIDADES!  🎉         ║");
            imprimirConsola("╠════════════════════════════════════════╣");
            imprimirConsola("  Has ganado la subasta:");
            imprimirConsola("  '" + tituloSubasta + "' (ID " + idSubasta + ")");
            imprimirConsola("  Precio final: " + precioFinal + "€");
            imprimirConsola("╚════════════════════════════════════════╝\n");
        }
    }

    /**
     * Método sincronizado para imprimir en consola
     * Evita que se mezclen mensajes de diferentes hilos
     */
    private static void imprimirConsola(String mensaje) {
        synchronized (consoleLock) {
            System.out.println(mensaje);
        }
    }

    /**
     * Espera y obtiene la siguiente respuesta del servidor
     * Bloquea hasta que haya una respuesta disponible
     */
    private static String esperarRespuesta() {
        try {
            return colaRespuestas.take(); // Bloquea hasta que haya respuesta
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return null;
        }
    }

    private static void cerrarConexion() {
        try {
            conexion = false;
            if (hiloLector != null) {
                hiloLector.interrupt();
            }
            if (out != null)
                out.close();
            if (in != null)
                in.close();
            if (socket != null)
                socket.close();
            if (scanner != null)
                scanner.close();
            imprimirConsola("[CLIENT] Conexión cerrada.");
        } catch (Exception e) {
            System.err.println("[CLIENT] Error al cerrar conexión: " + e.getMessage());
        }
    }

    private static boolean registro() {
        imprimirConsola("\n=== REGISTRO DE NUEVO USUARIO ===");
        System.out.print("Elige un nombre de usuario: ");
        String usuario = scanner.nextLine();

        if (usuario.trim().isEmpty()) {
            imprimirConsola("[CLIENT] El nombre de usuario no puede estar vacío.");
            return false;
        }

        System.out.print("Elige una contraseña: ");
        String password = scanner.nextLine();

        System.out.print("Confirma la contraseña: ");
        String passwordConfirm = scanner.nextLine();

        if (!password.equals(passwordConfirm)) {
            imprimirConsola("[CLIENT] Las contraseñas no coinciden");
            return false;
        }

        out.println("REGISTER:" + usuario + ":" + password);

        String respuesta = esperarRespuesta();
        if (respuesta != null && respuesta.startsWith("REGISTER_OK")) {
            nombreUsuario = usuario;
            imprimirConsola("✓ Registrado correctamente! Bienvenido " + usuario);
            return true;
        } else if (respuesta != null && respuesta.startsWith("REGISTER_ERROR:")) {
            String error = respuesta.substring(15);
            imprimirConsola("✗ " + error);
            return false;
        }

        return false;
    }

    private static boolean login() {
        imprimirConsola("\n=== INICIO DE SESIÓN ===");
        System.out.print("Escribe tu nombre: ");
        nombreUsuario = scanner.nextLine();
        System.out.print("Escribe la contraseña: ");
        String password = scanner.nextLine();

        out.println("LOGIN:" + nombreUsuario + ":" + password);

        String respuesta = esperarRespuesta();
        if (respuesta != null && respuesta.equals("LOGIN_OK")) {
            imprimirConsola("✓ Sesión iniciada correctamente");
            return true;
        } else if (respuesta != null && respuesta.startsWith("LOGIN_ERROR:")) {
            String error = respuesta.substring(12);
            imprimirConsola("✗ " + error);
            return false;
        }

        return false;
    }

    private static void mostrarMenu() {
        imprimirConsola("\n=== MENÚ PRINCIPAL ===");
        imprimirConsola("1. Listar subastas activas");
        imprimirConsola("2. Ver información de subasta");
        imprimirConsola("3. Realizar puja");
        imprimirConsola("4. Consultar saldo");
        imprimirConsola("5. Ver historial de pujas");
        imprimirConsola("6. Agregar una subasta");
        imprimirConsola("7. Ingresar dinero");
        imprimirConsola("8. Salir");

        System.out.print("Opción: ");

        int opcion = scanner.nextInt();
        scanner.nextLine(); // Consumir newline

        switch (opcion) {
            case 1:
                listarSubastas();
                break;
            case 2:
                infoSubasta();
                break;
            case 3:
                pujar();
                break;
            case 4:
                consultarSaldo();
                break;
            case 5:
                consultarHistorial();
                break;
            case 6:
                agregarSubasta();
                break;
            case 7:
                ingresarDinero();
                break;
            case 8:
                salir();
                break;
            default:
                imprimirConsola("[CLIENT] Opción inválida");
        }
    }

    private static void ingresarDinero() {
        System.out.print("¿Cuánto dinero quieres ingresar? ");
        double cantidad = scanner.nextDouble();
        scanner.nextLine();

        out.println("DEP:" + cantidad);

        String respuesta = esperarRespuesta();
        if (respuesta != null && respuesta.startsWith("DEP_OK:")) {
            String nuevoSaldo = respuesta.substring(7);
            imprimirConsola("✓ Depósito realizado. Nuevo saldo: " + nuevoSaldo + "€");
        } else if (respuesta != null && respuesta.startsWith("DEP_ERROR:")) {
            String error = respuesta.substring(10);
            imprimirConsola("✗ Error: " + error);
        }
    }

    private static void agregarSubasta() {
        imprimirConsola("\n=== AGREGAR NUEVA SUBASTA ===");
        System.out.print("Introduce el título de la subasta: ");
        String titulo = scanner.nextLine();

        System.out.print("Introduce el precio inicial: ");
        double precioInicial = scanner.nextDouble();

        System.out.print("Introduce la duración en segundos: ");
        int duracion = scanner.nextInt();
        scanner.nextLine();

        out.println("ADD:" + titulo + ":" + precioInicial + ":" + duracion);

        String respuesta = esperarRespuesta();
        if (respuesta != null && respuesta.startsWith("ADD_OK")) {
            imprimirConsola("✓ Subasta agregada correctamente");
        } else if (respuesta != null && respuesta.startsWith("ADD_ERROR:")) {
            String error = respuesta.substring(10);
            imprimirConsola("✗ Error: " + error);
        }
    }

    private static void listarSubastas() {
        out.println("LIST");

        String respuesta = esperarRespuesta();
        if (respuesta == null)
            return;

        if (respuesta.equals("LISTA_VACIA")) {
            imprimirConsola("\n📋 No hay subastas disponibles");
            return;
        } else if (respuesta.startsWith("LISTA:")) {
            String listaSubastas = respuesta.substring(6);
            String[] subastas = listaSubastas.split(";");

            imprimirConsola("\n╔════════════════════════════════════════════════════════════╗");
            imprimirConsola("║              SUBASTAS ACTIVAS                              ║");
            imprimirConsola("╚════════════════════════════════════════════════════════════╝");

            for (String subasta : subastas) {
                String[] partes = subasta.split("\\|");
                String id = partes[0];
                String titulo = partes[1];
                String precioActual = partes[2];
                String pujador = partes[3];
                String tiempoRestante = partes[4];

                imprimirConsola("\n[#" + id + "] " + titulo);
                imprimirConsola("  💰 Precio actual: " + precioActual + "€");
                imprimirConsola("  👤 Pujador líder: " + pujador);
                imprimirConsola("  ⏱️  Tiempo restante: " + tiempoRestante + "s");
            }
            imprimirConsola("");
        }
    }

    private static void infoSubasta() {
        System.out.print("Introduce el ID de la subasta: ");
        int idSubasta = scanner.nextInt();
        scanner.nextLine();

        out.println("INFO:" + idSubasta);

        String respuesta = esperarRespuesta();
        if (respuesta != null) {
            imprimirConsola("\n" + respuesta);
        }
    }

    private static void pujar() {
        System.out.print("Introduce el ID de la subasta: ");
        int idSubasta = scanner.nextInt();

        System.out.print("Introduce la cantidad a pujar: ");
        double cantidad = scanner.nextDouble();
        scanner.nextLine();

        out.println("BID:" + idSubasta + ":" + cantidad);

        String respuesta = esperarRespuesta();
        if (respuesta != null && respuesta.startsWith("BID_OK")) {
            imprimirConsola("✓ Puja aceptada correctamente");
        } else if (respuesta != null && respuesta.startsWith("BID_ERROR")) {
            String error = respuesta.substring(10);
            imprimirConsola("✗ Puja rechazada: " + error);
        }
    }

    private static void consultarHistorial() {
        out.println("CONSULT:history");

        String respuesta = esperarRespuesta();
        if (respuesta != null && respuesta.startsWith("HISTORIAL:")) {
            String historial = respuesta.substring(10);
            historial = historial.replace("{{NL}}", "\n");
            imprimirConsola("\n" + historial);
        } else {
            imprimirConsola("✗ Error al consultar historial");
        }
    }

    private static void consultarSaldo() {
        out.println("CONSULT:credit");

        String respuesta = esperarRespuesta();
        if (respuesta != null && respuesta.startsWith("SALDO:")) {
            String mensaje = respuesta.substring(6);
            mensaje = mensaje.replace("{{NL}}", "\n");
            imprimirConsola("\n💳 " + mensaje);
        } else {
            imprimirConsola("✗ Error al consultar saldo");
        }
    }

    private static void salir() {
        out.println("SALIR");
        conexion = false;
        imprimirConsola("\n👋 Hasta pronto!");
    }
}