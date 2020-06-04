package simulator;

import algorithms.BaseAlgo;
import algorithms.ZviAndGalAlgorithm;
import configurations.Config;
import cpu.CPU;
import map.Map;
import models.Point;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;

import static javax.swing.JOptionPane.showMessageDialog;

public class SimulationWindow {

    private JFrame frame;

    public static void main(String[] args) {
        EventQueue.invokeLater(() -> {
            try {
                SimulationWindow window = new SimulationWindow();
                window.frame.setVisible(true);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    public SimulationWindow() {
        initialize();
    }

    public static JLabel info_label;
    public static boolean return_home = false;
    boolean toogleStop = true;


    private void initialize() {
        frame = new JFrame();
        frame.setSize(900, 700);
        frame.setTitle("Drone.Drone Simulator");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.getContentPane().setLayout(null);

        /*
         * RETURN TO HOME
         */

        JButton returnBtn = new JButton("Return Home");
        returnBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                return_home = !return_home;
            }
        });
        returnBtn.setBounds(10, 610, 120, 50);
        frame.getContentPane().add(returnBtn);


        /*
         * Info label
         */

        info_label = new JLabel();
        info_label.setBounds(10, 475, 200, 150);
        frame.getContentPane().add(info_label);

        /*
         * Info label
         */

        info_label2 = new JLabel();
        info_label2.setBounds(10, 370, 200, 150);
        frame.getContentPane().add(info_label2);

        main();
    }

    public JLabel info_label2;
    public static boolean toogleRealMap = true;
    public static boolean toogleAI = true;

    public static BaseAlgo algorithm;

    public void main() {
        int map_num = 4;
        Point[] startPoints = {new Point(100, 50), new Point(50, 60), new Point(73, 68), new Point(84, 73),
                new Point(92, 100)};

        Map map = new Map(Config.root + "p1" + map_num + ".png", startPoints[map_num - 1]);

        algorithm = new ZviAndGalAlgorithm(map, frame);

        Painter painter = new Painter(algorithm);
        painter.setBounds(0, 0, 2000, 2000);
        frame.getContentPane().add(painter);

        CPU painterCPU = new CPU(200, "painter"); // 60 FPS painter
        painterCPU.addFunction(frame::repaint);
        painterCPU.play();

        algorithm.play();

        CPU updatesCPU = new CPU(60, "updates");
        updatesCPU.addFunction(algorithm.getDrone()::update);
        updatesCPU.play();

        CPU infoCPU = new CPU(6, "update_info");
        infoCPU.addFunction(this::updateInfo);
        infoCPU.play();
    }

    public void updateInfo(int deltaTime) {
        info_label.setText(algorithm.getDrone().getInfoHTML());
        info_label2.setText(
                "<html>" +
                        "<b>" + "Zvi Mints And Gal Hadida" + "</b><br>" +
                        "<p style=\"color:red\">" + "Battery: " + algorithm.getBattery() + " % </p>" +
                        "Speed: " + algorithm.getSpeed() + "<br>"
                        + "distRight: " + algorithm.getDistRight() + "<br>"
                        + "distLeft: " + algorithm.getDistLeft() + "<br>"
                        + "distForward: " + algorithm.getDistForward() + "<br>"
                        + "isRisky: " + algorithm.getRiskyState() + "<br>"
                        + "riskyDistance: " + algorithm.getRiskyDist()
                        + "</html>");

    }
}
