package ai;

import answer.AIAnswer;
import board.Board;
import enums.CellType;

import java.util.*;

public class RandomBot extends BaseBot {

    private final Random rnd = new Random();

    public RandomBot(final int id,
                     final String name,
                     final CellType myType) {
        super(id, name, myType);
    }

    @Override
    public AIAnswer getAnswer(final Board board) {
        final List<Coord> coordToAct = getAllPossibleCoordToAct(board);
        final int randomIdx = rnd.nextInt(coordToAct.size());
        final Coord coord = coordToAct.get(randomIdx);
        return new AIAnswer(coord.x, coord.y, myType);
    }
}
