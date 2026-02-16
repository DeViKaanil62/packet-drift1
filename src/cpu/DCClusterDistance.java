package src.cpu;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DCClusterDistance {

    public record Point(int x, int y) {}

    /**
     * Returns the average Euclidean distance from (qx, qy) to the k closest data points.
     * If fewer than k points exist, averages all available points.
     * k = 1 → equivalent to nearest neighbor distance
     */
    public static double averageDistanceToKClosest(
            List<Point> dataPoints,
            int qx, int qy,
            int k) {

        if (dataPoints == null || dataPoints.isEmpty()) {
            return 0.0;
        }

        if (k <= 0) k = 1;

        Point[] points = dataPoints.toArray(new Point[0]);

   
        mergeSortByX(points, 0, points.length - 1);

      
        double[] distances = new double[points.length];
        for (int i = 0; i < points.length; i++) {
            double dx = points[i].x - qx;
            double dy = points[i].y - qy;
            distances[i] = Math.sqrt(dx * dx + dy * dy);
        }

        
        Arrays.sort(distances);

        // Sum the k smallest distances
        double sum = 0.0;
        int actualK = Math.min(k, distances.length);
        for (int i = 0; i < actualK; i++) {
            sum += distances[i];
        }

        return sum / actualK;
    }

   
    private static void mergeSortByX(Point[] arr, int left, int right) {
        if (left >= right) {
            return;
        }

        int mid = left + (right - left) / 2;

        mergeSortByX(arr, left, mid);
        mergeSortByX(arr, mid + 1, right);

        mergeByX(arr, left, mid, right);
    }

    private static void mergeByX(Point[] arr, int left, int mid, int right) {
        int n1 = mid - left + 1;
        int n2 = right - mid;

        Point[] L = new Point[n1];
        Point[] R = new Point[n2];

        for (int i = 0; i < n1; i++) {
            L[i] = arr[left + i];
        }
        for (int j = 0; j < n2; j++) {
            R[j] = arr[mid + 1 + j];
        }

        int i = 0, j = 0, k = left;

        while (i < n1 && j < n2) {
            if (L[i].x <= R[j].x) {
                arr[k++] = L[i++];
            } else {
                arr[k++] = R[j++];
            }
        }

        while (i < n1) {
            arr[k++] = L[i++];
        }
        while (j < n2) {
            arr[k++] = R[j++];
        }
    }
}