package com.resimulators.simukraft.utils;

import com.google.common.collect.Maps;

import java.util.Map;

public enum Icons {
    HEART(52, 0, 9, 9, 52, 9, 9, 9, "a"),
    HALF_HEART(61, 0, 9, 9, 52, 9, 9, 9, "b"),
    EMPTY_HEART(52, 9, 9, 9, "c"),
    ARMOR(34, 9, 9, 9, "d"),
    HALF_ARMOR(25, 9, 9, 9, "e"),
    EMPTY_ARMOR(16, 9, 9, 9, "f"),
    HUNGER(52, 27, 9, 9, "g"),
    HALF_HUNGER(61, 27, 9, 9, "h"),
    EMPTY_HUNGER(16, 27, 9, 9, "i"),
    ABSORPTION_HEART(0, 160, 9, 9, "j"),
    HALF_ABSORPTION_HEART(0, 169, 9, 9, "k"),
    EXPERIENCE_BUBBLE(25, 18, 9, 9, "x");

    private final static Map<String, Icons> ELEMENTS = Maps.newHashMap();

    static {
        for (Icons icon : Icons.values()) {
            ELEMENTS.put(icon.symbol, icon);
        }
    }

    public final int u, v, su, sv;
    public final int bu, bv, bsu, bsv;
    public final String symbol;

    public static Icons bySymbol(String s) {
        return ELEMENTS.getOrDefault(s, Icons.EXPERIENCE_BUBBLE);
    }

    Icons(int u, int v, int su, int sv, String symbol) {
        this(u, v, su, sv, -1, -1, -1, -1, symbol);
    }

    Icons(int u, int v, int su, int sv, int bu, int bv, int bsu, int bsv, String symbol) {
        this.u = u;
        this.v = v;
        this.su = su;
        this.sv = sv;
        this.bu = bu;
        this.bv = bv;
        this.bsu = bsu;
        this.bsv = bsv;
        this.symbol = symbol;
    }
}
