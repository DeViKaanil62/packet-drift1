package src.cpu;

import src.board.TileType;
import src.graph.BoardGraph;
import src.graph.GraphNode;
import src.movement.Direction;

import java.util.*;

public class GreedyStrategy {

    private static final int DATA_VALUE = 100;
    private static final int DEATH_PENALTY = 99999;
    private static final int K_CLOSEST_FOR_CLUSTER = 3; // tune: 2–4 works well

    /**
     * Simulates a full slide in one direction without modifying the real board.
     */
    private static class SimulationResult {
        GraphNode endNode;
        int dataCollected;
        boolean hitsVirus;
        Set<GraphNode> collectedNodes;

        SimulationResult(GraphNode endNode, int dataCollected, boolean hitsVirus, Set<GraphNode> collectedNodes) {
            this.endNode = endNode;
            this.dataCollected = dataCollected;
            this.hitsVirus = hitsVirus;
            this.collectedNodes = collectedNodes;
        }
    }

    private SimulationResult simulateSlide(BoardGraph graph, GraphNode start, Direction dir) {
        GraphNode next = start.getNeighbor(dir);
        if (next == null || next.getType() == TileType.FIREWALL) {
            return new SimulationResult(start, 0, false, new HashSet<>());
        }

        GraphNode current = start;
        int dataCollected = 0;
        boolean hitsVirus = false;
        Set<GraphNode> collectedNodes = new HashSet<>();

        while (true) {
            current = next;

            if (current.getType() == TileType.DATA) {
                dataCollected++;
                collectedNodes.add(current);
            } else if (current.getType() == TileType.VIRUS) {
                hitsVirus = true;
            }

            if (current.getType() == TileType.HUB) {
                break;
            }

            next = current.getNeighbor(dir);
            if (next == null || next.getType() == TileType.FIREWALL) {
                break;
            }
        }

        return new SimulationResult(current, dataCollected, hitsVirus, collectedNodes);
    }

    /**
     * Computes average distance to the k-closest remaining data points using D&C preprocessing.
     */
    private double distanceToNearestCluster(BoardGraph graph, GraphNode from, Set<GraphNode> exclude) {
        List<DCClusterDistance.Point> dataPoints = new ArrayList<>();

        for (GraphNode node : graph.getAllNodes()) {
            if (node.getType() == TileType.DATA && !exclude.contains(node)) {
                dataPoints.add(new DCClusterDistance.Point(node.getX(), node.getY()));
            }
        }

        if (dataPoints.isEmpty()) {
            return 0.0;
        }

        return DCClusterDistance.averageDistanceToKClosest(
                dataPoints,
                from.getX(),
                from.getY(),
                K_CLOSEST_FOR_CLUSTER
        );
    }

    /**
     * Returns the best direction according to the enhanced greedy heuristic:
     * H = (dataCollected × 100) − average_distance_to_k_closest_data
     */
    public Direction getBestDirection(BoardGraph graph) {
        GraphNode playerNode = graph.getPlayerNode();
        Direction bestDir = null;
        double bestScore = Double.NEGATIVE_INFINITY;

        for (Direction dir : Direction.ALL) {
            SimulationResult sim = simulateSlide(graph, playerNode, dir);

            // Blocked/invalid move
            if (sim.endNode == playerNode && sim.dataCollected == 0) {
                continue;
            }

            double score;
            if (sim.hitsVirus) {
                score = -DEATH_PENALTY;
            } else {
                double avgClusterDist = distanceToNearestCluster(graph, sim.endNode, sim.collectedNodes);
                score = sim.dataCollected * DATA_VALUE - avgClusterDist;
            }

            if (score > bestScore) {
                bestScore = score;
                bestDir = dir;
            }
        }

        return bestDir;
    }
}