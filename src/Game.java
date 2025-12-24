package src;
import src.graph.BoardGraph;
import src.graph.GraphNode;
import src.movement.Direction;
import src.movement.MoveResult;
import src.movement.SlideSimulator;
import src.player.HumanPlayer;
import src.player.Player;
import src.board.TileType;

import java.util.Scanner;



public class Game {
    private BoardGraph graph;
    private Player player;
    private boolean isGameOver = false;
    private int hops = 0; 

    public Game() {
        String level = 
            "wwwwwwwwww" +
            "wbbbgbbgbw" +
            "wbwswmwbsw" +
            "wggbSbbgbw" +
            "wbwmwswbgw" +
            "wbgbbmbbgw" +
            "wbswgbbwbw" +
            "wwwwwwwwww";
        
        this.graph = new BoardGraph(level, 10, 8);
        this.player = new HumanPlayer();
    }

    public void start() {
        Scanner scanner = new Scanner(System.in);
        printBoard();

        while (!isGameOver) {
            Direction moveDir = player.getMove(scanner);
            
            if (moveDir != null) {
                MoveResult result = SlideSimulator.simulate(graph, moveDir);
                
                if (result.success) {
                    hops++; 
                }

                printBoard();
                
                if (!result.success) {
                    System.out.println("(!) Connection Blocked by Firewall.");
                } else {
                    if (result.isDead) {
                        System.out.println("SYSTEM FAILURE! Packet corrupted by Virus.");
                        isGameOver = true;
                    } else if (graph.getTotalData() == 0) {
                        System.out.println("DOWNLOAD COMPLETE! All data packets collected.");
                        isGameOver = true;
                    }
                }
            } else {
                printBoard();
            }
        }
        scanner.close();
    }

    private void printBoard() {
        System.out.print("\033[H\033[2J");
        System.out.flush();
        System.out.println("=========================================");
        System.out.println("          P A C K E T   D R I F T        ");
        System.out.println("=========================================");
        System.out.printf(" Data Packets Left: %-5d | Hops: %d%n", graph.getTotalData(), hops);
        System.out.println("-----------------------------------------");


        System.out.println(" LEGEND:");
        System.out.println("  " + getSymbolForLegend(TileType.START) + " : PACKET (You)");
        System.out.println("  " + getSymbolForLegend(TileType.DATA) + " : DATA (Collect these)");
        System.out.println("  " + getSymbolForLegend(TileType.VIRUS) + " : VIRUS (Avoid!)");
        System.out.println("  " + getSymbolForLegend(TileType.HUB) + " : HUB (Safe Stop)");
        System.out.println("  " + getSymbolForLegend(TileType.FIREWALL) + " : FIREWALL");
        System.out.println("-----------------------------------------");

        int currentRow = 0;
        System.out.print("  "); 
        
        for (GraphNode n : graph.getAllNodes()) {
            if (n.getY() > currentRow) {
                System.out.println();
                System.out.print("  "); 
                currentRow = n.getY();
            }
            System.out.print(getSymbol(n) + " ");
        }
        System.out.println("\n-----------------------------------------");
        
        System.out.println(" CONTROLS:");
        System.out.println("   [Q][W][E]   (Diagonals + Up)");
        System.out.println("   [A]   [D]   (Left / Right)");
        System.out.println("   [Z][X][C]   (Diagonals + Down)");
        System.out.println("   [P] to Exit");
        System.out.println("-----------------------------------------");
    }


    private String getSymbolForLegend(TileType t) {
        String ANSI_RESET = "\u001B[0m";
        String ANSI_RED = "\u001B[31m";
        String ANSI_GREEN = "\u001B[32m";
        String ANSI_BLUE = "\u001B[34m";
        String ANSI_CYAN = "\u001B[36m";
        String ANSI_PURPLE = "\u001B[35m";
        
        switch (t) {
            case START: return ANSI_CYAN + "@" + ANSI_RESET;
            case FIREWALL: return ANSI_BLUE + "#" + ANSI_RESET;
            case DATA: return ANSI_GREEN + "D" + ANSI_RESET;
            case VIRUS: return ANSI_RED + "V" + ANSI_RESET;
            case HUB: return ANSI_PURPLE + "H" + ANSI_RESET;
            default: return " ";
        }
    }

    private String getSymbol(GraphNode n) {
        String ANSI_RESET = "\u001B[0m";
        String ANSI_RED = "\u001B[31m";
        String ANSI_GREEN = "\u001B[32m";
        String ANSI_BLUE = "\u001B[34m";
        String ANSI_CYAN = "\u001B[36m";
        String ANSI_PURPLE = "\u001B[35m";

        if (n.hasPlayer()) return ANSI_CYAN + "@" + ANSI_RESET;
        
        switch (n.getType()) {
            case FIREWALL: return ANSI_BLUE + "#" + ANSI_RESET;
            case DATA: return ANSI_GREEN + "D" + ANSI_RESET; 
            case VIRUS: return ANSI_RED + "V" + ANSI_RESET;   
            case HUB: return ANSI_PURPLE + "H" + ANSI_RESET;  
            default: return " ";
        }
    }

    public static void main(String[] args) {
        Game game = new Game();
        game.start();
    }
}