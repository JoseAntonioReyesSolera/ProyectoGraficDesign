/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.dibuixets;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import net.sourceforge.tess4j.*;
import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

public class TextRecognition {

    private final Tesseract ts;

    public TextRecognition() {
        ts = new Tesseract();
        ts.setDatapath(new File("tessdata").getAbsolutePath());
        ts.setLanguage("eng");
    }

    public void recognizeTextFromImage(String imagePath, String outputImagePath) {
        try {
            BufferedImage image = getImage(imagePath);
            if (image != null) {
                // Realizar OCR
                String text = ts.doOCR(image);

                // Dibujar sobre la imagen
                BufferedImage processedImage = drawTextOnImage(image, text);

                // Guardar la imagen procesada
                ImageIO.write(processedImage, "png", new File(outputImagePath));
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

    private BufferedImage drawTextOnImage(BufferedImage image, String text) {
        Graphics2D g = image.createGraphics();
        g.setColor(Color.RED);  // Color del rectángulo
        g.setStroke(new BasicStroke(3)); // Grosor del rectángulo
        g.drawRect(50, 50, image.getWidth() - 100, image.getHeight() - 100); // Dibujar rectángulo

        g.setColor(Color.YELLOW); // Color del texto
        g.setFont(new Font("Arial", Font.BOLD, 28));
        g.drawString(text, 60, 80); // Dibujar texto en la imagen
        g.dispose();
        return image;
    }

    public static void main(String[] args) {
        System.load("C:\\opencv\\build\\java\\x64\\opencv_java490.dll");

        TextRecognition textRecognition = new TextRecognition();
        textRecognition.recognizeTextFromImage(
            "C:\\Users\\Óscar\\Documents\\NetBeansProjects\\dibuixets\\src\\main\\resources\\images\\image.png",
            "C:\\Users\\Óscar\\Documents\\NetBeansProjects\\dibuixets\\src\\main\\resources\\images\\image_processed.png"
        );
    }
}

