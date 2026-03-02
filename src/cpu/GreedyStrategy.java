package src.cpu;

import src.board.TileType;
import src.graph.BoardGraph;
import src.graph.GraphNode;
import src.movement.Direction;

import java.util.*;

public class GreedyStrategy {

    private static final int DATA_VALUE = 100;
    private static final int DEATH_PENALTY = 99999;
    private static final int LOOKAHEAD_DEPTH = 5;
    private static final double CLUSTER_PENALTY_WEIGHT = 15.0;  // Increased for aggression
    private static final double HUB_BONUS = 10.0;  // Small bonus for safe hub endings

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

    private double distanceToCluster(BoardGraph graph, GraphNode from, Set<GraphNode> exclude) {
        List<DCClusterDistance.Point> dataPoints = new ArrayList<>();

        for (GraphNode node : graph.getAllNodes()) {
            if (node.getType() == TileType.DATA && !exclude.contains(node)) {
                dataPoints.add(new DCClusterDistance.Point(node.getX(), node.getY()));
            }
        }

        if (dataPoints.isEmpty()) return 0.0;

        return DCClusterDistance.averageDistanceToKClosest(
                dataPoints,
                from.getX(),
                from.getY(),
                3
        );
    }

    // DP Solver instance – now minimax
    private final DPDepthSolver dpSolver = new DPDepthSolver();

    public Direction getBestDirection(BoardGraph graph) {
        GraphNode playerNode = graph.getPlayerNode();
        Direction bestDir = null;
        double bestScore = Double.NEGATIVE_INFINITY;

        DPMemoCache memo = new DPMemoCache();

        List<ScoredDirection> scoredDirs = new ArrayList<>();

        for (Direction dir : Direction.ALL) {
            SimulationResult sim = simulateSlide(graph, playerNode, dir);

            if (sim.endNode == playerNode && sim.dataCollected == 0) {
                continue;
            }

            double immediateScore = sim.hitsVirus ? -DEATH_PENALTY : sim.dataCollected * DATA_VALUE;

            // Minimax lookahead – assumes human minimizes CPU score
            double futureScore = dpSolver.dpMinimax(graph, sim.endNode, LOOKAHEAD_DEPTH - 1, false);  // false = human's turn next

            double clusterDist = memo.getOrComputeDistance(
                    graph,
                    sim.endNode,
                    sim.collectedNodes,
                    (g, f, e) -> distanceToCluster(g, f, e)
            );
            double clusterPenalty = clusterDist * CLUSTER_PENALTY_WEIGHT;

            // Hub bonus – encourage safe endings
            double hubBonus = (sim.endNode.getType() == TileType.HUB) ? HUB_BONUS : 0;

            double totalScore = immediateScore + futureScore - clusterPenalty + hubBonus;

            scoredDirs.add(new ScoredDirection(dir, totalScore, clusterDist, futureScore));

            if (totalScore > bestScore) {
                bestScore = totalScore;
                bestDir = dir;
            }
        }

        // Deadlock fix: If no valid move, skip turn
        if (bestDir == null) {
            System.out.println("CPU has no valid moves – skipping turn");
            return null;  // Game can handle null as skip
        }

        // Random tie-breaker for top 2 (avoid repetition)
        if (!scoredDirs.isEmpty()) {
            scoredDirs.sort((a, b) -> Double.compare(b.score, a.score));
            if (scoredDirs.size() > 1 && scoredDirs.get(0).score == scoredDirs.get(1).score) {
                // Random between top 2 if tie
                Random rand = new Random();
                bestDir = rand.nextBoolean() ? scoredDirs.get(0).dir : scoredDirs.get(1).dir;
            }
        }

        // Logging for debugging & demo
        if (!scoredDirs.isEmpty()) {
            System.out.println("Top 3 directions this turn (minimax DP + cluster):");
            for (int i = 0; i < Math.min(3, scoredDirs.size()); i++) {
                ScoredDirection sd = scoredDirs.get(i);
                System.out.printf("%d: %s | total=%.1f | futureMinimax=%.1f | clusterPenalty=%.1f%n",
                        i + 1, sd.dir, sd.score, sd.futureScore, sd.clusterDist * CLUSTER_PENALTY_WEIGHT);
            }
        } else {
            System.out.println("No valid moves.");
        }

        return bestDir;
    }

    private static class ScoredDirection {
        Direction dir;
        double score;
        double clusterDist;
        double futureScore;

        ScoredDirection(Direction dir, double score, double clusterDist, double futureScore) {
            this.dir = dir;
            this.score = score;
            this.clusterDist = clusterDist;
            this.futureScore = futureScore;
        }
    }
}