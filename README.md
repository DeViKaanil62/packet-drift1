## 📦 Packet Drift

Packet Drift is a 2-player, grid-based sliding puzzle game inspired by the Inertia puzzle.
A single packet moves across a fixed board, collecting data chunks while avoiding virus nodes.
The human player competes against a CPU that uses a Greedy strategy to maximize immediate score.

## 🎮 Game Rules 

- One shared packet
- Movement allowed in 8 directions (including diagonals)
- Packet slides continuously until it:
    - hits a Hub (stop tile)
    - hits a Firewall (blocked)
    - goes out of bounds
    - lands on a Virus (instant loss)

- 1 Data Chunk = 1 Byte
- Player with the highest bytes at the end wins

## 🤖 CPU Strategy

The CPU uses a Greedy algorithm:
- Simulates all 8 possible directions
- Chooses the move that gives the maximum immediate bytes
- Avoids moves that land on Virus tiles


## 🛠️ How to Clone, Compile, and Run

**1. Clone the repository**
```bash
git clone https://github.com/Nithin0306/packet-drift
```

**2. Navigate to the project folder**
```bash
cd packet-drift
```

**3. Navigate to the src directory**
```bash
cd src
```

**4. Compile all Java files**
```bash
javac -d . src\*.java src\board\*.java src\graph\*.java src\movement\*.java src\player\*.java src\cpu\*.java src\ui\*.java
```

**5. Run the game**
```bash
cd ..
java src.Game
```

## 📁 Project Structure (Review-1)
```bash
src/
 ├── Game.java
 ├── board/
 ├── graph/
 ├── movement/
 ├── player/
 └── cpu/
```

## 📌 Notes

- Requires Java JDK 8 or above
- Designed for DAA Review-1
- Focuses on Game Model, Graph Representation, and Greedy Algorithm