package algorithms;

import drone.Drone;
import simulator.Func;
import simulator.Graph;

import java.awt.*;

public interface BaseAlgo {
    enum mapState {blocked, explored, unexplored, visited}
    String getAlgoName();
    boolean getRiskyState();
    double getRiskyDist();
    double getSpeed();
    Graph graph = new Graph();
    void play();
    void speedUp();
    void speedDown();
    void spinBy(double degrees, boolean isFirst, Func func);
    void spinBy(double degrees);
    void paint(Graphics g);
    Drone getDrone();
}
