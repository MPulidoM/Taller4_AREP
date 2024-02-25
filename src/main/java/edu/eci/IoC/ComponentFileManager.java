package edu.eci.IoC;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.util.Base64;

/**
 * Clase que gestiona los archivos del componente.
 */
@Component
public class ComponentFileManager {

    /**
     * Ruta del archivo Kit.png.
     */
    public static String filepath = "src\\main\\resource\\Kit.png";

    /**
     * Ruta del archivo HTML PruebaHtml.html.
     */
    public static String htmlPath = "src\\main\\resource\\PruebaHtml.html";

    /**
     * Endpoint que devuelve un mensaje de prueba de funcionamiento.
     *
     * @return Mensaje de prueba de funcionamiento en formato HTTP.
     */
    @GetMapping("/hello")
    public static String getHello() {
        return "HTTP/1.1 200 OK\r\n" +
                "Content-type: text/html\r\n" +
                "\r\n" +
                "Hola Esto es una prueba de Funcionamiento";
    }

    /**
     * Endpoint que devuelve el archivo Kit.png en formato Base64.
     *
     * @return Kit.png en formato Base64 en formato HTTP.
     * @throws IOException Si ocurre un error de entrada/salida.
     */
    @GetMapping("/image")
    public static String getImagePng() throws IOException {
        File file = new File(filepath);
        byte[] bytes = Files.readAllBytes(file.toPath());
        String base64 = Base64.getEncoder().encodeToString(bytes);
        return "HTTP/1.1 200 OK\r\n"
                + "Content-Type: text/html\r\n"
                + "\r\n"
                + "<!DOCTYPE html>\r\n"
                + "<html>\r\n"
                + "    <head>\r\n"
                + "        <title>File Content</title>\r\n"
                + "    </head>\r\n"
                + "    <body>\r\n"
                + "         <center><h1>" + "Prueba Archivos PNG - Imagen" + "</h1></center>" + "\r\n"
                + "         <center><img src=\"data:image/jpeg;base64," + base64 + "\" alt=\"image\"></center>" + "\r\n"
                + "    </body>\r\n"
                + "</html>";
    }

    /**
     * Endpoint que devuelve el contenido del archivo PruebaHtml.html.
     *
     * @return Contenido del archivo PruebaHtml.html en formato HTTP.
     * @throws IOException Si ocurre un error de entrada/salida.
     */
    @GetMapping("/host")
    public static String getHTMLPages() throws IOException {
        File file = new File(htmlPath);
        StringBuilder body = fromArchiveToString(file);
        return "HTTP/1.1 200 OK\r\n"
                + "Content-Type: text/html\r\n"
                + "\r\n"
                + "<!DOCTYPE html>\r\n"
                + "<html>\r\n"
                + "    <head>\r\n"
                + "        <meta charset=\"UTF-8\">\r\n"
                + "        <title>File Adder</title>\r\n"
                + "    </head>\r\n"
                + "    <body>\r\n"
                + "        <pre>" + body + "</pre>\r\n"
                + "    </body>\r\n"
                + "</html>";
    }

    /**
     * Método que lee el contenido de un archivo y lo devuelve como una cadena de texto.
     *
     * @param file Archivo a leer.
     * @return Contenido del archivo como una cadena de texto.
     * @throws IOException Si ocurre un error de entrada/salida.
     */
    public static StringBuilder fromArchiveToString(File file) throws IOException {
        StringBuilder body = new StringBuilder();

        BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), "UTF-8"));
        String line;
        while ((line = reader.readLine()) != null) {
            body.append(line).append("\n");
        }
        reader.close();
        return body;
    }
}
