package src.cpu;

import src.board.TileType;
import src.graph.BoardGraph;
import src.graph.GraphNode;
import src.movement.Direction;

import java.util.*;

public class GreedyStrategy {

    private static final int DATA_VALUE = 100;
    private static final int DEATH_PENALTY = 99999;

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

    private double distanceToNearestData(BoardGraph graph, GraphNode from, Set<GraphNode> exclude) {
        List<DCClosestPair.Point> dataPoints = new ArrayList<>();

        for (GraphNode node : graph.getAllNodes()) {
            if (node.getType() == TileType.DATA && !exclude.contains(node)) {
                dataPoints.add(new DCClosestPair.Point(node.getX(), node.getY()));
            }
        }

        return DCClosestPair.findMinDistance(dataPoints, from.getX(), from.getY());
    }

    public Direction getBestDirection(BoardGraph graph) {
        GraphNode playerNode = graph.getPlayerNode();
        Direction bestDir = null;
        double bestScore = Double.NEGATIVE_INFINITY;

        // DP memo cache for this turn
        DPMemoCache memo = new DPMemoCache();

        for (Direction dir : Direction.ALL) {
            SimulationResult sim = simulateSlide(graph, playerNode, dir);

            if (sim.endNode == playerNode && sim.dataCollected == 0) {
                continue;
            }

            double score;
            if (sim.hitsVirus) {
                score = -DEATH_PENALTY;
            } else {
                double distance = memo.getOrComputeDistance(
                        graph,
                        sim.endNode,
                        sim.collectedNodes,
                        (g, f, e) -> distanceToNearestData(g, f, e)
                );
                score = sim.dataCollected * DATA_VALUE - distance;
            }

            if (score > bestScore) {
                bestScore = score;
                bestDir = dir;
            }
        }

        return bestDir;
    }
}