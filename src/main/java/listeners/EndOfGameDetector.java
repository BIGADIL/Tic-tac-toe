package listeners;

import board.ImplBoard;
import enums.CellType;
import enums.WinnerType;

public class EndOfGameDetector {

    public WinnerType detectWinner(final ImplBoard board) {
        final boolean isNoughtsWin = isWin(board, CellType.NOUGHTS);
        final boolean isCrossesWin = isWin(board, CellType.CROSSES);
        if (isCrossesWin && isNoughtsWin) {
            throw new IllegalStateException(String.format("isNoughtsWin=%s, isCrossesWin=%s", isNoughtsWin, isCrossesWin));
        }
        if (isCrossesWin) {
            return WinnerType.CROSSES;
        } else if (isNoughtsWin) {
            return WinnerType.NOUGHTS;
        } else if(board.isFullBoard()) {
            return WinnerType.DRAW;
        } else {
            return WinnerType.NONE;
        }
    }

    private boolean isWin(final ImplBoard board, final CellType cellType) {
        boolean isDiagonalWin = true;
        boolean isRevertDiagonalWin = true;
        final int boardSize = board.size();
        for (int i = 0; i < boardSize; i++) {
            isDiagonalWin &= board.getCell(i, i) == cellType;
            isRevertDiagonalWin &= board.getCell(i, boardSize - i - 1) == cellType;
            boolean isVerticalWin = true;
            boolean isHorizontalWin = true;
            for (int j = 0; j < boardSize; j++) {
                isHorizontalWin &= board.getCell(i, j) == cellType;
                isVerticalWin &= board.getCell(j, i) == cellType;
            }
            if (isHorizontalWin || isVerticalWin) {
                return true;
            }
        }
        return isDiagonalWin || isRevertDiagonalWin;
    }
}
