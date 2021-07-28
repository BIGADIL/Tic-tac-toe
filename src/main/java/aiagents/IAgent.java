package aiagents;

import answer.AIAnswer;
import board.ImplBoard;
import enums.CellType;

public interface IAgent {
    @FunctionalInterface
    interface IAgentFactory {
        IAgent createAgent(final CellType agentCellType);
    }

    AIAnswer getAnswer(final ImplBoard board);

    void setId(final int id);

    CellType getAgentCellType();

    String getName();
}