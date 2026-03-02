package src.cpu;

import src.board.TileType;
import src.graph.BoardGraph;
import src.graph.GraphNode;
import src.movement.Direction;

import java.util.*;

public class GreedyStrategy {

    private static final int DATA_VALUE = 100;
    private static final int DEATH_PENALTY = 99999;
    private static final int LOOKAHEAD_DEPTH = 3; // Tunable: Increase for deeper lookahead (e.g., 5-10 is feasible)

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

            if (current.getType() == TileType.HUB) break;

            next = current.getNeighbor(dir);
            if (next == null || next.getType() == TileType.FIREWALL) break;
        }

        return new SimulationResult(current, dataCollected, hitsVirus, collectedNodes);
    }

    public Direction getBestDirection(BoardGraph graph) {
        GraphNode playerNode = graph.getPlayerNode();
        Direction bestDir = null;
        double bestScore = Double.NEGATIVE_INFINITY;

        // Instance of DP solver for lookahead
        DPDepthSolver dpSolver = new DPDepthSolver();

        // For debug/logging: Collect scored directions
        List<ScoredDirection> scoredDirs = new ArrayList<>();

        for (Direction dir : Direction.ALL) {
            SimulationResult sim = simulateSlide(graph, playerNode, dir);

            if (sim.endNode == playerNode && sim.dataCollected == 0) {
                continue; // Invalid move
            }

            double immediateScore;
            if (sim.hitsVirus) {
                immediateScore = -DEATH_PENALTY;
            } else {
                immediateScore = sim.dataCollected * DATA_VALUE;
            }

            // Get future score using DP lookahead from end position
            double futureScore = dpSolver.dpMaxFrom(graph, sim.endNode, LOOKAHEAD_DEPTH);

            double totalScore = immediateScore + futureScore;

            scoredDirs.add(new ScoredDirection(dir, totalScore));

            if (totalScore > bestScore) {
                bestScore = totalScore;
                bestDir = dir;
            }
        }

        // Debug logging: Sort and print top 3 directions
        if (!scoredDirs.isEmpty()) {
            scoredDirs.sort((a, b) -> Double.compare(b.score, a.score)); // Descending order
            System.out.println("Top 3 directions for this turn:");
            for (int i = 0; i < Math.min(3, scoredDirs.size()); i++) {
                ScoredDirection sd = scoredDirs.get(i);
                System.out.println((i + 1) + ": " + sd.dir + " with total score: " + sd.score);
            }
        } else {
            System.out.println("No valid moves available.");
        }

        return bestDir;
    }

    private static class ScoredDirection {
        Direction dir;
        double score;

        ScoredDirection(Direction dir, double score) {
            this.dir = dir;
            this.score = score;
        }
    }
}