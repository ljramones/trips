package com.teamgannon.trips.noisegen.fractal;

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
import com.teamgannon.trips.noisegen.generators.NoiseGenerator;

import static com.teamgannon.trips.noisegen.NoiseUtils.*;

/**
 * Fractal noise processor that combines multiple octaves of noise.
 * Supports FBm (Fractional Brownian motion), Ridged, and PingPong fractal types.
 */
public class FractalProcessor {

    private final NoiseConfig config;
    private final NoiseGenerator generator;

    public FractalProcessor(NoiseConfig config, NoiseGenerator generator) {
        this.config = config;
        this.generator = generator;
    }

    // ==================== FBm ====================

    public float genFractalFBm2D(float x, float y) {
        int seed = config.getSeed();
        float sum = 0;
        float amp = config.getFractalBounding();
        float lacunarity = config.getLacunarity();
        float gain = config.getGain();
        float weightedStrength = config.getWeightedStrength();

        for (int i = 0; i < config.getOctaves(); i++) {
            float noise = generator.single2D(seed++, x, y);
            sum += noise * amp;
            amp *= Lerp(1.0f, FastMin(noise + 1, 2) * 0.5f, weightedStrength);

            x *= lacunarity;
            y *= lacunarity;
            amp *= gain;
        }

        return sum;
    }

    public float genFractalFBm3D(float x, float y, float z) {
        int seed = config.getSeed();
        float sum = 0;
        float amp = config.getFractalBounding();
        float lacunarity = config.getLacunarity();
        float gain = config.getGain();
        float weightedStrength = config.getWeightedStrength();

        for (int i = 0; i < config.getOctaves(); i++) {
            float noise = generator.single3D(seed++, x, y, z);
            sum += noise * amp;
            amp *= Lerp(1.0f, (noise + 1) * 0.5f, weightedStrength);

            x *= lacunarity;
            y *= lacunarity;
            z *= lacunarity;
            amp *= gain;
        }

        return sum;
    }

    // ==================== Ridged ====================

    public float genFractalRidged2D(float x, float y) {
        int seed = config.getSeed();
        float sum = 0;
        float amp = config.getFractalBounding();
        float lacunarity = config.getLacunarity();
        float gain = config.getGain();
        float weightedStrength = config.getWeightedStrength();

        for (int i = 0; i < config.getOctaves(); i++) {
            float noise = FastAbs(generator.single2D(seed++, x, y));
            sum += (noise * -2 + 1) * amp;
            amp *= Lerp(1.0f, 1 - noise, weightedStrength);

            x *= lacunarity;
            y *= lacunarity;
            amp *= gain;
        }

        return sum;
    }

    public float genFractalRidged3D(float x, float y, float z) {
        int seed = config.getSeed();
        float sum = 0;
        float amp = config.getFractalBounding();
        float lacunarity = config.getLacunarity();
        float gain = config.getGain();
        float weightedStrength = config.getWeightedStrength();

        for (int i = 0; i < config.getOctaves(); i++) {
            float noise = FastAbs(generator.single3D(seed++, x, y, z));
            sum += (noise * -2 + 1) * amp;
            amp *= Lerp(1.0f, 1 - noise, weightedStrength);

            x *= lacunarity;
            y *= lacunarity;
            z *= lacunarity;
            amp *= gain;
        }

        return sum;
    }

    // ==================== PingPong ====================

    public float genFractalPingPong2D(float x, float y) {
        int seed = config.getSeed();
        float sum = 0;
        float amp = config.getFractalBounding();
        float lacunarity = config.getLacunarity();
        float gain = config.getGain();
        float weightedStrength = config.getWeightedStrength();
        float pingPongStrength = config.getPingPongStrength();

        for (int i = 0; i < config.getOctaves(); i++) {
            float noise = PingPong((generator.single2D(seed++, x, y) + 1) * pingPongStrength);
            sum += (noise - 0.5f) * 2 * amp;
            amp *= Lerp(1.0f, noise, weightedStrength);

            x *= lacunarity;
            y *= lacunarity;
            amp *= gain;
        }

        return sum;
    }

    public float genFractalPingPong3D(float x, float y, float z) {
        int seed = config.getSeed();
        float sum = 0;
        float amp = config.getFractalBounding();
        float lacunarity = config.getLacunarity();
        float gain = config.getGain();
        float weightedStrength = config.getWeightedStrength();
        float pingPongStrength = config.getPingPongStrength();

        for (int i = 0; i < config.getOctaves(); i++) {
            float noise = PingPong((generator.single3D(seed++, x, y, z) + 1) * pingPongStrength);
            sum += (noise - 0.5f) * 2 * amp;
            amp *= Lerp(1.0f, noise, weightedStrength);

            x *= lacunarity;
            y *= lacunarity;
            z *= lacunarity;
            amp *= gain;
        }

        return sum;
    }

    // ==================== Billow (Extension) ====================

    /**
     * Generates 2D billow fractal noise.
     *
     * <p>Billow is the "inverse" of ridged noise - soft, cloud-like, billowy patterns.
     * Uses absolute value but doesn't invert the result like ridged does.
     *
     * @return Noise value typically in range [0, 1]
     */
    public float genFractalBillow2D(float x, float y) {
        int seed = config.getSeed();
        float sum = 0;
        float amp = config.getFractalBounding();
        float lacunarity = config.getLacunarity();
        float gain = config.getGain();
        float weightedStrength = config.getWeightedStrength();

        for (int i = 0; i < config.getOctaves(); i++) {
            float noise = FastAbs(generator.single2D(seed++, x, y)) * 2 - 1;
            sum += noise * amp;
            amp *= Lerp(1.0f, (noise + 1) * 0.5f, weightedStrength);

            x *= lacunarity;
            y *= lacunarity;
            amp *= gain;
        }

        return sum;
    }

    /**
     * Generates 3D billow fractal noise.
     */
    public float genFractalBillow3D(float x, float y, float z) {
        int seed = config.getSeed();
        float sum = 0;
        float amp = config.getFractalBounding();
        float lacunarity = config.getLacunarity();
        float gain = config.getGain();
        float weightedStrength = config.getWeightedStrength();

        for (int i = 0; i < config.getOctaves(); i++) {
            float noise = FastAbs(generator.single3D(seed++, x, y, z)) * 2 - 1;
            sum += noise * amp;
            amp *= Lerp(1.0f, (noise + 1) * 0.5f, weightedStrength);

            x *= lacunarity;
            y *= lacunarity;
            z *= lacunarity;
            amp *= gain;
        }

        return sum;
    }

    // ==================== Hybrid Multifractal (Extension) ====================

    /**
     * Generates 2D hybrid multifractal noise.
     *
     * <p>Hybrid multifractal combines multiplicative and additive octave blending.
     * First octave is added, subsequent octaves are multiplied by previous values.
     * This creates terrain-like features with varying detail based on local height.
     *
     * @return Noise value, range varies with parameters
     */
    public float genFractalHybridMulti2D(float x, float y) {
        int seed = config.getSeed();
        float lacunarity = config.getLacunarity();
        float gain = config.getGain();

        // First octave (additive base)
        float result = generator.single2D(seed++, x, y) + 1;  // Shift to [0, 2]
        float weight = result;
        float amp = gain;

        x *= lacunarity;
        y *= lacunarity;

        // Remaining octaves (multiplicative blend)
        for (int i = 1; i < config.getOctaves(); i++) {
            // Clamp weight to prevent runaway multiplication
            if (weight > 1) {
                weight = 1;
            }

            float noise = (generator.single2D(seed++, x, y) + 1) * amp;
            result += weight * noise;
            weight *= noise;

            x *= lacunarity;
            y *= lacunarity;
            amp *= gain;
        }

        // Normalize to approximately [-1, 1]
        return result * config.getFractalBounding() - 1;
    }

    /**
     * Generates 3D hybrid multifractal noise.
     */
    public float genFractalHybridMulti3D(float x, float y, float z) {
        int seed = config.getSeed();
        float lacunarity = config.getLacunarity();
        float gain = config.getGain();

        // First octave (additive base)
        float result = generator.single3D(seed++, x, y, z) + 1;
        float weight = result;
        float amp = gain;

        x *= lacunarity;
        y *= lacunarity;
        z *= lacunarity;

        // Remaining octaves (multiplicative blend)
        for (int i = 1; i < config.getOctaves(); i++) {
            if (weight > 1) {
                weight = 1;
            }

            float noise = (generator.single3D(seed++, x, y, z) + 1) * amp;
            result += weight * noise;
            weight *= noise;

            x *= lacunarity;
            y *= lacunarity;
            z *= lacunarity;
            amp *= gain;
        }

        return result * config.getFractalBounding() - 1;
    }
}
