package algorithms;

import java.awt.Color;
import java.awt.Graphics;
import java.util.ArrayList;

import cpu.CPU;
import drone.Drone;
import simulator.Func;
import simulator.Lidar;
import map.Map;
import map.Tools;
import configurations.WorldParams;
import models.Point;
import simulator.SimulationWindow;

public class ZviAndGalAlgorithm implements BaseAlgo {

    int map_size = 3000;

    public mapState map[][];
    public Drone drone;
    public Point droneStartingPoint;

    ArrayList<Point> points;

    int isRotating;
    ArrayList<Double> degrees_left;
    ArrayList<Func> degrees_left_func;

    CPU cpu;

    boolean justInitialized = true;
    public boolean risky = false;
    public int max_risky_distance = 150;
    public boolean try_to_escape = false;
    public double risky_dis = 0;
    public int max_angle_risky = 10;

    boolean is_lidars_max = false;
    double max_distance_between_points = 100;
    Point currentPoint;
    double lastGyroRotation = 0;

    // =========================== Constructor =========================== //
    public ZviAndGalAlgorithm(Map realMap) {
        System.out.println("Initialize " + getAlgoName() + " Algorithm");

        degrees_left = new ArrayList<>();
        degrees_left_func = new ArrayList<>();
        points = new ArrayList<>();

        // Adding Drone And Lidars
        drone = new Drone(realMap);
        drone.addLidar(0);
        drone.addLidar(90);
        drone.addLidar(-90);

        // Init map
        map = new mapState[map_size][map_size];
        for (int i = 0; i < map_size; i++) {
            for (int j = 0; j < map_size; j++) {
                map[i][j] = mapState.unexplored;
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
                if (map[i][j] != mapState.unexplored) {
                    if (map[i][j] == mapState.blocked) {
                        g.setColor(Color.RED);
                    } else if (map[i][j] == mapState.explored) {
                        g.setColor(Color.YELLOW);
                    } else if (map[i][j] == mapState.visited) {
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

    public void update(int deltaTime) {
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
            map[xi][yi] = state;
            return;
        }

        if (map[xi][yi] == mapState.unexplored) {
            map[xi][yi] = state;
        }
    }

    // =========================== Override Functions =============================== //
    @Override
    public void paint(Graphics g) {
        if (SimulationWindow.toogleRealMap) drone.map.paint(g);
        paintBlindMap(g);
        paintPoints(g);
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
    public double getSpeed(){
        return drone.speed;
    }


    // =========================== Main Method =============================== //
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

            if (SimulationWindow.return_home) {
                System.out.println("[Drone] I Dont want return to home.");
                SimulationWindow.return_home = false;
            } else {
                if (Tools.getDistanceBetweenPoints(getLastPoint(), dronePoint) >= max_distance_between_points) {
                    points.add(dronePoint);
                    graph.addVertex(dronePoint);
                }
            }
            if (!risky) {
                for (Lidar lidar: drone.lidars) {
                    int normalized = (lidar.degrees == 0) ? 1 : 3;
                    if (lidar.current_distance <= max_risky_distance / normalized) {
                        risky = true;
                        if (lidar.degrees == 0) risky_dis = lidar.current_distance;
                    }
                }
            } else { // Im Risky
                if (!try_to_escape) {

                    try_to_escape = true;

                    Lidar lidar1 = drone.lidars.get(1);
                    double a = lidar1.current_distance;

                    Lidar lidar2 = drone.lidars.get(2);
                    double b = lidar2.current_distance;

                    int spin_by = max_angle_risky;

                    if (a > 270 && b > 270) {
                        is_lidars_max = true;
                        Point l1 = Tools.getPointByDistance(dronePoint, lidar1.degrees + drone.getGyroRotation(), lidar1.current_distance);
                        Point l2 = Tools.getPointByDistance(dronePoint, lidar2.degrees + drone.getGyroRotation(), lidar2.current_distance);
                        Point last_point = getAvgLastPoint();
                        double dis_to_lidar1 = Tools.getDistanceBetweenPoints(last_point, l1);
                        double dis_to_lidar2 = Tools.getDistanceBetweenPoints(last_point, l2);

                        if (SimulationWindow.return_home) {
                            if (Tools.getDistanceBetweenPoints(getLastPoint(), dronePoint) < max_distance_between_points) {
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


                        if (a < b) {
                            spin_by *= (-1);
                        }
                    }


                    spinBy(spin_by, true, new Func() {
                        @Override
                        public void method() {
                            try_to_escape = false;
                            risky = false;
                        }
                    });
                }
            }
        }
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
        if (points.isEmpty()) return currentPoint;
        else return points.remove(points.size() - 1);
    }

    public Point getLastPoint() {
        if (points.size() == 0) return currentPoint;
        Point p = points.get(points.size() - 1);
        return p;
    }
}

