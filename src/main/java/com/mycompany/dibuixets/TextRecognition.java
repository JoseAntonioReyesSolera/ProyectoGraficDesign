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

    private static Tesseract ts;

    
    public static String getTextFromImage(File imageFile){
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

    private static String recognizeTextFromImage(String imagePath, String outputTextFilePath) {
        try {
            BufferedImage image = getImage(imagePath);
            if (image != null) {
                // Realizar OCR y obtener el texto
                String text = ts.doOCR(image);                
                return text;
            }
        } catch (TesseractException | IOException e) {
            e.printStackTrace();
        }
        return "";
    }

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
