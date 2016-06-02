package org.segodin.imageDeltaTool.service.data;

import org.segodin.imageDeltaTool.util.PointUtil;

import java.awt.Point;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Highlighting zone, contains two points to draw rectangle.
 * */
public class HighlightZone {

    private Point topLeftPoint;
    private Point bottomRightPoint;

    public HighlightZone(Point topLeftPoint, Point bottomRightPoint) {
        this.topLeftPoint = topLeftPoint;
        this.bottomRightPoint = bottomRightPoint;
    }

    public boolean isIntersects(HighlightZone other) {
        int minX = (int) topLeftPoint.getX();
        int minY = (int) topLeftPoint.getY();

        int maxX = (int) bottomRightPoint.getX();
        int maxY = (int) bottomRightPoint.getY();

        List<Point> otherPoints = Arrays.asList(
                other.getTopLeftPoint(),
                new Point((int)other.getBottomRightPoint().getX(), (int)other.getTopLeftPoint().getY()),
                new Point((int)other.getTopLeftPoint().getX(), (int)other.getBottomRightPoint().getY()),
                other.getBottomRightPoint()
        );
        for (Point point : otherPoints) {
            if (point.getX() >= minX && point.getX() <= maxX &&
                    point.getY() >= minY && point.getY() <= maxY) {
                return true;
            }
        }
        return false;
    }

    public double distanceToOther(HighlightZone other) {
        Point center = getCenter();
        Point otherCenter = other.getCenter();
        boolean otherOnTheLeft = center.getX() > otherCenter.getX();
        boolean otherAbove = center.getY() > otherCenter.getY();

        if (otherOnTheLeft) {
            if (otherAbove) {
                if (topLeftPoint.getY() > other.getBottomRightPoint().getY() && topLeftPoint.getX() > other.getBottomRightPoint().getX()) {
                    return PointUtil.getDistance(topLeftPoint, other.getBottomRightPoint());
                } else if (topLeftPoint.getX() >= other.getBottomRightPoint().getX()) {
                    return topLeftPoint.getX() - other.getBottomRightPoint().getX();
                } else {
                    return topLeftPoint.getY() - other.getBottomRightPoint().getY();
                }
            } else {
                Point bottomLeftPoint = new Point((int)topLeftPoint.getX(), (int)bottomRightPoint.getY());
                Point otherTopRightPoint = new Point((int)other.getBottomRightPoint().getX(), (int)other.getTopLeftPoint().getY());

                if (bottomLeftPoint.getY() < otherTopRightPoint.getY() && bottomLeftPoint.getX() > otherTopRightPoint.getX()) {
                    return PointUtil.getDistance(bottomLeftPoint, otherTopRightPoint);
                } else if (bottomLeftPoint.getX() >= otherTopRightPoint.getX()) {
                    return bottomLeftPoint.getX() - otherTopRightPoint.getX();
                } else {
                    return otherTopRightPoint.getY() - bottomLeftPoint.getY();
                }
            }
        } else {
            if (otherAbove) {
                Point topRightPoint = new Point((int)bottomRightPoint.getX(), (int)topLeftPoint.getY());
                Point otherBottomLeftPoint = new Point((int)other.getTopLeftPoint().getX(), (int)other.getBottomRightPoint().getY());

                if (topRightPoint.getY() > otherBottomLeftPoint.getY() && topRightPoint.getX() < otherBottomLeftPoint.getX()) {
                    return PointUtil.getDistance(topRightPoint, otherBottomLeftPoint);
                } else if (otherBottomLeftPoint.getX() >= topRightPoint.getX()) {
                    return otherBottomLeftPoint.getX() - topRightPoint.getX();
                } else {
                    return topRightPoint.getY() - otherBottomLeftPoint.getY();
                }
            } else {
                if (bottomRightPoint.getY() < other.getTopLeftPoint().getY() && bottomRightPoint.getX() < other.getTopLeftPoint().getX()) {
                    return PointUtil.getDistance(bottomRightPoint, other.getTopLeftPoint());
                } else if (bottomRightPoint.getX() < other.getTopLeftPoint().getX()) {
                    return other.getTopLeftPoint().getX() - bottomRightPoint.getX();
                } else {
                    return other.getTopLeftPoint().getY() - bottomRightPoint.getY();
                }
            }
        }
    }

    public void merge(HighlightZone other) {
        List<Double> xCoords = Arrays.asList(
                topLeftPoint.getX(), bottomRightPoint.getX(),
                other.getTopLeftPoint().getX(), other.getBottomRightPoint().getX()
        );
        List<Double> yCoords = Arrays.asList(
                topLeftPoint.getY(), bottomRightPoint.getY(),
                other.getTopLeftPoint().getY(), other.getBottomRightPoint().getY()
        );
        Collections.sort(xCoords, Double::compare);
        Collections.sort(yCoords, Double::compare);

        this.topLeftPoint = new Point(xCoords.get(0).intValue(), yCoords.get(0).intValue());
        this.bottomRightPoint = new Point(xCoords.get(xCoords.size() - 1).intValue(), yCoords.get(yCoords.size() - 1).intValue());
    }

    public Point getTopLeftPoint() {
        return topLeftPoint;
    }

    public Point getBottomRightPoint() {
        return bottomRightPoint;
    }

    public Point getCenter() {
        double width = bottomRightPoint.getX() - topLeftPoint.getX();
        double height = bottomRightPoint.getY() - topLeftPoint.getY();
        return new Point(
                (int) (topLeftPoint.getX() + width / 2),
                (int) (topLeftPoint.getY() + height / 2)
        );
    }
}
