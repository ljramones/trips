package com.teamgannon.trips.noisegen;

// MIT License
//
// Copyright(c) 2023 Jordan Peck (jordan.me2@gmail.com)
// Copyright(c) 2023 Contributors
//
// Permission is hereby granted, free of charge, to any person obtaining a copy
// of this software and associated documentation files(the "Software"), to deal
// in the Software without restriction, including without limitation the rights
// to use, copy, modify, merge, publish, distribute, sublicense, and / or sell
// copies of the Software, and to permit persons to whom the Software is
// furnished to do so, subject to the following conditions :
//
// The above copyright notice and this permission notice shall be included in all
// copies or substantial portions of the Software.
//
// THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
// IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
// FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.IN NO EVENT SHALL THE
// AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
// LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
// OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
// SOFTWARE.

import static com.teamgannon.trips.noisegen.NoiseGradients.*;

/**
 * Static utility methods for noise generation.
 * Includes fast math functions, interpolation, hashing, and gradient computations.
 */
public final class NoiseUtils {

    private NoiseUtils() {
        // Utility class, no instantiation
    }

    // Prime constants for hashing
    public static final int PrimeX = 501125321;
    public static final int PrimeY = 1136930381;
    public static final int PrimeZ = 1720413743;
    public static final int PrimeW = 1183186591;

    // ==================== Fast Math Functions ====================

    public static float FastMin(float a, float b) {
        return a < b ? a : b;
    }

    public static float FastMax(float a, float b) {
        return a > b ? a : b;
    }

    public static float FastAbs(float f) {
        return f < 0 ? -f : f;
    }

    public static float FastSqrt(float f) {
        return (float) Math.sqrt(f);
    }

    public static int FastFloor(float f) {
        return f >= 0 ? (int) f : (int) f - 1;
    }

    public static int FastRound(float f) {
        return f >= 0 ? (int) (f + 0.5f) : (int) (f - 0.5f);
    }

    // ==================== Interpolation Functions ====================

    public static float Lerp(float a, float b, float t) {
        return a + t * (b - a);
    }

    public static float InterpHermite(float t) {
        return t * t * (3 - 2 * t);
    }

    public static float InterpQuintic(float t) {
        return t * t * t * (t * (t * 6 - 15) + 10);
    }

    public static float CubicLerp(float a, float b, float c, float d, float t) {
        float p = (d - c) - (a - b);
        return t * t * t * p + t * t * ((a - b) - p) + t * (c - a) + b;
    }

    public static float PingPong(float t) {
        t -= (int) (t * 0.5f) * 2;
        return t < 1 ? t : 2 - t;
    }

    // ==================== Hashing Functions ====================

    public static int Hash(int seed, int xPrimed, int yPrimed) {
        int hash = seed ^ xPrimed ^ yPrimed;
        hash *= 0x27d4eb2d;
        return hash;
    }

    public static int Hash(int seed, int xPrimed, int yPrimed, int zPrimed) {
        int hash = seed ^ xPrimed ^ yPrimed ^ zPrimed;
        hash *= 0x27d4eb2d;
        return hash;
    }

    // ==================== Value Coordinate Functions ====================

    public static float ValCoord(int seed, int xPrimed, int yPrimed) {
        int hash = Hash(seed, xPrimed, yPrimed);
        hash *= hash;
        hash ^= hash << 19;
        return hash * (1 / 2147483648.0f);
    }

    public static float ValCoord(int seed, int xPrimed, int yPrimed, int zPrimed) {
        int hash = Hash(seed, xPrimed, yPrimed, zPrimed);
        hash *= hash;
        hash ^= hash << 19;
        return hash * (1 / 2147483648.0f);
    }

    // ==================== Gradient Coordinate Functions ====================

    public static float GradCoord(int seed, int xPrimed, int yPrimed, float xd, float yd) {
        int hash = Hash(seed, xPrimed, yPrimed);
        hash ^= hash >> 15;
        hash &= 127 << 1;

        float xg = Gradients2D[hash];
        float yg = Gradients2D[hash | 1];

        return xd * xg + yd * yg;
    }

    public static float GradCoord(int seed, int xPrimed, int yPrimed, int zPrimed, float xd, float yd, float zd) {
        int hash = Hash(seed, xPrimed, yPrimed, zPrimed);
        hash ^= hash >> 15;
        hash &= 63 << 2;

        float xg = Gradients3D[hash];
        float yg = Gradients3D[hash | 1];
        float zg = Gradients3D[hash | 2];

        return xd * xg + yd * yg + zd * zg;
    }
}
