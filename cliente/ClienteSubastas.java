package cliente;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

public class ClienteSubastas {

    // He puesto toda la clase estática porque así es más cómodo y no tiene por qué
    // dar problemas
    // Cada cliente se va a ejecutar por separado, no usamos objetos y ya.
    // No tenemos que instanciar objetos y tenemos acceso fácil
    private static final String HOST = "localhost";
    private static final int PUERTO = 5000;

    private static Socket socket;
    private static BufferedReader in;
    private static PrintWriter out;
    private static Scanner scanner;
    private static String nombreUsuario;
    private static boolean conexion;
    // Private static float saldo; ?????

    public static void main(String[] args) {
        try {
            socket = new Socket(HOST, PUERTO);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);
            scanner = new Scanner(System.in);

            System.out.println("SIUUU!! Conectado al server en " + HOST + ":" + PUERTO);

            boolean autenticado = false;
            while (!autenticado) {
                System.out.println("======= BIENVENIDO =======");
                System.out.println("1. Eres nuevo? Regístrate!");
                System.out.println("2. Iniciar sesión");
                System.out.println("3. Salir");
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
                        System.out.println("Hasta luego!");
                        socket.close();
                        break;

                    default: // Hecho automáticamente no sé
                        System.out.println("Opción inválida");
                        break;
                }
            }
            conexion = true;
            while (conexion) {
                mostrarMenu();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static boolean registro() {
        System.out.println("\n=== REGISTRO DE NUEVO USUARIO ===");
        System.out.print("Elige un nombre de usuario: ");
        String usuario = scanner.nextLine();

        System.out.print("Elige una contraseña: ");
        String password = scanner.nextLine();

        System.out.print("Confirma la contraseña: ");
        String passwordConfirm = scanner.nextLine();

        // Validación local
        if (!password.equals(passwordConfirm)) {
            System.out.println("Las contraseñas no coinciden");
            return false;
        }

        // Enviar solicitud de registro al servidor
        out.println("REGISTER:" + usuario + ":" + password);

        try {
            String respuesta = in.readLine();
            if (respuesta.startsWith("REGISTER_OK")) {
                nombreUsuario = usuario;
                System.out.println("Registrado correctamente! Bienvenido " + usuario);
                return true;
            } else if (respuesta.startsWith("REGISTER_ERROR:")) {
                String error = respuesta.substring(15);
                System.out.println(error);
                return false;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return false;
    }

    private static boolean login() {
        System.out.println("Escribe tu nombre:");
        nombreUsuario = scanner.nextLine();
        System.out.println("Escribe la contraseña:");
        // Guardo contraseña en atributo? No se va a volver a usar, no?
        out.println("LOGIN:" + nombreUsuario + ":" + scanner.nextLine());

        String respuesta;
        try {
            respuesta = in.readLine();
            if (respuesta.equals("LOGIN_OK")) {
                System.out.println("ME HE LOGEADO BIENN");
                return true;
            } else if (respuesta.startsWith("LOGIN_ERROR:")) {
            String error = respuesta.substring(12);
            System.out.println("✗ " + error);
            return false;
            }
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return false;
        }
        return false;

    }

    private static void mostrarMenu() {
        System.out.println("======= MENÚ =======");
        System.out.println("1. Listar subastas");
        System.out.println("2. Informacion sobre una subasta concreta");
        System.out.println("3. Pujar");
        System.out.println("4. Consultar saldo");
        System.out.println("5. Consultar historial");
        System.out.println("6. Salir");
        
        System.out.print("Opción: ");

        int opcion = scanner.nextInt();
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
                salir();
                break;
            default: // Hecho automáticamente no sé
                throw new AssertionError();
        }
    }

    private static void listarSubastas() {
        System.out.println("He solicitado la lista de subastas al servidor");
        out.println("LIST");
        // leer la respuesta

        try {
            String respuesta = in.readLine();

            if (respuesta.equals("LISTA_VACIA")) {
                System.out.println("No hay subastas disponibles");
                return;
            } else if (respuesta.startsWith("LISTA:")) {
                String listaSubastas = respuesta.substring(6); // Quitar "LISTA:"
                String[] subastas = listaSubastas.split(";");

                System.out.println("Subastas disponibles:");
                for (String subasta : subastas) {

                    String[] partes = subasta.split("\\|"); // | escapado

                    String id = partes[0];
                    String titulo = partes[1];
                    String precioActual = partes[2];
                    String pujador = partes[3];
                    String tiempoRestante = partes[4];

                    System.out.println(id + ". " + titulo);
                    System.out.println("   Precio actual: " + precioActual + "€");
                    System.out.println("   Pujador líder: " + pujador);
                    System.out.println("   Tiempo restante: " + tiempoRestante + " segundos");
                }
            }

        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    private static void infoSubasta() {
        System.out.println("Elige una subasta entre las disponibles:");
        int idSubasta = scanner.nextInt(); //Manejar si no mete un int?
        out.println("INFO:" + idSubasta);
        try {
            System.out.println(in.readLine());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void pujar() {
        System.out.println("Introduce el numero de la subasta en la que quieres pujar:");
        int idSubasta = scanner.nextInt();
        System.out.println("Introduce la cantidad que quieras pujar:");
        double cantidad = scanner.nextDouble();
        // Es como raro pasarle las dos cosas a la vez. Hacer un intermedio de pasarle
        // la subasta,
        // ver si existe o lo que sea, mostrarla, y ya pasarle la cantidad?
        out.println("BID:" + idSubasta + ":" + cantidad);

        try {
            String respuesta = in.readLine();
            if (respuesta.startsWith("BID_OK")) {
                System.out.println("La puja aceptada");

            } else if (respuesta.startsWith("BID_ERROR")) {
                String resto = respuesta.substring(10);
                System.out.println("La puja no fue aceptada: " + resto);

            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void consultarHistorial(){
    out.println("CONSULT:history");
    try {
        String respuesta = in.readLine();
        if (respuesta.startsWith("HISTORIAL:")) {
            String historial = respuesta.substring(10);
            historial = historial.replace("{{NL}}", "\n");
            System.out.println(historial);
        } else {
            System.out.println("Error al consultar historial");
        }
    } catch (IOException e) {
        e.printStackTrace();
    }
}

private static void consultarSaldo(){
    out.println("CONSULT:credit");
    try {
        String respuesta = in.readLine();
        if (respuesta.startsWith("SALDO:")) {
            String mensaje = respuesta.substring(6);
            System.out.println("\n " + mensaje);
        } else {
            System.out.println("Error al consultar saldo");
        }
    } catch (IOException e) {
        e.printStackTrace();
    }
}

    private static void salir() {
        out.println("SALIR");
        conexion = false;
    }
}
