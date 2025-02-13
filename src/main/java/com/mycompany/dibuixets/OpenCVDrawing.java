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

    public OpenCVDrawing(File imagePath){
        setUp(imagePath);
    }
    
    public OpenCVDrawing(String imagePath, JButton eraserButton) {
        System.load(Preferences.getOpenCVPath());
        this.eraserButton = eraserButton;
        image = Imgcodecs.imread(imagePath);
        background = image.clone();
        bufferedImage = matToBufferedImage(image);

        addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                startPoint = e.getPoint();
                saveState();
            }

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
            public void mouseDragged(MouseEvent e) {
                endPoint = e.getPoint();
                if (!drawingRectangle && !drawingCircle) {
                    Scalar color = eraserMode ? getBackgroundColor(startPoint) : new Scalar(currentColor.getBlue(), currentColor.getGreen(), currentColor.getRed());
                    Imgproc.line(image, new org.opencv.core.Point(startPoint.x, startPoint.y),
                            new org.opencv.core.Point(e.getX(), e.getY()), color, thickness);
                    startPoint = e.getPoint();
                }
                bufferedImage = matToBufferedImage(image);
                repaint();
            }
        });
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.drawImage(bufferedImage, 0, 0, this);
    }

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

    private void saveState() {
        undoStack.push(image.clone());
        redoStack.clear();
    }

    public void undo() {
        if (!undoStack.isEmpty()) {
            redoStack.push(image.clone());
            image = undoStack.pop();
            bufferedImage = matToBufferedImage(image);
            repaint();
        }
    }

    public void redo() {
        if (!redoStack.isEmpty()) {
            undoStack.push(image.clone());
            image = redoStack.pop();
            bufferedImage = matToBufferedImage(image);
            repaint();
        }
    }

    public void toggleEraser() {
        eraserMode = !eraserMode;
        eraserButton.setText(eraserMode ? "Erasing" : "Drawing");
    }

    public void toggleRectangleMode() {
        drawingRectangle = !drawingRectangle;
        drawingCircle = false;
    }

    public void toggleCircleMode() {
        drawingCircle = !drawingCircle;
        drawingRectangle = false;
    }

    private void drawRectangle() {
        if (startPoint != null && endPoint != null) {
            Imgproc.rectangle(image, new org.opencv.core.Point(startPoint.x, startPoint.y),
                    new org.opencv.core.Point(endPoint.x, endPoint.y),
                    new Scalar(currentColor.getBlue(), currentColor.getGreen(), currentColor.getRed()), thickness);
            bufferedImage = matToBufferedImage(image);
            repaint();
        }
    }

    private void drawCircle() {
        if (startPoint != null && endPoint != null) {
            int radius = (int) Math.sqrt(Math.pow(endPoint.x - startPoint.x, 2) + Math.pow(endPoint.y - startPoint.y, 2));
            Imgproc.circle(image, new org.opencv.core.Point(startPoint.x, startPoint.y),
                    radius, new Scalar(currentColor.getBlue(), currentColor.getGreen(), currentColor.getRed()), thickness);
            bufferedImage = matToBufferedImage(image);
            repaint();
        }
    }
    
    public void setColor(Color color) {
        this.currentColor = color;
    }
    
    public void setThickness(int size) {
        this.thickness = size;
    }
    
    public void saveImage(String path) {
        Imgcodecs.imwrite(path, image);
        JOptionPane.showMessageDialog(this, "Imatge desada com: " + path);
    }

    
    private void setUp(File image){
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
