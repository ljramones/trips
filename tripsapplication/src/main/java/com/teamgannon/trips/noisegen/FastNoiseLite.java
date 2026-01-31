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
//
// VERSION: 1.1.1 (Refactored)
// https://github.com/Auburn/FastNoiseLite

import com.teamgannon.trips.noisegen.NoiseTypes.*;
import com.teamgannon.trips.noisegen.fractal.FractalProcessor;
import com.teamgannon.trips.noisegen.generators.*;
import com.teamgannon.trips.noisegen.warp.DomainWarpProcessor;

/**
 * FastNoiseLite - Fast portable noise library.
 *
 * This is a facade class that maintains 100% backward compatibility with the original
 * monolithic FastNoiseLite while delegating to modular components for better maintainability.
 *
 * <h2>Usage Example:</h2>
 * <pre>{@code
 * FastNoiseLite noise = new FastNoiseLite(1337);
 * noise.SetNoiseType(NoiseType.OpenSimplex2);
 * noise.SetFractalType(FractalType.FBm);
 * noise.SetFractalOctaves(4);
 * float value = noise.GetNoise(x, y, z);
 * }</pre>
 */
public class FastNoiseLite {

    // Re-export enums for backward compatibility
    public enum NoiseType {
        OpenSimplex2,
        OpenSimplex2S,
        Cellular,
        Perlin,
        ValueCubic,
        Value
    }

    public enum RotationType3D {
        None,
        ImproveXYPlanes,
        ImproveXZPlanes
    }

    public enum FractalType {
        None,
        FBm,
        Ridged,
        PingPong,
        /** [EXT] Billow noise - soft, cloud-like (inverted ridged) */
        Billow,
        /** [EXT] Hybrid multifractal - multiplicative/additive blend for terrain */
        HybridMulti,
        DomainWarpProgressive,
        DomainWarpIndependent
    }

    public enum CellularDistanceFunction {
        Euclidean,
        EuclideanSq,
        Manhattan,
        Hybrid
    }

    public enum CellularReturnType {
        CellValue,
        Distance,
        Distance2,
        Distance2Add,
        Distance2Sub,
        Distance2Mul,
        Distance2Div
    }

    public enum DomainWarpType {
        OpenSimplex2,
        OpenSimplex2Reduced,
        BasicGrid
    }

    // Configuration
    private final NoiseConfig config = new NoiseConfig();

    // Generators (created lazily based on noise type)
    private NoiseGenerator simplexGen;
    private NoiseGenerator simplex2SGen;
    private CellularNoiseGen cellularGen;
    private NoiseGenerator perlinGen;
    private NoiseGenerator valueCubicGen;
    private NoiseGenerator valueGen;
    private NoiseGenerator simplex4DGen;

    // Processors
    private DomainWarpProcessor warpProcessor;

    /**
     * Create new FastNoise object with default seed (1337)
     */
    public FastNoiseLite() {
    }

    /**
     * Create new FastNoise object with specified seed
     */
    public FastNoiseLite(int seed) {
        SetSeed(seed);
    }

    // ==================== Configuration Setters ====================

    /**
     * Sets seed used for all noise types
     * Default: 1337
     */
    public void SetSeed(int seed) {
        config.setSeed(seed);
    }

    /**
     * Gets the current seed used for all noise types.
     * @return The current seed value
     */
    public int GetSeed() {
        return config.getSeed();
    }

    /**
     * Sets frequency for all noise types
     * Default: 0.01
     */
    public void SetFrequency(float frequency) {
        config.setFrequency(frequency);
    }

    /**
     * Sets noise algorithm used for GetNoise(...)
     * Default: OpenSimplex2
     */
    public void SetNoiseType(NoiseType noiseType) {
        config.setNoiseType(toInternal(noiseType));
    }

    /**
     * Sets domain rotation type for 3D Noise and 3D DomainWarp.
     * Can aid in reducing directional artifacts when sampling a 2D plane in 3D
     * Default: None
     */
    public void SetRotationType3D(RotationType3D rotationType3D) {
        config.setRotationType3D(toInternal(rotationType3D));
    }

    /**
     * Sets method for combining octaves in all fractal noise types
     * Default: None
     * Note: FractalType.DomainWarp... only affects DomainWarp(...)
     */
    public void SetFractalType(FractalType fractalType) {
        config.setFractalType(toInternal(fractalType));
    }

    /**
     * Sets octave count for all fractal noise types
     * Default: 3
     */
    public void SetFractalOctaves(int octaves) {
        config.setOctaves(octaves);
    }

    /**
     * Sets octave lacunarity for all fractal noise types
     * Default: 2.0
     */
    public void SetFractalLacunarity(float lacunarity) {
        config.setLacunarity(lacunarity);
    }

    /**
     * Sets octave gain for all fractal noise types
     * Default: 0.5
     */
    public void SetFractalGain(float gain) {
        config.setGain(gain);
    }

    /**
     * Sets octave weighting for all none DomainWarp fractal types
     * Default: 0.0
     * Note: Keep between 0...1 to maintain -1...1 output bounding
     */
    public void SetFractalWeightedStrength(float weightedStrength) {
        config.setWeightedStrength(weightedStrength);
    }

    /**
     * Sets strength of the fractal ping pong effect
     * Default: 2.0
     */
    public void SetFractalPingPongStrength(float pingPongStrength) {
        config.setPingPongStrength(pingPongStrength);
    }

    /**
     * Sets distance function used in cellular noise calculations
     * Default: EuclideanSq
     */
    public void SetCellularDistanceFunction(CellularDistanceFunction cellularDistanceFunction) {
        config.setCellularDistanceFunction(toInternal(cellularDistanceFunction));
        if (cellularGen != null) {
            cellularGen.setDistanceFunction(toInternal(cellularDistanceFunction));
        }
    }

    /**
     * Sets return type from cellular noise calculations
     * Default: Distance
     */
    public void SetCellularReturnType(CellularReturnType cellularReturnType) {
        config.setCellularReturnType(toInternal(cellularReturnType));
        if (cellularGen != null) {
            cellularGen.setReturnType(toInternal(cellularReturnType));
        }
    }

    /**
     * Sets the maximum distance a cellular point can move from its grid position
     * Default: 1.0
     * Note: Setting this higher than 1 will cause artifacts
     */
    public void SetCellularJitter(float cellularJitter) {
        config.setCellularJitterModifier(cellularJitter);
        if (cellularGen != null) {
            cellularGen.setJitterModifier(cellularJitter);
        }
    }

    /**
     * Sets the warp algorithm when using DomainWarp(...)
     * Default: OpenSimplex2
     */
    public void SetDomainWarpType(DomainWarpType domainWarpType) {
        config.setDomainWarpType(toInternal(domainWarpType));
    }

    /**
     * Sets the maximum warp distance from original position when using DomainWarp(...)
     * Default: 1.0
     */
    public void SetDomainWarpAmp(float domainWarpAmp) {
        config.setDomainWarpAmp(domainWarpAmp);
    }

    // ==================== Noise Generation ====================

    /**
     * 2D noise at given position using current settings
     * @return Noise output bounded between -1...1
     */
    public float GetNoise(float x, float y) {
        x *= config.getFrequency();
        y *= config.getFrequency();

        NoiseTypes.NoiseType noiseType = config.getNoiseType();
        switch (noiseType) {
            case OpenSimplex2:
            case OpenSimplex2S: {
                final float SQRT3 = 1.7320508075688772935274463415059f;
                final float F2 = 0.5f * (SQRT3 - 1);
                float t = (x + y) * F2;
                x += t;
                y += t;
            }
            break;
            default:
                break;
        }

        NoiseTypes.FractalType fractalType = config.getFractalType();
        switch (fractalType) {
            case FBm:
                return getFractalProcessor().genFractalFBm2D(x, y);
            case Ridged:
                return getFractalProcessor().genFractalRidged2D(x, y);
            case PingPong:
                return getFractalProcessor().genFractalPingPong2D(x, y);
            case Billow:
                return getFractalProcessor().genFractalBillow2D(x, y);
            case HybridMulti:
                return getFractalProcessor().genFractalHybridMulti2D(x, y);
            default:
                return getNoiseGenerator().single2D(config.getSeed(), x, y);
        }
    }

    /**
     * 3D noise at given position using current settings
     * @return Noise output bounded between -1...1
     */
    public float GetNoise(float x, float y, float z) {
        x *= config.getFrequency();
        y *= config.getFrequency();
        z *= config.getFrequency();

        TransformType3D transformType3D = config.getTransformType3D();
        switch (transformType3D) {
            case ImproveXYPlanes: {
                float xy = x + y;
                float s2 = xy * -0.211324865405187f;
                z *= 0.577350269189626f;
                x += s2 - z;
                y = y + s2 - z;
                z += xy * 0.577350269189626f;
            }
            break;
            case ImproveXZPlanes: {
                float xz = x + z;
                float s2 = xz * -0.211324865405187f;
                y *= 0.577350269189626f;
                x += s2 - y;
                z += s2 - y;
                y += xz * 0.577350269189626f;
            }
            break;
            case DefaultOpenSimplex2: {
                final float R3 = (float) (2.0 / 3.0);
                float r = (x + y + z) * R3;
                x = r - x;
                y = r - y;
                z = r - z;
            }
            break;
            default:
                break;
        }

        NoiseTypes.FractalType fractalType = config.getFractalType();
        switch (fractalType) {
            case FBm:
                return getFractalProcessor().genFractalFBm3D(x, y, z);
            case Ridged:
                return getFractalProcessor().genFractalRidged3D(x, y, z);
            case PingPong:
                return getFractalProcessor().genFractalPingPong3D(x, y, z);
            case Billow:
                return getFractalProcessor().genFractalBillow3D(x, y, z);
            case HybridMulti:
                return getFractalProcessor().genFractalHybridMulti3D(x, y, z);
            default:
                return getNoiseGenerator().single3D(config.getSeed(), x, y, z);
        }
    }

    /**
     * 4D noise at given position using current settings.
     * The W dimension is typically used for time-based animation or looping effects.
     *
     * <p>Note: Only simplex noise supports 4D. Other noise types will use
     * a dedicated 4D simplex generator regardless of the noise type setting.
     *
     * @param x X coordinate
     * @param y Y coordinate
     * @param z Z coordinate
     * @param w W coordinate (typically time or animation parameter)
     * @return Noise output bounded between -1...1
     */
    public float GetNoise(float x, float y, float z, float w) {
        x *= config.getFrequency();
        y *= config.getFrequency();
        z *= config.getFrequency();
        w *= config.getFrequency();

        // 4D only uses simplex noise (other types don't support 4D)
        NoiseGenerator gen = get4DNoiseGenerator();

        NoiseTypes.FractalType fractalType = config.getFractalType();
        switch (fractalType) {
            case FBm:
                return genFractalFBm4D(gen, x, y, z, w);
            case Ridged:
                return genFractalRidged4D(gen, x, y, z, w);
            case PingPong:
                return genFractalPingPong4D(gen, x, y, z, w);
            case Billow:
                return genFractalBillow4D(gen, x, y, z, w);
            case HybridMulti:
                return genFractalHybridMulti4D(gen, x, y, z, w);
            default:
                return gen.single4D(config.getSeed(), x, y, z, w);
        }
    }

    // ==================== Domain Warp ====================

    /**
     * 2D warps the input position using current domain warp settings
     *
     * Example usage with GetNoise:
     * <pre>{@code
     * DomainWarp(coord);
     * noise = GetNoise(coord.x, coord.y);
     * }</pre>
     */
    public void DomainWarp(Vector2 coord) {
        NoiseTypes.FractalType fractalType = config.getFractalType();
        switch (fractalType) {
            case DomainWarpProgressive:
                getWarpProcessor().domainWarpFractalProgressive(coord);
                break;
            case DomainWarpIndependent:
                getWarpProcessor().domainWarpFractalIndependent(coord);
                break;
            default:
                getWarpProcessor().domainWarpSingle(coord);
                break;
        }
    }

    /**
     * 3D warps the input position using current domain warp settings
     *
     * Example usage with GetNoise:
     * <pre>{@code
     * DomainWarp(coord);
     * noise = GetNoise(coord.x, coord.y, coord.z);
     * }</pre>
     */
    public void DomainWarp(Vector3 coord) {
        NoiseTypes.FractalType fractalType = config.getFractalType();
        switch (fractalType) {
            case DomainWarpProgressive:
                getWarpProcessor().domainWarpFractalProgressive(coord);
                break;
            case DomainWarpIndependent:
                getWarpProcessor().domainWarpFractalIndependent(coord);
                break;
            default:
                getWarpProcessor().domainWarpSingle(coord);
                break;
        }
    }

    // ==================== Internal Helpers ====================

    private NoiseGenerator getNoiseGenerator() {
        switch (config.getNoiseType()) {
            case OpenSimplex2:
                if (simplexGen == null) simplexGen = new SimplexNoiseGen(false);
                return simplexGen;
            case OpenSimplex2S:
                if (simplex2SGen == null) simplex2SGen = new SimplexNoiseGen(true);
                return simplex2SGen;
            case Cellular:
                if (cellularGen == null) {
                    cellularGen = new CellularNoiseGen(
                            config.getCellularDistanceFunction(),
                            config.getCellularReturnType(),
                            config.getCellularJitterModifier()
                    );
                }
                return cellularGen;
            case Perlin:
                if (perlinGen == null) perlinGen = new PerlinNoiseGen();
                return perlinGen;
            case ValueCubic:
                if (valueCubicGen == null) valueCubicGen = new ValueNoiseGen(true);
                return valueCubicGen;
            case Value:
                if (valueGen == null) valueGen = new ValueNoiseGen(false);
                return valueGen;
            default:
                if (simplexGen == null) simplexGen = new SimplexNoiseGen(false);
                return simplexGen;
        }
    }

    private FractalProcessor getFractalProcessor() {
        return new FractalProcessor(config, getNoiseGenerator());
    }

    private DomainWarpProcessor getWarpProcessor() {
        if (warpProcessor == null) {
            warpProcessor = new DomainWarpProcessor(config);
        }
        return warpProcessor;
    }

    private NoiseGenerator get4DNoiseGenerator() {
        if (simplex4DGen == null) {
            simplex4DGen = new Simplex4DNoiseGen();
        }
        return simplex4DGen;
    }

    // ==================== 4D Fractal Methods ====================

    private float genFractalFBm4D(NoiseGenerator gen, float x, float y, float z, float w) {
        int seed = config.getSeed();
        float sum = 0;
        float amp = config.getFractalBounding();
        float lacunarity = config.getLacunarity();
        float gain = config.getGain();

        for (int i = 0; i < config.getOctaves(); i++) {
            float noise = gen.single4D(seed++, x, y, z, w);
            sum += noise * amp;
            amp *= gain;

            x *= lacunarity;
            y *= lacunarity;
            z *= lacunarity;
            w *= lacunarity;
        }

        return sum;
    }

    private float genFractalRidged4D(NoiseGenerator gen, float x, float y, float z, float w) {
        int seed = config.getSeed();
        float sum = 0;
        float amp = config.getFractalBounding();
        float lacunarity = config.getLacunarity();
        float gain = config.getGain();

        for (int i = 0; i < config.getOctaves(); i++) {
            float noise = Math.abs(gen.single4D(seed++, x, y, z, w));
            sum += (noise * -2 + 1) * amp;
            amp *= gain;

            x *= lacunarity;
            y *= lacunarity;
            z *= lacunarity;
            w *= lacunarity;
        }

        return sum;
    }

    private float genFractalPingPong4D(NoiseGenerator gen, float x, float y, float z, float w) {
        int seed = config.getSeed();
        float sum = 0;
        float amp = config.getFractalBounding();
        float lacunarity = config.getLacunarity();
        float gain = config.getGain();
        float pingPongStrength = config.getPingPongStrength();

        for (int i = 0; i < config.getOctaves(); i++) {
            float noise = NoiseUtils.PingPong((gen.single4D(seed++, x, y, z, w) + 1) * pingPongStrength);
            sum += (noise - 0.5f) * 2 * amp;
            amp *= gain;

            x *= lacunarity;
            y *= lacunarity;
            z *= lacunarity;
            w *= lacunarity;
        }

        return sum;
    }

    private float genFractalBillow4D(NoiseGenerator gen, float x, float y, float z, float w) {
        int seed = config.getSeed();
        float sum = 0;
        float amp = config.getFractalBounding();
        float lacunarity = config.getLacunarity();
        float gain = config.getGain();
        float weightedStrength = config.getWeightedStrength();

        for (int i = 0; i < config.getOctaves(); i++) {
            float noise = NoiseUtils.FastAbs(gen.single4D(seed++, x, y, z, w)) * 2 - 1;
            sum += noise * amp;
            amp *= NoiseUtils.Lerp(1.0f, (noise + 1) * 0.5f, weightedStrength);
            amp *= gain;

            x *= lacunarity;
            y *= lacunarity;
            z *= lacunarity;
            w *= lacunarity;
        }

        return sum;
    }

    private float genFractalHybridMulti4D(NoiseGenerator gen, float x, float y, float z, float w) {
        int seed = config.getSeed();
        float lacunarity = config.getLacunarity();
        float gain = config.getGain();

        // First octave (additive base)
        float result = gen.single4D(seed++, x, y, z, w) + 1;
        float weight = result;
        float amp = gain;

        x *= lacunarity;
        y *= lacunarity;
        z *= lacunarity;
        w *= lacunarity;

        // Remaining octaves (multiplicative blend)
        for (int i = 1; i < config.getOctaves(); i++) {
            if (weight > 1) {
                weight = 1;
            }

            float noise = (gen.single4D(seed++, x, y, z, w) + 1) * amp;
            result += weight * noise;
            weight *= noise;

            x *= lacunarity;
            y *= lacunarity;
            z *= lacunarity;
            w *= lacunarity;
            amp *= gain;
        }

        return result * config.getFractalBounding() - 1;
    }

    // ==================== Enum Conversion Helpers ====================

    private static NoiseTypes.NoiseType toInternal(NoiseType type) {
        return NoiseTypes.NoiseType.values()[type.ordinal()];
    }

    private static NoiseTypes.RotationType3D toInternal(RotationType3D type) {
        return NoiseTypes.RotationType3D.values()[type.ordinal()];
    }

    private static NoiseTypes.FractalType toInternal(FractalType type) {
        return NoiseTypes.FractalType.values()[type.ordinal()];
    }

    private static NoiseTypes.CellularDistanceFunction toInternal(CellularDistanceFunction type) {
        return NoiseTypes.CellularDistanceFunction.values()[type.ordinal()];
    }

    private static NoiseTypes.CellularReturnType toInternal(CellularReturnType type) {
        return NoiseTypes.CellularReturnType.values()[type.ordinal()];
    }

    private static NoiseTypes.DomainWarpType toInternal(DomainWarpType type) {
        return NoiseTypes.DomainWarpType.values()[type.ordinal()];
    }
}
