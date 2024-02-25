package edu.eci.arep;
import org.junit.Before;
import org.junit.Test;

import edu.eci.IoC.ComponentFileManager;

import static org.junit.Assert.*;

import java.io.IOException;

public class ComponentFileManagerTest {

    private ComponentFileManager componentFileManager;


    @Test
    public void ShoulGetHelloPage() {
        componentFileManager = new ComponentFileManager();
        String helloResponse = ComponentFileManager.getHello();
        assertNotNull(helloResponse);
        assertTrue(helloResponse.contains("HTTP/1.1 200 OK"));
        assertTrue(helloResponse.contains("Hola Esto es una prueba de Funcionamiento"));
    }

    @Test
    public void ShouldGetContainImage() {
        componentFileManager = new ComponentFileManager();
        try {
            String imageResponse = ComponentFileManager.getImagePng();
            assertNotNull(imageResponse);
            assertTrue(imageResponse.contains("HTTP/1.1 200 OK"));
            assertTrue(imageResponse.contains("<center><h1>Prueba Archivos PNG - Imagen</h1></center>"));
            assertTrue(imageResponse.contains("data:image/jpeg;base64,"));
        } catch (IOException e) {
            fail("IOException occurred while testing getImage: " + e.getMessage());
        }
    }

    @Test
    public void testHTMLContent() throws IOException {
        componentFileManager = new ComponentFileManager();
        String html = ComponentFileManager.getHTMLPages();
        assertTrue(html.contains("<title>File Adder</title>"));

    }
}




