package simulator;

import algorithms.ZviAndGalAlgorithm;
import cpu.CPU;
import drone.Drone;
import map.Tools;
import models.Point;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

public class GraphPath  {
    public JFrame GV;
    public Drone drone;
    public ArrayList<Point> PointsGraph;
    public  static void start( ArrayList<Point> other) {

    }
    public GraphPath(ArrayList<Point> other,Drone t)  {
        PointsGraph=other;
        drone=t;
        initialize();
    }

    private void initialize() {
        GV = new JFrame();
        GV.setSize(900, 900);
        GV.setTitle("Graph PATH");
        GV.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        GV.getContentPane().setLayout(null);


        PainterGraph painter = new PainterGraph(this);


        painter.setBounds(0, 0, 2000, 2000);



        GV.getContentPane().add(painter);

        CPU painterCPU = new CPU(200, "painter"); // 60 FPS painter
        painterCPU.addFunction(GV::repaint);


        painterCPU.play();







    }
    public void paint(Graphics g) {
        paintPoints(g);
        DrawSmartLines(g);

    }
    public void DrawSmartLines(Graphics g) {
        //todo: fix that

        if(PointsGraph.size() == 0) return;
        if(PointsGraph.size() == 1) return;
        SimulationWindow.draw_smart_lines=false;
        for (int i = 0; i < PointsGraph.size(); i+=1) {
            Point firstP = PointsGraph.get(i);
            Point nextP = null;
            if(i + 1 < PointsGraph.size())
                nextP = PointsGraph.get(i+1);
            if(nextP != null) {
                g.setColor(Color.BLACK  );
                g.drawLine((int)firstP.x+ (int) drone.startPoint.x,(int)firstP.y+(int) drone.startPoint.y,(int)nextP.x+ (int) drone.startPoint.x,(int)nextP.y+(int) drone.startPoint.y);
                g.drawString(Tools.getDistanceBetweenPoints(firstP, nextP)+"px", (int) ((firstP.x+nextP.x)/2), (int) ((firstP.y+nextP.y)/2));
            }


        }
    }

    public void paintPoints(Graphics g) {
        for (int i = 0; i < PointsGraph.size(); i++) {
            Point p = PointsGraph.get(i);
            g.setColor(p.col);
            g.drawString(p.type,(int) p.x + (int) drone.startPoint.x - 10, (int) p.y + (int) drone.startPoint.y - 10);
            g.drawOval((int) p.x + (int) drone.startPoint.x - 10, (int) p.y + (int) drone.startPoint.y - 10, 20, 20);
        }
    }









}
