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

import com.teamgannon.trips.noisegen.NoiseTypes.*;

import static com.teamgannon.trips.noisegen.NoiseUtils.FastAbs;

/**
 * Mutable configuration holder for noise generation settings.
 * Manages all configurable parameters and derived values for FastNoiseLite.
 */
public class NoiseConfig {

    // Core settings
    private int seed = 1337;
    private float frequency = 0.01f;
    private NoiseType noiseType = NoiseType.OpenSimplex2;
    private RotationType3D rotationType3D = RotationType3D.None;
    private TransformType3D transformType3D = TransformType3D.DefaultOpenSimplex2;

    // Fractal settings
    private FractalType fractalType = FractalType.None;
    private int octaves = 3;
    private float lacunarity = 2.0f;
    private float gain = 0.5f;
    private float weightedStrength = 0.0f;
    private float pingPongStrength = 2.0f;
    private float fractalBounding = 1 / 1.75f;

    // Cellular settings
    private CellularDistanceFunction cellularDistanceFunction = CellularDistanceFunction.EuclideanSq;
    private CellularReturnType cellularReturnType = CellularReturnType.Distance;
    private float cellularJitterModifier = 1.0f;

    // Domain warp settings
    private DomainWarpType domainWarpType = DomainWarpType.OpenSimplex2;
    private TransformType3D warpTransformType3D = TransformType3D.DefaultOpenSimplex2;
    private float domainWarpAmp = 1.0f;

    // ==================== Getters ====================

    public int getSeed() {
        return seed;
    }

    public float getFrequency() {
        return frequency;
    }

    public NoiseType getNoiseType() {
        return noiseType;
    }

    public RotationType3D getRotationType3D() {
        return rotationType3D;
    }

    public TransformType3D getTransformType3D() {
        return transformType3D;
    }

    public FractalType getFractalType() {
        return fractalType;
    }

    public int getOctaves() {
        return octaves;
    }

    public float getLacunarity() {
        return lacunarity;
    }

    public float getGain() {
        return gain;
    }

    public float getWeightedStrength() {
        return weightedStrength;
    }

    public float getPingPongStrength() {
        return pingPongStrength;
    }

    public float getFractalBounding() {
        return fractalBounding;
    }

    public CellularDistanceFunction getCellularDistanceFunction() {
        return cellularDistanceFunction;
    }

    public CellularReturnType getCellularReturnType() {
        return cellularReturnType;
    }

    public float getCellularJitterModifier() {
        return cellularJitterModifier;
    }

    public DomainWarpType getDomainWarpType() {
        return domainWarpType;
    }

    public TransformType3D getWarpTransformType3D() {
        return warpTransformType3D;
    }

    public float getDomainWarpAmp() {
        return domainWarpAmp;
    }

    // ==================== Setters ====================

    public void setSeed(int seed) {
        this.seed = seed;
    }

    public void setFrequency(float frequency) {
        this.frequency = frequency;
    }

    public void setNoiseType(NoiseType noiseType) {
        this.noiseType = noiseType;
        updateTransformType3D();
    }

    public void setRotationType3D(RotationType3D rotationType3D) {
        this.rotationType3D = rotationType3D;
        updateTransformType3D();
        updateWarpTransformType3D();
    }

    public void setFractalType(FractalType fractalType) {
        this.fractalType = fractalType;
    }

    public void setOctaves(int octaves) {
        this.octaves = octaves;
        calculateFractalBounding();
    }

    public void setLacunarity(float lacunarity) {
        this.lacunarity = lacunarity;
    }

    public void setGain(float gain) {
        this.gain = gain;
        calculateFractalBounding();
    }

    public void setWeightedStrength(float weightedStrength) {
        this.weightedStrength = weightedStrength;
    }

    public void setPingPongStrength(float pingPongStrength) {
        this.pingPongStrength = pingPongStrength;
    }

    public void setCellularDistanceFunction(CellularDistanceFunction cellularDistanceFunction) {
        this.cellularDistanceFunction = cellularDistanceFunction;
    }

    public void setCellularReturnType(CellularReturnType cellularReturnType) {
        this.cellularReturnType = cellularReturnType;
    }

    public void setCellularJitterModifier(float cellularJitterModifier) {
        this.cellularJitterModifier = cellularJitterModifier;
    }

    public void setDomainWarpType(DomainWarpType domainWarpType) {
        this.domainWarpType = domainWarpType;
        updateWarpTransformType3D();
    }

    public void setDomainWarpAmp(float domainWarpAmp) {
        this.domainWarpAmp = domainWarpAmp;
    }

    // ==================== Internal Update Methods ====================

    private void calculateFractalBounding() {
        float g = FastAbs(gain);
        float amp = g;
        float ampFractal = 1.0f;
        for (int i = 1; i < octaves; i++) {
            ampFractal += amp;
            amp *= g;
        }
        fractalBounding = 1 / ampFractal;
    }

    private void updateTransformType3D() {
        switch (rotationType3D) {
            case ImproveXYPlanes:
                transformType3D = TransformType3D.ImproveXYPlanes;
                break;
            case ImproveXZPlanes:
                transformType3D = TransformType3D.ImproveXZPlanes;
                break;
            default:
                switch (noiseType) {
                    case OpenSimplex2:
                    case OpenSimplex2S:
                        transformType3D = TransformType3D.DefaultOpenSimplex2;
                        break;
                    default:
                        transformType3D = TransformType3D.None;
                        break;
                }
                break;
        }
    }

    private void updateWarpTransformType3D() {
        switch (rotationType3D) {
            case ImproveXYPlanes:
                warpTransformType3D = TransformType3D.ImproveXYPlanes;
                break;
            case ImproveXZPlanes:
                warpTransformType3D = TransformType3D.ImproveXZPlanes;
                break;
            default:
                switch (domainWarpType) {
                    case OpenSimplex2:
                    case OpenSimplex2Reduced:
                        warpTransformType3D = TransformType3D.DefaultOpenSimplex2;
                        break;
                    default:
                        warpTransformType3D = TransformType3D.None;
                        break;
                }
                break;
        }
    }
}
