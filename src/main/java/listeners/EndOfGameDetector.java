package listeners;

import board.Board;
import enums.CellType;
import enums.WinnerType;

public class EndOfGameDetector {

    public WinnerType detectWinner(final Board board) {
        if (isWin(CellType.NOUGHTS, board)) {
            return WinnerType.NOUGHTS;
        } else if (isWin(CellType.CROSSES, board)) {
            return WinnerType.CROSSES;
        } else if (isDraw(board)) {
            return WinnerType.DRAW;
        }
        return WinnerType.NONE;
    }

    public boolean isWin(final CellType cellType, final Board board) {
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

    public boolean isDraw(final Board board) {
        for (int i = 0; i < Board.BOARD_SIZE; i++) {
            for (int j = 0; j < Board.BOARD_SIZE; j++) {
                if (board.getCell(i, j) == CellType.EMPTY) {
                    return false;
                }
            }
        }
        return true;
    }
}
