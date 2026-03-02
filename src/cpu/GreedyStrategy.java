package src.cpu;

import src.board.TileType;
import src.graph.BoardGraph;
import src.graph.GraphNode;
import src.movement.Direction;

import java.util.*;

public class GreedyStrategy {

    private static final int DATA_VALUE = 100;
    private static final int DEATH_PENALTY = 99999;
    private static final int LOOKAHEAD_DEPTH = 4;          // tunable: 3–6 usually good
    private static final double CLUSTER_PENALTY_WEIGHT = 12.0;  // ← tune this (5–20 range)

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

    // ────────────────────────────────────────────────
    //  RESTORED: Original slide simulation (same as before)
    // ────────────────────────────────────────────────
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

    // ────────────────────────────────────────────────
    //  RESTORED: Cluster distance (divide & conquer part)
    // ────────────────────────────────────────────────
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
                2  // K_CLOSEST_FOR_CLUSTER = 2 as in original
        );
    }

    // =============================================
    //  Backtracking Recursion & State Exploration
    // =============================================
    

    private double getBestFutureScore(
            BoardGraph graph,
            GraphNode currentPos,
            int depthLeft,
            Set<GraphNode> alreadyCollected) {

        if (depthLeft == 0) {
            return 0.0;
        }

        double maxFuture = 0.0;

        for (Direction dir : Direction.ALL) {
            SimulationResult sim = simulateSlide(graph, currentPos, dir);

            // Basic pruning
            if (sim.endNode == currentPos && sim.dataCollected == 0) continue;
            if (sim.hitsVirus) continue;

            // Create updated collected set for this path
            Set<GraphNode> newCollected = new HashSet<>(alreadyCollected);
            newCollected.addAll(sim.collectedNodes);

            double immediate = sim.dataCollected * DATA_VALUE;

            double future = getBestFutureScore(
                    graph,
                    sim.endNode,
                    depthLeft - 1,
                    newCollected
            );

            maxFuture = Math.max(maxFuture, immediate + future);
        }

        return maxFuture;
    }

    

    public Direction getBestDirection(BoardGraph graph) {
        GraphNode playerNode = graph.getPlayerNode();
        Direction bestDir = null;
        double bestScore = Double.NEGATIVE_INFINITY;

        DPDepthSolver dpSolver = new DPDepthSolver();

        List<ScoredDirection> scoredDirs = new ArrayList<>();

        for (Direction dir : Direction.ALL) {
            SimulationResult sim = simulateSlide(graph, playerNode, dir);

            if (sim.endNode == playerNode && sim.dataCollected == 0) {
                continue; // invalid/no move
            }

            double immediateScore = sim.hitsVirus 
                    ? -DEATH_PENALTY 
                    : sim.dataCollected * DATA_VALUE;

            // DP lookahead (long-term exact)
            double futureScore = dpSolver.dpMaxFrom(graph, sim.endNode, LOOKAHEAD_DEPTH);

            // Divide & Conquer heuristic (short/mid-term greediness)
            double clusterDist = distanceToCluster(graph, sim.endNode, sim.collectedNodes);
            double clusterPenalty = clusterDist * CLUSTER_PENALTY_WEIGHT;

            // Hybrid total
            double totalScore = immediateScore + futureScore - clusterPenalty;

            scoredDirs.add(new ScoredDirection(dir, totalScore, clusterDist, futureScore));

            if (totalScore > bestScore) {
                bestScore = totalScore;
                bestDir = dir;
            }
        }

        // ────────────────────────────────────────────────
        // Enhanced logging — shows both components
        // ────────────────────────────────────────────────
        if (!scoredDirs.isEmpty()) {
            scoredDirs.sort((a, b) -> Double.compare(b.score, a.score));
            System.out.println("Top 3 directions this turn (hybrid DP + cluster):");
            for (int i = 0; i < Math.min(3, scoredDirs.size()); i++) {
                ScoredDirection sd = scoredDirs.get(i);
                System.out.printf("%d: %s | total=%.1f | futureDP=%.1f | clusterPenalty=%.1f%n",
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