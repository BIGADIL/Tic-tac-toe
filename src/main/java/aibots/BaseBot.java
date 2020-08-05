package aibots;

import board.ImplBoard;
import enums.CellType;
import enums.WinnerType;

import java.util.LinkedList;
import java.util.List;


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

    protected static class Coord {
        public final int x;
        public final int y;

        public Coord(final int x, final int y) {
            this.x = x;
            this.y = y;
        }
    }

    protected List<Coord> getAllPossibleCoordToAct(final ImplBoard board) {
        final List<Coord> result = new LinkedList<>();
        for (int i = 0; i < ImplBoard.BOARD_SIZE; i++) {
            for (int j = 0; j < ImplBoard.BOARD_SIZE; j++) {
                if (board.getCell(i, j) == CellType.EMPTY) {
                    result.add(new Coord(i, j));
                }
            }
        }
        return result;
    }

    protected WinCollector distributeWin(final WinnerType winnerType) {
        terminalNodes++;
        totalNodes++;
        final WinCollector result;
        if (!winnerType.isTerminalWinnerType()) {
            throw new IllegalStateException(winnerType + " is not terminal WinnerType");
        }
        if (winnerType == WinnerType.DRAW) {
            result = new WinCollector(0.0D, 0.5D, 0.0D, 0.0D, 1.0D);
        } else if (winnerType == botWinnerType) {
            result = new WinCollector(1.0D, 0.0D, 0.0D, 1.0D, 0.0D);
        } else {
            result = new WinCollector(0.0D, 0.0D, 1.0D, 0.0D, 0.0D);
        }
        return result.catchBrokenProbabilities();
    }

    protected static class WinCollector {
        double win;
        double drawWin;
        double pLoose;
        double pWin;
        double pDraw;

        WinCollector(final double win,
                     final double drawWin,
                     final double pLoose,
                     final double pWin,
                     final double pDraw) {
            this.win = win;
            this.drawWin = drawWin;
            this.pLoose = pLoose;
            this.pWin = pWin;
            this.pDraw = pDraw;
        }

        public void add(final WinCollector w, final double p) {
            win += p * w.win;
            drawWin += p * w.drawWin;
            pLoose += p * w.pLoose;
            pWin += p * w.pWin;
            pDraw += p * w.pDraw;
        }

        public double getTotalWin() {
            return win + drawWin;
        }

        public WinCollector catchBrokenProbabilities() {
            final double sumP = pDraw + pWin + pLoose;
            if (Math.abs(sumP - 1.0D) > EPS) {
                throw new IllegalStateException("Sum proba != 1: " + sumP);
            }
            return this;
        }

        @Override
        public String toString() {
            return String.format("totalWin = %.2f win=%.2f, drawWin=%.2f, pLoose=%.2f, pWin=%.2f, pDraw=%.2f",
                    getTotalWin(),
                    win,
                    drawWin,
                    pLoose,
                    pWin,
                    pDraw);
        }
    }
}
