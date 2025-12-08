package servidor;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import compartido.Subasta;
import compartido.Usuario;

import jakarta.xml.bind.*;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "subastas")
class SubastasXML { // Para no mezclar logica de negocio con persistencia
    @XmlElement(name = "subasta")
    public List<Subasta> subastas = new ArrayList<>();
}

@XmlRootElement(name = "usuarios")
class UsuariosXML {
    @XmlElement(name = "usuario")
    public List<Usuario> usuarios = new ArrayList<>();
}

public class GestorXML {
    private static final String ARCHIVO_SUBASTAS = "./persistencia/subastas.xml";
    private static final String ARCHIVO_USUARIOS = "./persistencia/usuarios.xml";

    public static void guardarSubastas(List<Subasta> subastas) {
        try {
            SubastasXML wrapper = new SubastasXML();
            wrapper.subastas = new ArrayList<>(subastas); // Metes todas las subastas en el contenedor

            JAXBContext context = JAXBContext.newInstance(SubastasXML.class);
            Marshaller marshaller = context.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
            marshaller.marshal(wrapper, new File(ARCHIVO_SUBASTAS));

            System.out.println("[XML] Subastas guardadas en " + ARCHIVO_SUBASTAS);

        } catch (JAXBException e) {
            System.err.println("[ERROR XML] No se pudieron guardar subastas: " + e.getMessage());
        }
    }

    public static List<Subasta> cargarSubastas() {
        try {
            File archivo = new File(ARCHIVO_SUBASTAS);
            File directorio = archivo.getParentFile();
            if (directorio != null && !directorio.exists()) {
                directorio.mkdirs();
            }

            if (!archivo.exists()) {
                System.out.println("[XML] No se encontró el archivo de subastas. Se cargarán subastas vacías.");
                return new ArrayList<>();
            }

            JAXBContext context = JAXBContext.newInstance(SubastasXML.class);
            Unmarshaller unmarshaller = context.createUnmarshaller();
            SubastasXML wrapper = (SubastasXML) unmarshaller.unmarshal(archivo);

            System.out.println("[XML] Se cargaron " + wrapper.subastas.size() + " subastas");
            return wrapper.subastas;

        } catch (JAXBException e) {
            System.err.println("[ERROR XML] No se pudieron cargar subastas: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    public static void guardarUsuarios(Collection<Usuario> usuarios) {
        try {
            UsuariosXML wrapper = new UsuariosXML();
            wrapper.usuarios = new ArrayList<>(usuarios);

            JAXBContext context = JAXBContext.newInstance(UsuariosXML.class);
            Marshaller marshaller = context.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
            marshaller.marshal(wrapper, new File(ARCHIVO_USUARIOS));

            System.out.println("[XML] Usuarios guardados en " + ARCHIVO_USUARIOS);
        } catch (JAXBException e) {
            System.err.println("[ERROR XML] No se pudieron guardar usuarios: " + e.getMessage());
        }
    }

    public static List<Usuario> cargarUsuarios() {
        try {
            File archivo = new File(ARCHIVO_USUARIOS);

            File directorio = archivo.getParentFile();
            if (directorio != null && !directorio.exists()) {
                directorio.mkdirs();
            }

            if (!archivo.exists()) {
                System.out.println("[XML] No hay archivo de usuarios. Usando estado en memoria.");
                return new ArrayList<>();
            }
            JAXBContext context = JAXBContext.newInstance(UsuariosXML.class);
            Unmarshaller unmarshaller = context.createUnmarshaller();
            UsuariosXML wrapper = (UsuariosXML) unmarshaller.unmarshal(archivo);

            System.out.println("[XML] Se cargaron " + wrapper.usuarios.size() + " usuarios");
            return wrapper.usuarios;
        } catch (JAXBException e) {
            System.err.println("[ERROR XML] No se pudieron cargar usuarios: " + e.getMessage());
            return new ArrayList<>();
        }
    }
}