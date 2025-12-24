package src.graph;

import src.board.Tile;
import src.board.TileType;
import src.movement.Direction;

import java.util.EnumMap;
import java.util.Map;



public class GraphNode {
    private final int x, y;
    private final Tile tile;
    private final Map<Direction, GraphNode> neighbors;

    public GraphNode(int x, int y, TileType type) {
        this.x = x;
        this.y = y;
        this.tile = new Tile(type);
        this.neighbors = new EnumMap<>(Direction.class);
    }

    public void addNeighbor(Direction d, GraphNode node) {
        neighbors.put(d, node);
    }

    public GraphNode getNeighbor(Direction d) {
        return neighbors.get(d);
    }

    public TileType getType() { return tile.getType(); }
    public void setType(TileType t) { tile.setType(t); }
    public boolean hasPlayer() { return tile.hasPlayer(); }
    public void setPlayer(boolean p) { tile.setPlayer(p); }
    
    public int getX() { return x; }
    public int getY() { return y; }
}