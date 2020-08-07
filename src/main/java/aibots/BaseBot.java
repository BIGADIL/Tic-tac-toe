package aibots;

import aibots.common.WinCollector;
import enums.CellType;
import enums.WinnerType;


public abstract class BaseBot implements IBot {
    public abstract static class BaseBotFactory implements IBotFactory {
        protected int botIdx = 0;

        public abstract BaseBot createBot(final CellType botCellType);
    }

    protected static final double EPS = 1.0e-9;

    protected final String name;
    protected final CellType botCellType;
    protected final WinnerType botWinnerType;

    protected int id = -1;
    protected int totalNodes = 0;
    protected int terminalNodes = 0;

    protected BaseBot(final String name,
                      final CellType botCellType) {
        if (!botCellType.isPlayingCellType()) {
            throw new IllegalArgumentException(botCellType + " is not playing");
        }
        this.name = name;
        this.botCellType = botCellType;
        botWinnerType = botCellType == CellType.CROSSES ? WinnerType.CROSSES : WinnerType.NOUGHTS;
    }

    @Override
    public void setId(final int id) {
        this.id = id;
    }

    @Override
    public CellType getBotCellType() {
        return botCellType;
    }

    @Override
    public String getName() {
        return name;
    }

    protected WinCollector distributeWin(final WinnerType winnerType) {
        terminalNodes++;
        totalNodes++;
        final WinCollector result;
        if (!winnerType.isTerminalWinnerType()) {
            throw new IllegalStateException(winnerType + " is not terminal WinnerType");
        }
        if (winnerType == WinnerType.DRAW) {
            result = new WinCollector(0.0D, 0.0D, 0.0D, 0.0D, 0.0D, 1.0D);
        } else if (winnerType == botWinnerType) {
            result = new WinCollector(1.0D, 0.0D, 0.0D, 0.0D, 1.0D, 0.0D);
        } else {
            result = new WinCollector(0.0D, 0.0D, -1.0D, 1.0D, 0.0D, 0.0D);
        }
        return result.catchBrokenProbabilities();
    }


}
