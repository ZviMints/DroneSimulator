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
        frame.setSize(1800, 700);
        frame.setTitle("Drone.Drone Simulator");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.getContentPane().setLayout(null);

        /*
         * Stop \ Resume
         */

        JButton stopBtn = new JButton("Start/Pause");
        stopBtn.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                if (toogleStop) {
                    CPU.stopAllCPUS();
                } else {
                    CPU.resumeAllCPUS();
                }
                toogleStop = !toogleStop;
            }
        });
        stopBtn.setBounds(1300, 0, 170, 50);
        frame.getContentPane().add(stopBtn);
        /*
         * Speeds
         */

        JButton speedBtn1 = new JButton("speedUp");
        speedBtn1.addActionListener(e -> algorithm.speedUp());
        speedBtn1.setBounds(1300, 100, 100, 50);
        frame.getContentPane().add(speedBtn1);

        JButton speedBtn2 = new JButton("speedDown");
        speedBtn2.addActionListener(e -> algorithm.speedDown());
        speedBtn2.setBounds(1400, 100, 100, 50);
        frame.getContentPane().add(speedBtn2);

        /*
         * Spins
         */

        JButton spinBtn1 = new JButton("spin180");
        spinBtn1.addActionListener(e -> algorithm.spinBy(180));
        spinBtn1.setBounds(1300, 200, 100, 50);
        frame.getContentPane().add(spinBtn1);

        JButton spinBtn2 = new JButton("spin90");
        spinBtn2.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                algorithm.spinBy(90);
            }
        });
        spinBtn2.setBounds(1400, 200, 100, 50);
        frame.getContentPane().add(spinBtn2);

        JButton spinBtn3 = new JButton("spin60");
        spinBtn3.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                algorithm.spinBy(60);
            }
        });
        spinBtn3.setBounds(1500, 200, 100, 50);
        frame.getContentPane().add(spinBtn3);

        JButton spinBtn4 = new JButton("spin45");
        spinBtn4.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                algorithm.spinBy(60);
            }
        });
        spinBtn4.setBounds(1300, 300, 100, 50);
        frame.getContentPane().add(spinBtn4);

        JButton spinBtn5 = new JButton("spin30");
        spinBtn5.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                algorithm.spinBy(30);
            }
        });
        spinBtn5.setBounds(1400, 300, 100, 50);
        frame.getContentPane().add(spinBtn5);

        JButton spinBtn6 = new JButton("spin-30");
        spinBtn6.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                algorithm.spinBy(-30);
            }
        });
        spinBtn6.setBounds(1500, 300, 100, 50);
        frame.getContentPane().add(spinBtn6);

        JButton spinBtn7 = new JButton("spin-45");
        spinBtn7.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                algorithm.spinBy(-45);
            }
        });
        spinBtn7.setBounds(1600, 300, 100, 50);
        frame.getContentPane().add(spinBtn7);

        JButton spinBtn8 = new JButton("spin-60");
        spinBtn8.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                algorithm.spinBy(-60);
            }
        });
        spinBtn8.setBounds(1700, 300, 100, 50);
        frame.getContentPane().add(spinBtn8);

        /*
         * Toogle real map
         */

        JButton toogleMapBtn = new JButton("toogle Map");
        toogleMapBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                toogleRealMap = !toogleRealMap;
            }
        });
        toogleMapBtn.setBounds(1300, 400, 120, 50);
        frame.getContentPane().add(toogleMapBtn);

        /*
         * Toogle AI
         */

        JButton toogleAIBtn = new JButton("toogle AI");
        toogleAIBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                toogleAI = !toogleAI;
            }
        });
        toogleAIBtn.setBounds(1400, 400, 120, 50);
        frame.getContentPane().add(toogleAIBtn);

        /*
         * RETURN TO HOME
         */

        JButton returnBtn = new JButton("Return Home");
        returnBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                return_home = !return_home;
            }
        });
        returnBtn.setBounds(1500, 400, 120, 50);
        frame.getContentPane().add(returnBtn);

        JButton Graph = new JButton("Open simulator.Graph");
        Graph.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                algorithm.graph.drawGraph();
            }
        });
        Graph.setBounds(1600, 400, 120, 50);
        frame.getContentPane().add(Graph);

        /*
         * Info label
         */

        info_label = new JLabel();
        info_label.setBounds(1300, 500, 300, 200);
        frame.getContentPane().add(info_label);

        /*
         * Info label
         */

        info_label2 = new JLabel();
        info_label2.setBounds(1400, 450, 300, 200);
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

        algorithm = new ZviAndGalAlgorithm(map);

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
                        "Speed: " + algorithm.getSpeed() + "<br>"
                        + "isRisky: " + algorithm.getRiskyState() + "<br>"
                        + "riskyDistance: " + algorithm.getRiskyDist()
                        + "</html>");

    }
}
