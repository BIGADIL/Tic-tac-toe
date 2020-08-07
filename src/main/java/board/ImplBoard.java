package board;

import answer.AIAnswer;
import enums.CellType;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class ImplBoard {

    private final CellType[][] cells;
    private int nextPlayerId = 0;
    private int numEmptyCells;
    private final int boardSize;

    public ImplBoard(final int boardSize) {
        this.boardSize = boardSize;
        cells = new CellType[boardSize][boardSize];
        for (final CellType[] cell : cells) {
            Arrays.fill(cell, CellType.EMPTY);
        }
        numEmptyCells = boardSize * boardSize;
    }

    private ImplBoard(final ImplBoard board) {
        boardSize = board.boardSize;
        cells = new CellType[boardSize][boardSize];
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

    public List<Coord> getAllPossibleCoordToAct() {
        final List<Coord> result = new LinkedList<>();
        for (int i = 0; i < cells.length; i++) {
            for (int j = 0; j < cells.length; j++) {
                if (cells[i][j] == CellType.EMPTY) {
                    result.add(new Coord(i, j));
                }
            }
        }
        return result;
    }

    public int size() {
        return boardSize;
    }
}
