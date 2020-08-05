package board;

import answer.AIAnswer;
import enums.CellType;

import java.util.Arrays;

public class ImplBoard {
    public static final int BOARD_SIZE = 3;
    private final CellType[][] cells = new CellType[BOARD_SIZE][BOARD_SIZE];
    private int nextPlayerId = 0;
    private int numEmptyCells;

    public ImplBoard() {
        for (final CellType[] cell : cells) {
            Arrays.fill(cell, CellType.EMPTY);
        }
        numEmptyCells = BOARD_SIZE * BOARD_SIZE;
    }

    private ImplBoard(final ImplBoard board) {
        for (int i = 0; i < board.cells.length; i++) {
            System.arraycopy(board.cells[i], 0, cells[i], 0, board.cells[i].length);
        }
        nextPlayerId = board.nextPlayerId;
        numEmptyCells = board.numEmptyCells;
    }

    public ImplBoard getCopy(final AIAnswer answer) {
        final ImplBoard copy = getCopy();
        copy.act(answer);
        return copy;
    }

    public ImplBoard getCopy() {
        return new ImplBoard(this);
    }

    public void act(final AIAnswer answer) {
        final int x = answer.x;
        final int y = answer.y;
        cells[x][y] = answer.cellType;
        numEmptyCells--;
        nextPlayerId = (nextPlayerId + 1) % 2;
    }

    public CellType getCell(final int x, final int y) {
        return cells[x][y];
    }

    public int getNextPlayerId() {
        return nextPlayerId;
    }

    public boolean isFullBoard() {
        return numEmptyCells == 0;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        for (final CellType[] cellsRow : cells) {
            sb.append('|');
            for (final CellType cell : cellsRow) {
                sb.append(cell.code).append('|');
            }
            sb.append('\n');
        }
        return sb.toString();
    }
}
