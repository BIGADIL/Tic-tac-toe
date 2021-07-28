package aiagents;

import answer.AIAnswer;
import board.Coord;
import board.ImplBoard;
import enums.CellType;

import java.util.*;

public class RandomAgent extends BaseAgent {
    public static class RandomAgentFactory extends BaseAgentFactory {
        @Override
        public RandomAgent createAgent(final CellType agentCellType) {
            final String name = "RandomAgent-" + botIdx++;
            return new RandomAgent(name, agentCellType);
        }
    }

    private final Random rnd = new Random();

    public RandomAgent(final String name,
                       final CellType agentCellType) {
        super(name, agentCellType);
    }

    @Override
    public AIAnswer getAnswer(final ImplBoard board) {
        final List<Coord> coordToAct = board.getAllPossibleCoordToAct();
        final int randomIdx = rnd.nextInt(coordToAct.size());
        final Coord coord = coordToAct.get(randomIdx);
        return new AIAnswer(coord.x, coord.y, agentCellType);
    }
}
