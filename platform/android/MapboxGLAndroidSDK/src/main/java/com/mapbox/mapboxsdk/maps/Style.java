package com.mapbox.mapboxsdk.maps;

import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.support.annotation.IntRange;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringDef;
import android.util.DisplayMetrics;
import com.mapbox.mapboxsdk.constants.MapboxConstants;
import com.mapbox.mapboxsdk.style.layers.Layer;
import com.mapbox.mapboxsdk.style.layers.TransitionOptions;
import com.mapbox.mapboxsdk.style.light.Light;
import com.mapbox.mapboxsdk.style.sources.Source;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The proxy object for current map style.
 * <p>
 * To create new instances of this object, create a new instance using a {@link Builder} and load the style with
 * {@link MapboxMap#setStyle(Builder)}. This object is returned from {@link MapboxMap#getStyle()} once the style
 * has been loaded by underlying map.
 * </p>
 */
@SuppressWarnings("unchecked")
public class Style {

  private final NativeMap nativeMap;
  private final HashMap<String, Source> sources = new HashMap<>();
  private final HashMap<String, Layer> layers = new HashMap<>();
  private final HashMap<String, Bitmap> images = new HashMap<>();
  private final Builder builder;
  private boolean fullyLoaded;

  /**
   * Private constructor to build a style object.
   *
   * @param builder   the builder used for creating this style
   * @param nativeMap the map object used to load this style
   */
  private Style(@NonNull Builder builder, @NonNull NativeMap nativeMap) {
    this.builder = builder;
    this.nativeMap = nativeMap;
  }

  /**
   * Returns the current style url.
   *
   * @return the style url
   */
  @NonNull
  public String getUrl() {
    validateState("getUrl");
    return nativeMap.getStyleUrl();
  }

  /**
   * Returns the current style json.
   *
   * @return the style json
   */
  @NonNull
  public String getJson() {
    validateState("getJson");
    return nativeMap.getStyleJson();
  }

  //
  // Source
  //

  /**
   * Retrieve all the sources in the style
   *
   * @return all the sources in the current style
   */
  @NonNull
  public List<Source> getSources() {
    validateState("getSources");
    return nativeMap.getSources();
  }

  /**
   * Adds the source to the map. The source must be newly created and not added to the map before
   *
   * @param source the source to add
   */
  public void addSource(@NonNull Source source) {
    validateState("addSource");
    sources.put(source.getId(), source);
    nativeMap.addSource(source);
  }

  /**
   * Retrieve a source by id
   *
   * @param id the source's id
   * @return the source if present in the current style
   */
  @Nullable
  public Source getSource(String id) {
    validateState("getSource");
    Source source = sources.get(id);
    if (source == null) {
      source = nativeMap.getSource(id);
    }
    return source;
  }

  /**
   * Tries to cast the Source to T, throws ClassCastException if it's another type.
   *
   * @param sourceId the id used to look up a layer
   * @param <T>      the generic type of a Source
   * @return the casted Source, null if another type
   */
  @Nullable
  public <T extends Source> T getSourceAs(@NonNull String sourceId) {
    validateState("getSourceAs");
    // noinspection unchecked
    if (sources.containsKey(sourceId)) {
      return (T) sources.get(sourceId);
    }
    return (T) nativeMap.getSource(sourceId);
  }

  /**
   * Removes the source from the style.
   *
   * @param sourceId the source to remove
   * @return the source handle or null if the source was not present
   */
  public boolean removeSource(@NonNull String sourceId) {
    validateState("removeSource");
    sources.remove(sourceId);
    return nativeMap.removeSource(sourceId);
  }

  /**
   * Removes the source, preserving the reference for re-use
   *
   * @param source the source to remove
   * @return the source
   */
  public boolean removeSource(@NonNull Source source) {
    validateState("removeSource");
    sources.remove(source.getId());
    return nativeMap.removeSource(source);
  }

  //
  // Layer
  //

  /**
   * Adds the layer to the map. The layer must be newly created and not added to the map before
   *
   * @param layer the layer to add
   */
  public void addLayer(@NonNull Layer layer) {
    validateState("addLayer");
    layers.put(layer.getId(), layer);
    nativeMap.addLayer(layer);
  }

  /**
   * Adds the layer to the map. The layer must be newly created and not added to the map before
   *
   * @param layer the layer to add
   * @param below the layer id to add this layer before
   */
  public void addLayerBelow(@NonNull Layer layer, @NonNull String below) {
    validateState("addLayerBelow");
    layers.put(layer.getId(), layer);
    nativeMap.addLayerBelow(layer, below);
  }

  /**
   * Adds the layer to the map. The layer must be newly created and not added to the map before
   *
   * @param layer the layer to add
   * @param above the layer id to add this layer above
   */
  public void addLayerAbove(@NonNull Layer layer, @NonNull String above) {
    validateState("addLayerAbove");
    layers.put(layer.getId(), layer);
    nativeMap.addLayerAbove(layer, above);
  }

  /**
   * Adds the layer to the map at the specified index. The layer must be newly
   * created and not added to the map before
   *
   * @param layer the layer to add
   * @param index the index to insert the layer at
   */
  public void addLayerAt(@NonNull Layer layer, @IntRange(from = 0) int index) {
    validateState("addLayerAbove");
    layers.put(layer.getId(), layer);
    nativeMap.addLayerAt(layer, index);
  }

  /**
   * Get the layer by id
   *
   * @param id the layer's id
   * @return the layer, if present in the style
   */
  @Nullable
  public Layer getLayer(@NonNull String id) {
    validateState("getLayer");
    Layer layer = layers.get(id);
    if (layer == null) {
      layer = nativeMap.getLayer(id);
    }
    return layer;
  }

  /**
   * Tries to cast the Layer to T, throws ClassCastException if it's another type.
   *
   * @param layerId the layer id used to look up a layer
   * @param <T>     the generic attribute of a Layer
   * @return the casted Layer, null if another type
   */
  @Nullable
  public <T extends Layer> T getLayerAs(@NonNull String layerId) {
    validateState("getLayerAs");
    // noinspection unchecked
    return (T) nativeMap.getLayer(layerId);
  }

  /**
   * Retrieve all the layers in the style
   *
   * @return all the layers in the current style
   */
  @NonNull
  public List<Layer> getLayers() {
    validateState("getLayers");
    return nativeMap.getLayers();
  }

  /**
   * Removes the layer. Any references to the layer become invalid and should not be used anymore
   *
   * @param layerId the layer to remove
   * @return the removed layer or null if not found
   */
  public boolean removeLayer(@NonNull String layerId) {
    validateState("removeLayer");
    layers.remove(layerId);
    return nativeMap.removeLayer(layerId);
  }

  /**
   * Removes the layer. The reference is re-usable after this and can be re-added
   *
   * @param layer the layer to remove
   * @return the layer
   */
  public boolean removeLayer(@NonNull Layer layer) {
    validateState("removeLayer");
    layers.remove(layer.getId());
    return nativeMap.removeLayer(layer);
  }

  /**
   * Removes the layer. Any other references to the layer become invalid and should not be used anymore
   *
   * @param index the layer index
   * @return the removed layer or null if not found
   */
  public boolean removeLayerAt(@IntRange(from = 0) int index) {
    validateState("removeLayerAt");
    return nativeMap.removeLayerAt(index);
  }

  //
  // Image
  //

  /**
   * Adds an image to be used in the map's style
   *
   * @param name  the name of the image
   * @param image the pre-multiplied Bitmap
   */
  public void addImage(@NonNull String name, @NonNull Bitmap image) {
    addImage(name, image, false);
  }

  /**
   * Adds an image to be used in the map's style
   *
   * @param name   the name of the image
   * @param bitmap the pre-multiplied Bitmap
   * @param sdf    the flag indicating image is an SDF or template image
   */
  public void addImage(@NonNull final String name, @NonNull final Bitmap bitmap, boolean sdf) {
    validateState("addImage");
    new BitmapImageConversionTask(nativeMap, sdf).execute(new HashMap<String, Bitmap>() {
      {
        put(name, bitmap);
      }
    });
  }

  /**
   * Adds an images to be used in the map's style.
   */
  public void addImages(@NonNull HashMap<String, Bitmap> images) {
    addImages(images, false);
  }

  /**
   * Adds an images to be used in the map's style.
   */
  public void addImages(@NonNull HashMap<String, Bitmap> images, boolean sdf) {
    validateState("addImages");
    new BitmapImageConversionTask(nativeMap, sdf).execute(images);
  }

  /**
   * Removes an image from the map's style.
   *
   * @param name the name of the image to remove
   */
  public void removeImage(@NonNull String name) {
    validateState("removeImage");
    nativeMap.removeImage(name);
  }

  /**
   * Get an image from the map's style using an id.
   *
   * @param id the id of the image
   * @return the image bitmap
   */
  @Nullable
  public Bitmap getImage(@NonNull String id) {
    validateState("getImage");
    return nativeMap.getImage(id);
  }

  //
  // Transition
  //

  /**
   * <p>
   * Set the transition duration for style changes.
   * </p>
   * The default value for delay and duration is zero, so any changes take effect without animation.
   *
   * @param transitionOptions the transition options
   */
  public void setTransition(@NonNull TransitionOptions transitionOptions) {
    validateState("setTransition");
    nativeMap.setTransitionOptions(transitionOptions);
  }

  /**
   * <p>
   * Get the transition for style changes.
   * </p>
   * The default value for delay and transition is zero, so any changes take effect without animation.
   *
   * @return TransitionOptions the transition options
   */
  @NonNull
  public TransitionOptions getTransition() {
    validateState("getTransition");
    return nativeMap.getTransitionOptions();
  }

  //
  // Light
  //

  /**
   * Get the light source used to change lighting conditions on extruded fill layers.
   *
   * @return the global light source
   */
  @Nullable
  public Light getLight() {
    validateState("getLight");
    return nativeMap.getLight();
  }

  //
  // State
  //

  /**
   * Called when the underlying map will start loading a new style. This method will clean up this style
   * by setting the java sources and layers in a detached state and removing them from core.
   */
  void onWillStartLoadingMap() {
    fullyLoaded = false;
    for (Source source : sources.values()) {
      if (source != null) {
        source.setDetached();
        nativeMap.removeSource(source);
      }
    }

    for (Layer layer : layers.values()) {
      if (layer != null) {
        layer.setDetached();
        nativeMap.removeLayer(layer);
      }
    }

    for (Map.Entry<String, Bitmap> bitmapEntry : images.entrySet()) {
      nativeMap.removeImage(bitmapEntry.getKey());
      bitmapEntry.getValue().recycle();
    }

    sources.clear();
    layers.clear();
    images.clear();
  }

  /**
   * Called when the underlying map has finished loading this style.
   * This method will add all components added to the builder that were defined with the 'with' prefix.
   */
  void onDidFinishLoadingStyle() {
    if (!fullyLoaded) {
      fullyLoaded = true;
      for (Source source : builder.sources) {
        addSource(source);
      }

      for (Builder.LayerWrapper layerWrapper : builder.layers) {
        if (layerWrapper instanceof Builder.LayerAtWrapper) {
          addLayerAt(layerWrapper.layer, ((Builder.LayerAtWrapper) layerWrapper).index);
        } else if (layerWrapper instanceof Builder.LayerAboveWrapper) {
          addLayerAbove(layerWrapper.layer, ((Builder.LayerAboveWrapper) layerWrapper).aboveLayer);
        } else if (layerWrapper instanceof Builder.LayerBelowWrapper) {
          addLayerBelow(layerWrapper.layer, ((Builder.LayerBelowWrapper) layerWrapper).belowLayer);
        } else {
          // just add layer to map, but below annotations
          addLayerBelow(layerWrapper.layer, MapboxConstants.LAYER_ID_ANNOTATIONS);
        }
      }

      for (Builder.ImageWrapper image : builder.images) {
        addImage(image.id, image.bitmap, image.sdf);
      }

      if (builder.transitionOptions != null) {
        setTransition(builder.transitionOptions);
      }
    }
  }

  /**
   * Returns true if the style is fully loaded. Returns false if style hasn't been fully loaded or a new style is
   * underway of being loaded.
   *
   * @return True if fully loaded, false otherwise
   */
  public boolean isFullyLoaded() {
    return fullyLoaded;
  }

  /**
   * Validates the style state, throw an IllegalArgumentException on invalid state.
   *
   * @param methodCall the calling method name
   */
  private void validateState(String methodCall) {
    if (!fullyLoaded) {
      throw new IllegalStateException(
        String.format("Calling %s when a newer style is loading/has loaded.", methodCall)
      );
    }
  }

  //
  // Builder
  //

  /**
   * Builder for composing a style object.
   */
  public static class Builder {

    private final List<Source> sources = new ArrayList<>();
    private final List<LayerWrapper> layers = new ArrayList<>();
    private final List<ImageWrapper> images = new ArrayList<>();

    private TransitionOptions transitionOptions;
    private String styleUrl;
    private String styleJson;

    /**
     * <p>
     * Will loads a new map style asynchronous from the specified URL.
     * </p>
     * {@code url} can take the following forms:
     * <ul>
     * <li>{@code Style#StyleUrl}: load one of the bundled styles in {@link Style}.</li>
     * <li>{@code mapbox://styles/<user>/<style>}:
     * loads the style from a <a href="https://www.mapbox.com/account/">Mapbox account.</a>
     * {@code user} is your username. {@code style} is the ID of your custom
     * style created in <a href="https://www.mapbox.com/studio">Mapbox Studio</a>.</li>
     * <li>{@code http://...} or {@code https://...}:
     * loads the style over the Internet from any web server.</li>
     * <li>{@code asset://...}:
     * loads the style from the APK {@code assets/} directory.
     * This is used to load a style bundled with your app.</li>
     * <li>{@code file://...}:
     * loads the style from a file path. This is used to load a style from disk.
     * </li>
     * </li>
     * <li>{@code null}: loads the default {@link Style#MAPBOX_STREETS} style.</li>
     * </ul>
     * <p>
     * This method is asynchronous and will return before the style finishes loading.
     * If you wish to wait for the map to finish loading, listen to the {@link MapView.OnDidFinishLoadingStyleListener}
     * callback or use {@link MapboxMap#setStyle(String, OnStyleLoaded)} instead.
     * </p>
     * If the style fails to load or an invalid style URL is set, the map view will become blank.
     * An error message will be logged in the Android logcat and {@link MapView.OnDidFailLoadingMapListener} callback
     * will be triggered.
     *
     * @param url The URL of the map style
     * @return this
     * @see Style
     */
    @NonNull
    public Builder fromUrl(@NonNull String url) {
      this.styleUrl = url;
      return this;
    }

    /**
     * Will load a new map style from a json string.
     * <p>
     * If the style fails to load or an invalid style URL is set, the map view will become blank.
     * An error message will be logged in the Android logcat and {@link MapView.OnDidFailLoadingMapListener} callback
     * will be triggered.
     * </p>
     *
     * @return this
     */
    @NonNull
    public Builder fromJson(@NonNull String styleJson) {
      this.styleJson = styleJson;
      return this;
    }

    /**
     * Will add the source when map style has loaded.
     *
     * @param source the source to add
     * @return this
     */
    @NonNull
    public Builder withSource(@NonNull Source source) {
      sources.add(source);
      return this;
    }

    /**
     * Will add the layer when the style has loaded.
     *
     * @param layer the layer to be added
     * @return this
     */
    @NonNull
    public Builder withLayer(@NonNull Layer layer) {
      layers.add(new LayerWrapper(layer));
      return this;
    }

    /**
     * Will add the layer when the style has loaded at a specified index.
     *
     * @param layer the layer to be added
     * @return this
     */
    @NonNull
    public Builder withLayerAt(@NonNull Layer layer, int index) {
      layers.add(new LayerAtWrapper(layer, index));
      return this;
    }

    /**
     * Will add the layer when the style has loaded above a specified layer id.
     *
     * @param layer the layer to be added
     * @return this
     */
    @NonNull
    public Builder withLayerAbove(@NonNull Layer layer, @NonNull String aboveLayerId) {
      layers.add(new LayerAboveWrapper(layer, aboveLayerId));
      return this;
    }

    /**
     * Will add the layer when the style has loaded below a specified layer id.
     *
     * @param layer the layer to be added
     * @return this
     */
    @NonNull
    public Builder withLayerBelow(@NonNull Layer layer, @NonNull String belowLayerId) {
      layers.add(new LayerBelowWrapper(layer, belowLayerId));
      return this;
    }

    /**
     * Will add the transition when the map style has loaded.
     *
     * @param transition the transition to be added
     * @return this
     */
    @NonNull
    public Builder withTransition(@NonNull TransitionOptions transition) {
      this.transitionOptions = transition;
      return this;
    }

    /**
     * Will add the image when the map style has loaded.
     *
     * @param id    the id for the image
     * @param image the image to be added
     * @return this
     */
    @NonNull
    public Builder withImage(@NonNull String id, @NonNull Bitmap image) {
      return this.withImage(id, image, false);
    }

    /**
     * Will add the image when the map style has loaded.
     *
     * @param id    the id for the image
     * @param image the image to be added
     * @return this
     */
    @NonNull
    public Builder withImage(@NonNull String id, @NonNull Bitmap image, boolean sdf) {
      images.add(new ImageWrapper(id, image, sdf));
      return this;
    }

    String getUrl() {
      return styleUrl;
    }

    String getJson() {
      return styleJson;
    }

    List<Source> getSources() {
      return sources;
    }

    List<LayerWrapper> getLayers() {
      return layers;
    }

    List<ImageWrapper> getImages() {
      return images;
    }

    TransitionOptions getTransitionOptions() {
      return transitionOptions;
    }

    /**
     * Build the composed style.
     */
    Style build(@NonNull NativeMap nativeMap) {
      return new Style(this, nativeMap);
    }

    class ImageWrapper {
      Bitmap bitmap;
      String id;
      boolean sdf;

      ImageWrapper(String id, Bitmap bitmap, boolean sdf) {
        this.id = id;
        this.bitmap = bitmap;
        this.sdf = sdf;
      }
    }

    class LayerWrapper {
      Layer layer;

      LayerWrapper(Layer layer) {
        this.layer = layer;
      }
    }

    class LayerAboveWrapper extends LayerWrapper {
      String aboveLayer;

      LayerAboveWrapper(Layer layer, String aboveLayer) {
        super(layer);
        this.aboveLayer = aboveLayer;
      }
    }

    class LayerBelowWrapper extends LayerWrapper {
      String belowLayer;

      LayerBelowWrapper(Layer layer, String belowLayer) {
        super(layer);
        this.belowLayer = belowLayer;
      }
    }

    class LayerAtWrapper extends LayerWrapper {
      int index;

      LayerAtWrapper(Layer layer, int index) {
        super(layer);
        this.index = index;
      }
    }
  }

  private static class BitmapImageConversionTask extends AsyncTask<HashMap<String, Bitmap>, Void, List<Image>> {

    private NativeMap nativeMap;
    private boolean sdf;

    BitmapImageConversionTask(NativeMap nativeMap, boolean sdf) {
      this.nativeMap = nativeMap;
      this.sdf = sdf;
    }

    @NonNull
    @Override
    protected List<Image> doInBackground(HashMap<String, Bitmap>... params) {
      HashMap<String, Bitmap> bitmapHashMap = params[0];

      List<Image> images = new ArrayList<>();
      ByteBuffer buffer;
      String name;
      Bitmap bitmap;

      for (Map.Entry<String, Bitmap> stringBitmapEntry : bitmapHashMap.entrySet()) {
        name = stringBitmapEntry.getKey();
        bitmap = stringBitmapEntry.getValue();

        if (bitmap.getConfig() != Bitmap.Config.ARGB_8888) {
          bitmap = bitmap.copy(Bitmap.Config.ARGB_8888, false);
        }

        buffer = ByteBuffer.allocate(bitmap.getByteCount());
        bitmap.copyPixelsToBuffer(buffer);

        float pixelRatio = (float) bitmap.getDensity() / DisplayMetrics.DENSITY_DEFAULT;

        images.add(new Image(buffer.array(), pixelRatio, name, bitmap.getWidth(), bitmap.getHeight(), sdf));
      }

      return images;
    }

    @Override
    protected void onPostExecute(@NonNull List<Image> images) {
      super.onPostExecute(images);
      if (nativeMap != null) {
        nativeMap.addImages(images.toArray(new Image[images.size()]));
      }
    }
  }

  /**
   * Callback to be invoked when a style has finished loading.
   */
  public interface OnStyleLoaded {
    /**
     * Invoked when a style has finished loading.
     *
     * @param style the style that has finished loading
     */
    void onStyleLoaded(@NonNull Style style);
  }

  //
  // Style URL constants
  //

  /**
   * Indicates the parameter accepts one of the values from Style. Using one of these
   * constants means your map style will always use the latest version and may change as we
   * improve the style
   */
  @StringDef( {MAPBOX_STREETS, OUTDOORS, LIGHT, DARK, SATELLITE, SATELLITE_STREETS, TRAFFIC_DAY, TRAFFIC_NIGHT})
  @Retention(RetentionPolicy.SOURCE)
  public @interface StyleUrl {
  }

  // IMPORTANT: If you change any of these you also need to edit them in strings.xml

  /**
   * Mapbox Streets: A complete basemap, perfect for incorporating your own data. Using this
   * constant means your map style will always use the latest version and may change as we
   * improve the style.
   */
  public static final String MAPBOX_STREETS = "mapbox://styles/mapbox/streets-v11";

  /**
   * Outdoors: A general-purpose style tailored to outdoor activities. Using this constant means
   * your map style will always use the latest version and may change as we improve the style.
   */
  public static final String OUTDOORS = "mapbox://styles/mapbox/outdoors-v11";

  /**
   * Light: Subtle light backdrop for data visualizations. Using this constant means your map
   * style will always use the latest version and may change as we improve the style.
   */
  public static final String LIGHT = "mapbox://styles/mapbox/light-v10";

  /**
   * Dark: Subtle dark backdrop for data visualizations. Using this constant means your map style
   * will always use the latest version and may change as we improve the style.
   */
  public static final String DARK = "mapbox://styles/mapbox/dark-v10";

  /**
   * Satellite: A beautiful global satellite and aerial imagery layer. Using this constant means
   * your map style will always use the latest version and may change as we improve the style.
   */
  public static final String SATELLITE = "mapbox://styles/mapbox/satellite-v9";

  /**
   * Satellite Streets: Global satellite and aerial imagery with unobtrusive labels. Using this
   * constant means your map style will always use the latest version and may change as we
   * improve the style.
   */
  public static final String SATELLITE_STREETS = "mapbox://styles/mapbox/satellite-streets-v11";

  /**
   * Traffic Day: Color-coded roads based on live traffic congestion data. Traffic data is currently
   * available in
   * <a href="https://www.mapbox.com/api-documentation/pages/traffic-countries.html">these select
   * countries</a>. Using this constant means your map style will always use the latest version and
   * may change as we improve the style.
   */
  public static final String TRAFFIC_DAY = "mapbox://styles/mapbox/traffic-day-v2";

  /**
   * Traffic Night: Color-coded roads based on live traffic congestion data, designed to maximize
   * legibility in low-light situations. Traffic data is currently available in
   * <a href="https://www.mapbox.com/api-documentation/pages/traffic-countries.html">these select
   * countries</a>. Using this constant means your map style will always use the latest version and
   * may change as we improve the style.
   */
  public static final String TRAFFIC_NIGHT = "mapbox://styles/mapbox/traffic-night-v2";
}
