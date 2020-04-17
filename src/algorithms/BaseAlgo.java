package algorithms;

import drone.Drone;
import simulator.Func;
import simulator.Graph;

import java.awt.*;

public interface BaseAlgo {
    boolean is_risky = false;
    double risky_dis = 0;
    Graph graph = new Graph();
    void play();
    void speedUp();
    void speedDown();
    void spinBy(double degrees, boolean isFirst, Func func);
    void spinBy(double degrees);
    void paint(Graphics g);
    Drone getDrone();
}
