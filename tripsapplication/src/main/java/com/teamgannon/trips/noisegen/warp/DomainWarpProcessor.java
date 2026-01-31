package com.teamgannon.trips.noisegen.warp;

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

import com.teamgannon.trips.noisegen.NoiseConfig;
import com.teamgannon.trips.noisegen.NoiseTypes.DomainWarpType;
import com.teamgannon.trips.noisegen.NoiseTypes.TransformType3D;
import com.teamgannon.trips.noisegen.Vector2;
import com.teamgannon.trips.noisegen.Vector3;

import static com.teamgannon.trips.noisegen.NoiseGradients.*;
import static com.teamgannon.trips.noisegen.NoiseUtils.*;

/**
 * Domain warp processor that applies coordinate distortion.
 * Supports progressive and independent fractal domain warp modes.
 */
public class DomainWarpProcessor {

    private final NoiseConfig config;

    public DomainWarpProcessor(NoiseConfig config) {
        this.config = config;
    }

    // ==================== Domain Warp Single ====================

    public void domainWarpSingle(Vector2 coord) {
        int seed = config.getSeed();
        float amp = config.getDomainWarpAmp() * config.getFractalBounding();
        float freq = config.getFrequency();

        float xs = coord.x;
        float ys = coord.y;
        DomainWarpType warpType = config.getDomainWarpType();

        switch (warpType) {
            case OpenSimplex2:
            case OpenSimplex2Reduced: {
                final float SQRT3 = 1.7320508075688772935274463415059f;
                final float F2 = 0.5f * (SQRT3 - 1);
                float t = (xs + ys) * F2;
                xs += t;
                ys += t;
            }
            break;
            default:
                break;
        }

        doSingleDomainWarp(seed, amp, freq, xs, ys, coord);
    }

    public void domainWarpSingle(Vector3 coord) {
        int seed = config.getSeed();
        float amp = config.getDomainWarpAmp() * config.getFractalBounding();
        float freq = config.getFrequency();

        float xs = coord.x;
        float ys = coord.y;
        float zs = coord.z;
        TransformType3D warpTransformType3D = config.getWarpTransformType3D();

        switch (warpTransformType3D) {
            case ImproveXYPlanes: {
                float xy = xs + ys;
                float s2 = xy * -0.211324865405187f;
                zs *= 0.577350269189626f;
                xs += s2 - zs;
                ys = ys + s2 - zs;
                zs += xy * 0.577350269189626f;
            }
            break;
            case ImproveXZPlanes: {
                float xz = xs + zs;
                float s2 = xz * -0.211324865405187f;
                ys *= 0.577350269189626f;
                xs += s2 - ys;
                zs += s2 - ys;
                ys += xz * 0.577350269189626f;
            }
            break;
            case DefaultOpenSimplex2: {
                final float R3 = (float) (2.0 / 3.0);
                float r = (xs + ys + zs) * R3;
                xs = r - xs;
                ys = r - ys;
                zs = r - zs;
            }
            break;
            default:
                break;
        }

        doSingleDomainWarp(seed, amp, freq, xs, ys, zs, coord);
    }

    // ==================== Domain Warp Fractal Progressive ====================

    public void domainWarpFractalProgressive(Vector2 coord) {
        int seed = config.getSeed();
        float amp = config.getDomainWarpAmp() * config.getFractalBounding();
        float freq = config.getFrequency();
        DomainWarpType warpType = config.getDomainWarpType();
        float gain = config.getGain();
        float lacunarity = config.getLacunarity();

        for (int i = 0; i < config.getOctaves(); i++) {
            float xs = coord.x;
            float ys = coord.y;
            switch (warpType) {
                case OpenSimplex2:
                case OpenSimplex2Reduced: {
                    final float SQRT3 = 1.7320508075688772935274463415059f;
                    final float F2 = 0.5f * (SQRT3 - 1);
                    float t = (xs + ys) * F2;
                    xs += t;
                    ys += t;
                }
                break;
                default:
                    break;
            }

            doSingleDomainWarp(seed, amp, freq, xs, ys, coord);

            seed++;
            amp *= gain;
            freq *= lacunarity;
        }
    }

    public void domainWarpFractalProgressive(Vector3 coord) {
        int seed = config.getSeed();
        float amp = config.getDomainWarpAmp() * config.getFractalBounding();
        float freq = config.getFrequency();
        TransformType3D warpTransformType3D = config.getWarpTransformType3D();
        float gain = config.getGain();
        float lacunarity = config.getLacunarity();

        for (int i = 0; i < config.getOctaves(); i++) {
            float xs = coord.x;
            float ys = coord.y;
            float zs = coord.z;
            switch (warpTransformType3D) {
                case ImproveXYPlanes: {
                    float xy = xs + ys;
                    float s2 = xy * -0.211324865405187f;
                    zs *= 0.577350269189626f;
                    xs += s2 - zs;
                    ys = ys + s2 - zs;
                    zs += xy * 0.577350269189626f;
                }
                break;
                case ImproveXZPlanes: {
                    float xz = xs + zs;
                    float s2 = xz * -0.211324865405187f;
                    ys *= 0.577350269189626f;
                    xs += s2 - ys;
                    zs += s2 - ys;
                    ys += xz * 0.577350269189626f;
                }
                break;
                case DefaultOpenSimplex2: {
                    final float R3 = (float) (2.0 / 3.0);
                    float r = (xs + ys + zs) * R3;
                    xs = r - xs;
                    ys = r - ys;
                    zs = r - zs;
                }
                break;
                default:
                    break;
            }

            doSingleDomainWarp(seed, amp, freq, xs, ys, zs, coord);

            seed++;
            amp *= gain;
            freq *= lacunarity;
        }
    }

    // ==================== Domain Warp Fractal Independent ====================

    public void domainWarpFractalIndependent(Vector2 coord) {
        float xs = coord.x;
        float ys = coord.y;
        DomainWarpType warpType = config.getDomainWarpType();

        switch (warpType) {
            case OpenSimplex2:
            case OpenSimplex2Reduced: {
                final float SQRT3 = 1.7320508075688772935274463415059f;
                final float F2 = 0.5f * (SQRT3 - 1);
                float t = (xs + ys) * F2;
                xs += t;
                ys += t;
            }
            break;
            default:
                break;
        }

        int seed = config.getSeed();
        float amp = config.getDomainWarpAmp() * config.getFractalBounding();
        float freq = config.getFrequency();
        float gain = config.getGain();
        float lacunarity = config.getLacunarity();

        for (int i = 0; i < config.getOctaves(); i++) {
            doSingleDomainWarp(seed, amp, freq, xs, ys, coord);

            seed++;
            amp *= gain;
            freq *= lacunarity;
        }
    }

    public void domainWarpFractalIndependent(Vector3 coord) {
        float xs = coord.x;
        float ys = coord.y;
        float zs = coord.z;
        TransformType3D warpTransformType3D = config.getWarpTransformType3D();

        switch (warpTransformType3D) {
            case ImproveXYPlanes: {
                float xy = xs + ys;
                float s2 = xy * -0.211324865405187f;
                zs *= 0.577350269189626f;
                xs += s2 - zs;
                ys = ys + s2 - zs;
                zs += xy * 0.577350269189626f;
            }
            break;
            case ImproveXZPlanes: {
                float xz = xs + zs;
                float s2 = xz * -0.211324865405187f;
                ys *= 0.577350269189626f;
                xs += s2 - ys;
                zs += s2 - ys;
                ys += xz * 0.577350269189626f;
            }
            break;
            case DefaultOpenSimplex2: {
                final float R3 = (float) (2.0 / 3.0);
                float r = (xs + ys + zs) * R3;
                xs = r - xs;
                ys = r - ys;
                zs = r - zs;
            }
            break;
            default:
                break;
        }

        int seed = config.getSeed();
        float amp = config.getDomainWarpAmp() * config.getFractalBounding();
        float freq = config.getFrequency();
        float gain = config.getGain();
        float lacunarity = config.getLacunarity();

        for (int i = 0; i < config.getOctaves(); i++) {
            doSingleDomainWarp(seed, amp, freq, xs, ys, zs, coord);

            seed++;
            amp *= gain;
            freq *= lacunarity;
        }
    }

    // ==================== Internal Methods ====================

    private void doSingleDomainWarp(int seed, float amp, float freq, float x, float y, Vector2 coord) {
        switch (config.getDomainWarpType()) {
            case OpenSimplex2:
                singleDomainWarpSimplexGradient(seed, amp * 38.283687591552734375f, freq, x, y, coord, false);
                break;
            case OpenSimplex2Reduced:
                singleDomainWarpSimplexGradient(seed, amp * 16.0f, freq, x, y, coord, true);
                break;
            case BasicGrid:
                singleDomainWarpBasicGrid(seed, amp, freq, x, y, coord);
                break;
        }
    }

    private void doSingleDomainWarp(int seed, float amp, float freq, float x, float y, float z, Vector3 coord) {
        switch (config.getDomainWarpType()) {
            case OpenSimplex2:
                singleDomainWarpOpenSimplex2Gradient(seed, amp * 32.69428253173828125f, freq, x, y, z, coord, false);
                break;
            case OpenSimplex2Reduced:
                singleDomainWarpOpenSimplex2Gradient(seed, amp * 7.71604938271605f, freq, x, y, z, coord, true);
                break;
            case BasicGrid:
                singleDomainWarpBasicGrid(seed, amp, freq, x, y, z, coord);
                break;
        }
    }

    // ==================== Domain Warp Basic Grid ====================

    private void singleDomainWarpBasicGrid(int seed, float warpAmp, float frequency, float x, float y, Vector2 coord) {
        float xf = x * frequency;
        float yf = y * frequency;

        int x0 = FastFloor(xf);
        int y0 = FastFloor(yf);

        float xs = InterpHermite(xf - x0);
        float ys = InterpHermite(yf - y0);

        x0 *= PrimeX;
        y0 *= PrimeY;
        int x1 = x0 + PrimeX;
        int y1 = y0 + PrimeY;

        int hash0 = Hash(seed, x0, y0) & (255 << 1);
        int hash1 = Hash(seed, x1, y0) & (255 << 1);

        float lx0x = Lerp(RandVecs2D[hash0], RandVecs2D[hash1], xs);
        float ly0x = Lerp(RandVecs2D[hash0 | 1], RandVecs2D[hash1 | 1], xs);

        hash0 = Hash(seed, x0, y1) & (255 << 1);
        hash1 = Hash(seed, x1, y1) & (255 << 1);

        float lx1x = Lerp(RandVecs2D[hash0], RandVecs2D[hash1], xs);
        float ly1x = Lerp(RandVecs2D[hash0 | 1], RandVecs2D[hash1 | 1], xs);

        coord.x += Lerp(lx0x, lx1x, ys) * warpAmp;
        coord.y += Lerp(ly0x, ly1x, ys) * warpAmp;
    }

    private void singleDomainWarpBasicGrid(int seed, float warpAmp, float frequency, float x, float y, float z, Vector3 coord) {
        float xf = x * frequency;
        float yf = y * frequency;
        float zf = z * frequency;

        int x0 = FastFloor(xf);
        int y0 = FastFloor(yf);
        int z0 = FastFloor(zf);

        float xs = InterpHermite(xf - x0);
        float ys = InterpHermite(yf - y0);
        float zs = InterpHermite(zf - z0);

        x0 *= PrimeX;
        y0 *= PrimeY;
        z0 *= PrimeZ;
        int x1 = x0 + PrimeX;
        int y1 = y0 + PrimeY;
        int z1 = z0 + PrimeZ;

        int hash0 = Hash(seed, x0, y0, z0) & (255 << 2);
        int hash1 = Hash(seed, x1, y0, z0) & (255 << 2);

        float lx0x = Lerp(RandVecs3D[hash0], RandVecs3D[hash1], xs);
        float ly0x = Lerp(RandVecs3D[hash0 | 1], RandVecs3D[hash1 | 1], xs);
        float lz0x = Lerp(RandVecs3D[hash0 | 2], RandVecs3D[hash1 | 2], xs);

        hash0 = Hash(seed, x0, y1, z0) & (255 << 2);
        hash1 = Hash(seed, x1, y1, z0) & (255 << 2);

        float lx1x = Lerp(RandVecs3D[hash0], RandVecs3D[hash1], xs);
        float ly1x = Lerp(RandVecs3D[hash0 | 1], RandVecs3D[hash1 | 1], xs);
        float lz1x = Lerp(RandVecs3D[hash0 | 2], RandVecs3D[hash1 | 2], xs);

        float lx0y = Lerp(lx0x, lx1x, ys);
        float ly0y = Lerp(ly0x, ly1x, ys);
        float lz0y = Lerp(lz0x, lz1x, ys);

        hash0 = Hash(seed, x0, y0, z1) & (255 << 2);
        hash1 = Hash(seed, x1, y0, z1) & (255 << 2);

        lx0x = Lerp(RandVecs3D[hash0], RandVecs3D[hash1], xs);
        ly0x = Lerp(RandVecs3D[hash0 | 1], RandVecs3D[hash1 | 1], xs);
        lz0x = Lerp(RandVecs3D[hash0 | 2], RandVecs3D[hash1 | 2], xs);

        hash0 = Hash(seed, x0, y1, z1) & (255 << 2);
        hash1 = Hash(seed, x1, y1, z1) & (255 << 2);

        lx1x = Lerp(RandVecs3D[hash0], RandVecs3D[hash1], xs);
        ly1x = Lerp(RandVecs3D[hash0 | 1], RandVecs3D[hash1 | 1], xs);
        lz1x = Lerp(RandVecs3D[hash0 | 2], RandVecs3D[hash1 | 2], xs);

        coord.x += Lerp(lx0y, Lerp(lx0x, lx1x, ys), zs) * warpAmp;
        coord.y += Lerp(ly0y, Lerp(ly0x, ly1x, ys), zs) * warpAmp;
        coord.z += Lerp(lz0y, Lerp(lz0x, lz1x, ys), zs) * warpAmp;
    }

    // ==================== Domain Warp Simplex/OpenSimplex2 ====================

    private void singleDomainWarpSimplexGradient(int seed, float warpAmp, float frequency, float x, float y, Vector2 coord, boolean outGradOnly) {
        final float SQRT3 = 1.7320508075688772935274463415059f;
        final float G2 = (3 - SQRT3) / 6;

        x *= frequency;
        y *= frequency;

        int i = FastFloor(x);
        int j = FastFloor(y);
        float xi = x - i;
        float yi = y - j;

        float t = (xi + yi) * G2;
        float x0 = xi - t;
        float y0 = yi - t;

        i *= PrimeX;
        j *= PrimeY;

        float vx, vy;
        vx = vy = 0;

        float a = 0.5f - x0 * x0 - y0 * y0;
        if (a > 0) {
            float aaaa = (a * a) * (a * a);
            float xo, yo;
            if (outGradOnly) {
                int hash = Hash(seed, i, j) & (255 << 1);
                xo = RandVecs2D[hash];
                yo = RandVecs2D[hash | 1];
            } else {
                int hash = Hash(seed, i, j);
                int index1 = hash & (127 << 1);
                int index2 = (hash >> 7) & (255 << 1);
                float xg = Gradients2D[index1];
                float yg = Gradients2D[index1 | 1];
                float value = x0 * xg + y0 * yg;
                float xgo = RandVecs2D[index2];
                float ygo = RandVecs2D[index2 | 1];
                xo = value * xgo;
                yo = value * ygo;
            }
            vx += aaaa * xo;
            vy += aaaa * yo;
        }

        float c = (float) (2 * (1 - 2 * G2) * (1 / G2 - 2)) * t + ((float) (-2 * (1 - 2 * G2) * (1 - 2 * G2)) + a);
        if (c > 0) {
            float x2 = x0 + (2 * G2 - 1);
            float y2 = y0 + (2 * G2 - 1);
            float cccc = (c * c) * (c * c);
            float xo, yo;
            if (outGradOnly) {
                int hash = Hash(seed, i + PrimeX, j + PrimeY) & (255 << 1);
                xo = RandVecs2D[hash];
                yo = RandVecs2D[hash | 1];
            } else {
                int hash = Hash(seed, i + PrimeX, j + PrimeY);
                int index1 = hash & (127 << 1);
                int index2 = (hash >> 7) & (255 << 1);
                float xg = Gradients2D[index1];
                float yg = Gradients2D[index1 | 1];
                float value = x2 * xg + y2 * yg;
                float xgo = RandVecs2D[index2];
                float ygo = RandVecs2D[index2 | 1];
                xo = value * xgo;
                yo = value * ygo;
            }
            vx += cccc * xo;
            vy += cccc * yo;
        }

        if (y0 > x0) {
            float x1 = x0 + G2;
            float y1 = y0 + (G2 - 1);
            float b = 0.5f - x1 * x1 - y1 * y1;
            if (b > 0) {
                float bbbb = (b * b) * (b * b);
                float xo, yo;
                if (outGradOnly) {
                    int hash = Hash(seed, i, j + PrimeY) & (255 << 1);
                    xo = RandVecs2D[hash];
                    yo = RandVecs2D[hash | 1];
                } else {
                    int hash = Hash(seed, i, j + PrimeY);
                    int index1 = hash & (127 << 1);
                    int index2 = (hash >> 7) & (255 << 1);
                    float xg = Gradients2D[index1];
                    float yg = Gradients2D[index1 | 1];
                    float value = x1 * xg + y1 * yg;
                    float xgo = RandVecs2D[index2];
                    float ygo = RandVecs2D[index2 | 1];
                    xo = value * xgo;
                    yo = value * ygo;
                }
                vx += bbbb * xo;
                vy += bbbb * yo;
            }
        } else {
            float x1 = x0 + (G2 - 1);
            float y1 = y0 + G2;
            float b = 0.5f - x1 * x1 - y1 * y1;
            if (b > 0) {
                float bbbb = (b * b) * (b * b);
                float xo, yo;
                if (outGradOnly) {
                    int hash = Hash(seed, i + PrimeX, j) & (255 << 1);
                    xo = RandVecs2D[hash];
                    yo = RandVecs2D[hash | 1];
                } else {
                    int hash = Hash(seed, i + PrimeX, j);
                    int index1 = hash & (127 << 1);
                    int index2 = (hash >> 7) & (255 << 1);
                    float xg = Gradients2D[index1];
                    float yg = Gradients2D[index1 | 1];
                    float value = x1 * xg + y1 * yg;
                    float xgo = RandVecs2D[index2];
                    float ygo = RandVecs2D[index2 | 1];
                    xo = value * xgo;
                    yo = value * ygo;
                }
                vx += bbbb * xo;
                vy += bbbb * yo;
            }
        }

        coord.x += vx * warpAmp;
        coord.y += vy * warpAmp;
    }

    private void singleDomainWarpOpenSimplex2Gradient(int seed, float warpAmp, float frequency, float x, float y, float z, Vector3 coord, boolean outGradOnly) {
        x *= frequency;
        y *= frequency;
        z *= frequency;

        int i = FastRound(x);
        int j = FastRound(y);
        int k = FastRound(z);
        float x0 = x - i;
        float y0 = y - j;
        float z0 = z - k;

        int xNSign = (int) (-x0 - 1.0f) | 1;
        int yNSign = (int) (-y0 - 1.0f) | 1;
        int zNSign = (int) (-z0 - 1.0f) | 1;

        float ax0 = xNSign * -x0;
        float ay0 = yNSign * -y0;
        float az0 = zNSign * -z0;

        i *= PrimeX;
        j *= PrimeY;
        k *= PrimeZ;

        float vx, vy, vz;
        vx = vy = vz = 0;

        float a = (0.6f - x0 * x0) - (y0 * y0 + z0 * z0);
        for (int l = 0; ; l++) {
            if (a > 0) {
                float aaaa = (a * a) * (a * a);
                float xo, yo, zo;
                if (outGradOnly) {
                    int hash = Hash(seed, i, j, k) & (255 << 2);
                    xo = RandVecs3D[hash];
                    yo = RandVecs3D[hash | 1];
                    zo = RandVecs3D[hash | 2];
                } else {
                    int hash = Hash(seed, i, j, k);
                    int index1 = hash & (63 << 2);
                    int index2 = (hash >> 6) & (255 << 2);
                    float xg = Gradients3D[index1];
                    float yg = Gradients3D[index1 | 1];
                    float zg = Gradients3D[index1 | 2];
                    float value = x0 * xg + y0 * yg + z0 * zg;
                    float xgo = RandVecs3D[index2];
                    float ygo = RandVecs3D[index2 | 1];
                    float zgo = RandVecs3D[index2 | 2];
                    xo = value * xgo;
                    yo = value * ygo;
                    zo = value * zgo;
                }
                vx += aaaa * xo;
                vy += aaaa * yo;
                vz += aaaa * zo;
            }

            float b = a;
            int i1 = i;
            int j1 = j;
            int k1 = k;
            float x1 = x0;
            float y1 = y0;
            float z1 = z0;

            if (ax0 >= ay0 && ax0 >= az0) {
                x1 += xNSign;
                b = b + ax0 + ax0;
                i1 -= xNSign * PrimeX;
            } else if (ay0 > ax0 && ay0 >= az0) {
                y1 += yNSign;
                b = b + ay0 + ay0;
                j1 -= yNSign * PrimeY;
            } else {
                z1 += zNSign;
                b = b + az0 + az0;
                k1 -= zNSign * PrimeZ;
            }

            if (b > 1) {
                b -= 1;
                float bbbb = (b * b) * (b * b);
                float xo, yo, zo;
                if (outGradOnly) {
                    int hash = Hash(seed, i1, j1, k1) & (255 << 2);
                    xo = RandVecs3D[hash];
                    yo = RandVecs3D[hash | 1];
                    zo = RandVecs3D[hash | 2];
                } else {
                    int hash = Hash(seed, i1, j1, k1);
                    int index1 = hash & (63 << 2);
                    int index2 = (hash >> 6) & (255 << 2);
                    float xg = Gradients3D[index1];
                    float yg = Gradients3D[index1 | 1];
                    float zg = Gradients3D[index1 | 2];
                    float value = x1 * xg + y1 * yg + z1 * zg;
                    float xgo = RandVecs3D[index2];
                    float ygo = RandVecs3D[index2 | 1];
                    float zgo = RandVecs3D[index2 | 2];
                    xo = value * xgo;
                    yo = value * ygo;
                    zo = value * zgo;
                }
                vx += bbbb * xo;
                vy += bbbb * yo;
                vz += bbbb * zo;
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

            seed += 1293373;
        }

        coord.x += vx * warpAmp;
        coord.y += vy * warpAmp;
        coord.z += vz * warpAmp;
    }
}
