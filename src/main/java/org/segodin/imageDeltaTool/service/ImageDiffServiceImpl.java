package org.segodin.imageDeltaTool.service;

import org.segodin.imageDeltaTool.service.data.HighlightZone;
import org.segodin.imageDeltaTool.service.data.ImageData;
import org.segodin.imageDeltaTool.util.PointUtil;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

public class ImageDiffServiceImpl implements ImageDiffService {

    @Override
    public BufferedImage getHighlightedImage(ImageData origin, ImageData toCompare) {

        if (origin.getWidth() != toCompare.getWidth() && origin.getHeight() != toCompare.getHeight() ) {
            throw new IllegalStateException("Comparing images with different sizes or offset is not implemented yet.");
        }

        if (origin.getPixel(0, 0).length > 3) {
            throw new IllegalStateException("Unsupported image format, should be RGB.");
        }

        List<Point> differentPoints = getDifferentPoints(origin, toCompare);

        if (differentPoints.size() > 0) {
            /**
             * Percent of how many different pixels could have given images.
             * */
            double differenceFactor = 0.5;

            int differenceLimit = (int) (origin.getHeight() * origin.getWidth() * differenceFactor);

            if (differentPoints.size() > differenceLimit) {
                throw new IllegalStateException("Images have to many differences.");
            }

            double maxDistanceFactor = 0.1;
            /**
             * Max distance between points, to treat them as different highlight zones.
             * Counts as 10% from smallest image dimension
             * */
            double rawMaxPointDistance = (origin.getWidth() >= origin.getHeight() ? origin.getHeight() : origin.getWidth()) * maxDistanceFactor;

            List<HighlightZone> differentZones = getDifferentZones(differentPoints, rawMaxPointDistance);

            return highlightImage(differentZones, toCompare.copyBufferedImage());
        }
        return null;
    }

    protected List<HighlightZone> getDifferentZones(List<Point> points, double rawMaxPointDistance) {
        Collections.sort(points, PointUtil.COMPARATOR_BY_DISTANCE_TO_CENTER);

        List<PointGroup> pointGroups = new ArrayList<>();

        Iterator<Point> pointIterator = points.iterator();

        PointGroup previousGroup = new PointGroup();
        pointGroups.add(previousGroup);

        Point previousPoint = pointIterator.next();
        previousGroup.addPoint(previousPoint);

        /**
         * Group points, which have distance to previous point lesser than specified distance - rawMaxPointDistance
         * */
        while (pointIterator.hasNext()) {
            Point point = pointIterator.next();

            double distanceToPrevious = PointUtil.getDistance(point, previousPoint);

            if (distanceToPrevious > rawMaxPointDistance) {
                PointGroup newPointGroup = new PointGroup();
                pointGroups.add(newPointGroup);

                previousGroup = newPointGroup;
            }

            previousGroup.addPoint(point);
            previousPoint = point;
        }

        /**
         * Creates zones from groups. Takes smallest x, y for first rectangle point and largest x, y for second rectangle point.
         * */
        List<HighlightZone> nonMergedZones = pointGroups.stream().map(pointGroup -> {
            int minX;
            int minY;

            int maxX;
            int maxY;

            Collections.sort(pointGroup.getPoints(), new Comparator<Point>() {
                @Override
                public int compare(Point o1, Point o2) {
                    return Double.compare(o1.getX(), o2.getX());
                }
            });

            minX = (int) pointGroup.getPoints().get(0).getX();
            maxX = (int) pointGroup.getPoints().get(pointGroup.getPoints().size() - 1).getX();

            Collections.sort(pointGroup.getPoints(), new Comparator<Point>() {
                @Override
                public int compare(Point o1, Point o2) {
                    return Double.compare(o1.getY(), o2.getY());
                }
            });

            minY = (int) pointGroup.getPoints().get(0).getY();
            maxY = (int) pointGroup.getPoints().get(pointGroup.getPoints().size() - 1).getY();

            return new HighlightZone(new Point(minX, minY), new Point(maxX, maxY));
        }).collect(Collectors.<HighlightZone>toList());

        /**
         * Merge intersected zones, or zones, which are too near to each other
         * */
        List<HighlightZone> finalZones = new ArrayList<>(nonMergedZones);
        for (Iterator<HighlightZone> finalIterator = finalZones.iterator(); finalIterator.hasNext();) {
            HighlightZone finalZone = finalIterator.next();
            /**
             * Need to remove current finalZone from nonMergedZones, not to check with itself
             * */
            boolean removed = nonMergedZones.remove(finalZone);
            if (removed) {
                /**
                 * Current finalZone was not merged yet, need to check if we can merge it with left nonMergedZones
                 * */
                for (Iterator<HighlightZone> nonMergedIterator = nonMergedZones.iterator(); nonMergedIterator.hasNext();) {
                    HighlightZone nonMergedZone = nonMergedIterator.next();
                    if (finalZone.isIntersects(nonMergedZone) || finalZone.distanceToOther(nonMergedZone) <= rawMaxPointDistance) {
                        /**
                         * Current nonMergedZone intersects with current finalZone or lies too near to it, then we can merge zones.
                         * */
                        finalZone.merge(nonMergedZone);
                        /**
                         * merged zone should be removed
                         * */
                        nonMergedIterator.remove();
                    }
                }
            } else {
                /**
                 * if finalZone doesn't exist in nonMergedZones list, then it means that it was already merged, and we should remove this finalZone
                 * */
                finalIterator.remove();
            }
        }

        return finalZones;
    }

    /**
     * Draw rectangles to highlight different zones on the target image.
     * */
    protected BufferedImage highlightImage(List<HighlightZone> zones, BufferedImage imageTarget) {

        Graphics2D g2 = imageTarget.createGraphics();
        g2.setStroke(new BasicStroke(2));
        g2.setColor(Color.RED);

        for (HighlightZone zone : zones) {
            int frameX = (int) zone.getTopLeftPoint().getX();
            int frameY = (int) zone.getTopLeftPoint().getY();
            int frameWidth = (int) (zone.getBottomRightPoint().getX() - frameX);
            int frameHeight = (int) (zone.getBottomRightPoint().getY() - frameY);
            /**
             * distance from different pixels to highlighting frame = 5 pixels
             * */
            g2.drawRect(
                    frameX - 5, frameY - 5,
                    frameWidth + 10, frameHeight + 10
            );
        }

        g2.dispose();
        return imageTarget;
    }

    /**
     * @return points which have different colors on given images
     * TODO color noise suppression
     * */
    protected List<Point> getDifferentPoints(ImageData origin, ImageData toCompare) {
        int width = origin.getWidth();
        int height = origin.getHeight();

        List<Point> differentPoints = new ArrayList<>();

        /**
         * Find min and max color components difference, to suppress some color noise later
         * */
        int[] maxExistingColorComponentDelta = new int[]{0, 0, 0};
        int[] minExistingColorComponentDelta = new int[]{255, 255, 255};
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                if (origin.getRGB(x, y) != toCompare.getRGB(x, y)) {
                    int[] originPixel = origin.getPixel(x, y);
                    int[] toComparePixel = toCompare.getPixel(x, y);

                    for (int i = 0; i < 3; i++) {
                        if (originPixel[i] != toComparePixel[i]) {
                            int delta = Math.abs(originPixel[i] - toComparePixel[i]);
                            if (delta > maxExistingColorComponentDelta[i]) {
                                maxExistingColorComponentDelta[i] = delta;
                            }
                            if (delta < minExistingColorComponentDelta[i]) {
                                minExistingColorComponentDelta[i] = delta;
                            }
                        }
                    }
                }
            }
        }

        /**
         * Average max color components difference. Cheap way to suppress color noise.
         * */
        int[] maxColorComponentDelta = new int[3];
        for (int i = 0; i < 3; i++) {
            maxColorComponentDelta[i] = (maxExistingColorComponentDelta[i] + minExistingColorComponentDelta[i]) / 2;
        }

        /**
         * Collect points with different pixels.
         * */
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                int[] originPixel = origin.getPixel(x, y);
                int[] toComparePixel = toCompare.getPixel(x, y);
                boolean isDifferent = false;

                for (int i = 0; i < 3; i++) {
                    int originComponent = originPixel[i];
                    int toCompareComponent = toComparePixel[i];

                    if (Math.abs(originComponent - toCompareComponent) > maxColorComponentDelta[i]) {
                        isDifferent = true;
                        break;
                    }
                }

                if (isDifferent) {
                    differentPoints.add(new Point(x, y));
                }
            }
        }

        return differentPoints;
    }

    /**
     * Represents points which should be highlighted with single frame
     * */
    protected class PointGroup {

        private List<Point> points = new ArrayList<>();

        public void addPoint(Point point) {
            points.add(point);
        }

        public List<Point> getPoints() {
            return points;
        }
    }
}
