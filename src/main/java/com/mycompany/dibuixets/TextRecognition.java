package com.mycompany.dibuixets;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

/**
 * Clase para el reconocimiento de texto en imágenes utilizando Tesseract y OpenCV.
 */
public class TextRecognition {

    private static Tesseract ts;

    /**
     * Obtiene el texto de una imagen utilizando OCR.
     *
     * @param imageFile Archivo de imagen a procesar.
     * @return Texto extraído de la imagen.
     */
    public static String getTextFromImage(File imageFile) {
        // Cargar la librería de OpenCV
        System.load(Preferences.getOpenCVPath());
        File textFolder = new File("SavedText");
        
        ts = new Tesseract();
        ts.setDatapath(new File("tessdata").getAbsolutePath());
        ts.setLanguage("eng");
        
        if (!textFolder.exists()) {
            textFolder.mkdir();
        }
        
        return recognizeTextFromImage(
            imageFile.getAbsolutePath(),
            textFolder.getAbsolutePath() + "/analizedText.txt"
        );
    }

    /**
     * Realiza el reconocimiento de texto a partir de una imagen.
     *
     * @param imagePath Ruta de la imagen a analizar.
     * @param outputTextFilePath Ruta del archivo donde se guardará el texto extraído.
     * @return Texto reconocido en la imagen.
     */
    private static String recognizeTextFromImage(String imagePath, String outputTextFilePath) {
        try {
            BufferedImage image = getImage(imagePath);
            if (image != null) {
                // Realizar OCR y obtener el texto
                return ts.doOCR(image);
            }
        } catch (TesseractException | IOException e) {
            e.printStackTrace();
        }
        return "";
    }

    /**
     * Carga y procesa una imagen para mejorar la precisión del OCR.
     *
     * @param imgPath Ruta de la imagen a procesar.
     * @return Imagen procesada como BufferedImage.
     * @throws IOException Si ocurre un error al leer la imagen.
     */
    private static BufferedImage getImage(String imgPath) throws IOException {
        Mat mat = Imgcodecs.imread(imgPath);
        if (mat.empty()) {
            return null;
        }

        Mat gray = new Mat();
        Imgproc.cvtColor(mat, gray, Imgproc.COLOR_BGR2GRAY);

        Mat resized = new Mat();
        Size size = new Size(mat.width() * 1.9, mat.height() * 1.9);
        Imgproc.resize(gray, resized, size);

        MatOfByte mof = new MatOfByte();
        Imgcodecs.imencode(".png", resized, mof);
        BufferedImage bufferedImage = ImageIO.read(new ByteArrayInputStream(mof.toArray()));

        mat.release();
        gray.release();
        resized.release();
        mof.release();

        return bufferedImage;
    }
}
