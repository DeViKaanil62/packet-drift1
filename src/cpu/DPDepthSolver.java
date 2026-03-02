package src.cpu;

import src.graph.BoardGraph;
import src.graph.GraphNode;
import src.board.TileType;
import src.movement.Direction;

import java.util.HashMap;
import java.util.Map;

public class DPDepthSolver {

    private final Map<String, Double> dpTable = new HashMap<>();

    private static final int DATA_VALUE = 100;
    private static final int DEATH_PENALTY = 99999;
    private static final int MAX_DEPTH = 5;  // Increased – stronger prediction

    /**
     * Minimax DP: CPU maximizes, assumes Human minimizes CPU's score.
     * isCpuTurn=true → maximize (CPU turn)
     * isCpuTurn=false → minimize (Human turn, predicts opponent blocks)
     */
    public double dpMinimax(BoardGraph graph,
                            GraphNode currentNode,
                            int depthLeft,
                            boolean isCpuTurn) {

        if (depthLeft == 0) {
            return 0;
        }

        String key = currentNode.getX() + "," + currentNode.getY() + "," + depthLeft + "," + (isCpuTurn ? "1" : "0");

        if (dpTable.containsKey(key)) {
            return dpTable.get(key);
        }

        double bestScore = isCpuTurn ? Double.NEGATIVE_INFINITY : Double.POSITIVE_INFINITY;

        for (Direction dir : Direction.ALL) {
            SimulationResult sim = simulateSlide(graph, currentNode, dir);

            if (sim.endNode == currentNode && sim.dataCollected == 0) {
                continue;
            }

            double immediateScore = sim.hitsVirus ? -DEATH_PENALTY : sim.dataCollected * DATA_VALUE;

            double futureScore = dpMinimax(graph, sim.endNode, depthLeft - 1, !isCpuTurn);

            double total = immediateScore + futureScore;

            if (isCpuTurn) {
                bestScore = Math.max(bestScore, total);
            } else {
                bestScore = Math.min(bestScore, total);
            }
        }

        if (bestScore == Double.NEGATIVE_INFINITY || bestScore == Double.POSITIVE_INFINITY) {
            bestScore = 0;
        }

        dpTable.put(key, bestScore);
        return bestScore;
    }

    private SimulationResult simulateSlide(BoardGraph graph, GraphNode start, Direction dir) {
        GraphNode next = start.getNeighbor(dir);
        if (next == null || next.getType() == TileType.FIREWALL) {
            return new SimulationResult(start, 0, false);
        }

        GraphNode current = start;
        int dataCollected = 0;
        boolean hitsVirus = false;

        while (true) {
            current = next;

            if (current.getType() == TileType.DATA) {
                dataCollected++;
            }

            if (current.getType() == TileType.VIRUS) {
                hitsVirus = true;
            }

            if (current.getType() == TileType.HUB) break;

            next = current.getNeighbor(dir);

            if (next == null || next.getType() == TileType.FIREWALL) break;
        }

        return new SimulationResult(current, dataCollected, hitsVirus);
    }

    private static class SimulationResult {
        GraphNode endNode;
        int dataCollected;
        boolean hitsVirus;

        SimulationResult(GraphNode endNode, int dataCollected, boolean hitsVirus) {
            this.endNode = endNode;
            this.dataCollected = dataCollected;
            this.hitsVirus = hitsVirus;
        }
    }
}