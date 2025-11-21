package com.eventzone.eventzone.service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
public class ImagenService {

    private final String uploadDir = "C:\\Users\\wul4p\\Desktop\\proyecto_practicas\\eventzone\\uploads";

    public String guardarImagen(MultipartFile archivo) throws IOException {
        // Crear carpeta si no existe
        File carpeta = new File(uploadDir);
        if (!carpeta.exists()) carpeta.mkdirs();

        // Nombre único
        String nombreOriginal = archivo.getOriginalFilename();
        String nombreFinal = UUID.randomUUID() + "_" + nombreOriginal;

        Path ruta = Paths.get(uploadDir, nombreFinal);

        // Guardar archivo
        archivo.transferTo(ruta.toFile());

        // Retornar URL final para frontend
        return "/uploads/" + nombreFinal;
    }
}

