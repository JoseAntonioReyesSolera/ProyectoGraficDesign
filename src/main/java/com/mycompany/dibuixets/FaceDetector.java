/**
 * Clase encargada de detectar rostros en una imagen y guardar el resultado con los rostros marcados.
 */
package com.mycompany.dibuixets;

import java.io.File;
import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;
import org.opencv.objdetect.Objdetect;

/**
 * Clase FaceDetector que proporciona funcionalidad para detectar rostros en una imagen
 * y guardar la imagen con los rostros resaltados.
 */
public class FaceDetector {

    /**
     * Detecta rostros en la imagen proporcionada y guarda la imagen con los rostros resaltados.
     *
     * @param imagePath Archivo de imagen en el que se buscarán los rostros.
     * @return Archivo de imagen con los rostros detectados y resaltados.
     */
    public static File detectAndSave(File imagePath) {
        // Cargar la biblioteca OpenCV
        System.load(Preferences.getOpenCVPath());
        
        // Crear objeto para almacenar las detecciones de rostros
        MatOfRect faces = new MatOfRect();
        
        // Leer la imagen desde el archivo
        Mat image = Imgcodecs.imread(imagePath.getAbsolutePath());
        
        // Convertir la imagen a escala de grises para mejorar la detección
        Mat grayFrame = new Mat();
        Imgproc.cvtColor(image, grayFrame, Imgproc.COLOR_BGR2GRAY);
        
        // Mejorar el contraste de la imagen en escala de grises
        Imgproc.equalizeHist(grayFrame, grayFrame);
        
        // Definir el tamaño mínimo de rostros detectables en píxeles
        int height = grayFrame.height();
        int absoluteFaceSize = 0;
        if (Math.round(height * 0.2f) > 0) {
            absoluteFaceSize = Math.round(height * 0.2f);
        }
        
        // Cargar el clasificador de rostros preentrenado
        CascadeClassifier faceCascade = new CascadeClassifier();
        faceCascade.load("data/haarcascade_frontalface_alt2.xml");
        
        // Detectar rostros en la imagen
        faceCascade.detectMultiScale(grayFrame, faces, 1.1, 2, 0 | Objdetect.CASCADE_SCALE_IMAGE, 
                new Size(absoluteFaceSize, absoluteFaceSize), new Size());
        
        // Dibujar rectángulos alrededor de los rostros detectados
        Rect[] faceArray = faces.toArray();
        for (int i = 0; i < faceArray.length; i++) {
            Imgproc.rectangle(image, faceArray[i], new Scalar(255, 123, 45), 3);
        }
        
        // Guardar la imagen con los rostros resaltados
        Imgcodecs.imwrite("images/output.jpg", image);
        
        return new File("images/output.jpg");
    }
}
