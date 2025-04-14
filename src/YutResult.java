import java.util.Random;

enum YutResult {
    BACKDO(-1, false), DO(1, false), GAE(2, false), GEOL(3, false), YUT(4, true), MO(5, true);

    private final int stepCount;
    private final boolean extra;

    YutResult(int stepCount, boolean extra) {
        this.stepCount = stepCount;
        this.extra = extra;
    }

    public int getStepCount() {
        return stepCount;
    }

    public boolean grantsExtraThrow() {
        return extra;
    }

    public static YutResult throwYut(Random rand) {
        int r = rand.nextInt(32);
        if (r == 0) return BACKDO;
        if (r <= 7) return DO;
        if (r <= 15) return GAE;
        if (r <= 23) return GEOL;
        if (r <= 28) return YUT;
        return MO;
    }

    @Override
    public String toString() {
        switch(this) {
            case BACKDO: return "빽도";
            case DO:     return "도";
            case GAE:    return "개";
            case GEOL:   return "걸";
            case YUT:    return "윷";
            case MO:     return "모";
            default:     return super.toString();
        }
    }
}

