package cliente;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

public class ClienteSubastas {

//He puesto toda la clase estática porque así es más cómodo y no tiene por qué dar problemas
//Cada cliente se va a ejecutar por separado, no usamos objetos y ya.
//No tenemos que instanciar objetos y tenemos acceso fácil

    private static final String HOST = "localhost";
    private static final int PUERTO = 5000;

    private static Socket socket;
    private static BufferedReader in;
    private static PrintWriter out;
    private static Scanner scanner;
    private static String nombreUsuario;
    //Private static float saldo;   ?????
    

    public static void main(String[] args) {
        try {
            socket = new Socket(HOST, PUERTO);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);
            scanner = new Scanner(System.in);

            System.out.println("SIUUU!! Conectado al server en " + HOST + ":" + PUERTO);
            
            if (login()){
                mostrarMenu();
            }


        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static boolean login(){
        System.out.println("Escribe tu nombre:");
        nombreUsuario = scanner.nextLine();
        System.out.println("Escribe la contraseña:");
        //Guardo contraseña en atributo? No se va a volver a usar, no?
        out.println("LOGIN:" + nombreUsuario + ":" + scanner.nextLine());

        String respuesta;
        try {
            respuesta = in.readLine();
            if (respuesta.equals("LOGIN_OK")){
                System.out.println("ME HE LOGEADO BIENN");
            return true; 
        }
        else{
            return false;
        }
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return false;
        }
        
    }
    private static void mostrarMenu(){
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
                default: //Hecho automáticamente no sé
                    throw new AssertionError();
            }
    }

    private static void listarSubastas(){
        out.println("LIST");
        //leer la respuesta
    }

    private static void pujar(){
        System.out.println("Introduce el numero de la subasta en la que quieres pujar:");
        int idSubasta = scanner.nextInt();
        System.out.println("Introduce la cantidad que quieras pujar:");
        int cantidad = scanner.nextInt();
        //Es como raro pasarle las dos cosas a la vez. Hacer un intermedio de pasarle la subasta,
        //ver si existe o lo que sea, mostrarla, y ya pasarle la cantidad?
        out.println("BID:" + idSubasta + ":" + cantidad);
    }
    private static void salir(){
        //No se
    }
}
