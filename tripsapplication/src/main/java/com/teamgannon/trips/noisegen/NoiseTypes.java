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

/**
 * Noise type enumerations for FastNoiseLite.
 * Extracted from the original monolithic FastNoiseLite class for modularity.
 */
public final class NoiseTypes {

    private NoiseTypes() {
        // Utility class, no instantiation
    }

    /**
     * Noise algorithm types for GetNoise(...) method.
     */
    public enum NoiseType {
        OpenSimplex2,
        OpenSimplex2S,
        Cellular,
        Perlin,
        ValueCubic,
        Value
    }

    /**
     * Domain rotation type for 3D Noise and 3D DomainWarp.
     * Can aid in reducing directional artifacts when sampling a 2D plane in 3D.
     */
    public enum RotationType3D {
        None,
        ImproveXYPlanes,
        ImproveXZPlanes
    }

    /**
     * Method for combining octaves in all fractal noise types.
     */
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

    /**
     * Distance function used in cellular noise calculations.
     */
    public enum CellularDistanceFunction {
        Euclidean,
        EuclideanSq,
        Manhattan,
        Hybrid
    }

    /**
     * Return type from cellular noise calculations.
     */
    public enum CellularReturnType {
        CellValue,
        Distance,
        Distance2,
        Distance2Add,
        Distance2Sub,
        Distance2Mul,
        Distance2Div
    }

    /**
     * Warp algorithm type for DomainWarp(...) method.
     */
    public enum DomainWarpType {
        OpenSimplex2,
        OpenSimplex2Reduced,
        BasicGrid
    }

    /**
     * Internal transform type for 3D coordinate transformations.
     * Used internally by noise generators but exposed publicly for subpackage access.
     */
    public enum TransformType3D {
        None,
        ImproveXYPlanes,
        ImproveXZPlanes,
        DefaultOpenSimplex2
    }
}
