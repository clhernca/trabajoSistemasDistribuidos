package servidor;

import compartido.Usuario;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class GestorUsuarios {
        private Map<String, Usuario> usuarios;

        public GestorUsuarios() {
                this.usuarios = Collections.synchronizedMap(new HashMap<>());
        }
        public void crearUsuario(String nombre, String contraseña, double saldoInicial) {
        if (!usuarios.containsKey(nombre)) {
            usuarios.put(nombre, new Usuario(nombre, contraseña, saldoInicial));
//            System.out.println("EN EL PUT CON " + nombre+contraseña+saldoInicial);
        }
    }

    public synchronized boolean registrar(String nombre, String contrasena) {
        if (obtenerUsuario(nombre) != null) {
            return false;
        }
        
        Usuario nuevoUsuario = new Usuario(nombre, contrasena, 1000.0);
        agregarUsuario(nuevoUsuario);
        
        // Guardar en el XML???????????
        
        return true;
    }

    public Usuario login(String nombre, String contraseña) { 
        System.out.println("FUNCION LOGIN DE GESTOR: PARAMETROS: " + nombre + contraseña);
        Usuario usuario = usuarios.get(nombre);
        if (usuario != null && usuario.getContraseña().equals(contraseña)) {
            return usuario;
        }
        return null;
    }

    public Usuario obtenerUsuario(String nombre) {
        return usuarios.get(nombre);
    }

    public boolean existeUsuario(String nombre) {
        return usuarios.containsKey(nombre);
    }

    public void agregarUsuario(Usuario usuario) {
        usuarios.put(usuario.getNombre(), usuario);
    }

    public Collection<Usuario> obtenerTodosUsuarios() {
        return usuarios.values();
    }
    
    
    public void inicializarUsuariosDemo() {
        crearUsuario("alice", "pass123", 500.00);
        crearUsuario("bob", "pass456", 400.00);
        crearUsuario("charlie", "pass789", 600.00);
        System.out.println("[SERVIDOR] Se crearon 3 usuarios de demo");
    
    }
}


