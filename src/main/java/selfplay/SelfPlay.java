package selfplay;

import ai.BaseBot;
import ai.RandomBot;
import ai.SimBot;
import answer.AIAnswer;
import board.Board;
import enums.CellType;
import enums.WinnerType;
import listeners.AIAnswerValidator;
import listeners.EndOfGameDetector;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

public class SelfPlay {

    private final BaseBot[] bots = new BaseBot[2];

    private final BaseBot[] botsToPlay = new BaseBot[2];

    private int firstPlayerId = 0;

    private final EndOfGameDetector eogDetector = new EndOfGameDetector();

    private final AIAnswerValidator validator = new AIAnswerValidator();

    private Board board;

    private final Map<CellType, BaseBot> cellsOnBots = new EnumMap<>(CellType.class);

    private final Map<BaseBot, Integer> winRate = new HashMap<>();

    public SelfPlay() {
        bots[0] = new RandomBot(0, "RandomBot", CellType.CROSSES);
        bots[1] = new SimBot(1, "SimBot", CellType.NOUGHTS);
        for (final BaseBot bot : bots) {
            cellsOnBots.put(bot.myType, bot);
        }
        board = new Board();
    }

    public void prepareBotsForPlay() {
        botsToPlay[0] = bots[firstPlayerId];
        botsToPlay[1] = bots[(firstPlayerId + 1) % 2];
        firstPlayerId = (firstPlayerId + 1) % 2;
        board = new Board();
    }

    public void gameLoop() {
        while (true) {
            for (final BaseBot bot : botsToPlay) {
                final AIAnswer answer = bot.getAnswer(board);
                validator.validateAnswer(answer, board, bot.myType);
                board.act(answer);
                final WinnerType winnerType = eogDetector.detectWinner(board);
                switch (winnerType) {
                    case DRAW -> {
                        return;
                    }
                    case NONE -> {

                    }
                    case CROSSES -> {
                        final BaseBot winnerBot = cellsOnBots.get(CellType.CROSSES);
                        winRate.merge(winnerBot, 1, Integer::sum);
                        return;
                    }
                    case NOUGHTS -> {
                        final BaseBot winnerBot = cellsOnBots.get(CellType.NOUGHTS);
                        winRate.merge(winnerBot, 1, Integer::sum);
                        return;
                    }
                    default -> throw new IllegalArgumentException("Unexpected type = " + winnerType);
                }
            }
        }
    }

    public static void main(final String[] args) {
        final SelfPlay selfPlay = new SelfPlay();
        final int numGames = 1000;
        for (int i = 0; i < numGames; i++) {
            selfPlay.prepareBotsForPlay();
            selfPlay.gameLoop();
        }
        double draw = numGames;
        for (final Map.Entry<BaseBot, Integer> e : selfPlay.winRate.entrySet()) {
            final double winRate = e.getValue() / (double) numGames  * 100;
            draw -= e.getValue();
            System.out.println(String.format("%s's win rate = %.2f %%", e.getKey().name, winRate));
        }
        draw = draw / numGames * 100;
        System.out.println(String.format("Draw=%.2f %%", draw));
    }
}
