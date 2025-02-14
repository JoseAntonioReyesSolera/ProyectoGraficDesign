package com.mycompany.dibuixets;

import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Stack;
import org.opencv.core.Point; // Para dibujar en la imagen con OpenCV

/**
 * Clase que permite dibujar sobre una imagen usando OpenCV y Java Swing.
 */
public class OpenCVDrawing extends JPanel {
    private Mat image, background;
    private BufferedImage bufferedImage;
    private java.awt.Point startPoint, endPoint;
    private Color currentColor = Color.RED;
    private int thickness = 2;
    private boolean eraserMode = false;
    private boolean drawingRectangle = false;
    private boolean drawingCircle = false;
    private Stack<Mat> undoStack = new Stack<>();
    private Stack<Mat> redoStack = new Stack<>();
    private JButton eraserButton;
    private double scaleX, scaleY, scale;
    private int newWidth, newHeight; 
    private int offsetX, offsetY;
    private static Main mainFrame;

    
    /**
     * Constructor de la clase OpenCVDrawing.
     * 
     * @param imagePath Ruta de la imagen a cargar.
     * @param eraserButton Botón para activar el modo borrador.
     */ 
    public OpenCVDrawing(String imagePath, JButton eraserButton) {
        System.load(Preferences.getOpenCVPath());
        this.eraserButton = eraserButton;
        image = Imgcodecs.imread(imagePath);
        background = image.clone();
        bufferedImage = matToBufferedImage(image);

    addMouseListener(new MouseAdapter() {
        @Override
        public void mousePressed(MouseEvent e) {
            startPoint = convertMousePointToImage(e.getPoint());
            saveState();
        }

        @Override
        public void mouseReleased(MouseEvent e) {
            if (drawingRectangle) {
                drawRectangle();
            } else if (drawingCircle) {
                drawCircle();
            }
            startPoint = null;
            endPoint = null;
        }
    });
    
    addMouseMotionListener(new MouseMotionAdapter() {
        @Override
        public void mouseDragged(MouseEvent e) {
            endPoint = convertMousePointToImage(e.getPoint());

            if (!drawingRectangle && !drawingCircle) {
                Scalar color = eraserMode ? getBackgroundColor(startPoint) : new Scalar(
                        currentColor.getBlue(), currentColor.getGreen(), currentColor.getRed());
                Imgproc.line(image, new org.opencv.core.Point(startPoint.x, startPoint.y),
                             new org.opencv.core.Point(endPoint.x, endPoint.y), color, thickness);
                startPoint = endPoint;
            }

            bufferedImage = matToBufferedImage(image);
            repaint();
        }
    });

    }
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        if (bufferedImage != null) {
            int panelWidth = getWidth();
            int panelHeight = getHeight();
            int imgWidth = bufferedImage.getWidth();
            int imgHeight = bufferedImage.getHeight();

            // Calcular escala y mantener proporción
            scaleX = (double) panelWidth / imgWidth;
            scaleY = (double) panelHeight / imgHeight;
            scale = Math.min(scaleX, scaleY);

            newWidth = (int) (imgWidth * scale);
            newHeight = (int) (imgHeight * scale);

            offsetX = (panelWidth - newWidth) / 2;
            offsetY = (panelHeight - newHeight) / 2;

            // Dibujar la imagen escalada y centrada
            g.drawImage(bufferedImage, offsetX, offsetY, newWidth, newHeight, this);
        }
    }
    
    private java.awt.Point convertMousePointToImage(java.awt.Point panelPoint) {
        int imgX = (int) ((panelPoint.x - offsetX) / scale);
        int imgY = (int) ((panelPoint.y - offsetY) / scale);

        // Asegurar que las coordenadas están dentro de los límites de la imagen
        imgX = Math.max(0, Math.min(imgX, image.width() - 1));
        imgY = Math.max(0, Math.min(imgY, image.height() - 1));

        return new java.awt.Point(imgX, imgY);
    }



    /**
     * Convierte una imagen de OpenCV (Mat) a BufferedImage.
     * 
     * @param mat Imagen de OpenCV.
     * @return Imagen convertida a BufferedImage.
     */
    private BufferedImage matToBufferedImage(Mat mat) {
         Mat rgbMat = new Mat();
        Imgproc.cvtColor(mat, rgbMat, Imgproc.COLOR_BGR2RGB); // Convertir de BGR a RGB

        int width = rgbMat.width();
        int height = rgbMat.height();
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_3BYTE_BGR);
        byte[] data = new byte[width * height * (int) rgbMat.elemSize()];
        rgbMat.get(0, 0, data);
        image.getRaster().setDataElements(0, 0, width, height, data);
        return image;
    }

    private Scalar getBackgroundColor(java.awt.Point point) {
        double[] bgColor = background.get(point.y, point.x);
        return new Scalar(bgColor);
    }
    
    
    /**
     * Guarda el estado actual de la imagen para permitir deshacer cambios.
     */
    private void saveState() {
        undoStack.push(image.clone());
        redoStack.clear();
    }

    
    /**
     * Deshace la última acción realizada sobre la imagen.
     */
    public void undo() {
        if (!undoStack.isEmpty()) {
            redoStack.push(image.clone());
            image = undoStack.pop();
            bufferedImage = matToBufferedImage(image);
            repaint();
        }
    }

    /**
     * Rehace la última acción deshecha.
     */
    public void redo() {
        if (!redoStack.isEmpty()) {
            undoStack.push(image.clone());
            image = redoStack.pop();
            bufferedImage = matToBufferedImage(image);
            repaint();
        }
    }
    /**
     * Cambia entre el modo de dibujo y el modo borrador.
     */
    public void toggleEraser() {
        eraserMode = !eraserMode;
        eraserButton.setText(eraserMode ? "Erasing" : "Drawing");
    }

    /**
    * Alterna el modo de dibujo de rectángulos.
    * Si estaba activado, se desactiva y viceversa.
    * Además, desactiva el modo de dibujo de círculos.
    */
   public void toggleRectangleMode() {
       drawingRectangle = !drawingRectangle;
       drawingCircle = false;
   }

    /**
     * Alterna el modo de dibujo de círculos.
     * Si estaba activado, se desactiva y viceversa.
     * Además, desactiva el modo de dibujo de rectángulos.
     */
    public void toggleCircleMode() {
        drawingCircle = !drawingCircle;
        drawingRectangle = false;
    }

    /**
     * Dibuja un rectángulo en la imagen si los puntos de inicio y fin están definidos.
     * Se utiliza la librería OpenCV para realizar el dibujo.
     */
    private void drawRectangle() {
        if (startPoint != null && endPoint != null) {
            Imgproc.rectangle(image, new org.opencv.core.Point(startPoint.x, startPoint.y),
                    new org.opencv.core.Point(endPoint.x, endPoint.y),
                    new Scalar(currentColor.getBlue(), currentColor.getGreen(), currentColor.getRed()), thickness);
            bufferedImage = matToBufferedImage(image);
            repaint();
        }
    }

    /**
     * Dibuja un círculo en la imagen si los puntos de inicio y fin están definidos.
     * Se calcula el radio a partir de la distancia entre los puntos.
     * Se utiliza la librería OpenCV para realizar el dibujo.
     */
    private void drawCircle() {
        if (startPoint != null && endPoint != null) {
            int radius = (int) Math.sqrt(Math.pow(endPoint.x - startPoint.x, 2) + Math.pow(endPoint.y - startPoint.y, 2));
            Imgproc.circle(image, new org.opencv.core.Point(startPoint.x, startPoint.y),
                    radius, new Scalar(currentColor.getBlue(), currentColor.getGreen(), currentColor.getRed()), thickness);
            bufferedImage = matToBufferedImage(image);
            repaint();
        }
    }
    
    /**
     * Establece el color actual para el dibujo.
     * @param color El nuevo color a utilizar.
     */
    public void setColor(Color color) {
        this.currentColor = color;
    }
    
    /**
     * Establece el grosor del trazo para el dibujo.
     * @param size El nuevo grosor a utilizar.
     */
    public void setThickness(int size) {
        this.thickness = size;
    }

    
    
    /**
     * Guarda la imagen editada en la ruta especificada.
     * 
     * @param path Ruta donde se guardará la imagen.
     */
    public void saveImage(String path) {
        Imgcodecs.imwrite(path, image);
        JOptionPane.showMessageDialog(this, "Imatge desada com: " + path);
        mainFrame.setImage(new File(path));
    }

    /**
     * Configura y muestra la aplicación de dibujo con OpenCV.
     * 
     * @param image Archivo de imagen a editar.
     * @param mainFrame Instancia principal de la aplicación.
     */
    public static void setUp(File image, Main mainFrame){
        OpenCVDrawing.mainFrame = mainFrame;
        String imagePath = image.getAbsolutePath();
        JFrame frame = new JFrame("OpenCV Drawing App");

        JButton undoButton = new JButton("Undo");
        JButton redoButton = new JButton("Redo");
        JButton eraserButton = new JButton("Drawing");
        JButton rectButton = new JButton("Rectangle");
        JButton circleButton = new JButton("Circle");

        OpenCVDrawing panel = new OpenCVDrawing(imagePath, eraserButton);
        
        
        
        
        JPanel controlPanel = new JPanel();
        JButton colorButton = new JButton("Seleccionar Color");
        colorButton.addActionListener(e -> {
            Color chosenColor = JColorChooser.showDialog(null, "Escull un color", panel.currentColor);
            if (chosenColor != null) {
                panel.setColor(chosenColor);
            }
        });

        JSlider brushSlider = new JSlider(1, 10, 2);
        brushSlider.addChangeListener(e -> panel.setThickness(brushSlider.getValue()));

        JButton saveButton = new JButton("Guardar");
        saveButton.addActionListener(e -> panel.saveImage("images/saved_image.jpg"));
        
        
        undoButton.addActionListener(e -> panel.undo());
        redoButton.addActionListener(e -> panel.redo());
        eraserButton.addActionListener(e -> panel.toggleEraser());
        rectButton.addActionListener(e -> panel.toggleRectangleMode());
        circleButton.addActionListener(e -> panel.toggleCircleMode());

        JPanel controls = new JPanel();
        controls.add(undoButton);
        controls.add(redoButton);
        controls.add(eraserButton);
        controls.add(rectButton);
        controls.add(circleButton);
        
        controlPanel.add(colorButton);
        controlPanel.add(new JLabel("Mida del pinzell:"));
        controlPanel.add(brushSlider);
        controlPanel.add(saveButton);

        frame.add(controlPanel, BorderLayout.SOUTH);
        frame.add(controls, BorderLayout.NORTH);
        frame.add(panel);
        frame.setSize(800, 600);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setVisible(true);
    }
}
