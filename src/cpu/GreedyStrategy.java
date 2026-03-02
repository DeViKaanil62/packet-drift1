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
    private static final double CLUSTER_PENALTY_WEIGHT = 12.0;  // tune: 5–20 range

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
                3  // k=3
        );
    }

    // DP Solver instance (from Member 1)
    private final DPDepthSolver dpSolver = new DPDepthSolver();

    public Direction getBestDirection(BoardGraph graph) {
        GraphNode playerNode = graph.getPlayerNode();
        Direction bestDir = null;
        double bestScore = Double.NEGATIVE_INFINITY;

        // Minimal DP: memo cache for cluster distance (from Review 2)
        DPMemoCache memo = new DPMemoCache();

        // For validation/logging
        List<ScoredDirection> scoredDirs = new ArrayList<>();

        for (Direction dir : Direction.ALL) {
            SimulationResult sim = simulateSlide(graph, playerNode, dir);

            if (sim.endNode == playerNode && sim.dataCollected == 0) {
                continue;
            }

            double immediateScore = sim.hitsVirus ? -DEATH_PENALTY : sim.dataCollected * DATA_VALUE;

            // DP lookahead (long-term optimal, from Member 1)
            double futureScore = dpSolver.dpMaxFrom(graph, sim.endNode, LOOKAHEAD_DEPTH - 1);

            // D&C cluster heuristic (short/mid-term, from Review 2)
            double clusterDist = memo.getOrComputeDistance(
                    graph,
                    sim.endNode,
                    sim.collectedNodes,
                    (g, f, e) -> distanceToCluster(g, f, e)
            );
            double clusterPenalty = clusterDist * CLUSTER_PENALTY_WEIGHT;

            // Hybrid total score
            double totalScore = immediateScore + futureScore - clusterPenalty;

            scoredDirs.add(new ScoredDirection(dir, totalScore, clusterDist, futureScore));

            if (totalScore > bestScore) {
                bestScore = totalScore;
                bestDir = dir;
            }
        }

        // Validation logging (Member 2's part – helps in demo & testing)
        if (!scoredDirs.isEmpty()) {
            scoredDirs.sort((a, b) -> Double.compare(b.score, a.score));
            System.out.println("Top 3 directions this turn (hybrid DP lookahead + cluster):");
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