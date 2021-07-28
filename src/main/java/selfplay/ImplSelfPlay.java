package selfplay;

import aiagents.*;
import answer.AIAnswer;
import board.ImplBoard;
import enums.CellType;
import enums.WinnerType;
import listeners.AIAnswerValidator;
import listeners.EndOfGameDetector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class ImplSelfPlay implements ISelfPlay {

    private static final int NUM_PLAYERS = 2;
    private static final Logger logger = LoggerFactory.getLogger(ImplSelfPlay.class);

    private final IAgent[] agents;
    private final EndOfGameDetector eogDetector;
    private final AIAnswerValidator validator;
    private final int boardSize;

    private ImplBoard board;
    private final Map<IAgent, Integer> winRate;
    private int numDraws;
    private boolean isFirstGame;


    public ImplSelfPlay(final IAgent.IAgentFactory[] factories, final int boardSize) {
        if (factories.length != NUM_PLAYERS) {
            throw new IllegalStateException("Factories length " + factories.length + " != " + NUM_PLAYERS);
        }
        agents = new IAgent[NUM_PLAYERS];
        agents[0] = factories[0].createAgent(CellType.NOUGHTS);
        agents[1] = factories[1].createAgent(CellType.CROSSES);
        this.boardSize = boardSize;
        board = new ImplBoard(boardSize);
        numDraws = 0;
        eogDetector = new EndOfGameDetector();
        validator = new AIAnswerValidator();
        winRate = new HashMap<>();
        for (final IAgent bot : agents) {
            winRate.put(bot, 0);
        }
        isFirstGame = true;
    }

    @Override
    public void playSomeGames(final int numGames) {
        for (int i = 0; i < numGames; i++) {
            prepareBotsForPlay();
            gameLoop();
        }
        printResults();
    }

    private void prepareBotsForPlay() {
        if (isFirstGame) {
            isFirstGame = false;
            agents[0].setId(0);
            agents[1].setId(1);
            return;
        }
        final IAgent tmpBot = agents[0];
        agents[0] = agents[1];
        agents[1] = tmpBot;
        agents[0].setId(0);
        agents[1].setId(1);
        board = new ImplBoard(boardSize);
    }

    private void gameLoop() {
        while (true) {
            for (final IAgent bot : agents) {
                final AIAnswer answer = bot.getAnswer(board.getCopy());
                validator.validateAnswer(answer, board, bot.getAgentCellType());
                board.act(answer);
                final WinnerType winnerType = eogDetector.detectWinner(board);
                switch (winnerType) {
                    case DRAW:
                        numDraws++;
                        return;
                    case NONE:
                        break;
                    case CROSSES:
                        final IAgent crossWinnerBot = findWinnerByCellType(CellType.CROSSES);
                        winRate.merge(crossWinnerBot, 1, Integer::sum);
                        return;
                    case NOUGHTS:
                        final IAgent noughtsWinnerBot = findWinnerByCellType(CellType.NOUGHTS);
                        winRate.merge(noughtsWinnerBot, 1, Integer::sum);
                        return;
                    default:
                        throw new IllegalArgumentException("Unexpected type = " + winnerType);
                }
            }
        }
    }

    private IAgent findWinnerByCellType(final CellType cellType) {
        return Arrays.stream(agents).filter(bot -> bot.getAgentCellType() == cellType).findFirst().orElseThrow();
    }

    private void printResults() {
        final double percentFactor = 100.0D;
        final int numGames = winRate.values().stream().mapToInt(Integer::intValue).sum() + numDraws;
        winRate.keySet().forEach(bot -> {
            final double winRatePercent = winRate.get(bot) / (double) numGames * percentFactor;
            logger.info("{}'s win rate = {} %", bot.getName(), winRatePercent);
        });
        final double drawPercent = numDraws / (double) numGames * percentFactor;
        logger.info("Draw={} %", drawPercent);
    }


    public static void main(final String[] args) {
        final IAgent.IAgentFactory[] factories = {
                new RandomAgent.RandomAgentFactory(),
                new SimpleMinMaxAgent.SimpleMinMaxAgentFactory(),
        };
        final ISelfPlay selfPlay = new ImplSelfPlay(factories, 3);
        selfPlay.playSomeGames(100);
    }
}
