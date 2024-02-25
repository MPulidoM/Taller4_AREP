package edu.eci.arep;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

public class ServerStarter {
    public static void main(String[] args) {
        HttpServer httpServer = HttpServer.getInstance();
        if(httpServer != null){
            try {
                httpServer.start(args);
            } catch (ClassNotFoundException | InvocationTargetException | IllegalAccessException | IOException e) {
                System.err.println("Error al iniciar el servidor HTTP:");
                e.printStackTrace();
            }
        }
    }
}
