package src.cpu;

import src.graph.BoardGraph;
import src.graph.GraphNode;
import src.board.TileType;

import java.util.*;

public class DPMemoCache {

    private final Map<String, Double> memo;

    public DPMemoCache() {
        this.memo = new HashMap<>();
    }

    /**
     * Gets cached cluster distance if available, else computes and caches it.
     */
    public double getOrComputeDistance(
            BoardGraph graph,
            GraphNode from,
            Set<GraphNode> exclude,
            DistanceCalculator calculator) {

        String key = from.getX() + "," + from.getY() + "," + getDataHash(graph, exclude);
        Double cached = memo.get(key);

        if (cached != null) {
            return cached;
        }

        double distance = calculator.compute(graph, from, exclude);
        memo.put(key, distance);
        return distance;
    }

    private String getDataHash(BoardGraph graph, Set<GraphNode> exclude) {
        List<String> remaining = new ArrayList<>();
        for (GraphNode node : graph.getAllNodes()) {
            if (node.getType() == TileType.DATA && !exclude.contains(node)) {
                remaining.add(node.getX() + ":" + node.getY());
            }
        }
        Collections.sort(remaining);
        return String.join(",", remaining);
    }

    @FunctionalInterface
    public interface DistanceCalculator {
        double compute(BoardGraph graph, GraphNode from, Set<GraphNode> exclude);
    }
}