package src.graph;

import src.board.TileType;
import src.movement.Direction;

import java.util.ArrayList;
import java.util.List;



public class BoardGraph {
    private GraphNode playerNode;
    private List<GraphNode> allNodes;
    private int width, height;
    private int totalData; 

    public BoardGraph(String layout, int w, int h) {
        this.width = w;
        this.height = h;
        this.allNodes = new ArrayList<>();
        this.totalData = 0;
        initialize(layout);
    }

    private void initialize(String layout) {
        GraphNode[][] tempGrid = new GraphNode[height][width];
        char[] chars = layout.toCharArray();

        // 1. Create Nodes
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                TileType type = parseChar(chars[y * width + x]);
                GraphNode node = new GraphNode(x, y, type);
                
                if (type == TileType.DATA) totalData++;
                if (type == TileType.START) {
                    playerNode = node;
                    playerNode.setPlayer(true);
                    node.setType(TileType.HUB); // Start acts as a Hub
                }

                tempGrid[y][x] = node;
                allNodes.add(node);
            }
        }

        // 2. Link Neighbors
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                GraphNode current = tempGrid[y][x];
                for (Direction d : Direction.ALL) {
                    int nx = x + d.dx;
                    int ny = y + d.dy;
                    if (nx >= 0 && nx < width && ny >= 0 && ny < height) {
                        current.addNeighbor(d, tempGrid[ny][nx]);
                    }
                }
            }
        }
    }

    private TileType parseChar(char c) {
        switch (c) {
            case 'w': return TileType.FIREWALL;
            case 'g': return TileType.DATA;     
            case 'm': return TileType.VIRUS;    
            case 's': return TileType.HUB;      
            case 'S': return TileType.START;
            case 'b': return TileType.BLANK;
            default: return TileType.BLANK;
        }
    }

    public GraphNode getPlayerNode() { return playerNode; }
    public void setPlayerNode(GraphNode node) { this.playerNode = node; }
    public List<GraphNode> getAllNodes() { return allNodes; }
    public int getTotalData() { return totalData; }
    public void decreaseDataCount() { totalData--; }
}