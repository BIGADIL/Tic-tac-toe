package listeners;

import board.ImplBoard;
import enums.CellType;
import enums.WinnerType;

public class EndOfGameDetector {

    public WinnerType detectWinner(final ImplBoard board) {
        final boolean isNoughtsWin = isWin(CellType.NOUGHTS, board);
        final boolean isCrossesWin = isWin(CellType.CROSSES, board);
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

    private boolean isWin(final CellType cellType, final ImplBoard board) {
        final boolean isFirstCol = board.getCell(0, 0) == cellType && board.getCell(1,  0) == cellType && board.getCell(2, 0) == cellType;
        final boolean isSecondCol = board.getCell(0, 1) == cellType && board.getCell(1,  1) == cellType && board.getCell(2, 1) == cellType;
        final boolean isThirdCol = board.getCell(0, 2) == cellType && board.getCell(1,  2) == cellType && board.getCell(2, 2) == cellType;

        final boolean isFirstRow = board.getCell(0, 0) == cellType && board.getCell(0,  1) == cellType && board.getCell(0, 2) == cellType;
        final boolean isSecondRow = board.getCell(1, 0) == cellType && board.getCell(1,  1) == cellType && board.getCell(1, 2) == cellType;
        final boolean isThirdRow = board.getCell(2, 0) == cellType && board.getCell(2,  1) == cellType && board.getCell(2, 2) == cellType;

        final boolean isHauptdiagonale = board.getCell(0, 0) == cellType && board.getCell(1,  1) == cellType && board.getCell(2, 2) == cellType;
        final boolean isRevertHauptdiagonale = board.getCell(2, 0) == cellType && board.getCell(1,  1) == cellType && board.getCell(0, 2) == cellType;

        return isFirstCol || isSecondCol || isThirdCol || isFirstRow || isSecondRow || isThirdRow || isHauptdiagonale || isRevertHauptdiagonale;
    }
}
