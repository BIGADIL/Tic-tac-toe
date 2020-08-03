package ai;

import board.Board;
import enums.CellType;

import java.util.LinkedList;
import java.util.List;


public abstract class BaseBot implements IBot {

    public final int id;

    public final String name;

    public final CellType myType;

    protected BaseBot(final int id, final String name,
                      final CellType myType) {
        this.id = id;
        this.name = name;
        this.myType = myType;
    }

    protected static class Coord {
        public final int x;
        public final int y;

        public Coord(final int x, final int y) {
            this.x = x;
            this.y = y;
        }
    }

    protected List<Coord> getAllPossibleCoordToAct(final Board board) {
        final List<Coord> result = new LinkedList<>();
        for (int i = 0; i < Board.BOARD_SIZE; i++) {
            for (int j = 0; j < Board.BOARD_SIZE; j++) {
                if (board.getCell(i, j) == CellType.EMPTY) {
                    result.add(new Coord(i, j));
                }
            }
        }
        return result;
    }
}
