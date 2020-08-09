package aibots.common;

public class WinCollector {
    private static final double EPS = 1e-9;

    double win;
    double drawWin;
    double looseWin;
    double pLoose;
    double pWin;
    double pDraw;

    public WinCollector(final double win,
                 final double drawWin,
                 final double looseWin,
                 final double pLoose,
                 final double pWin,
                 final double pDraw) {
        this.win = win;
        this.drawWin = drawWin;
        this.looseWin = looseWin;
        this.pLoose = pLoose;
        this.pWin = pWin;
        this.pDraw = pDraw;
    }

    private WinCollector(final WinCollector w) {
        win = w.win;
        drawWin = w.drawWin;
        looseWin = w.looseWin;
        pLoose = w.pLoose;
        pWin = w.pWin;
        pDraw = w.pDraw;
    }

    public WinCollector getCopy() {
        return new WinCollector(this);
    }

    public void add(final WinCollector w, final double p) {
        win += p * w.win;
        drawWin += p * w.drawWin;
        looseWin += p * w.looseWin;
        pLoose += p * w.pLoose;
        pWin += p * w.pWin;
        pDraw += p * w.pDraw;
    }

    public double getTotalWin() {
        return win + drawWin + looseWin;
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
        return String.format("totalWin = %.2f win=%.2f, drawWin=%.2f, looseWin=%.2f, pLoose=%.2f, pWin=%.2f, pDraw=%.2f",
                getTotalWin(),
                win,
                drawWin,
                looseWin,
                pLoose,
                pWin,
                pDraw);
    }
}