package com.eventzone.eventzone.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
public class ImagenService {

	// Inyecta la ruta desde application.properties
	@Value("${app.upload.dir}")
	private String uploadDir;

	public String guardarImagen(MultipartFile archivo) throws IOException {
		// Crear carpeta si no existe
		File carpeta = new File(uploadDir);
		if (!carpeta.exists()) {
			carpeta.mkdirs();
		}

		// Generar nombre único
		String nombreFinal = UUID.randomUUID() + "_" + archivo.getOriginalFilename();

		Path ruta = Paths.get(uploadDir, nombreFinal);

		// Guardar archivo
		archivo.transferTo(ruta.toFile());

		// Retornar ruta para frontend
		return "/uploads/" + nombreFinal;
	}

	public void eliminarImagen(String imagenUrl) {
		if (imagenUrl == null || imagenUrl.trim().isEmpty()) return;

		String nombreArchivo = imagenUrl.replace("/uploads/", "");
		Path ruta = Paths.get(uploadDir, nombreArchivo);

		try {
			Files.deleteIfExists(ruta);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}