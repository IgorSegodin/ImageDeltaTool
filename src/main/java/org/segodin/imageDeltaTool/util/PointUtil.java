package org.segodin.imageDeltaTool.util;

import java.awt.Point;
import java.util.Comparator;

public class PointUtil {

    /**
     * Sorting points by coordinates and distance to center 0:0
     * */
    public static final Comparator<Point> COMPARATOR_BY_DISTANCE_TO_CENTER = (a, b) -> {
        if (a.getX() > b.getX() && a.getY() > b.getY()) {
            return 1;
        } else if (a.getX() == b.getX() && a.getY() == b.getY()) {
            return 0;
        } else {
            /**
             * Comparing distance from 0:0 to x:y
             * */
            double distanceA = Math.sqrt(a.getX() * a.getX() + a.getY() * a.getY());
            double distanceB = Math.sqrt(b.getX() * b.getX() + b.getY() * b.getY());
            return distanceA > distanceB ? 1 : -1;
        }
    };

    public static double getDistance(Point pointA, Point pointB) {
        double a = Math.abs(pointA.getX() - pointB.getX());
        double b = Math.abs(pointA.getY() - pointB.getY());

        return Math.sqrt(Math.pow(a, 2) + Math.pow(b, 2));
    }
}
