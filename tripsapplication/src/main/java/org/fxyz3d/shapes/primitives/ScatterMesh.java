/**
 * ScatterMesh.java
 *
 * Copyright (c) 2013-2019, F(X)yz
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 *     * Neither the name of F(X)yz, any associated website, nor the
 * names of its contributors may be used to endorse or promote products
 * derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL F(X)yz BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package org.fxyz3d.shapes.primitives;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.fxyz3d.scene.paint.Patterns;
import org.fxyz3d.shapes.primitives.helper.MarkerFactory;
import org.fxyz3d.shapes.primitives.helper.MeshHelper;
import org.fxyz3d.shapes.primitives.helper.TriangleMeshHelper;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Group;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.CullFace;
import javafx.scene.shape.DrawMode;
import javafx.scene.shape.TriangleMesh;
import org.fxyz3d.geometry.Point3D;
import org.fxyz3d.shapes.primitives.helper.TextureMode;
import org.fxyz3d.scene.paint.Palette.ColorPalette;

/**
 *
 * @author José Pereda
 */
public class ScatterMesh extends Group implements TextureMode {

    private final static List<Point3D> DEFAULT_SCATTER_DATA = Arrays.asList(new Point3D(0f,0f,0f),
            new Point3D(1f,1f,1f), new Point3D(2f,2f,2f));
    private final static double DEFAULT_HEIGHT = 0.1d;
    private final static int DEFAULT_LEVEL = 0;
    private final static boolean DEFAULT_JOIN_SEGMENTS = true;

    private ObservableList<TexturedMesh> meshes=null;

    public ScatterMesh(){
        this(DEFAULT_SCATTER_DATA,DEFAULT_JOIN_SEGMENTS,DEFAULT_HEIGHT,DEFAULT_LEVEL);
    }

    public ScatterMesh(List<Point3D> scatterData){
        this(scatterData,DEFAULT_JOIN_SEGMENTS,DEFAULT_HEIGHT,DEFAULT_LEVEL);
    }


    public ScatterMesh(List<Point3D> scatterData, double height){
        this(scatterData,DEFAULT_JOIN_SEGMENTS,height,DEFAULT_LEVEL);
    }

    public ScatterMesh(List<Point3D> scatterData, boolean joinSegments, double height, int level){
        setScatterData(scatterData);
        setJoinSegments(joinSegments);
        setHeight(height);
        setLevel(level);

        idProperty().addListener(o -> updateMesh());
        updateMesh();
    }

    private final ObjectProperty<List<Point3D>> scatterData = new SimpleObjectProperty<List<Point3D>>(DEFAULT_SCATTER_DATA){

        @Override
        protected void invalidated() {
            if (meshes != null) {
                updateMesh();
            }
        }
    };

    public List<Point3D> getScatterData() {
        return scatterData.get();
    }

    public final void setScatterData(List<Point3D> value) {
        scatterData.set(value);
    }

    public ObjectProperty<List<Point3D>> scatterDataProperty() {
        return scatterData;
    }

    private final ObjectProperty<List<Number>> functionData = new SimpleObjectProperty<List<Number>>(){
        @Override
        protected void invalidated() {
            if (meshes != null) {
                updateF(get());
            }
        }
    };

    public List<Number> getFunctionData() {
        return functionData.get();
    }

    public void setFunctionData(List<Number> value) {
        functionData.set(value);
    }

    public ObjectProperty<List<Number>> functionDataProperty() {
        return functionData;
    }

    // dot
    private final ObjectProperty<MarkerFactory.Marker> marker = new SimpleObjectProperty<MarkerFactory.Marker>(this, "dot", MarkerFactory.Marker.TETRAHEDRA) {
        @Override
        protected void invalidated() {
            if (meshes != null) {
                updateMesh();
            }
        }
    };
    public final ObjectProperty<MarkerFactory.Marker> markerProperty() {
        return marker;
    }
    public final MarkerFactory.Marker getMarker() {
        return marker.get();
    }
    public final void setMarker(MarkerFactory.Marker value) {
        marker.set(value);
    }

    private final DoubleProperty height = new SimpleDoubleProperty(DEFAULT_HEIGHT){
        @Override
        protected void invalidated() {
            if (meshes != null) {
                updateMesh();
            }
        }
    };

    public double getHeight() {
        return height.get();
    }

    public final void setHeight(double value) {
        height.set(value);
    }

    public DoubleProperty heightProperty() {
        return height;
    }

    private final IntegerProperty level = new SimpleIntegerProperty(DEFAULT_LEVEL){

        @Override
        protected void invalidated() {
            if (meshes != null) {
                updateMesh();
            }
        }

    };

    public final int getLevel() {
        return level.get();
    }

    public final void setLevel(int value) {
        level.set(value);
    }

    public final IntegerProperty levelProperty() {
        return level;
    }

    private final BooleanProperty joinSegments = new SimpleBooleanProperty(DEFAULT_JOIN_SEGMENTS){
        @Override
        protected void invalidated() {
            if (meshes != null) {
                updateMesh();
            }
        }
    };

    public boolean isJoinSegments() {
        return joinSegments.get();
    }

    public final void setJoinSegments(boolean value) {
        joinSegments.set(value);
    }

    public BooleanProperty joinSegmentsProperty() {
        return joinSegments;
    }

    /**
     * CullFace property for controlling backface culling on all particle meshes.
     * Default is CullFace.BACK. Set to CullFace.NONE for particles visible from all angles.
     */
    private final ObjectProperty<CullFace> cullFace = new SimpleObjectProperty<>(CullFace.BACK) {
        @Override
        protected void invalidated() {
            applyCullFace(get());
        }
    };

    /**
     * Gets the current CullFace setting.
     * @return the CullFace value
     */
    public CullFace getCullFace() {
        return cullFace.get();
    }

    /**
     * Sets the CullFace for all particle meshes.
     * Use CullFace.NONE to make particles visible from all angles.
     * @param value the CullFace to apply
     */
    public void setCullFace(CullFace value) {
        cullFace.set(value);
    }

    /**
     * Property for CullFace setting.
     * @return the cullFace property
     */
    public ObjectProperty<CullFace> cullFaceProperty() {
        return cullFace;
    }

    /**
     * Applies the current CullFace setting to all meshes.
     */
    private void applyCullFace(CullFace cf) {
        if (meshes != null) {
            meshes.forEach(m -> m.setCullFace(cf));
        }
    }

    // ==================== Per-Particle Color Support ====================

    /**
     * Whether per-particle coloring is enabled.
     * When true, each particle's colorIndex field is used to look up its color
     * from the color palette.
     */
    private final BooleanProperty perParticleColor = new SimpleBooleanProperty(false) {
        @Override
        protected void invalidated() {
            if (meshes != null) {
                updateMesh();
            }
        }
    };

    /**
     * Gets whether per-particle coloring is enabled.
     * @return true if per-particle coloring is enabled
     */
    public boolean isPerParticleColor() {
        return perParticleColor.get();
    }

    /**
     * Enables or disables per-particle coloring.
     * When enabled, each Point3D's colorIndex field determines its color from the palette.
     * @param value true to enable per-particle coloring
     */
    public void setPerParticleColor(boolean value) {
        perParticleColor.set(value);
    }

    /**
     * Property for per-particle color mode.
     * @return the perParticleColor property
     */
    public BooleanProperty perParticleColorProperty() {
        return perParticleColor;
    }

    /**
     * The color palette for per-particle coloring.
     * Colors are mapped to indices 0-255 based on their position in the list.
     */
    private List<Color> colorPaletteColors = null;

    /**
     * The generated palette texture.
     * When opacity support is disabled: 256x1 pixels (color only).
     * When opacity support is enabled: 256x256 pixels (color x opacity).
     */
    private WritableImage paletteTexture = null;

    /**
     * Whether per-particle opacity is enabled.
     * When true, each particle's opacity field is used to set its transparency.
     */
    private final BooleanProperty perParticleOpacity = new SimpleBooleanProperty(false) {
        @Override
        protected void invalidated() {
            if (meshes != null) {
                // Rebuild palette texture with opacity support
                if (colorPaletteColors != null) {
                    paletteTexture = createPaletteTexture(colorPaletteColors);
                    if (perParticleColor.get()) {
                        updateMesh();
                    }
                }
            }
        }
    };

    /**
     * Gets whether per-particle opacity is enabled.
     * @return true if per-particle opacity is enabled
     */
    public boolean isPerParticleOpacity() {
        return perParticleOpacity.get();
    }

    /**
     * Enables or disables per-particle opacity.
     * When enabled, each Point3D's opacity field (0.0-1.0) determines its transparency.
     * @param value true to enable per-particle opacity
     */
    public void setPerParticleOpacity(boolean value) {
        perParticleOpacity.set(value);
    }

    /**
     * Property for per-particle opacity mode.
     * @return the perParticleOpacity property
     */
    public BooleanProperty perParticleOpacityProperty() {
        return perParticleOpacity;
    }

    /**
     * Sets the color palette for per-particle coloring.
     * <p>
     * The palette colors are interpolated to fill 256 entries. For example,
     * a 2-color palette creates a gradient from color 0 to color 1.
     * <p>
     * After setting the palette, particles can use colorIndex 0-255 to select colors.
     *
     * @param colors the list of colors for the palette (at least 1 color required)
     */
    public void setColorPaletteColors(List<Color> colors) {
        if (colors == null || colors.isEmpty()) {
            throw new IllegalArgumentException("Color palette must have at least one color");
        }
        this.colorPaletteColors = new ArrayList<>(colors);
        this.paletteTexture = createPaletteTexture(colors);

        // If per-particle color is already enabled, rebuild the mesh
        if (perParticleColor.get() && meshes != null) {
            updateMesh();
        }
    }

    /**
     * Gets the current color palette.
     * @return the list of palette colors, or null if not set
     */
    public List<Color> getColorPaletteColors() {
        return colorPaletteColors;
    }

    /**
     * Creates a palette texture from the given colors.
     * <p>
     * When per-particle opacity is disabled: Creates a 256x1 texture (color only).
     * When per-particle opacity is enabled: Creates a 256x256 texture where:
     * <ul>
     *   <li>X (U coord) = color index (0-255)</li>
     *   <li>Y (V coord) = opacity level (row 0 = transparent, row 255 = opaque)</li>
     * </ul>
     *
     * @param colors the source colors
     * @return the palette texture
     */
    private WritableImage createPaletteTexture(List<Color> colors) {
        if (perParticleOpacity.get()) {
            return createPaletteTextureWithOpacity(colors);
        } else {
            return createPaletteTextureColorOnly(colors);
        }
    }

    /**
     * Creates a 256x1 pixel palette texture (color only, no opacity support).
     */
    private WritableImage createPaletteTextureColorOnly(List<Color> colors) {
        WritableImage img = new WritableImage(256, 1);
        PixelWriter pw = img.getPixelWriter();

        for (int i = 0; i < 256; i++) {
            Color c = interpolateColor(colors, i);
            pw.setColor(i, 0, c);
        }

        return img;
    }

    /**
     * Creates a 256x256 pixel palette texture with opacity support.
     * X dimension = color index (0-255)
     * Y dimension = opacity level (0 = transparent, 255 = opaque)
     */
    private WritableImage createPaletteTextureWithOpacity(List<Color> colors) {
        WritableImage img = new WritableImage(256, 256);
        PixelWriter pw = img.getPixelWriter();

        for (int x = 0; x < 256; x++) {
            // Get the base color for this column
            Color baseColor = interpolateColor(colors, x);

            for (int y = 0; y < 256; y++) {
                // y = 0 is top of texture (v = 0) = transparent
                // y = 255 is bottom of texture (v = 1) = opaque
                // So opacity = y / 255.0
                double alpha = y / 255.0;
                Color c = new Color(
                        baseColor.getRed(),
                        baseColor.getGreen(),
                        baseColor.getBlue(),
                        alpha
                );
                pw.setColor(x, y, c);
            }
        }

        return img;
    }

    /**
     * Interpolates a color from the palette at the given index (0-255).
     */
    private Color interpolateColor(List<Color> colors, int index) {
        if (colors.size() == 1) {
            return colors.get(0);
        }
        // Interpolate between colors
        double t = index / 255.0;
        double scaledIndex = t * (colors.size() - 1);
        int lowerIdx = (int) Math.floor(scaledIndex);
        int upperIdx = Math.min(lowerIdx + 1, colors.size() - 1);
        double fraction = scaledIndex - lowerIdx;
        return colors.get(lowerIdx).interpolate(colors.get(upperIdx), fraction);
    }

    /**
     * Convenience method to enable per-particle coloring with a specified palette.
     * <p>
     * Example usage:
     * <pre>{@code
     * scatterMesh.enablePerParticleColor(Arrays.asList(
     *     Color.RED,      // colorIndex 0-85 → red to orange
     *     Color.ORANGE,   // colorIndex 86-170 → orange to yellow
     *     Color.YELLOW    // colorIndex 171-255 → yellow
     * ));
     * }</pre>
     *
     * @param paletteColors the colors for the gradient palette
     */
    public void enablePerParticleColor(List<Color> paletteColors) {
        setColorPaletteColors(paletteColors);
        setPerParticleColor(true);
    }

    /**
     * Disables per-particle coloring and reverts to single-color mode.
     */
    public void disablePerParticleColor() {
        setPerParticleColor(false);
    }

    /**
     * Applies the palette texture to all meshes.
     */
    private void applyPaletteTexture() {
        if (paletteTexture != null && meshes != null) {
            meshes.forEach(m -> {
                PhongMaterial material = new PhongMaterial();
                material.setDiffuseMap(paletteTexture);
                m.setMaterial(material);
            });
        }
    }

    // ==================== Per-Particle Scale Support (Phase 3) ====================

    /**
     * Whether per-particle scaling is enabled.
     * When true, each particle's scale field is used to size its marker.
     */
    private final BooleanProperty perParticleScale = new SimpleBooleanProperty(false) {
        @Override
        protected void invalidated() {
            if (meshes != null) {
                updateMesh();
            }
        }
    };

    /**
     * Gets whether per-particle scaling is enabled.
     * @return true if per-particle scaling is enabled
     */
    public boolean isPerParticleScale() {
        return perParticleScale.get();
    }

    /**
     * Enables or disables per-particle scaling.
     * When enabled, each Point3D's scale field determines its marker size.
     * Scale of 1.0 = normal (height property), 0.5 = half, 2.0 = double.
     * @param value true to enable per-particle scaling
     */
    public void setPerParticleScale(boolean value) {
        perParticleScale.set(value);
    }

    /**
     * Property for per-particle scale mode.
     * @return the perParticleScale property
     */
    public BooleanProperty perParticleScaleProperty() {
        return perParticleScale;
    }

    /**
     * Convenience method to enable per-particle color and scale with a palette.
     * <p>
     * Example usage:
     * <pre>{@code
     * // Create points with color and scale
     * points.add(new Point3D(x, y, z, colorIndex, scale));
     *
     * // Enable both features
     * scatterMesh.enablePerParticleAttributes(Arrays.asList(Color.RED, Color.YELLOW));
     * }</pre>
     *
     * @param paletteColors the colors for the gradient palette (null to skip color)
     */
    public void enablePerParticleAttributes(List<Color> paletteColors) {
        if (paletteColors != null && !paletteColors.isEmpty()) {
            setColorPaletteColors(paletteColors);
            setPerParticleColor(true);
        }
        setPerParticleScale(true);
    }

    /**
     * Convenience method to enable all per-particle attributes: color, scale, and opacity.
     * <p>
     * Example usage:
     * <pre>{@code
     * // Create points with color, scale, and opacity
     * points.add(new Point3D(x, y, z, colorIndex, scale, opacity));
     *
     * // Enable all features
     * scatterMesh.enableAllPerParticleAttributes(Arrays.asList(Color.RED, Color.YELLOW));
     * }</pre>
     * <p>
     * Note: Opacity requires per-particle color to also be enabled (they share the palette texture).
     *
     * @param paletteColors the colors for the gradient palette (required for opacity)
     */
    public void enableAllPerParticleAttributes(List<Color> paletteColors) {
        if (paletteColors == null || paletteColors.isEmpty()) {
            throw new IllegalArgumentException("Color palette is required for opacity support");
        }
        setPerParticleOpacity(true);  // Set first so texture is created correctly
        setColorPaletteColors(paletteColors);
        setPerParticleColor(true);
        setPerParticleScale(true);
    }

    /**
     * Disables all per-particle attributes (color, scale, and opacity).
     */
    public void disablePerParticleAttributes() {
        setPerParticleColor(false);
        setPerParticleScale(false);
        setPerParticleOpacity(false);
    }

    // ==================== Efficient Position Updates (Phase 2) ====================

    /**
     * Number of vertices per marker shape (cached after mesh creation).
     * Used for efficient position updates.
     */
    private int verticesPerMarker = -1;

    /**
     * Cached positions from last mesh build.
     * Used to calculate deltas for efficient updates.
     */
    private List<Point3D> cachedPositions = null;

    /**
     * Updates particle positions efficiently without full mesh rebuild.
     * <p>
     * This method is much faster than calling setScatterData() because it directly
     * modifies the mesh's points array rather than rebuilding the entire mesh.
     * <p>
     * Requirements:
     * <ul>
     *   <li>joinSegments must be true (combined mesh mode)</li>
     *   <li>Particle count must match the original count</li>
     *   <li>Mesh must have been built at least once</li>
     * </ul>
     * <p>
     * If any requirement is not met, this method falls back to a full rebuild.
     *
     * @param newPositions the new positions for all particles (same count as original)
     * @return true if efficient update was used, false if full rebuild occurred
     */
    public boolean updatePositions(List<Point3D> newPositions) {
        // Validate preconditions for efficient update
        if (!canUseEfficientUpdate(newPositions)) {
            // Fall back to full rebuild
            setScatterData(newPositions);
            return false;
        }

        // Get the combined mesh
        TexturedMesh texturedMesh = meshes.get(0);
        TriangleMesh mesh = (TriangleMesh) texturedMesh.getMesh();
        if (mesh == null) {
            setScatterData(newPositions);
            return false;
        }

        // Get the points array
        javafx.collections.ObservableFloatArray points = mesh.getPoints();

        // Update each particle's vertices by applying delta
        for (int p = 0; p < newPositions.size(); p++) {
            Point3D oldPos = cachedPositions.get(p);
            Point3D newPos = newPositions.get(p);

            float dx = newPos.x - oldPos.x;
            float dy = newPos.y - oldPos.y;
            float dz = newPos.z - oldPos.z;

            // Skip if no change
            if (dx == 0 && dy == 0 && dz == 0) {
                continue;
            }

            // Update all vertices for this particle
            int baseIdx = p * verticesPerMarker * 3;
            for (int v = 0; v < verticesPerMarker; v++) {
                int idx = baseIdx + v * 3;
                points.set(idx, points.get(idx) + dx);
                points.set(idx + 1, points.get(idx + 1) + dy);
                points.set(idx + 2, points.get(idx + 2) + dz);
            }
        }

        // Update cached positions (copy the new positions)
        for (int i = 0; i < newPositions.size(); i++) {
            Point3D newPos = newPositions.get(i);
            Point3D cached = cachedPositions.get(i);
            cached.x = newPos.x;
            cached.y = newPos.y;
            cached.z = newPos.z;
        }

        // Update the property without triggering rebuild
        scatterData.set(newPositions);

        return true;
    }

    /**
     * Checks if efficient position update can be used.
     */
    private boolean canUseEfficientUpdate(List<Point3D> newPositions) {
        // Must be in joined segments mode
        if (!joinSegments.get()) {
            return false;
        }

        // Must have meshes created
        if (meshes == null || meshes.isEmpty()) {
            return false;
        }

        // Must have cached info from previous build
        if (verticesPerMarker <= 0 || cachedPositions == null) {
            return false;
        }

        // Particle count must match
        if (newPositions == null || newPositions.size() != cachedPositions.size()) {
            return false;
        }

        return true;
    }

    /**
     * Updates a single particle's position efficiently.
     * <p>
     * This is even more efficient than updatePositions() when only one or a few
     * particles need to move.
     *
     * @param particleIndex the index of the particle to update
     * @param newPosition the new position
     * @return true if update was successful, false if index out of bounds
     */
    public boolean updateParticlePosition(int particleIndex, Point3D newPosition) {
        if (!canUseEfficientUpdate(cachedPositions)) {
            return false;
        }

        if (particleIndex < 0 || particleIndex >= cachedPositions.size()) {
            return false;
        }

        TexturedMesh texturedMesh = meshes.get(0);
        TriangleMesh mesh = (TriangleMesh) texturedMesh.getMesh();
        if (mesh == null) {
            return false;
        }

        Point3D oldPos = cachedPositions.get(particleIndex);
        float dx = newPosition.x - oldPos.x;
        float dy = newPosition.y - oldPos.y;
        float dz = newPosition.z - oldPos.z;

        if (dx == 0 && dy == 0 && dz == 0) {
            return true; // No change needed
        }

        javafx.collections.ObservableFloatArray points = mesh.getPoints();
        int baseIdx = particleIndex * verticesPerMarker * 3;

        for (int v = 0; v < verticesPerMarker; v++) {
            int idx = baseIdx + v * 3;
            points.set(idx, points.get(idx) + dx);
            points.set(idx + 1, points.get(idx + 1) + dy);
            points.set(idx + 2, points.get(idx + 2) + dz);
        }

        // Update cached position
        oldPos.x = newPosition.x;
        oldPos.y = newPosition.y;
        oldPos.z = newPosition.z;

        return true;
    }

    /**
     * Gets the number of vertices per marker shape.
     * Useful for debugging and understanding mesh structure.
     *
     * @return vertices per marker, or -1 if not yet computed
     */
    public int getVerticesPerMarker() {
        return verticesPerMarker;
    }

    /**
     * Gets the total number of particles currently in the mesh.
     *
     * @return particle count, or 0 if no mesh exists
     */
    public int getParticleCount() {
        return cachedPositions != null ? cachedPositions.size() : 0;
    }

    /**
     * Caches mesh info after creation for efficient updates.
     */
    private void cacheMeshInfo() {
        if (!joinSegments.get() || meshes == null || meshes.isEmpty()) {
            verticesPerMarker = -1;
            cachedPositions = null;
            return;
        }

        List<Point3D> data = scatterData.get();
        if (data == null || data.isEmpty()) {
            verticesPerMarker = -1;
            cachedPositions = null;
            return;
        }

        // Calculate vertices per marker from the mesh
        TexturedMesh texturedMesh = meshes.get(0);
        TriangleMesh mesh = (TriangleMesh) texturedMesh.getMesh();
        if (mesh != null) {
            int totalVertices = mesh.getPoints().size() / 3;
            verticesPerMarker = totalVertices / data.size();

            // Cache positions (make copies to track deltas)
            cachedPositions = new ArrayList<>(data.size());
            for (Point3D p : data) {
                cachedPositions.add(new Point3D(p.x, p.y, p.z, p.f, p.colorIndex));
            }
        }
    }

    protected final void updateMesh() {

        meshes=FXCollections.<TexturedMesh>observableArrayList();

        createMarkers();
        if(joinSegments.get()){
//            System.out.println("Single mesh created");
        }
        getChildren().setAll(meshes);
        applyCullFace(cullFace.get());
        updateTransforms();

        // Cache mesh info for efficient position updates
        cacheMeshInfo();
    }

    private AtomicInteger index;
    private void createMarkers() {
        if(!joinSegments.get()){
            List<TexturedMesh> markers = new ArrayList<>();
            index = new AtomicInteger();
            scatterData.get().forEach(point3d ->
                    markers.add(getMarker().getMarker(getId() + "-" + index.getAndIncrement(), height.get(), level.get(), point3d)));
            meshes.addAll(markers);
        } else if (perParticleColor.get() && colorPaletteColors != null) {
            // Per-particle color mode (also supports scale): use colorIndex from each Point3D
            createMarkersWithPerParticleColor();
        } else if (perParticleScale.get()) {
            // Per-particle scale mode (without color): use scale from each Point3D
            createMarkersWithPerParticleScale();
        } else {
            // Standard mode: single color and size for all particles
            // Set marker id as f
            AtomicInteger i = new AtomicInteger();
            List<Point3D> indexedData = scatterData.get().stream()
                    .map(p -> new Point3D(p.x, p.y, p.z, i.getAndIncrement()))
                    .collect(Collectors.toList());

            TexturedMesh marker = getMarker().getMarker(getId(), height.get(), level.get(), indexedData.get(0));
            /*
            Combine new polyMesh with previous polyMesh into one single polyMesh
            */
            MeshHelper mh = new MeshHelper((TriangleMesh) marker.getMesh());
            TexturedMesh dot1 = getMarker().getMarker("", height.get(), level.get(), null);
            MeshHelper mh1 = new MeshHelper((TriangleMesh) dot1.getMesh());
            mh.addMesh(mh1, indexedData.stream().skip(1).collect(Collectors.toList()));
            marker.updateMesh(mh);
            meshes.add(marker);
        }
    }

    /**
     * Creates markers with per-particle color support.
     * Each particle uses its colorIndex to look up its color from the palette texture.
     * If per-particle opacity is enabled, the opacity field determines the V texture coordinate.
     */
    private void createMarkersWithPerParticleColor() {
        List<Point3D> data = scatterData.get();
        if (data.isEmpty()) {
            return;
        }

        // Create the first marker at the first point's position
        Point3D firstPoint = data.get(0);
        TexturedMesh marker = getMarker().getMarker(getId(), height.get(), level.get(), firstPoint);

        // Create template marker at origin for combining
        TexturedMesh templateMarker = getMarker().getMarker("", height.get(), level.get(), null);
        MeshHelper template = new MeshHelper((TriangleMesh) templateMarker.getMesh());

        // Build combined mesh
        MeshHelper mh = new MeshHelper((TriangleMesh) marker.getMesh());

        // For the first marker, we need to set its texCoords to use colorIndex and opacity
        // U = colorIndex/255 (picks column in texture)
        // V = opacity (picks row in texture, where 0=transparent, 1=opaque)
        float u = firstPoint.colorIndex / 255.0f;
        float v = perParticleOpacity.get() ? firstPoint.opacity : 0.5f;
        mh.setTexCoords(new float[]{u, v});

        // Update face texCoord indices to point to index 0
        int[] faces = mh.getFaces();
        for (int i = 1; i < faces.length; i += 2) {
            faces[i] = 0; // All texCoord references point to single entry
        }
        mh.setFaces(faces);

        // Add remaining particles with their colorIndex and opacity values
        if (data.size() > 1) {
            mh.addMeshWithColorIndex(template, data.subList(1, data.size()), perParticleOpacity.get());
        }

        marker.updateMesh(mh);
        meshes.add(marker);

        // Apply the palette texture
        applyPaletteTexture();
    }

    /**
     * Creates markers with per-particle scale support (without per-particle color).
     * Each particle uses its scale field to size its marker.
     */
    private void createMarkersWithPerParticleScale() {
        List<Point3D> data = scatterData.get();
        if (data.isEmpty()) {
            return;
        }

        // Create the first marker at the first point's position with scale
        Point3D firstPoint = data.get(0);
        float firstScale = firstPoint.scale > 0 ? firstPoint.scale : 1.0f;
        TexturedMesh marker = getMarker().getMarker(getId(), height.get() * firstScale, level.get(), firstPoint);

        if (data.size() == 1) {
            meshes.add(marker);
            return;
        }

        // Create template marker at origin (unscaled) for combining
        TexturedMesh templateMarker = getMarker().getMarker("", height.get(), level.get(), null);
        MeshHelper template = new MeshHelper((TriangleMesh) templateMarker.getMesh());

        // Build combined mesh starting with the first (already scaled) marker
        MeshHelper mh = new MeshHelper((TriangleMesh) marker.getMesh());

        // Add remaining particles with their scale values
        mh.addMeshWithScale(template, data.subList(1, data.size()));

        marker.updateMesh(mh);
        meshes.add(marker);
    }

    @Override
    public void setTextureModeNone() {
        meshes.stream().forEach(m->m.setTextureModeNone());
    }

    @Override
    public void setTextureModeNone(Color color) {
        meshes.stream().forEach(m->m.setTextureModeNone(color));
    }

    @Override
    public void setTextureModeNone(Color color, String image) {
        meshes.stream().forEach(m->m.setTextureModeNone(color,image));
    }

    @Override
    public void setTextureModeImage(String image) {
        meshes.stream().forEach(m->m.setTextureModeImage(image));
    }

    @Override
    public void setTextureModePattern(Patterns.CarbonPatterns pattern, double scale) {
        meshes.stream().forEach(m->m.setTextureModePattern(pattern, scale));
    }

    @Override
    public void setTextureModeVertices3D(int colors, Function<Point3D, Number> dens) {
        meshes.stream().forEach(m->m.setTextureModeVertices3D(colors, dens));
    }

    @Override
    public void setTextureModeVertices3D(ColorPalette palette, Function<Point3D, Number> dens) {
        meshes.stream().forEach(m->m.setTextureModeVertices3D(palette, dens));
    }

    @Override
    public void setTextureModeVertices3D(int colors, Function<Point3D, Number> dens, double min, double max) {
        meshes.stream().forEach(m->m.setTextureModeVertices3D(colors, dens, min, max));
    }

    @Override
    public void setTextureModeVertices1D(int colors, Function<Number, Number> function) {
        meshes.stream().forEach(m->m.setTextureModeVertices1D(colors, function));
    }

    @Override
    public void setTextureModeVertices1D(ColorPalette palette, Function<Number, Number> function) {
        meshes.stream().forEach(m->m.setTextureModeVertices1D(palette, function));
    }

    @Override
    public void setTextureModeVertices1D(int colors, Function<Number, Number> function, double min, double max) {
        meshes.stream().forEach(m->m.setTextureModeVertices1D(colors, function, min, max));
    }

    @Override
    public void setTextureModeFaces(int colors) {
        meshes.stream().forEach(m->m.setTextureModeFaces(colors));
    }

    @Override
    public void setTextureModeFaces(ColorPalette palette) {
        meshes.stream().forEach(m->m.setTextureModeFaces(palette));
    }

    @Override
    public void updateF(List<Number> values) {
        meshes.stream().forEach(m->m.updateF(values));
    }

    @Override
    public void setTextureOpacity(double value) {
        meshes.stream().forEach(m->m.setTextureOpacity(value));
    }

    public void setDrawMode(DrawMode mode) {
        meshes.stream().forEach(m->m.setDrawMode(mode));
    }

    private void updateTransforms() {
        meshes.stream().forEach(m->m.updateTransforms());
    }

    public TexturedMesh getMeshFromId(String id){
        return meshes.stream().filter(p->p.getId().equals(id)).findFirst().orElse(meshes.get(0));
    }

    public Color getDiffuseColor() {
        return meshes.stream()
                .findFirst()
                .map(m -> m.getDiffuseColor())
                .orElse(TriangleMeshHelper.DEFAULT_DIFFUSE_COLOR);
    }

}
