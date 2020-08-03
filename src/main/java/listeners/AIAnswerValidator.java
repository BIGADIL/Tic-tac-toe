package listeners;

import answer.AIAnswer;
import board.Board;
import enums.CellType;

public class AIAnswerValidator {

    public void validateAnswer(final AIAnswer answer,
                               final Board board,
                               final CellType expectedCell) {
        final int x = answer.x;
        final int y = answer.y;
        final CellType cell = board.getCell(x, y);
        if (cell != CellType.EMPTY) {
            final String message = String.format("Can't perform act on non empty cell (%d, %d) = %s", x, y, cell);
            throw new IllegalStateException(message);
        }
        if (answer.cellType != expectedCell) {
            throw new IllegalStateException("Expect=" + expectedCell + ", actual=" + answer.cellType);
        }
    }
}
