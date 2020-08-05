package enums;

import java.util.EnumSet;
import java.util.Set;

public enum WinnerType {
    NONE,
    NOUGHTS,
    CROSSES,
    DRAW,
    ;

    public boolean isTerminalWinnerType() {
        return terminalWinnerTypesSet.contains(this);
    }

    private static final Set<WinnerType> terminalWinnerTypesSet = EnumSet.of(NOUGHTS, CROSSES, DRAW);
}