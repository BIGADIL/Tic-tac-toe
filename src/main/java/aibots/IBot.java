package aibots;

import answer.AIAnswer;
import board.ImplBoard;
import enums.CellType;

public interface IBot {
    @FunctionalInterface
    interface IBotFactory {
        IBot createBot(final CellType botCellType);
    }

    AIAnswer getAnswer(final ImplBoard board);

    void setId(final int id);

    CellType getBotCellType();

    String getName();
}