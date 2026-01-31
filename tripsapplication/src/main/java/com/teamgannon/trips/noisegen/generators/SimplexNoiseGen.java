package com.teamgannon.trips.noisegen.generators;

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

import static com.teamgannon.trips.noisegen.NoiseUtils.*;

/**
 * OpenSimplex2 noise generator implementation.
 * Provides both standard OpenSimplex2 and smooth OpenSimplex2S variants.
 */
public class SimplexNoiseGen implements NoiseGenerator {

    private final boolean useSmooth;

    /**
     * Create a SimplexNoiseGen.
     * @param useSmooth If true, use OpenSimplex2S (smoother). If false, use standard OpenSimplex2.
     */
    public SimplexNoiseGen(boolean useSmooth) {
        this.useSmooth = useSmooth;
    }

    @Override
    public float single2D(int seed, float x, float y) {
        return useSmooth ? singleOpenSimplex2S2D(seed, x, y) : singleSimplex2D(seed, x, y);
    }

    @Override
    public float single3D(int seed, float x, float y, float z) {
        return useSmooth ? singleOpenSimplex2S3D(seed, x, y, z) : singleOpenSimplex2_3D(seed, x, y, z);
    }

    // ==================== 2D OpenSimplex2 (Simplex) ====================

    private float singleSimplex2D(int seed, float x, float y) {
        // 2D OpenSimplex2 case uses the same algorithm as ordinary Simplex.
        final float SQRT3 = 1.7320508075688772935274463415059f;
        final float G2 = (3 - SQRT3) / 6;

        int i = FastFloor(x);
        int j = FastFloor(y);
        float xi = x - i;
        float yi = y - j;

        float t = (xi + yi) * G2;
        float x0 = xi - t;
        float y0 = yi - t;

        i *= PrimeX;
        j *= PrimeY;

        float n0, n1, n2;

        float a = 0.5f - x0 * x0 - y0 * y0;
        if (a <= 0) n0 = 0;
        else {
            n0 = (a * a) * (a * a) * GradCoord(seed, i, j, x0, y0);
        }

        float c = (float) (2 * (1 - 2 * G2) * (1 / G2 - 2)) * t + ((float) (-2 * (1 - 2 * G2) * (1 - 2 * G2)) + a);
        if (c <= 0) n2 = 0;
        else {
            float x2 = x0 + (2 * G2 - 1);
            float y2 = y0 + (2 * G2 - 1);
            n2 = (c * c) * (c * c) * GradCoord(seed, i + PrimeX, j + PrimeY, x2, y2);
        }

        if (y0 > x0) {
            float x1 = x0 + G2;
            float y1 = y0 + (G2 - 1);
            float b = 0.5f - x1 * x1 - y1 * y1;
            if (b <= 0) n1 = 0;
            else {
                n1 = (b * b) * (b * b) * GradCoord(seed, i, j + PrimeY, x1, y1);
            }
        } else {
            float x1 = x0 + (G2 - 1);
            float y1 = y0 + G2;
            float b = 0.5f - x1 * x1 - y1 * y1;
            if (b <= 0) n1 = 0;
            else {
                n1 = (b * b) * (b * b) * GradCoord(seed, i + PrimeX, j, x1, y1);
            }
        }

        return (n0 + n1 + n2) * 99.83685446303647f;
    }

    // ==================== 3D OpenSimplex2 ====================

    private float singleOpenSimplex2_3D(int seed, float x, float y, float z) {
        // 3D OpenSimplex2 case uses two offset rotated cube grids.
        int i = FastRound(x);
        int j = FastRound(y);
        int k = FastRound(z);
        float x0 = x - i;
        float y0 = y - j;
        float z0 = z - k;

        int xNSign = (int) (-1.0f - x0) | 1;
        int yNSign = (int) (-1.0f - y0) | 1;
        int zNSign = (int) (-1.0f - z0) | 1;

        float ax0 = xNSign * -x0;
        float ay0 = yNSign * -y0;
        float az0 = zNSign * -z0;

        i *= PrimeX;
        j *= PrimeY;
        k *= PrimeZ;

        float value = 0;
        float a = (0.6f - x0 * x0) - (y0 * y0 + z0 * z0);

        for (int l = 0; ; l++) {
            if (a > 0) {
                value += (a * a) * (a * a) * GradCoord(seed, i, j, k, x0, y0, z0);
            }

            if (ax0 >= ay0 && ax0 >= az0) {
                float b = a + ax0 + ax0;
                if (b > 1) {
                    b -= 1;
                    value += (b * b) * (b * b) * GradCoord(seed, i - xNSign * PrimeX, j, k, x0 + xNSign, y0, z0);
                }
            } else if (ay0 > ax0 && ay0 >= az0) {
                float b = a + ay0 + ay0;
                if (b > 1) {
                    b -= 1;
                    value += (b * b) * (b * b) * GradCoord(seed, i, j - yNSign * PrimeY, k, x0, y0 + yNSign, z0);
                }
            } else {
                float b = a + az0 + az0;
                if (b > 1) {
                    b -= 1;
                    value += (b * b) * (b * b) * GradCoord(seed, i, j, k - zNSign * PrimeZ, x0, y0, z0 + zNSign);
                }
            }

            if (l == 1) break;

            ax0 = 0.5f - ax0;
            ay0 = 0.5f - ay0;
            az0 = 0.5f - az0;

            x0 = xNSign * ax0;
            y0 = yNSign * ay0;
            z0 = zNSign * az0;

            a += (0.75f - ax0) - (ay0 + az0);

            i += (xNSign >> 1) & PrimeX;
            j += (yNSign >> 1) & PrimeY;
            k += (zNSign >> 1) & PrimeZ;

            xNSign = -xNSign;
            yNSign = -yNSign;
            zNSign = -zNSign;

            seed = ~seed;
        }

        return value * 32.69428253173828125f;
    }

    // ==================== 2D OpenSimplex2S ====================

    private float singleOpenSimplex2S2D(int seed, float x, float y) {
        // 2D OpenSimplex2S case is a modified 2D simplex noise.
        final float SQRT3 = 1.7320508075688772935274463415059f;
        final float G2 = (3 - SQRT3) / 6;

        int i = FastFloor(x);
        int j = FastFloor(y);
        float xi = x - i;
        float yi = y - j;

        i *= PrimeX;
        j *= PrimeY;
        int i1 = i + PrimeX;
        int j1 = j + PrimeY;

        float t = (xi + yi) * G2;
        float x0 = xi - t;
        float y0 = yi - t;

        float a0 = (2.0f / 3.0f) - x0 * x0 - y0 * y0;
        float value = (a0 * a0) * (a0 * a0) * GradCoord(seed, i, j, x0, y0);

        float a1 = (float) (2 * (1 - 2 * G2) * (1 / G2 - 2)) * t + ((float) (-2 * (1 - 2 * G2) * (1 - 2 * G2)) + a0);
        float x1 = x0 - (float) (1 - 2 * G2);
        float y1 = y0 - (float) (1 - 2 * G2);
        value += (a1 * a1) * (a1 * a1) * GradCoord(seed, i1, j1, x1, y1);

        // Nested conditionals were faster than compact bit logic/arithmetic.
        float xmyi = xi - yi;
        if (t > G2) {
            if (xi + xmyi > 1) {
                float x2 = x0 + (float) (3 * G2 - 2);
                float y2 = y0 + (float) (3 * G2 - 1);
                float a2 = (2.0f / 3.0f) - x2 * x2 - y2 * y2;
                if (a2 > 0) {
                    value += (a2 * a2) * (a2 * a2) * GradCoord(seed, i + (PrimeX << 1), j + PrimeY, x2, y2);
                }
            } else {
                float x2 = x0 + G2;
                float y2 = y0 + (G2 - 1);
                float a2 = (2.0f / 3.0f) - x2 * x2 - y2 * y2;
                if (a2 > 0) {
                    value += (a2 * a2) * (a2 * a2) * GradCoord(seed, i, j + PrimeY, x2, y2);
                }
            }

            if (yi - xmyi > 1) {
                float x3 = x0 + (float) (3 * G2 - 1);
                float y3 = y0 + (float) (3 * G2 - 2);
                float a3 = (2.0f / 3.0f) - x3 * x3 - y3 * y3;
                if (a3 > 0) {
                    value += (a3 * a3) * (a3 * a3) * GradCoord(seed, i + PrimeX, j + (PrimeY << 1), x3, y3);
                }
            } else {
                float x3 = x0 + (G2 - 1);
                float y3 = y0 + G2;
                float a3 = (2.0f / 3.0f) - x3 * x3 - y3 * y3;
                if (a3 > 0) {
                    value += (a3 * a3) * (a3 * a3) * GradCoord(seed, i + PrimeX, j, x3, y3);
                }
            }
        } else {
            if (xi + xmyi < 0) {
                float x2 = x0 + (float) (1 - G2);
                float y2 = y0 - G2;
                float a2 = (2.0f / 3.0f) - x2 * x2 - y2 * y2;
                if (a2 > 0) {
                    value += (a2 * a2) * (a2 * a2) * GradCoord(seed, i - PrimeX, j, x2, y2);
                }
            } else {
                float x2 = x0 + (G2 - 1);
                float y2 = y0 + G2;
                float a2 = (2.0f / 3.0f) - x2 * x2 - y2 * y2;
                if (a2 > 0) {
                    value += (a2 * a2) * (a2 * a2) * GradCoord(seed, i + PrimeX, j, x2, y2);
                }
            }

            if (yi < xmyi) {
                float x2 = x0 - G2;
                float y2 = y0 - (G2 - 1);
                float a2 = (2.0f / 3.0f) - x2 * x2 - y2 * y2;
                if (a2 > 0) {
                    value += (a2 * a2) * (a2 * a2) * GradCoord(seed, i, j - PrimeY, x2, y2);
                }
            } else {
                float x2 = x0 + G2;
                float y2 = y0 + (G2 - 1);
                float a2 = (2.0f / 3.0f) - x2 * x2 - y2 * y2;
                if (a2 > 0) {
                    value += (a2 * a2) * (a2 * a2) * GradCoord(seed, i, j + PrimeY, x2, y2);
                }
            }
        }

        return value * 18.24196194486065f;
    }

    // ==================== 3D OpenSimplex2S ====================

    private float singleOpenSimplex2S3D(int seed, float x, float y, float z) {
        // 3D OpenSimplex2S case uses two offset rotated cube grids.
        int i = FastFloor(x);
        int j = FastFloor(y);
        int k = FastFloor(z);
        float xi = x - i;
        float yi = y - j;
        float zi = z - k;

        i *= PrimeX;
        j *= PrimeY;
        k *= PrimeZ;
        int seed2 = seed + 1293373;

        int xNMask = (int) (-0.5f - xi);
        int yNMask = (int) (-0.5f - yi);
        int zNMask = (int) (-0.5f - zi);

        float x0 = xi + xNMask;
        float y0 = yi + yNMask;
        float z0 = zi + zNMask;
        float a0 = 0.75f - x0 * x0 - y0 * y0 - z0 * z0;
        float value = (a0 * a0) * (a0 * a0) * GradCoord(seed,
                i + (xNMask & PrimeX), j + (yNMask & PrimeY), k + (zNMask & PrimeZ), x0, y0, z0);

        float x1 = xi - 0.5f;
        float y1 = yi - 0.5f;
        float z1 = zi - 0.5f;
        float a1 = 0.75f - x1 * x1 - y1 * y1 - z1 * z1;
        value += (a1 * a1) * (a1 * a1) * GradCoord(seed2,
                i + PrimeX, j + PrimeY, k + PrimeZ, x1, y1, z1);

        float xAFlipMask0 = ((xNMask | 1) << 1) * x1;
        float yAFlipMask0 = ((yNMask | 1) << 1) * y1;
        float zAFlipMask0 = ((zNMask | 1) << 1) * z1;
        float xAFlipMask1 = (-2 - (xNMask << 2)) * x1 - 1.0f;
        float yAFlipMask1 = (-2 - (yNMask << 2)) * y1 - 1.0f;
        float zAFlipMask1 = (-2 - (zNMask << 2)) * z1 - 1.0f;

        boolean skip5 = false;
        float a2 = xAFlipMask0 + a0;
        if (a2 > 0) {
            float x2 = x0 - (xNMask | 1);
            float y2 = y0;
            float z2 = z0;
            value += (a2 * a2) * (a2 * a2) * GradCoord(seed,
                    i + (~xNMask & PrimeX), j + (yNMask & PrimeY), k + (zNMask & PrimeZ), x2, y2, z2);
        } else {
            float a3 = yAFlipMask0 + zAFlipMask0 + a0;
            if (a3 > 0) {
                float x3 = x0;
                float y3 = y0 - (yNMask | 1);
                float z3 = z0 - (zNMask | 1);
                value += (a3 * a3) * (a3 * a3) * GradCoord(seed,
                        i + (xNMask & PrimeX), j + (~yNMask & PrimeY), k + (~zNMask & PrimeZ), x3, y3, z3);
            }

            float a4 = xAFlipMask1 + a1;
            if (a4 > 0) {
                float x4 = (xNMask | 1) + x1;
                float y4 = y1;
                float z4 = z1;
                value += (a4 * a4) * (a4 * a4) * GradCoord(seed2,
                        i + (xNMask & (PrimeX * 2)), j + PrimeY, k + PrimeZ, x4, y4, z4);
                skip5 = true;
            }
        }

        boolean skip9 = false;
        float a6 = yAFlipMask0 + a0;
        if (a6 > 0) {
            float x6 = x0;
            float y6 = y0 - (yNMask | 1);
            float z6 = z0;
            value += (a6 * a6) * (a6 * a6) * GradCoord(seed,
                    i + (xNMask & PrimeX), j + (~yNMask & PrimeY), k + (zNMask & PrimeZ), x6, y6, z6);
        } else {
            float a7 = xAFlipMask0 + zAFlipMask0 + a0;
            if (a7 > 0) {
                float x7 = x0 - (xNMask | 1);
                float y7 = y0;
                float z7 = z0 - (zNMask | 1);
                value += (a7 * a7) * (a7 * a7) * GradCoord(seed,
                        i + (~xNMask & PrimeX), j + (yNMask & PrimeY), k + (~zNMask & PrimeZ), x7, y7, z7);
            }

            float a8 = yAFlipMask1 + a1;
            if (a8 > 0) {
                float x8 = x1;
                float y8 = (yNMask | 1) + y1;
                float z8 = z1;
                value += (a8 * a8) * (a8 * a8) * GradCoord(seed2,
                        i + PrimeX, j + (yNMask & (PrimeY << 1)), k + PrimeZ, x8, y8, z8);
                skip9 = true;
            }
        }

        boolean skipD = false;
        float aA = zAFlipMask0 + a0;
        if (aA > 0) {
            float xA = x0;
            float yA = y0;
            float zA = z0 - (zNMask | 1);
            value += (aA * aA) * (aA * aA) * GradCoord(seed,
                    i + (xNMask & PrimeX), j + (yNMask & PrimeY), k + (~zNMask & PrimeZ), xA, yA, zA);
        } else {
            float aB = xAFlipMask0 + yAFlipMask0 + a0;
            if (aB > 0) {
                float xB = x0 - (xNMask | 1);
                float yB = y0 - (yNMask | 1);
                float zB = z0;
                value += (aB * aB) * (aB * aB) * GradCoord(seed,
                        i + (~xNMask & PrimeX), j + (~yNMask & PrimeY), k + (zNMask & PrimeZ), xB, yB, zB);
            }

            float aC = zAFlipMask1 + a1;
            if (aC > 0) {
                float xC = x1;
                float yC = y1;
                float zC = (zNMask | 1) + z1;
                value += (aC * aC) * (aC * aC) * GradCoord(seed2,
                        i + PrimeX, j + PrimeY, k + (zNMask & (PrimeZ << 1)), xC, yC, zC);
                skipD = true;
            }
        }

        if (!skip5) {
            float a5 = yAFlipMask1 + zAFlipMask1 + a1;
            if (a5 > 0) {
                float x5 = x1;
                float y5 = (yNMask | 1) + y1;
                float z5 = (zNMask | 1) + z1;
                value += (a5 * a5) * (a5 * a5) * GradCoord(seed2,
                        i + PrimeX, j + (yNMask & (PrimeY << 1)), k + (zNMask & (PrimeZ << 1)), x5, y5, z5);
            }
        }

        if (!skip9) {
            float a9 = xAFlipMask1 + zAFlipMask1 + a1;
            if (a9 > 0) {
                float x9 = (xNMask | 1) + x1;
                float y9 = y1;
                float z9 = (zNMask | 1) + z1;
                value += (a9 * a9) * (a9 * a9) * GradCoord(seed2,
                        i + (xNMask & (PrimeX * 2)), j + PrimeY, k + (zNMask & (PrimeZ << 1)), x9, y9, z9);
            }
        }

        if (!skipD) {
            float aD = xAFlipMask1 + yAFlipMask1 + a1;
            if (aD > 0) {
                float xD = (xNMask | 1) + x1;
                float yD = (yNMask | 1) + y1;
                float zD = z1;
                value += (aD * aD) * (aD * aD) * GradCoord(seed2,
                        i + (xNMask & (PrimeX << 1)), j + (yNMask & (PrimeY << 1)), k + PrimeZ, xD, yD, zD);
            }
        }

        return value * 9.046026385208288f;
    }
}
