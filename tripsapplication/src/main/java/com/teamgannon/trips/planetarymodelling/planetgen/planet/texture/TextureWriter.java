package com.teamgannon.trips.planetarymodelling.planetgen.planet.texture;


import com.teamgannon.trips.planetarymodelling.planetgen.math.Color;

public interface TextureWriter<T> {
    void setColor(int x, int y, Color color);

    T getTexture();
}
