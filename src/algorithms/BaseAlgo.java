package algorithms;

import drone.Drone;
import simulator.Func;
import simulator.Graph;

import java.awt.*;

public interface BaseAlgo {
    enum mapState {blocked, explored, unexplored, visited,lines}
    String getAlgoName();
    double getDistRight();
    double getDistLeft();
    double getDistForward();
    boolean getRiskyState();
    double getRiskyDist();
    double getSpeed();
    double getBattery();
    void play();
    void speedUp();
    void speedDown();
    void spinBy(double degrees, boolean isFirst, Func func);
    void spinBy(double degrees);
    void paint(Graphics g);
    Drone getDrone();
     Graph graph = new Graph();


}
