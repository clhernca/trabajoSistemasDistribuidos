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
    // Private static float saldo; ?????

    public static void main(String[] args) {
        try {
            socket = new Socket(HOST, PUERTO);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);
            scanner = new Scanner(System.in);

            System.out.println("SIUUU!! Conectado al server en " + HOST + ":" + PUERTO);

            if (login()) {
                mostrarMenu();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
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
            } else {
                return false;
            }
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return false;
        }

    }

    private static void mostrarMenu() {
        System.out.println("======= MENÚ =======");
        System.out.println("1. Listar subastas");
        System.out.println("2. Pujar");
        System.out.println("3. Salir");
        System.out.print("Opción: ");

        int opcion = scanner.nextInt();
        switch (opcion) {
            case 1:
                listarSubastas();
                break;
            case 2:
                pujar();
                break;
            case 3:
                salir();
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
                String resto = respuesta.substring(10); // Quitar "BID_ERROR:"
                System.out.println("La puja no fue aceptada: " + resto);

            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private static void salir() {
        // No se
    }
}
