/**
 * Aplicación para capturar imágenes desde la webcam utilizando OpenCV y Swing.
 */
package com.mycompany.dibuixets;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.videoio.VideoCapture;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;

/**
 * Clase que representa una aplicación para capturar imágenes desde la webcam.
 * Extiende JFrame y utiliza OpenCV para la captura de imágenes.
 */
public class WebcamCaptureApp extends JFrame {
    private JLabel imageLabel;
    private VideoCapture capture;
    private Mat frame;
    private boolean capturing = false;

    /**
     * Constructor de la aplicación de captura de imágenes de la webcam.
     * Configura la interfaz gráfica y los elementos necesarios.
     */
    public WebcamCaptureApp() {
        setTitle("Captura d'Imatges de la Webcam");
        setSize(640, 480);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        imageLabel = new JLabel();
        add(imageLabel, BorderLayout.CENTER);

        JButton captureButton = new JButton("Capturar");
        captureButton.addActionListener(e -> captureImage());
        add(captureButton, BorderLayout.SOUTH);
    }

    /**
     * Captura una imagen de la webcam y la guarda en un archivo.
     * Solicita al usuario un nombre de archivo y lo almacena en la carpeta "images".
     */
    private void captureImage() {
        String fileName = JOptionPane.showInputDialog(this, "Introdueix el nom del fitxer:");
        if (fileName != null && !fileName.trim().isEmpty()) {
            File outputFile = new File("images/" + fileName + ".jpg");
            Imgcodecs.imwrite(outputFile.getAbsolutePath(), frame);
            JOptionPane.showMessageDialog(this, "Imatge desada com: " + outputFile.getAbsolutePath());
        }
    }

    /**
     * Inicia la captura de video desde la webcam y muestra los fotogramas en la interfaz gráfica.
     * Carga la biblioteca de OpenCV antes de iniciar la captura.
     */
    public void start() {
        System.load(Preferences.getOpenCVPath());
        capture = new VideoCapture(0);
        frame = new Mat();

        if (!capture.isOpened()) {
            JOptionPane.showMessageDialog(this, "No s'ha pogut obrir la webcam.");
            return;
        }

        capturing = true;
        new Thread(() -> {
            while (capturing) {
                capture.read(frame);
                if (!frame.empty()) {
                    BufferedImage img = matToBufferedImage(frame);
                    ImageIcon icon = new ImageIcon(img);
                    imageLabel.setIcon(icon);
                    imageLabel.repaint();
                }
            }
        }).start();
    }

    /**
     * Convierte un objeto Mat de OpenCV a BufferedImage.
     *
     * @param mat Matriz de imagen de OpenCV.
     * @return BufferedImage representando la imagen.
     */
    private BufferedImage matToBufferedImage(Mat mat) {
        MatOfByte matOfByte = new MatOfByte();
        Imgcodecs.imencode(".jpg", mat, matOfByte);
        byte[] byteArray = matOfByte.toArray();
        BufferedImage img = null;
        try {
            img = ImageIO.read(new ByteArrayInputStream(byteArray));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return img;
    }

    /**
     * Método principal que inicia la aplicación en el hilo de eventos de Swing.
     * 
     * @param args Argumentos de la línea de comandos (no utilizados).
     */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            WebcamCaptureApp app = new WebcamCaptureApp();
            app.setVisible(true);
            app.start();
        });
    }
}
