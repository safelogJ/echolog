package com.safelogj.echolog;

public enum ColorsPalette {

    BLACK(0xFF000000),
    WHITE(0xFFFFFFFF),
    YELLOW(0xFFFFFF00),
    BLUE(0xFF0000FF),
    DARK_BLUE(0xFF003366),
    DARK_GREEN(0xFF006400),
    DARK_GRAY(0xFF333333),
    ORANGE(0xFFFFA500);

    private final int color;

    ColorsPalette(int color) {
        this.color = color;
    }

    public int getColor() {
        return color;
    }
}
