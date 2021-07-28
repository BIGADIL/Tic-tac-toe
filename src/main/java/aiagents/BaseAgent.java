package aiagents;

import enums.CellType;
import enums.WinnerType;


public abstract class BaseAgent implements IAgent {
    public abstract static class BaseAgentFactory implements IAgentFactory {
        protected int botIdx = 0;

        public abstract BaseAgent createAgent(final CellType agentCellType);
    }

    protected final String name;
    protected final CellType agentCellType;
    protected final WinnerType agentWinnerType;

    protected int id = -1;

    protected BaseAgent(final String name,
                        final CellType agentCellType) {
        if (!agentCellType.isPlayingCellType()) {
            throw new IllegalArgumentException(agentCellType + " is not playing");
        }
        this.name = name;
        this.agentCellType = agentCellType;
        agentWinnerType = agentCellType == CellType.CROSSES ? WinnerType.CROSSES : WinnerType.NOUGHTS;
    }

    @Override
    public void setId(final int id) {
        this.id = id;
    }

    @Override
    public CellType getAgentCellType() {
        return agentCellType;
    }

    @Override
    public String getName() {
        return name;
    }
}
