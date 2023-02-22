// Java Program to take a Snapshot from System Camera
// using OpenCV

// Importing openCV modules
package org.example;
import java.sql.*;
import java.awt.Font;
// importing swing and awt classes
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
// Importing date class of sql package
import java.sql.Date;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.videoio.VideoCapture;


public class Camera extends JFrame {
    private JLabel cameraScreen;
    private JButton btnCapture;
    private VideoCapture capture;
    private Mat image;
    private boolean clicked = false;
    JLabel motion = new JLabel("");

    public Camera()

    {

        try {
            Connection connection = DriverManager.getConnection("jdbc:postgresql:Motion-log", "postgres", "ingsoc");
            Statement st = connection.createStatement();
            ResultSet rs = st.executeQuery("SELECT * FROM log");
            while (rs.next())
            {
                System.out.print("Date: " + rs.getString("date") + ", ");
                System.out.print("Time: " + rs.getString("time") + ", ");
                System.out.println();
            }
            rs.close();
            st.close();
        } catch (SQLException ex) {
            ex.printStackTrace();
            System.out.println("no");
        }
        setLayout(null);

        cameraScreen = new JLabel();
        cameraScreen.setBounds(0, 0, 640, 480);
        add(cameraScreen);


        motion.setBounds(250, 480, 400, 40);
        motion.setFont(new Font("Serif", Font.PLAIN, 30));


        add(motion);


        setSize(new Dimension(640, 560));
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        setVisible(true);
        capture = new VideoCapture(0);
    }

    public void startCamera() {
        Mat frame = new Mat();
        Mat firstFrame = new Mat();
        Mat gray = new Mat();
        Mat frameDelta = new Mat();
        Mat thresh = new Mat();
        List<MatOfPoint> cnts = new ArrayList<MatOfPoint>();
        image = new Mat();
        capture.read(frame);
        ArrayList<String> log = new ArrayList<String>();
        Imgproc.cvtColor(frame, firstFrame, Imgproc.COLOR_BGR2GRAY);
        Imgproc.GaussianBlur(firstFrame, firstFrame, new Size(21, 21), 0);

        image = new Mat();
        byte[] imageData;

        ImageIcon icon;

        while (capture.read(frame)) {

            Imgproc.cvtColor(frame, gray, Imgproc.COLOR_BGR2GRAY);
            Imgproc.GaussianBlur(gray, gray, new Size(21, 21), 0);
            Core.absdiff(firstFrame, gray, frameDelta);
            Imgproc.threshold(frameDelta, thresh, 25, 255, Imgproc.THRESH_BINARY);

            Imgproc.dilate(thresh, thresh, new Mat(), new Point(-1, -1), 2);
            Imgproc.findContours(thresh, cnts, new Mat(), Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);
            capture.read(image);

            final MatOfByte buf = new MatOfByte();
            Imgcodecs.imencode(".jpg", image, buf);

            imageData = buf.toArray();

            icon = new ImageIcon(imageData);
            cameraScreen.setIcon(icon);



            motion.setText("No Movement");
            for (int i = 0; i < cnts.size(); i++) {


                if (Imgproc.contourArea(cnts.get(i)) < 500) {



                }else {

                    motion.setText("Motion Detected");
                    cnts = new ArrayList<MatOfPoint>();

                }
            }

        }
    }

    public static void main(String[] args)
    {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        EventQueue.invokeLater(new Runnable() {
            @Override public void run()
            {
                final Camera camera = new Camera();
                new Thread(new Runnable() {
                    @Override public void run()
                    {
                        camera.startCamera();
                    }
                }).start();

            }
        });
    }
}