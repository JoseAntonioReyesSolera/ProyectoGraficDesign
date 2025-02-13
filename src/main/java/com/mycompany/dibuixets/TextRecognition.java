package com.mycompany.dibuixets;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import javax.imageio.ImageIO;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

public class TextRecognition {

    private final Tesseract ts;

    public TextRecognition() {
        ts = new Tesseract();
        ts.setDatapath(new File("tessdata").getAbsolutePath());
        ts.setLanguage("eng");
    }

    public void recognizeTextFromImage(String imagePath, String outputTextFilePath) {
        try {
            BufferedImage image = getImage(imagePath);
            if (image != null) {
                // Realizar OCR y obtener el texto
                String text = ts.doOCR(image);

                // Escribir el texto reconocido en un archivo .txt
                Files.write(Paths.get(outputTextFilePath), text.getBytes(), StandardOpenOption.CREATE);
                
                System.out.println("Archivo de texto generado: " + outputTextFilePath);
            }
        } catch (TesseractException | IOException e) {
            e.printStackTrace();
        }
    }

    private BufferedImage getImage(String imgPath) throws IOException {
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

    public static void main(String[] args) {
        // Cargar la librería de OpenCV
        System.load("C:\\opencv\\build\\java\\x64\\opencv_java490.dll");

        TextRecognition textRecognition = new TextRecognition();
        textRecognition.recognizeTextFromImage(
            "C:\\Users\\Óscar\\Documents\\NetBeansProjects\\dibuixets\\src\\main\\resources\\images\\eurotext.png",
            "C:\\Users\\Óscar\\Documents\\NetBeansProjects\\dibuixets\\src\\main\\resources\\images\\eurotext.txt"
        );
    }
}
