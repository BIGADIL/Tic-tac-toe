package ai;

import answer.AIAnswer;
import board.Board;
import enums.CellType;
import enums.WinnerType;
import listeners.EndOfGameDetector;

import java.util.*;

public class SimBot extends BaseBot {

    private final Map<WinnerType, CellType> map = new EnumMap<>(WinnerType.class);

    private final EndOfGameDetector eogDetector = new EndOfGameDetector();

    private static final class AnswerAndWin implements Comparable<AnswerAndWin> {
        final AIAnswer answer;
        final WinCollector winCollector;

        private AnswerAndWin(final AIAnswer answer, final WinCollector winCollector) {
            this.answer = answer;
            this.winCollector = winCollector;
        }

        @Override
        public int compareTo(final AnswerAndWin o) {
            return -Double.compare(winCollector.win, o.winCollector.win);
        }

        @Override
        public boolean equals(final Object obj) {
            if (!(obj instanceof AnswerAndWin)) return false;
            return compareTo((AnswerAndWin) obj) == 0;
        }

        @Override
        public int hashCode() {
            return Objects.hash(answer, winCollector);
        }
    }

    public SimBot(final int id, final String name, final CellType myType) {
        super(id, name, myType);
        map.put(WinnerType.CROSSES, CellType.CROSSES);
        map.put(WinnerType.NOUGHTS, CellType.NOUGHTS);
    }

    @Override
    public AIAnswer getAnswer(final Board board) {
        final List<Coord> coordToAct = getAllPossibleCoordToAct(board);
        final List<AnswerAndWin> aw = new ArrayList<>();
        for (final Coord coord : coordToAct) {
            final AIAnswer aiAnswer = new AIAnswer(coord.x, coord.y, myType);
            final Board copy = board.getCopy();
            copy.act(aiAnswer);
            final WinCollector winCollector = getWinByGameTree(copy);
            aw.add(new AnswerAndWin(aiAnswer, winCollector));
        }
        Collections.sort(aw);
        return aw.get(0).answer;
    }

    private WinCollector getWinByGameTree(final Board board) {
        final WinnerType winnerType = eogDetector.detectWinner(board);
        if (winnerType != WinnerType.NONE) {
            return distributeWin(winnerType);
        }
        final var nextPlayerId = board.getNextPlayerId();
        final List<Coord> coordToAct = getAllPossibleCoordToAct(board);
        if (nextPlayerId == id) {
            final List<AnswerAndWin> aw = new ArrayList<>();
            for (final Coord coord : coordToAct) {
                final AIAnswer aiAnswer = new AIAnswer(coord.x, coord.y, myType);
                final Board copy = board.getCopy();
                copy.act(aiAnswer);
                final WinCollector winCollector = getWinByGameTree(copy);
                aw.add(new AnswerAndWin(aiAnswer, winCollector));
            }
            Collections.sort(aw);
            return aw.get(0).winCollector;
        } else {
            final List<WinCollector> winCollectors = new ArrayList<>();
            for (final Coord coord : coordToAct) {
                final AIAnswer aiAnswer = new AIAnswer(coord.x, coord.y, CellType.getRevertType(myType));
                final Board copy = board.getCopy();
                copy.act(aiAnswer);
                final WinCollector winCollector = getWinByGameTree(copy);
                winCollectors.add(winCollector);
            }
            return getMixedDecision(winCollectors);
        }
    }

    private WinCollector getMixedDecision(final List<WinCollector> winCollectors) {
        final double sequenceLength = (double) winCollectors.size();
        final double p = 1.0D / sequenceLength;
        final WinCollector mixedWin = new WinCollector(0.0D);
        for (final WinCollector winCollector : winCollectors) {
            mixedWin.add(winCollector, p);
        }
        return mixedWin;
    }

    private WinCollector distributeWin(final WinnerType winnerType) {
        if (winnerType == WinnerType.DRAW) {
            return new WinCollector(IWinCollector.WIN_FOR_DRAW);
        }
        final CellType winnerCellType = map.get(winnerType);
        if (winnerCellType == myType) {
            return new WinCollector(IWinCollector.WIN_FOR_WIN);
        }
        return new WinCollector(IWinCollector.WIN_FOR_LOOSE);
    }

    interface IWinCollector {
        double WIN_FOR_DRAW = 0.5D;

        double WIN_FOR_WIN = 1.0D;

        double WIN_FOR_LOOSE = 0.0D;

        void add(final IWinCollector w, final double p);

        double getWin();
    }

    private static class WinCollector implements IWinCollector{
        double win;

        WinCollector(final double win) {
            this.win = win;
        }

        @Override
        public void add(final IWinCollector w, final double p) {
            win += p * w.getWin();
        }

        @Override
        public double getWin() {
            return win;
        }
    }
}
