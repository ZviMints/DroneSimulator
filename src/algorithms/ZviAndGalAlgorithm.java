package algorithms;

import configurations.WorldParams;
import cpu.CPU;
import drone.Drone;
import map.Map;
import map.Tools;
import models.Point;
import org.apache.commons.lang3.time.DateUtils;
import simulator.Func;
import simulator.Lidar;
import simulator.SimulationWindow;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.locks.Lock;

import static javax.swing.JOptionPane.showMessageDialog;

public class ZviAndGalAlgorithm implements BaseAlgo {

    int map_size = 3000;

    int lock_time = 5;
    Date lock = DateUtils.addSeconds(new Date(), lock_time);

    public mapState statesMap[][];
    public Drone drone;
    public Point droneStartingPoint;

    ArrayList<Point> points;
    ArrayList<Point> smart_points;
    public Date startDate;

    int isRotating;
    ArrayList<Double> degrees_left;
    ArrayList<Func> degrees_left_func;

    CPU cpu;
    Map map;
    JFrame frame;
    boolean justInitialized = true;
    public boolean risky = false;
    public int max_risky_distance = 150;

    public boolean try_to_escape = false;
    public double risky_dis = 0;
    public int max_angle_risky = 10;

    boolean is_lidars_max = false;
    double max_distance_between_points = 300;
    Point currentPoint;
    double lastGyroRotation = 0;

    // =========================== Constructor =========================== //
    public ZviAndGalAlgorithm(Map realMap, JFrame frame) {
        this.frame = frame;
        this.startDate = new Date();
        System.out.println("Initialize " + getAlgoName() + " Algorithm");

        degrees_left = new ArrayList<>();
        degrees_left_func = new ArrayList<>();
        points = new ArrayList<>();
        smart_points = new ArrayList<>();

        // Adding Drone And Lidars
        drone = new Drone(realMap);
        drone.addLidar(0);
        drone.addLidar(90);
        drone.addLidar(-90);

        this.map = realMap;

        // Init map
        statesMap = new mapState[map_size][map_size];
        for (int i = 0; i < map_size; i++) {
            for (int j = 0; j < map_size; j++) {
                statesMap[i][j] = mapState.unexplored;
            }
        }

        droneStartingPoint = new Point(map_size / 2, map_size / 2);

        isRotating = 0;
        cpu = new CPU(200, "Auto_AI");
        cpu.addFunction(this::update);
    }

    // ===========================  Functions =============================== //
    enum SpeedStates {speedUp, speedDown, remain}

    public SpeedStates speed_state = SpeedStates.remain;


    public void paintBlindMap(Graphics g) {
        Color c = g.getColor();
        int i = (int) droneStartingPoint.y - (int) drone.startPoint.x;
        int startY = i;
        for (; i < map_size; i++) {
            int j = (int) droneStartingPoint.x - (int) drone.startPoint.y;
            int startX = j;
            for (; j < map_size; j++) {
                if (statesMap[i][j] != mapState.unexplored) {
                    if (statesMap[i][j] == mapState.blocked) {
                        g.setColor(Color.RED);
                    } else if (statesMap[i][j] == mapState.explored) {
                        g.setColor(Color.YELLOW);
                    } else if (statesMap[i][j] == mapState.visited) {
                        g.setColor(Color.BLUE);
                    }
                    g.drawLine(i - startY, j - startX, i - startY, j - startX);
                }
            }
        }
        g.setColor(c);
    }


    public void paintPoints(Graphics g) {
        for (int i = 0; i < points.size(); i++) {
            Point p = points.get(i);
            g.drawOval((int) p.x + (int) drone.startPoint.x - 10, (int) p.y + (int) drone.startPoint.y - 10, 20, 20);
        }
    }
    public void DrawSmartLines(Graphics g) {
        if(smart_points.size() == 0) return;
        if(smart_points.size() == 1) return;
        for (int i = 0; i < smart_points.size(); i+=1) {
            Point firstP = smart_points.get(i);
            Point nextP = null;
            if(i + 1 < smart_points.size())
                nextP = smart_points.get(i+1);
            if(nextP != null) {
                g.setColor(Color.cyan);
                g.drawLine((int)firstP.x,(int)firstP.y,(int)nextP.x,(int)nextP.y);
            }
        }
    }
    public void paintSmartPoints(Graphics g) {
        for (int i = 0; i < smart_points.size(); i++) {
            Point p = smart_points.get(i);
            g.setColor(Color.green);
            g.drawOval((int) p.x + (int) drone.startPoint.x - 10, (int) p.y + (int) drone.startPoint.y - 10, 20, 20);
            if(i == smart_points.size() - 1)
            g.drawString("Last",(int) p.x + (int) drone.startPoint.x - 10, (int) p.y + (int) drone.startPoint.y - 10);

        }
    }

    public void update(int deltaTime) {

        if(getBattery() < 0) {
            //todo: seperate gui and logic, can be done later
            frame.setVisible(false); //you can't see me!
            frame.dispose(); //Destroy the JFrame object
            cpu.stopAllCPUS();
            showMessageDialog(null, "No Battery! Dammit");
            return;
        }

        if(getBattery() == 50)  SimulationWindow.return_home = true;

        updateVisited();
        updateMapByLidars();
        Think();

        if (isRotating != 0) updateRotating(deltaTime);
        if (speed_state == SpeedStates.speedUp) {
            drone.speedUp(deltaTime * 5);
            speed_state = SpeedStates.remain;
        } else if (speed_state == SpeedStates.speedDown) {
            drone.slowDown(deltaTime * 5);
            speed_state = SpeedStates.remain;
        }
    }

    public void spinBy(double degrees, boolean isFirst, Func func) {
        lastGyroRotation = drone.getGyroRotation();
        if (isFirst) {
            degrees_left.add(0, degrees);
            degrees_left_func.add(0, func);


        } else {
            degrees_left.add(degrees);
            degrees_left_func.add(func);
        }
        isRotating = 1;
    }

    public void spinBy(double degrees) {
        lastGyroRotation = drone.getGyroRotation();
        degrees_left.add(degrees);
        degrees_left_func.add(null);
        isRotating = 1;
    }

    public void updateRotating(int deltaTime) {
        if (degrees_left.size() != 0) {

            double degrees_left_to_rotate = degrees_left.get(0);
            boolean isLeft = true;
            if (degrees_left_to_rotate > 0) {
                isLeft = false;
            }

            double curr = drone.getGyroRotation();
            double just_rotated = 0;

            if (isLeft) {
                just_rotated = curr - lastGyroRotation;
                if (just_rotated > 0) {
                    just_rotated = -(360 - just_rotated);
                }
            } else {
                just_rotated = curr - lastGyroRotation;
                if (just_rotated < 0) {
                    just_rotated = 360 + just_rotated;
                }
            }

            lastGyroRotation = curr;
            degrees_left_to_rotate -= just_rotated;
            degrees_left.remove(0);
            degrees_left.add(0, degrees_left_to_rotate);

            if ((isLeft && degrees_left_to_rotate >= 0) || (!isLeft && degrees_left_to_rotate <= 0)) {
                degrees_left.remove(0);

                Func func = degrees_left_func.get(0);
                if (func != null) {
                    func.method();
                }
                degrees_left_func.remove(0);


                if (degrees_left.size() == 0) {
                    isRotating = 0;
                }
                return;
            }

            int direction = (int) (degrees_left_to_rotate / Math.abs(degrees_left_to_rotate));
            drone.rotateLeft(deltaTime * direction);

        }
    }


    public void updateMapByLidars() {
        Point dronePoint = drone.getOpticalSensorLocation();
        Point fromPoint = new Point(dronePoint.x + droneStartingPoint.x, dronePoint.y + droneStartingPoint.y);

        for (int i = 0; i < drone.lidars.size(); i++) {
            Lidar lidar = drone.lidars.get(i);
            double rotation = drone.getGyroRotation() + lidar.degrees;
            for (int distanceInCM = 0; distanceInCM < lidar.current_distance; distanceInCM++) {
                Point p = Tools.getPointByDistance(fromPoint, rotation, distanceInCM);
                setPixel(p.x, p.y, mapState.explored);
            }

            if (lidar.current_distance > 0 && lidar.current_distance < WorldParams.lidarLimit - WorldParams.lidarNoise) {
                Point p = Tools.getPointByDistance(fromPoint, rotation, lidar.current_distance);
                setPixel(p.x, p.y, mapState.blocked);
            }
        }
    }

    public void updateVisited() {
        Point dronePoint = drone.getOpticalSensorLocation();
        Point fromPoint = new Point(dronePoint.x + droneStartingPoint.x, dronePoint.y + droneStartingPoint.y);
        setPixel(fromPoint.x, fromPoint.y, mapState.visited);
    }

    public void setPixel(double x, double y, mapState state) {
        int xi = (int) x;
        int yi = (int) y;

        if (state == mapState.visited) {
            statesMap[xi][yi] = state;
            return;
        }

        if (statesMap[xi][yi] == mapState.unexplored) {
            statesMap[xi][yi] = state;
        }
    }

    public void remainSpeed() {
        speed_state = SpeedStates.remain;
    }


    // =========================== Override Functions =============================== //
    @Override
    public void paint(Graphics g) {
        if (SimulationWindow.toogleRealMap) drone.map.paint(g);
        if(SimulationWindow.draw_smart_lines) DrawSmartLines(g);
        paintBlindMap(g);
        paintPoints(g);
        paintSmartPoints(g);
        Blink(g);
        drone.paint(g);
    }

    @Override
    public String getAlgoName() {
        return "ZviAndGal Algorithm";
    }

    @Override
    public void speedUp() {
        speed_state = SpeedStates.speedUp;
    }

    @Override
    public void speedDown() {
        speed_state = SpeedStates.speedDown;
    }

    @Override
    public Drone getDrone() {
        return drone;
    }

    @Override
    public void play() {
        drone.play();
        cpu.play();
    }

    @Override
    public boolean getRiskyState() {
        return this.risky;
    }

    @Override
    public double getRiskyDist() {
        return this.risky_dis;
    }

    @Override
    public double getSpeed() {
        return drone.speed;
    }

    @Override
    public double getDistForward() {
        return drone.lidars.get(0).current_distance;
    }

    @Override
    public double getDistRight() {
        return drone.lidars.get(1).current_distance;
    }

    @Override
    public double getDistLeft() {
        return drone.lidars.get(2).current_distance;
    }


    // =========================== Main Method =============================== //
    public boolean calculateRisky(double current_distance, int max_risky_distance) {
        if (current_distance <= max_risky_distance) {
            return true;
        }
        return false;
    }

    public void Blink(Graphics g) {

        Lidar forward = drone.lidars.get(0);
        double dist_forward = forward.current_distance;

        Lidar right = drone.lidars.get(1);
        double a = right.current_distance;

        Lidar left = drone.lidars.get(2);
        double b = left.current_distance;

        g.setColor(((a < 8 || b < 8 || dist_forward < 10) && a != 0.0 && b != 0.0 && dist_forward != 0.0) ? Color.RED : ((SimulationWindow.return_home) ? Color.GREEN : Color.LIGHT_GRAY));
        for (int i = 0; i < map.map.length; i++) {
            for (int j = 0; j < map.map[0].length; j++) {
                if (map.isNotMap(i, j)) {
                    g.drawLine(i, j, i, j);
                }
            }
        }
    }

    public void Think() {
        if (SimulationWindow.toogleAI) {

            if (justInitialized) {
                Point dronePoint = drone.getOpticalSensorLocation();
                currentPoint = new Point(dronePoint);
                points.add(dronePoint);
                graph.addVertex(dronePoint);
                justInitialized = false;
            }

            Point dronePoint = drone.getOpticalSensorLocation();

            // Exercise 4
            if(relevantPoint() && new Date().after(lock)) {
                smart_points.add(dronePoint);
                graph.addVertex(dronePoint);
                lock = DateUtils.addSeconds(lock, lock_time); // add seconds


            }
            if (!(SimulationWindow.return_home)) {
                if (Tools.getDistanceBetweenPoints(getLastPoint(), dronePoint) >= max_distance_between_points ){
                    points.add(dronePoint);
                    graph.addVertex(dronePoint);
                }
            } else { // SimulationWindow.return_home
                if (points.isEmpty()) {
                    SimulationWindow.return_home = false;
                    cpu.stopAllCPUS();
                }
            }

            Lidar forward = drone.lidars.get(0);
            double dist_forward = forward.current_distance;

            Lidar right = drone.lidars.get(1);
            double dist_right = right.current_distance;

            Lidar left = drone.lidars.get(2);
            double dist_left = left.current_distance;

            for (Lidar lidar : drone.lidars) {
                int normalized = (lidar.degrees == 0) ? 1 : 3;
                if (calculateRisky(lidar.current_distance, (max_risky_distance / normalized))) {
                    risky = true;
                    if (lidar.degrees == 0) risky_dis = lidar.current_distance;
                }
            }

            // For Safety
            if (getRiskyDist() != 0.0 &&
                    ((dist_right < ((drone.speed * 80) / WorldParams.max_speed)) && (dist_left < ((drone.speed * 80) / WorldParams.max_speed))) ||
                    (dist_forward < ((drone.speed * 150) / WorldParams.max_speed)))
                if (drone.speed != WorldParams.min_speed) speedDown();

            // Remove Last Point
            if (SimulationWindow.return_home) {
                if (!smart_points.isEmpty())
                    if (Tools.getDistanceBetweenPoints(dronePoint, smart_points.get(smart_points.size() - 1)) <= 35)
                        removeLastPoint();
            }

            // Checking State
            if (!risky) {
                // Return Home
                if (SimulationWindow.return_home) {
                    if (!smart_points.isEmpty()) {
                        double rotation = Tools.getRotationBetweenPoints(dronePoint, smart_points.get(smart_points.size() - 1));
                        spinBy(rotation);
                    }
                } else if (dist_forward > dist_right && dist_forward > dist_left)
                    if (drone.speed != WorldParams.max_speed) speedUp();
            } else {
                if (!try_to_escape) {
                    try_to_escape = true;

                    int spin_by = max_angle_risky;

                    if (dist_right > 270 && dist_left > 270) {
                        is_lidars_max = true;
                        Point l1 = Tools.getPointByDistance(dronePoint, right.degrees + drone.getGyroRotation(), right.current_distance);
                        Point l2 = Tools.getPointByDistance(dronePoint, left.degrees + drone.getGyroRotation(), left.current_distance);
                        Point last_point = getAvgLastPoint();
                        double dis_to_lidar1 = Tools.getDistanceBetweenPoints(last_point, l1);
                        double dis_to_lidar2 = Tools.getDistanceBetweenPoints(last_point, l2);

                        if (SimulationWindow.return_home) {
                            if (Tools.getDistanceBetweenPoints(dronePoint, smart_points.get(smart_points.size() - 1)) <= 35) {
                                removeLastPoint();
                            }
                        } else {
                            if (Tools.getDistanceBetweenPoints(getLastPoint(), dronePoint) >= max_distance_between_points) {
                                points.add(dronePoint);
                                graph.addVertex(dronePoint);
                            }
                        }

                        spin_by = 90;

                        if (SimulationWindow.return_home) {
                            spin_by *= -1;
                        }

                        if (dis_to_lidar1 < dis_to_lidar2) {
                            spin_by *= (-1);
                        }
                    } else {
                        if (dist_forward > 150) speedUp();
                        if (drone.speed == WorldParams.min_speed) {
                            System.out.println("Hard Situation. Im Thinking...");
                            // Looking for dist_right new place to go
                            if (dist_forward > dist_right && dist_forward > dist_left) spin_by = 0;
                            else if (dist_right < dist_forward && dist_right < dist_left) spin_by *= -1;
                            else spin_by *= 1;
                        } else {
                            if (dist_forward >= dist_right && dist_forward >= dist_left) spin_by = 0;
                            else if (dist_right < dist_left) {
                                spin_by *= (-1);
                            }
                        }
                        if (SimulationWindow.return_home) {
                            if(!smart_points.isEmpty()) {
                                double rotation = Tools.getRotationBetweenPoints(dronePoint, smart_points.get(smart_points.size() - 1));
                                if (!(rotation >= -90 && rotation <= 90))
                                    spin_by += -1;
                            }
                        }
                    }

                    spinBy(spin_by, true, () -> {
                        try_to_escape = false;
                        risky = false;
                    });
                }
            }
        }

    }
    public Boolean relevantPoint() {
        Lidar forward = drone.lidars.get(0);
        double dist_forward = forward.current_distance;

        Lidar right = drone.lidars.get(1);
        double dist_right = right.current_distance;

        Lidar left = drone.lidars.get(2);
        double dist_left = left.current_distance;
        if(dist_right > 300 && dist_left < 100 ) return true; // Right turn
        if(dist_left > 300 && dist_right < 100 ) return true; // Left turn
        return false;
    }
    public Point getAvgLastPoint() {
        if (points.size() < 2) return currentPoint;
        else {
            Point p1 = points.get(points.size() - 1);
            Point p2 = points.get(points.size() - 2);
            return new Point((p1.x + p2.x) / 2, (p1.y + p2.y) / 2);
        }
    }

    public Point removeLastPoint() {
        if (smart_points.isEmpty()) return currentPoint;
        else return smart_points.remove(smart_points.size() - 1);
    }

    public Point getLastPoint() {
        if (points.size() == 0) return currentPoint;
        Point p = points.get(points.size() - 1);
        return p;
    }

    // Exercise 3
    @Override
    public double getBattery() {
        Date diff = new Date(new Date().getTime() - this.startDate.getTime());

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(diff);
        int seconds = calendar.get(Calendar.SECOND);
        int minutes = calendar.get(Calendar.MINUTE);
        return (minutes < 1) ? (60 - seconds) * 100/60 : -1;
    }
}

