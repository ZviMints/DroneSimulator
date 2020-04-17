package drone;

import configurations.Config;
import configurations.WorldParams;
import cpu.CPU;
import map.Map;
import map.Tools;
import models.Point;
import simulator.Lidar;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class Drone {
    private double gyroRotation;
    private Point sensorOpticalFlow;

    private Point pointFromStart;
    public Point startPoint; // Where the drone first time located
    public List<Lidar> lidars;
    private String drone_img_path = Config.root + "drone_3_pixels.png";
    public Map map;
    private double rotation;
    private double speed;
    private CPU cpu;

    public Drone(Map map) {
        this.map = map;
        this.startPoint = map.drone_start_point;
        pointFromStart = new Point();
        sensorOpticalFlow = new Point();
        lidars = new ArrayList<>();
        speed = WorldParams.start_speed;
        rotation = 0;
        gyroRotation = rotation;
        cpu = new CPU(100, "drone");
    }

    public void play() {
        cpu.play();
    }
    public void stop() { cpu.stop(); }

    public void addLidar(int degrees) {
        Lidar lidar = new Lidar(this, degrees);
        lidars.add(lidar);
        cpu.addFunction(i -> lidar.getSimulationDistance());
    }

    public Point getPointOnMap() {
        double x = startPoint.x + pointFromStart.x;
        double y = startPoint.y + pointFromStart.y;
        return new Point(x, y);
    }

    public void update(int deltaTime) {

        double distancedMoved = (speed * 100) * ((double) deltaTime / 1000);

        pointFromStart = Tools.getPointByDistance(pointFromStart, rotation, distancedMoved);

        double noiseToDistance = Tools.noiseBetween(WorldParams.min_motion_accuracy, WorldParams.max_motion_accuracy, false);
        sensorOpticalFlow = Tools.getPointByDistance(sensorOpticalFlow, rotation, distancedMoved * noiseToDistance);

        double noiseToRotation = Tools.noiseBetween(WorldParams.min_rotation_accuracy, WorldParams.max_rotation_accuracy, false);
        double milli_per_minute = 60000;
        gyroRotation += (1 - noiseToRotation) * deltaTime / milli_per_minute;
        gyroRotation = formatRotation(gyroRotation);
    }

    public static double formatRotation(double rotationValue) {
        rotationValue %= 360;
        if (rotationValue < 0) {
            rotationValue = 360 - rotationValue;
        }
        return rotationValue;
    }

    public double getRotation() {
        return rotation;
    }

    public double getGyroRotation() {
        return gyroRotation;
    }

    public Point getOpticalSensorLocation() {
        return new Point(sensorOpticalFlow);
    }


    public void rotateLeft(int deltaTime) {
        double rotationChanged = WorldParams.rotation_per_second * deltaTime / 1000;

        rotation += rotationChanged;
        rotation = formatRotation(rotation);

        gyroRotation += rotationChanged;
        gyroRotation = formatRotation(gyroRotation);
    }

    public void speedUp(int deltaTime) {
        speed += (WorldParams.accelerate_per_second * deltaTime / 1000);
        if (speed > WorldParams.max_speed) {
            speed = WorldParams.max_speed;
        }
        System.out.println("[speedUp] speed is now equal to: " + speed);
    }

    public void slowDown(int deltaTime) {
        speed -= (WorldParams.accelerate_per_second * deltaTime / 1000);
        if (speed < 0) {
            speed = 0;
        }
        System.out.println("[slowDown] speed is now equal to: " + speed);
    }


    boolean initPaint = false;
    BufferedImage mImage;
    int j = 0;

    public void paint(Graphics g) {
        if (!initPaint) {
            try {
                File f = new File(drone_img_path);
                mImage = ImageIO.read(f);
                initPaint = true;
            } catch (Exception ex) {

            }
        }

        for (int i = 0; i < lidars.size(); i++) {
            Lidar lidar = lidars.get(i);
            lidar.paint(g);
        }
    }

    public String getInfoHTML() {
        DecimalFormat df = new DecimalFormat("#.####");

        return "<html>"
                + "Rotation: " + df.format(rotation) + "<br>"
                + "Location: " + pointFromStart + "<br>"
                + "gyroRotation: " + df.format(gyroRotation) + "<br>"
                + "sensorOpticalFlow: " + sensorOpticalFlow + "<br>"
                + "</html>";
    }
}
