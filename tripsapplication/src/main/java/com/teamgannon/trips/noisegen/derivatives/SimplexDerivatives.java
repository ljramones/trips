package com.teamgannon.trips.noisegen.derivatives;

import static com.teamgannon.trips.noisegen.NoiseGradients.*;
import static com.teamgannon.trips.noisegen.NoiseUtils.*;

/**
 * Analytical derivative computation for simplex noise.
 *
 * <p>Computes exact gradients of simplex noise without numerical differentiation,
 * providing both the noise value and its partial derivatives in a single pass.
 *
 * <p>This is significantly faster than computing numerical derivatives (which
 * requires 2-6 additional noise samples depending on dimension).
 *
 * <p><b>Extension:</b> This class is not part of the original FastNoiseLite library.
 */
public class SimplexDerivatives {

    private final int seed;

    // Skewing factors for 2D simplex
    private static final float F2 = 0.5f * ((float) Math.sqrt(3.0) - 1.0f);
    private static final float G2 = (3.0f - (float) Math.sqrt(3.0)) / 6.0f;

    // Skewing factors for 3D simplex
    private static final float F3 = 1.0f / 3.0f;
    private static final float G3 = 1.0f / 6.0f;

    public SimplexDerivatives(int seed) {
        this.seed = seed;
    }

    /**
     * Evaluate 2D simplex noise with analytical derivatives.
     *
     * @param seed Noise seed
     * @param x X coordinate
     * @param y Y coordinate
     * @return NoiseWithGradient2D containing value and partial derivatives
     */
    public NoiseDerivatives.NoiseWithGradient2D evaluate2D(int seed, float x, float y) {
        // Skew input space to determine simplex cell
        float s = (x + y) * F2;
        int i = FastFloor(x + s);
        int j = FastFloor(y + s);

        // Unskew cell origin back to (x, y) space
        float t = (i + j) * G2;
        float X0 = i - t;
        float Y0 = j - t;

        // Distance from cell origin
        float x0 = x - X0;
        float y0 = y - Y0;

        // Determine which simplex we're in
        int i1, j1;
        if (x0 > y0) {
            i1 = 1;
            j1 = 0;
        } else {
            i1 = 0;
            j1 = 1;
        }

        // Offsets for other corners
        float x1 = x0 - i1 + G2;
        float y1 = y0 - j1 + G2;
        float x2 = x0 - 1.0f + 2.0f * G2;
        float y2 = y0 - 1.0f + 2.0f * G2;

        // Calculate contributions and derivatives from each corner
        float value = 0f;
        float dx = 0f;
        float dy = 0f;

        // Corner 0
        float t0 = 0.5f - x0 * x0 - y0 * y0;
        if (t0 > 0) {
            int gi0 = gradientIndex2D(seed, i, j);
            float gx0 = Gradients2D[gi0];
            float gy0 = Gradients2D[gi0 + 1];

            float t02 = t0 * t0;
            float t04 = t02 * t02;
            float gdot0 = gx0 * x0 + gy0 * y0;

            value += t04 * gdot0;

            // Derivative contribution
            // d/dx[t^4 * (g.x)] = 4*t^3 * dt/dx * (g.x) + t^4 * g.x
            // dt/dx = -2*x0, so d/dx = t^3 * (-8*x0*(g.x) + t*g.x) = t^3 * (g.x*(t - 8*x0*x0) - 8*x0*y0*g.y)
            // Simplified: derivative = t^3 * (g - 8 * (g.x) * x)
            float t03 = t02 * t0;
            dx += t03 * (-8.0f * x0 * gdot0 + t0 * gx0);
            dy += t03 * (-8.0f * y0 * gdot0 + t0 * gy0);
        }

        // Corner 1
        float t1 = 0.5f - x1 * x1 - y1 * y1;
        if (t1 > 0) {
            int gi1 = gradientIndex2D(seed, i + i1, j + j1);
            float gx1 = Gradients2D[gi1];
            float gy1 = Gradients2D[gi1 + 1];

            float t12 = t1 * t1;
            float t14 = t12 * t12;
            float gdot1 = gx1 * x1 + gy1 * y1;

            value += t14 * gdot1;

            float t13 = t12 * t1;
            dx += t13 * (-8.0f * x1 * gdot1 + t1 * gx1);
            dy += t13 * (-8.0f * y1 * gdot1 + t1 * gy1);
        }

        // Corner 2
        float t2 = 0.5f - x2 * x2 - y2 * y2;
        if (t2 > 0) {
            int gi2 = gradientIndex2D(seed, i + 1, j + 1);
            float gx2 = Gradients2D[gi2];
            float gy2 = Gradients2D[gi2 + 1];

            float t22 = t2 * t2;
            float t24 = t22 * t22;
            float gdot2 = gx2 * x2 + gy2 * y2;

            value += t24 * gdot2;

            float t23 = t22 * t2;
            dx += t23 * (-8.0f * x2 * gdot2 + t2 * gx2);
            dy += t23 * (-8.0f * y2 * gdot2 + t2 * gy2);
        }

        // Scale to [-1, 1] range (approximate)
        float scale = 70.0f;
        return new NoiseDerivatives.NoiseWithGradient2D(
            value * scale,
            dx * scale,
            dy * scale
        );
    }

    /**
     * Evaluate 3D simplex noise with analytical derivatives.
     *
     * @param seed Noise seed
     * @param x X coordinate
     * @param y Y coordinate
     * @param z Z coordinate
     * @return NoiseWithGradient3D containing value and partial derivatives
     */
    public NoiseDerivatives.NoiseWithGradient3D evaluate3D(int seed, float x, float y, float z) {
        // Skew input space
        float s = (x + y + z) * F3;
        int i = FastFloor(x + s);
        int j = FastFloor(y + s);
        int k = FastFloor(z + s);

        // Unskew cell origin
        float t = (i + j + k) * G3;
        float X0 = i - t;
        float Y0 = j - t;
        float Z0 = k - t;

        // Distance from cell origin
        float x0 = x - X0;
        float y0 = y - Y0;
        float z0 = z - Z0;

        // Determine which simplex we're in
        int i1, j1, k1, i2, j2, k2;

        if (x0 >= y0) {
            if (y0 >= z0) {
                i1 = 1; j1 = 0; k1 = 0;
                i2 = 1; j2 = 1; k2 = 0;
            } else if (x0 >= z0) {
                i1 = 1; j1 = 0; k1 = 0;
                i2 = 1; j2 = 0; k2 = 1;
            } else {
                i1 = 0; j1 = 0; k1 = 1;
                i2 = 1; j2 = 0; k2 = 1;
            }
        } else {
            if (y0 < z0) {
                i1 = 0; j1 = 0; k1 = 1;
                i2 = 0; j2 = 1; k2 = 1;
            } else if (x0 < z0) {
                i1 = 0; j1 = 1; k1 = 0;
                i2 = 0; j2 = 1; k2 = 1;
            } else {
                i1 = 0; j1 = 1; k1 = 0;
                i2 = 1; j2 = 1; k2 = 0;
            }
        }

        // Offsets for remaining corners
        float x1 = x0 - i1 + G3;
        float y1 = y0 - j1 + G3;
        float z1 = z0 - k1 + G3;

        float x2 = x0 - i2 + 2.0f * G3;
        float y2 = y0 - j2 + 2.0f * G3;
        float z2 = z0 - k2 + 2.0f * G3;

        float x3 = x0 - 1.0f + 3.0f * G3;
        float y3 = y0 - 1.0f + 3.0f * G3;
        float z3 = z0 - 1.0f + 3.0f * G3;

        float value = 0f;
        float dx = 0f;
        float dy = 0f;
        float dz = 0f;

        // Corner 0
        float t0 = 0.6f - x0 * x0 - y0 * y0 - z0 * z0;
        if (t0 > 0) {
            int gi0 = gradientIndex3D(seed, i, j, k);
            float gx0 = Gradients3D[gi0];
            float gy0 = Gradients3D[gi0 + 1];
            float gz0 = Gradients3D[gi0 + 2];

            float t02 = t0 * t0;
            float t04 = t02 * t02;
            float gdot0 = gx0 * x0 + gy0 * y0 + gz0 * z0;

            value += t04 * gdot0;

            float t03 = t02 * t0;
            dx += t03 * (-8.0f * x0 * gdot0 + t0 * gx0);
            dy += t03 * (-8.0f * y0 * gdot0 + t0 * gy0);
            dz += t03 * (-8.0f * z0 * gdot0 + t0 * gz0);
        }

        // Corner 1
        float t1 = 0.6f - x1 * x1 - y1 * y1 - z1 * z1;
        if (t1 > 0) {
            int gi1 = gradientIndex3D(seed, i + i1, j + j1, k + k1);
            float gx1 = Gradients3D[gi1];
            float gy1 = Gradients3D[gi1 + 1];
            float gz1 = Gradients3D[gi1 + 2];

            float t12 = t1 * t1;
            float t14 = t12 * t12;
            float gdot1 = gx1 * x1 + gy1 * y1 + gz1 * z1;

            value += t14 * gdot1;

            float t13 = t12 * t1;
            dx += t13 * (-8.0f * x1 * gdot1 + t1 * gx1);
            dy += t13 * (-8.0f * y1 * gdot1 + t1 * gy1);
            dz += t13 * (-8.0f * z1 * gdot1 + t1 * gz1);
        }

        // Corner 2
        float t2 = 0.6f - x2 * x2 - y2 * y2 - z2 * z2;
        if (t2 > 0) {
            int gi2 = gradientIndex3D(seed, i + i2, j + j2, k + k2);
            float gx2 = Gradients3D[gi2];
            float gy2 = Gradients3D[gi2 + 1];
            float gz2 = Gradients3D[gi2 + 2];

            float t22 = t2 * t2;
            float t24 = t22 * t22;
            float gdot2 = gx2 * x2 + gy2 * y2 + gz2 * z2;

            value += t24 * gdot2;

            float t23 = t22 * t2;
            dx += t23 * (-8.0f * x2 * gdot2 + t2 * gx2);
            dy += t23 * (-8.0f * y2 * gdot2 + t2 * gy2);
            dz += t23 * (-8.0f * z2 * gdot2 + t2 * gz2);
        }

        // Corner 3
        float t3 = 0.6f - x3 * x3 - y3 * y3 - z3 * z3;
        if (t3 > 0) {
            int gi3 = gradientIndex3D(seed, i + 1, j + 1, k + 1);
            float gx3 = Gradients3D[gi3];
            float gy3 = Gradients3D[gi3 + 1];
            float gz3 = Gradients3D[gi3 + 2];

            float t32 = t3 * t3;
            float t34 = t32 * t32;
            float gdot3 = gx3 * x3 + gy3 * y3 + gz3 * z3;

            value += t34 * gdot3;

            float t33 = t32 * t3;
            dx += t33 * (-8.0f * x3 * gdot3 + t3 * gx3);
            dy += t33 * (-8.0f * y3 * gdot3 + t3 * gy3);
            dz += t33 * (-8.0f * z3 * gdot3 + t3 * gz3);
        }

        // Scale to [-1, 1] range (approximate)
        float scale = 32.0f;
        return new NoiseDerivatives.NoiseWithGradient3D(
            value * scale,
            dx * scale,
            dy * scale,
            dz * scale
        );
    }

    /**
     * Get gradient index for 2D coordinates.
     * Uses same hashing as NoiseUtils.GradCoord for consistency.
     */
    private int gradientIndex2D(int seed, int x, int y) {
        int hash = seed;
        hash ^= x * PrimeX;
        hash ^= y * PrimeY;
        hash *= 0x27d4eb2d;
        hash ^= hash >> 15;
        // 128 gradients with 2 components each: indices 0, 2, 4, ..., 254
        return hash & (127 << 1);
    }

    /**
     * Get gradient index for 3D coordinates.
     * Uses same hashing as NoiseUtils.GradCoord for consistency.
     * Gradients3D has 64 gradients with 4 components each (256 total floats).
     */
    private int gradientIndex3D(int seed, int x, int y, int z) {
        int hash = seed;
        hash ^= x * PrimeX;
        hash ^= y * PrimeY;
        hash ^= z * PrimeZ;
        hash *= 0x27d4eb2d;
        hash ^= hash >> 15;
        // 64 gradients with 4 components each: indices 0, 4, 8, ..., 252
        return hash & (63 << 2);
    }
}
