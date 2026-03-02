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

    /**
     * Memoized recursive function:
     * Returns maximum achievable score from this node within depthLeft moves.
     */
    public double dpMaxFrom(BoardGraph graph,
                            GraphNode currentNode,
                            int depthLeft) {

        //  Base Case
        if (depthLeft == 0) {
            return 0;
        }

        //  DP Key
        String key = currentNode.getX() + ","
                   + currentNode.getY() + ","
                   + depthLeft;

        // Memo Check
        if (dpTable.containsKey(key)) {
            return dpTable.get(key);
        }

        double bestScore = Double.NEGATIVE_INFINITY;

        // Try all 8 directions
        for (Direction dir : Direction.ALL) {

            SimulationResult sim =
                    simulateSlide(graph, currentNode, dir);

            // Invalid move (blocked)
            if (sim.endNode == currentNode
                && sim.dataCollected == 0) {
                continue;
            }

            double immediateScore;

            if (sim.hitsVirus) {
                immediateScore = -DEATH_PENALTY;
            } else {
                immediateScore = sim.dataCollected * DATA_VALUE;
            }

            double futureScore =
                    immediateScore
                    + dpMaxFrom(graph,
                                sim.endNode,
                                depthLeft - 1);

            bestScore = Math.max(bestScore, futureScore);
        }

        if (bestScore == Double.NEGATIVE_INFINITY) {
            bestScore = 0;
        }

        // Store in DP table
        dpTable.put(key, bestScore);

        return bestScore;
    }

    /**
     * Slide simulation (copied from GreedyStrategy)
     */
    private SimulationResult simulateSlide(BoardGraph graph,
                                           GraphNode start,
                                           Direction dir) {

        GraphNode next = start.getNeighbor(dir);

        if (next == null
            || next.getType() == TileType.FIREWALL) {
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

            if (current.getType() == TileType.HUB) {
                break;
            }

            next = current.getNeighbor(dir);

            if (next == null
                || next.getType() == TileType.FIREWALL) {
                break;
            }
        }

        return new SimulationResult(current,
                                    dataCollected,
                                    hitsVirus);
    }

    private static class SimulationResult {
        GraphNode endNode;
        int dataCollected;
        boolean hitsVirus;

        SimulationResult(GraphNode endNode,
                         int dataCollected,
                         boolean hitsVirus) {
            this.endNode = endNode;
            this.dataCollected = dataCollected;
            this.hitsVirus = hitsVirus;
        }
    }
}
