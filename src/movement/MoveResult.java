package src.movement;


public class MoveResult {
    public boolean success;
    public boolean isDead; 
    public int dataCollected; 

    public MoveResult(boolean success, boolean isDead, int dataCollected) {
        this.success = success;
        this.isDead = isDead;
        this.dataCollected = dataCollected;
    }
}