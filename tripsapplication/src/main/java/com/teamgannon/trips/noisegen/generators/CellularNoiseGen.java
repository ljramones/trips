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

import com.teamgannon.trips.noisegen.NoiseTypes.CellularDistanceFunction;
import com.teamgannon.trips.noisegen.NoiseTypes.CellularReturnType;

import static com.teamgannon.trips.noisegen.NoiseGradients.*;
import static com.teamgannon.trips.noisegen.NoiseUtils.*;

/**
 * Cellular (Voronoi/Worley) noise generator implementation.
 */
public class CellularNoiseGen implements NoiseGenerator {

    private CellularDistanceFunction distanceFunction;
    private CellularReturnType returnType;
    private float jitterModifier;

    public CellularNoiseGen(CellularDistanceFunction distanceFunction,
                           CellularReturnType returnType,
                           float jitterModifier) {
        this.distanceFunction = distanceFunction;
        this.returnType = returnType;
        this.jitterModifier = jitterModifier;
    }

    public void setDistanceFunction(CellularDistanceFunction distanceFunction) {
        this.distanceFunction = distanceFunction;
    }

    public void setReturnType(CellularReturnType returnType) {
        this.returnType = returnType;
    }

    public void setJitterModifier(float jitterModifier) {
        this.jitterModifier = jitterModifier;
    }

    @Override
    public float single2D(int seed, float x, float y) {
        int xr = FastRound(x);
        int yr = FastRound(y);

        float distance0 = Float.MAX_VALUE;
        float distance1 = Float.MAX_VALUE;
        int closestHash = 0;

        float cellularJitter = 0.43701595f * jitterModifier;

        int xPrimed = (xr - 1) * PrimeX;
        int yPrimedBase = (yr - 1) * PrimeY;

        switch (distanceFunction) {
            default:
            case Euclidean:
            case EuclideanSq:
                for (int xi = xr - 1; xi <= xr + 1; xi++) {
                    int yPrimed = yPrimedBase;

                    for (int yi = yr - 1; yi <= yr + 1; yi++) {
                        int hash = Hash(seed, xPrimed, yPrimed);
                        int idx = hash & (255 << 1);

                        float vecX = (xi - x) + RandVecs2D[idx] * cellularJitter;
                        float vecY = (yi - y) + RandVecs2D[idx | 1] * cellularJitter;

                        float newDistance = vecX * vecX + vecY * vecY;

                        distance1 = FastMax(FastMin(distance1, newDistance), distance0);
                        if (newDistance < distance0) {
                            distance0 = newDistance;
                            closestHash = hash;
                        }
                        yPrimed += PrimeY;
                    }
                    xPrimed += PrimeX;
                }
                break;
            case Manhattan:
                for (int xi = xr - 1; xi <= xr + 1; xi++) {
                    int yPrimed = yPrimedBase;

                    for (int yi = yr - 1; yi <= yr + 1; yi++) {
                        int hash = Hash(seed, xPrimed, yPrimed);
                        int idx = hash & (255 << 1);

                        float vecX = (xi - x) + RandVecs2D[idx] * cellularJitter;
                        float vecY = (yi - y) + RandVecs2D[idx | 1] * cellularJitter;

                        float newDistance = FastAbs(vecX) + FastAbs(vecY);

                        distance1 = FastMax(FastMin(distance1, newDistance), distance0);
                        if (newDistance < distance0) {
                            distance0 = newDistance;
                            closestHash = hash;
                        }
                        yPrimed += PrimeY;
                    }
                    xPrimed += PrimeX;
                }
                break;
            case Hybrid:
                for (int xi = xr - 1; xi <= xr + 1; xi++) {
                    int yPrimed = yPrimedBase;

                    for (int yi = yr - 1; yi <= yr + 1; yi++) {
                        int hash = Hash(seed, xPrimed, yPrimed);
                        int idx = hash & (255 << 1);

                        float vecX = (xi - x) + RandVecs2D[idx] * cellularJitter;
                        float vecY = (yi - y) + RandVecs2D[idx | 1] * cellularJitter;

                        float newDistance = (FastAbs(vecX) + FastAbs(vecY)) + (vecX * vecX + vecY * vecY);

                        distance1 = FastMax(FastMin(distance1, newDistance), distance0);
                        if (newDistance < distance0) {
                            distance0 = newDistance;
                            closestHash = hash;
                        }
                        yPrimed += PrimeY;
                    }
                    xPrimed += PrimeX;
                }
                break;
        }

        if (distanceFunction == CellularDistanceFunction.Euclidean && returnType != CellularReturnType.CellValue) {
            distance0 = FastSqrt(distance0);
            if (returnType != CellularReturnType.Distance) {
                distance1 = FastSqrt(distance1);
            }
        }

        switch (returnType) {
            case CellValue:
                return closestHash * (1 / 2147483648.0f);
            case Distance:
                return distance0 - 1;
            case Distance2:
                return distance1 - 1;
            case Distance2Add:
                return (distance1 + distance0) * 0.5f - 1;
            case Distance2Sub:
                return distance1 - distance0 - 1;
            case Distance2Mul:
                return distance1 * distance0 * 0.5f - 1;
            case Distance2Div:
                return distance0 / distance1 - 1;
            default:
                return 0;
        }
    }

    @Override
    public float single3D(int seed, float x, float y, float z) {
        int xr = FastRound(x);
        int yr = FastRound(y);
        int zr = FastRound(z);

        float distance0 = Float.MAX_VALUE;
        float distance1 = Float.MAX_VALUE;
        int closestHash = 0;

        float cellularJitter = 0.39614353f * jitterModifier;

        int xPrimed = (xr - 1) * PrimeX;
        int yPrimedBase = (yr - 1) * PrimeY;
        int zPrimedBase = (zr - 1) * PrimeZ;

        switch (distanceFunction) {
            case Euclidean:
            case EuclideanSq:
                for (int xi = xr - 1; xi <= xr + 1; xi++) {
                    int yPrimed = yPrimedBase;

                    for (int yi = yr - 1; yi <= yr + 1; yi++) {
                        int zPrimed = zPrimedBase;

                        for (int zi = zr - 1; zi <= zr + 1; zi++) {
                            int hash = Hash(seed, xPrimed, yPrimed, zPrimed);
                            int idx = hash & (255 << 2);

                            float vecX = (xi - x) + RandVecs3D[idx] * cellularJitter;
                            float vecY = (yi - y) + RandVecs3D[idx | 1] * cellularJitter;
                            float vecZ = (zi - z) + RandVecs3D[idx | 2] * cellularJitter;

                            float newDistance = vecX * vecX + vecY * vecY + vecZ * vecZ;

                            distance1 = FastMax(FastMin(distance1, newDistance), distance0);
                            if (newDistance < distance0) {
                                distance0 = newDistance;
                                closestHash = hash;
                            }
                            zPrimed += PrimeZ;
                        }
                        yPrimed += PrimeY;
                    }
                    xPrimed += PrimeX;
                }
                break;
            case Manhattan:
                for (int xi = xr - 1; xi <= xr + 1; xi++) {
                    int yPrimed = yPrimedBase;

                    for (int yi = yr - 1; yi <= yr + 1; yi++) {
                        int zPrimed = zPrimedBase;

                        for (int zi = zr - 1; zi <= zr + 1; zi++) {
                            int hash = Hash(seed, xPrimed, yPrimed, zPrimed);
                            int idx = hash & (255 << 2);

                            float vecX = (xi - x) + RandVecs3D[idx] * cellularJitter;
                            float vecY = (yi - y) + RandVecs3D[idx | 1] * cellularJitter;
                            float vecZ = (zi - z) + RandVecs3D[idx | 2] * cellularJitter;

                            float newDistance = FastAbs(vecX) + FastAbs(vecY) + FastAbs(vecZ);

                            distance1 = FastMax(FastMin(distance1, newDistance), distance0);
                            if (newDistance < distance0) {
                                distance0 = newDistance;
                                closestHash = hash;
                            }
                            zPrimed += PrimeZ;
                        }
                        yPrimed += PrimeY;
                    }
                    xPrimed += PrimeX;
                }
                break;
            case Hybrid:
                for (int xi = xr - 1; xi <= xr + 1; xi++) {
                    int yPrimed = yPrimedBase;

                    for (int yi = yr - 1; yi <= yr + 1; yi++) {
                        int zPrimed = zPrimedBase;

                        for (int zi = zr - 1; zi <= zr + 1; zi++) {
                            int hash = Hash(seed, xPrimed, yPrimed, zPrimed);
                            int idx = hash & (255 << 2);

                            float vecX = (xi - x) + RandVecs3D[idx] * cellularJitter;
                            float vecY = (yi - y) + RandVecs3D[idx | 1] * cellularJitter;
                            float vecZ = (zi - z) + RandVecs3D[idx | 2] * cellularJitter;

                            float newDistance = (FastAbs(vecX) + FastAbs(vecY) + FastAbs(vecZ)) + (vecX * vecX + vecY * vecY + vecZ * vecZ);

                            distance1 = FastMax(FastMin(distance1, newDistance), distance0);
                            if (newDistance < distance0) {
                                distance0 = newDistance;
                                closestHash = hash;
                            }
                            zPrimed += PrimeZ;
                        }
                        yPrimed += PrimeY;
                    }
                    xPrimed += PrimeX;
                }
                break;
            default:
                break;
        }

        if (distanceFunction == CellularDistanceFunction.Euclidean && returnType != CellularReturnType.CellValue) {
            distance0 = FastSqrt(distance0);
            if (returnType != CellularReturnType.Distance) {
                distance1 = FastSqrt(distance1);
            }
        }

        switch (returnType) {
            case CellValue:
                return closestHash * (1 / 2147483648.0f);
            case Distance:
                return distance0 - 1;
            case Distance2:
                return distance1 - 1;
            case Distance2Add:
                return (distance1 + distance0) * 0.5f - 1;
            case Distance2Sub:
                return distance1 - distance0 - 1;
            case Distance2Mul:
                return distance1 * distance0 * 0.5f - 1;
            case Distance2Div:
                return distance0 / distance1 - 1;
            default:
                return 0;
        }
    }
}
