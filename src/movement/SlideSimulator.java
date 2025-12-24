package src.movement;



import src.board.TileType;
import src.graph.BoardGraph;
import src.graph.GraphNode;

public class SlideSimulator {

    public static MoveResult simulate(BoardGraph graph, Direction dir) {
        GraphNode current = graph.getPlayerNode();
        GraphNode next = current.getNeighbor(dir);

        
        if (next == null || next.getType() == TileType.FIREWALL) {
            return new MoveResult(false, false, 0);
        }

        current.setPlayer(false);
        int dataCollected = 0;
        boolean crashed = false;

        
        while (true) {
            current = next;

            if (current.getType() == TileType.DATA) {
                current.setType(TileType.BLANK);
                graph.decreaseDataCount();
                dataCollected++;
            } else if (current.getType() == TileType.VIRUS) {
                crashed = true;
                break;
            }

            if (current.getType() == TileType.HUB) break;

            GraphNode lookAhead = current.getNeighbor(dir);
            if (lookAhead == null || lookAhead.getType() == TileType.FIREWALL) {
                break;
            }
            next = lookAhead;
        }

        current.setPlayer(true);
        graph.setPlayerNode(current);
        
        return new MoveResult(true, crashed, dataCollected);
    }
}