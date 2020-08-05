package aibots;

import answer.AIAnswer;
import board.ImplBoard;
import enums.CellType;

import java.util.*;

public class RandomBot extends BaseBot {
    public static class RandomBotFactory extends BaseBotFactory {
        @Override
        public RandomBot createBot(final CellType botCellType) {
            final String name = "RandomBot-" + botIdx++;
            return new RandomBot(name, botCellType);
        }
    }

    public static final IBotFactory factory = new RandomBotFactory();

    private final Random rnd = new Random();

    public RandomBot(final String name,
                     final CellType myType) {
        super(name, myType);
    }

    @Override
    public AIAnswer getAnswer(final ImplBoard board) {
        final List<Coord> coordToAct = getAllPossibleCoordToAct(board);
        final int randomIdx = rnd.nextInt(coordToAct.size());
        final Coord coord = coordToAct.get(randomIdx);
        return new AIAnswer(coord.x, coord.y, botCellType);
    }
}
