package edu.eci.arep;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;

import edu.eci.IoC.Component;
import edu.eci.IoC.GetMapping;


/**
 * La clase HttpServer es un servidor HTTP simple que implementa la inversión
 * de control para mapear rutas HTTP a métodos anotados con @GetMapping en clases
 * anotadas con @Component.También proporciona una página de inicio predeterminada
 * para cargar archivos.
 */
public class HttpServer {
    /**
     * Mapa que almacena los métodos HTTP como valores y las rutas como claves.
     */
    public static Map<String,Method> services = new HashMap<>();
    /**
     * Ruta base para los archivos de clase.
     */
    public static final String pathToClasses = "edu/eci/IoC";
    /**
     * Instancia única del servidor HTTP.
     */
    private static HttpServer instance = new HttpServer();
    /**
     * Constructor privado para evitar la creación de instancias no deseadas.
     */
    private HttpServer() {}

    /**
     * Obtiene la instancia única del servidor HTTP.
     *
     * @return La instancia única del servidor HTTP.
     */
    public static HttpServer getInstance() {
        return instance;
    }

    /**
     * Inicia el servidor HTTP y escucha en un socket del servidor.
     *
     * @param args Argumentos de línea de comandos.
     * @throws IOException Si ocurre un error de entrada/salida.
     * @throws ClassNotFoundException Si no se encuentra una clase especificada.
     * @throws InvocationTargetException Si ocurre un error al invocar un método.
     * @throws IllegalAccessException Si no se tiene acceso a un miembro.
     */
    public void start(String[] args) throws IOException, ClassNotFoundException, InvocationTargetException, IllegalAccessException {
        inversionOfControl();
        ServerSocket serverSocket = establishServerSocket();
        boolean running = true;
        while (running) {
            Socket clientSocket = processClientConnection(serverSocket);
            handleClientCommunication(clientSocket);
        }
        serverSocket.close();
    }

    /**
     * Establece un socket del servidor en el puerto especificado.
     *
     * @return El socket del servidor establecido.
     * @throws IOException Sicurre un error de entrada/salida.
     */
    private ServerSocket establishServerSocket() {
        try {
            return new ServerSocket(35000);
        } catch (IOException e) {
            System.err.println("Could not listen on port: 35000.");
            System.exit(1);
            return null;
        }
    }

    /**
     * Procesa la conexión de un cliente y devuelve el socket de la conexión.
     *
     * @param serverSocket El socket del servidor para aceptar conexiones.
     * @return El socket de la conexión del cliente.
     * @throws IOException Si ocurre un error de entrada/salida.
     */
    private Socket processClientConnection(ServerSocket serverSocket) {
        try {
            System.out.println("Listo para recibir ...");
            return serverSocket.accept();
        } catch (IOException e) {
            System.err.println("Accept failed.");
            System.exit(1);
            return null;
        }
    }

    /**
     * Maneja la comunicación con el cliente y procesa su solicitud.
     *
     * @param clientSocket El socket de la conexión del cliente.
     * @throws IOException Si ocurre un error de entrada/salida.
     * @throws InvocationTargetException Si ocurre un error al invocar un método.
     * @throws IllegalAccessException Si no se tiene acceso a un miembro.
     */
    private void handleClientCommunication(Socket clientSocket) throws IOException, InvocationTargetException, IllegalAccessException {
        try (Socket socket = clientSocket;
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

            String request = processClientRequest(in);
            String response = processRequest(request);
            sendServerResponse(out, response);

        } catch (IOException e) {
            System.out.println("Error en la comunicación con el cliente: " + e.getMessage());
        } catch (InvocationTargetException e) {
            System.out.println("Error en la invocación del método: " + e.getMessage());
        } catch (IllegalAccessException e) {
            System.out.println("Error de acceso al método: " + e.getMessage());
        } finally {
            if (!clientSocket.isClosed()) {
                clientSocket.close();
            }
        }
    }

    /**
     * Procesa la solicitud del cliente y devuelve la ruta y el verbo de la solicitud.
     *
     * @param in El lector de buffer para leer la solicitud del cliente.
     * @return La ruta y el verbo de la solicitud del cliente.
     * @throws IOException Si ocurre un error de entrada/salida.
     */
    private String processClientRequest(BufferedReader in) throws IOException {
        String inputLine, request = "/simple";
        String verb = "";
        boolean firstLine = true;

        do {
            inputLine = in.readLine();
            if (inputLine == null) {
                break;
            }
            System.out.println("Received: " + inputLine);
            if (firstLine) {
                String[] requestTokens = inputLine.split(" ");
                if (requestTokens.length >= 2) {
                    request = requestTokens[1];
                    verb = requestTokens[0];
                }
                firstLine = false;
            }
            if (inputLine.contains("title?name")) {
                String[] firstSplit = inputLine.split("=");
            }
        } while (in.ready());

        return verb + " " + request;
    }

    /**
     * Procesa la solicitud y devuelve la respuesta correspondiente.
     *
     * @param request La solicitud a procesar.
     * @return La respuesta correspondiente a la solicitud.
     * @throws InvocationTargetException Si ocurre un error al invocar un método.
     * @throws IllegalAccessException Si no se tiene acceso a un miembro.
     */
    private String processRequest(String request) throws InvocationTargetException, IllegalAccessException {
        String[] requestParts = request.split(" ");
        String verb = requestParts[0];
        String path = requestParts[1];

        if ("GET".equalsIgnoreCase(verb) && services.containsKey(path)) {
            Object result = services.get(path).invoke(null);
            return result != null ? result.toString() : "";
        } else {
            return getHomeIndex();
        }
    }

    /**
     * Envía la respuesta al cliente.
     *
     * @param out El escritor de impresión para enviar la respuesta al cliente.
     * @param response La respuesta a enviar al cliente.
     */
    private void sendServerResponse(PrintWriter out, String response) {
        out.println(response);
        out.close();
    }

    /**
     * Obtiene una lista de clases en el directorio especificado.
     *
     * @return Una lista de clases en el directorio especificado.
     * @throws Exception Si ocurre un error al obtener las clases.
     */
    private List<Class<?>> getClasses(){
        List<Class<?>> classes = new ArrayList<>();
        try{
            for (String cp: getClassProperty()){
                File file = new File(cp + "/" + pathToClasses);
                if(file.exists() && file.isDirectory()){
                    for (File cf: Objects.requireNonNull(file.listFiles())){
                        if(cf.isFile() && cf.getName().endsWith(".class")){
                            String rootTemp = pathToClasses.replace("/",".");
                            String className = rootTemp+"."+cf.getName().replace(".class","");
                            Class<?> clasS =  Class.forName(className);
                            classes.add(clasS);
                        }
                    }
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return classes;
    }

    /**
     * Obtiene las rutas de clase desde la propiedad del sistema.
     *
     * @return Una lista de rutas de clase.
     */
    public ArrayList<String> getClassProperty(){
         String classPath = System.getProperty("java.class.path");
        String[] classPaths =  classPath.split(System.getProperty("path.separator"));
        return new ArrayList<>(Arrays.asList(classPaths));
    }

    /**
     * Realiza la inversión de control al analizar las clases y métodos anotados con @Component y @GetMapping,
     * y almacenarlos en el mapa de servicios.
     *
     * @throws ClassNotFoundException Si ocurre un error al obtener una clase.
     */
    public  void inversionOfControl() throws ClassNotFoundException{
        List<Class<?>> classes = getClasses();
        for (Class<?> clasS:classes){
            if(clasS.isAnnotationPresent(Component.class)){
                Class<?> c = Class.forName(clasS.getName());
                Method[] m = c.getMethods();
                for (Method me: m){
                    if(me.isAnnotationPresent(GetMapping.class)){
                        String key = me.getAnnotation(GetMapping.class).value();
                        services.put(key,me);
                    }
                }
            }

        }
    }

    /**
     * Devuelve una cadena de texto que representa el contenido HTML de la página de inicio del servidor.
     *
     * @return Una cadena de texto que representa el contenido HTML de la página de inicio del servidor.
     */
    public static String getHomeIndex() {
        return "HTTP/1.1 200 OK\r\n"
                + "Content-Type: text/html\r\n"
                + "\r\n"
                + "<!DOCTYPE html>\n" +
                "<html lang=\"en\">\n" +
                "<head>\n" +
                "    <meta charset=\"UTF-8\">\n" +
                "    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n" +
                "    <title>File Hub</title>\n" +
                "    <style>\n" +
                "        body {\n" +
                "             background-image: url(https://i.pinimg.com/736x/fb/1a/7b/fb1a7be0ff2251b4ff0b9e443b74c638.jpg);\n" +
                "            font-family: \"Ubuntu\", Impact;\n" +
                "            display: flex;\n" +
                "            justify-content: center;\n" +
                "            align-items: center;\n" +
                "            height: 100vh;\n" +
                "            margin: 0;\n" +
                "        }\n" +
                "\n" +
                "        form {\n" +
                "            display: inline-block;\n" +
                "            text-align: center;\n" +
                "        }\n" +
                "\n" +
                "        label, input[type=\"file\"], input[type=\"button\"] {\n" +
                "            display: block;\n" +
                "            margin: 10px 0;\n" +
                "            text-align: center;\n" +
                "        }\n" +
                "\n" +
                "        #uploadMsg {\n" +
                "            margin-top: 20px;\n" +
                "        }\n" +
                "    </style>\n" +
                "</head>\n" +
                "<body>\n" +
                "    <form action=\"/upload\" method=\"POST\" enctype=\"multipart/form-data\">\n" +
                "        <label for=\"file\">Sube tu archivo aquí.</label><br>\n" +
                "        <input type=\"file\" id=\"file\" name=\"file\"><br><br>\n" +
                "        <input type=\"button\" value=\"Visualiza tu Archivo\" onclick=\"uploadFile()\">\n" +
                "    </form>\n" +
                "\n" +
                "    <div id=\"uploadMsg\"></div>\n" +
                "\n" +
                "    <script>\n" +
                "        function uploadFile() {\n" +
                "            const fileInput = document.getElementById(\"file\");\n" +
                "            const file = fileInput.files[0];\n" +
                "            const formData = new FormData();\n" +
                "            formData.append(\"file\", file);\n" +
                "\n" +
                "            const xhr = new XMLHttpRequest();\n" +
                "            xhr.onload = function () {\n" +
                "                document.getElementById(\"uploadMsg\").innerHTML = this.responseText;\n" +
                "            };\n" +
                "            xhr.open(\"POST\", \"/upload\");\n" +
                "            xhr.send(formData);\n" +
                "        }\n" +
                "    </script>\n" +
                "</body>\n" +
                "</html>";
    }

}