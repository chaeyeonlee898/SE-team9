//YutResult.java
package model;

import java.util.Random;

public enum YutResult {
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
        int r = rand.nextInt(64);
        if (r == 0) return BACKDO;
        if (r <= 16) return DO;
        if (r <= 40) return GAE;
        if (r <= 56) return GEOL;
        if (r <= 60) return YUT;
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

    public static YutResult fromBits(int bits) {
        int count = Integer.bitCount(bits);
        switch (count) {
            case 4: return YUT;
            case 0: return MO;
            case 1: return DO;
            case 2: return GAE;
            case 3: return GEOL;
            default: return BACKDO;  // 이론상 여기로 오진 않습니다.
        }
    }
}

