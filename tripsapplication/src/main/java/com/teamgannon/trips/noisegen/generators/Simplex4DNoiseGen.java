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

import com.teamgannon.trips.noisegen.NoiseGradients;
import static com.teamgannon.trips.noisegen.NoiseUtils.*;

/**
 * 4D Simplex noise generator implementation.
 * Provides full 4D simplex noise support for time-based animations and higher-dimensional applications.
 *
 * <p>4D simplex noise is useful for:
 * <ul>
 *   <li>Smoothly animated 3D noise (using W as time)</li>
 *   <li>Looping animations (by moving in a circle through XW or YW plane)</li>
 *   <li>Volumetric effects with temporal variation</li>
 * </ul>
 *
 * <p>Based on Stefan Gustavson's simplex noise implementation with optimizations
 * by Peter Eastman and better rank ordering from 2012.
 */
public class Simplex4DNoiseGen implements NoiseGenerator {

    // Skewing and unskewing factors for 4 dimensions
    private static final float F4 = (float) ((Math.sqrt(5.0) - 1.0) / 4.0);  // 0.309016994...
    private static final float G4 = (float) ((5.0 - Math.sqrt(5.0)) / 20.0); // 0.138196601...

    /**
     * Create a new 4D Simplex noise generator.
     */
    public Simplex4DNoiseGen() {
    }

    @Override
    public float single2D(int seed, float x, float y) {
        // For 2D, delegate to a simple projection of 4D
        return single4D(seed, x, y, 0, 0);
    }

    @Override
    public float single3D(int seed, float x, float y, float z) {
        // For 3D, delegate to a simple projection of 4D
        return single4D(seed, x, y, z, 0);
    }

    @Override
    public float single4D(int seed, float x, float y, float z, float w) {
        // Noise contributions from the five corners
        float n0, n1, n2, n3, n4;

        // Skew the (x,y,z,w) space to determine which cell of 24 simplices we're in
        float s = (x + y + z + w) * F4;
        int i = FastFloor(x + s);
        int j = FastFloor(y + s);
        int k = FastFloor(z + s);
        int l = FastFloor(w + s);

        // Factor for 4D unskewing
        float t = (i + j + k + l) * G4;
        // Unskew the cell origin back to (x,y,z,w) space
        float X0 = i - t;
        float Y0 = j - t;
        float Z0 = k - t;
        float W0 = l - t;

        // The x,y,z,w distances from the cell origin
        float x0 = x - X0;
        float y0 = y - Y0;
        float z0 = z - Z0;
        float w0 = w - W0;

        // For the 4D case, the simplex is a 4D shape.
        // To find out which of the 24 possible simplices we're in, we need to
        // determine the magnitude ordering of x0, y0, z0 and w0.
        // Six pair-wise comparisons are performed between each possible pair
        // of the four coordinates, and the results are used to rank the numbers.
        int rankx = 0;
        int ranky = 0;
        int rankz = 0;
        int rankw = 0;

        if (x0 > y0) rankx++;
        else ranky++;
        if (x0 > z0) rankx++;
        else rankz++;
        if (x0 > w0) rankx++;
        else rankw++;
        if (y0 > z0) ranky++;
        else rankz++;
        if (y0 > w0) ranky++;
        else rankw++;
        if (z0 > w0) rankz++;
        else rankw++;

        // The integer offsets for the simplex corners
        int i1, j1, k1, l1; // Second simplex corner
        int i2, j2, k2, l2; // Third simplex corner
        int i3, j3, k3, l3; // Fourth simplex corner

        // Rank 3 denotes the largest coordinate.
        i1 = rankx >= 3 ? 1 : 0;
        j1 = ranky >= 3 ? 1 : 0;
        k1 = rankz >= 3 ? 1 : 0;
        l1 = rankw >= 3 ? 1 : 0;

        // Rank 2 denotes the second largest coordinate.
        i2 = rankx >= 2 ? 1 : 0;
        j2 = ranky >= 2 ? 1 : 0;
        k2 = rankz >= 2 ? 1 : 0;
        l2 = rankw >= 2 ? 1 : 0;

        // Rank 1 denotes the second smallest coordinate.
        i3 = rankx >= 1 ? 1 : 0;
        j3 = ranky >= 1 ? 1 : 0;
        k3 = rankz >= 1 ? 1 : 0;
        l3 = rankw >= 1 ? 1 : 0;

        // The fifth corner has all coordinate offsets = 1

        // Offsets for corners in (x,y,z,w) coords
        float x1 = x0 - i1 + G4;
        float y1 = y0 - j1 + G4;
        float z1 = z0 - k1 + G4;
        float w1 = w0 - l1 + G4;

        float x2 = x0 - i2 + 2.0f * G4;
        float y2 = y0 - j2 + 2.0f * G4;
        float z2 = z0 - k2 + 2.0f * G4;
        float w2 = w0 - l2 + 2.0f * G4;

        float x3 = x0 - i3 + 3.0f * G4;
        float y3 = y0 - j3 + 3.0f * G4;
        float z3 = z0 - k3 + 3.0f * G4;
        float w3 = w0 - l3 + 3.0f * G4;

        float x4 = x0 - 1.0f + 4.0f * G4;
        float y4 = y0 - 1.0f + 4.0f * G4;
        float z4 = z0 - 1.0f + 4.0f * G4;
        float w4 = w0 - 1.0f + 4.0f * G4;

        // Calculate hashed gradient indices
        int gi0 = hash4D(seed, i, j, k, l) & 31;
        int gi1 = hash4D(seed, i + i1, j + j1, k + k1, l + l1) & 31;
        int gi2 = hash4D(seed, i + i2, j + j2, k + k2, l + l2) & 31;
        int gi3 = hash4D(seed, i + i3, j + j3, k + k3, l + l3) & 31;
        int gi4 = hash4D(seed, i + 1, j + 1, k + 1, l + 1) & 31;

        // Calculate the contribution from the five corners
        float t0 = 0.6f - x0 * x0 - y0 * y0 - z0 * z0 - w0 * w0;
        if (t0 < 0) {
            n0 = 0.0f;
        } else {
            t0 *= t0;
            n0 = t0 * t0 * gradCoord4D(gi0, x0, y0, z0, w0);
        }

        float t1 = 0.6f - x1 * x1 - y1 * y1 - z1 * z1 - w1 * w1;
        if (t1 < 0) {
            n1 = 0.0f;
        } else {
            t1 *= t1;
            n1 = t1 * t1 * gradCoord4D(gi1, x1, y1, z1, w1);
        }

        float t2 = 0.6f - x2 * x2 - y2 * y2 - z2 * z2 - w2 * w2;
        if (t2 < 0) {
            n2 = 0.0f;
        } else {
            t2 *= t2;
            n2 = t2 * t2 * gradCoord4D(gi2, x2, y2, z2, w2);
        }

        float t3 = 0.6f - x3 * x3 - y3 * y3 - z3 * z3 - w3 * w3;
        if (t3 < 0) {
            n3 = 0.0f;
        } else {
            t3 *= t3;
            n3 = t3 * t3 * gradCoord4D(gi3, x3, y3, z3, w3);
        }

        float t4 = 0.6f - x4 * x4 - y4 * y4 - z4 * z4 - w4 * w4;
        if (t4 < 0) {
            n4 = 0.0f;
        } else {
            t4 *= t4;
            n4 = t4 * t4 * gradCoord4D(gi4, x4, y4, z4, w4);
        }

        // Sum up and scale the result to cover the range [-1,1]
        return 27.0f * (n0 + n1 + n2 + n3 + n4);
    }

    @Override
    public boolean supports4D() {
        return true;
    }

    /**
     * Hash function for 4D coordinates.
     */
    private static int hash4D(int seed, int x, int y, int z, int w) {
        int hash = seed;
        hash ^= x * PrimeX;
        hash ^= y * PrimeY;
        hash ^= z * PrimeZ;
        hash ^= w * PrimeW;

        hash *= 0x27d4eb2d;
        return hash;
    }

    /**
     * Calculate gradient dot product for 4D.
     */
    private static float gradCoord4D(int gradIdx, float x, float y, float z, float w) {
        int idx = gradIdx << 2; // gradIdx * 4
        return NoiseGradients.Gradients4D[idx] * x + NoiseGradients.Gradients4D[idx + 1] * y +
               NoiseGradients.Gradients4D[idx + 2] * z + NoiseGradients.Gradients4D[idx + 3] * w;
    }
}
