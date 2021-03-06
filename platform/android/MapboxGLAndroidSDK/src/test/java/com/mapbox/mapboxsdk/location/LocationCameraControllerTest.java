package com.mapbox.mapboxsdk.location;

import android.graphics.PointF;
import android.location.Location;

import com.mapbox.android.gestures.AndroidGesturesManager;
import com.mapbox.android.gestures.MoveGestureDetector;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.camera.CameraUpdate;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.location.modes.CameraMode;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.Projection;
import com.mapbox.mapboxsdk.maps.UiSettings;

import junit.framework.Assert;

import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class LocationCameraControllerTest {

  @Test
  public void setCameraMode_mapTransitionsAreCancelled() {
    MapboxMap mapboxMap = mock(MapboxMap.class);
    LocationCameraController camera = buildCamera(mapboxMap);
    camera.initializeOptions(mock(LocationComponentOptions.class));

    camera.setCameraMode(CameraMode.TRACKING_GPS);

    verify(mapboxMap).cancelTransitions();
  }

  @Test
  public void setCameraMode_gestureThresholdIsAdjusted() {
    MoveGestureDetector moveGestureDetector = mock(MoveGestureDetector.class);
    LocationCameraController camera = buildCamera(moveGestureDetector);
    LocationComponentOptions options = mock(LocationComponentOptions.class);
    float moveThreshold = 5f;
    when(options.trackingInitialMoveThreshold()).thenReturn(moveThreshold);
    when(options.trackingGesturesManagement()).thenReturn(true);
    camera.initializeOptions(options);

    camera.setCameraMode(CameraMode.TRACKING_GPS);

    verify(moveGestureDetector).setMoveThreshold(moveThreshold);
  }

  @Test
  public void setCameraMode_gestureThresholdNotAdjustedWhenDisabled() {
    MoveGestureDetector moveGestureDetector = mock(MoveGestureDetector.class);
    LocationCameraController camera = buildCamera(moveGestureDetector);
    LocationComponentOptions options = mock(LocationComponentOptions.class);
    float moveThreshold = 5f;
    when(options.trackingInitialMoveThreshold()).thenReturn(moveThreshold);
    when(options.trackingGesturesManagement()).thenReturn(false);
    camera.initializeOptions(options);

    camera.setCameraMode(CameraMode.TRACKING_GPS);

    verify(moveGestureDetector, times(0)).setMoveThreshold(moveThreshold);
    verify(moveGestureDetector, times(0)).setMoveThreshold(0f);
  }

  @Test
  public void setCameraMode_gestureThresholdIsResetWhenNotTracking() {
    MoveGestureDetector moveGestureDetector = mock(MoveGestureDetector.class);
    LocationCameraController camera = buildCamera(moveGestureDetector);
    LocationComponentOptions options = mock(LocationComponentOptions.class);
    when(options.trackingGesturesManagement()).thenReturn(true);
    camera.initializeOptions(options);

    camera.setCameraMode(CameraMode.NONE);

    verify(moveGestureDetector, times(2)).setMoveThreshold(0f); // one for initialization
  }

  @Test
  public void setCameraMode_notTrackingAdjustsFocalPoint() {
    MapboxMap mapboxMap = mock(MapboxMap.class);
    when(mapboxMap.getUiSettings()).thenReturn(mock(UiSettings.class));
    LocationCameraController camera = buildCamera(mapboxMap);
    camera.initializeOptions(mock(LocationComponentOptions.class));

    camera.setCameraMode(CameraMode.TRACKING_GPS);
    camera.setCameraMode(CameraMode.NONE);

    verify(mapboxMap.getUiSettings()).setFocalPoint(null);
  }

  @Test
  public void setCameraMode_trackingChangeListenerCameraDismissedIsCalled() {
    OnCameraTrackingChangedListener internalTrackingChangedListener = mock(OnCameraTrackingChangedListener.class);
    LocationCameraController camera = buildCamera(internalTrackingChangedListener);
    camera.initializeOptions(mock(LocationComponentOptions.class));

    camera.setCameraMode(CameraMode.TRACKING_GPS);
    camera.setCameraMode(CameraMode.NONE);

    verify(internalTrackingChangedListener).onCameraTrackingDismissed();
  }

  @Test
  public void setCameraMode_internalCameraTrackingChangeListenerIsCalled() {
    OnCameraTrackingChangedListener internalTrackingChangedListener = mock(OnCameraTrackingChangedListener.class);
    LocationCameraController camera = buildCamera(internalTrackingChangedListener);
    camera.initializeOptions(mock(LocationComponentOptions.class));
    int cameraMode = CameraMode.NONE;

    camera.setCameraMode(cameraMode);

    verify(internalTrackingChangedListener).onCameraTrackingChanged(cameraMode);
  }

  @Test
  public void setCameraMode_cancelTransitionsWhenSet() {
    MapboxMap mapboxMap = mock(MapboxMap.class);
    when(mapboxMap.getUiSettings()).thenReturn(mock(UiSettings.class));
    when(mapboxMap.getProjection()).thenReturn(mock(Projection.class));
    LocationCameraController camera = buildCamera(mapboxMap);
    camera.initializeOptions(mock(LocationComponentOptions.class));

    camera.setCameraMode(CameraMode.NONE_COMPASS);
    verify(mapboxMap, times(1)).cancelTransitions();

    camera.setCameraMode(CameraMode.NONE_GPS);
    verify(mapboxMap, times(2)).cancelTransitions();

    camera.setCameraMode(CameraMode.TRACKING);
    verify(mapboxMap, times(3)).cancelTransitions();

    camera.setCameraMode(CameraMode.TRACKING_COMPASS);
    verify(mapboxMap, times(4)).cancelTransitions();

    camera.setCameraMode(CameraMode.TRACKING_GPS);
    verify(mapboxMap, times(5)).cancelTransitions();

    camera.setCameraMode(CameraMode.TRACKING_GPS_NORTH);
    verify(mapboxMap, times(6)).cancelTransitions();
  }

  @Test
  public void setCameraMode_dontCancelTransitionsWhenNoneSet() {
    MapboxMap mapboxMap = mock(MapboxMap.class);
    when(mapboxMap.getUiSettings()).thenReturn(mock(UiSettings.class));
    when(mapboxMap.getProjection()).thenReturn(mock(Projection.class));
    LocationCameraController camera = buildCamera(mapboxMap);
    camera.initializeOptions(mock(LocationComponentOptions.class));

    camera.setCameraMode(CameraMode.NONE);
    verify(mapboxMap, never()).cancelTransitions();
  }

  @Test
  public void onNewLatLngValue_cameraModeTrackingUpdatesLatLng() {
    MapboxMap mapboxMap = mock(MapboxMap.class);
    when(mapboxMap.getUiSettings()).thenReturn(mock(UiSettings.class));
    when(mapboxMap.getProjection()).thenReturn(mock(Projection.class));
    LocationCameraController camera = buildCamera(mapboxMap);
    camera.initializeOptions(mock(LocationComponentOptions.class));
    camera.setCameraMode(CameraMode.TRACKING);
    LatLng latLng = mock(LatLng.class);

    camera.onNewLatLngValue(latLng);

    verify(mapboxMap).moveCamera(any(CameraUpdate.class));
  }

  @Test
  public void onNewLatLngValue_cameraModeTrackingGpsNorthUpdatesLatLng() {
    MapboxMap mapboxMap = mock(MapboxMap.class);
    when(mapboxMap.getUiSettings()).thenReturn(mock(UiSettings.class));
    when(mapboxMap.getProjection()).thenReturn(mock(Projection.class));
    LocationCameraController camera = buildCamera(mapboxMap);
    camera.initializeOptions(mock(LocationComponentOptions.class));
    camera.setCameraMode(CameraMode.TRACKING_GPS_NORTH);
    LatLng latLng = mock(LatLng.class);

    camera.onNewLatLngValue(latLng);

    verify(mapboxMap).moveCamera(any(CameraUpdate.class));
  }

  @Test
  public void onNewLatLngValue_cameraModeTrackingGpsUpdatesLatLng() {
    MapboxMap mapboxMap = mock(MapboxMap.class);
    when(mapboxMap.getUiSettings()).thenReturn(mock(UiSettings.class));
    when(mapboxMap.getProjection()).thenReturn(mock(Projection.class));
    LocationCameraController camera = buildCamera(mapboxMap);
    camera.initializeOptions(mock(LocationComponentOptions.class));
    camera.setCameraMode(CameraMode.TRACKING_GPS);
    LatLng latLng = mock(LatLng.class);

    camera.onNewLatLngValue(latLng);

    verify(mapboxMap).moveCamera(any(CameraUpdate.class));
  }

  @Test
  public void onNewLatLngValue_cameraModeTrackingCompassUpdatesLatLng() {
    MapboxMap mapboxMap = mock(MapboxMap.class);
    when(mapboxMap.getUiSettings()).thenReturn(mock(UiSettings.class));
    when(mapboxMap.getProjection()).thenReturn(mock(Projection.class));
    LocationCameraController camera = buildCamera(mapboxMap);
    camera.initializeOptions(mock(LocationComponentOptions.class));
    camera.setCameraMode(CameraMode.TRACKING_COMPASS);
    LatLng latLng = mock(LatLng.class);

    camera.onNewLatLngValue(latLng);

    verify(mapboxMap).moveCamera(any(CameraUpdate.class));
  }

  @Test
  public void onNewLatLngValue_cameraModeNoneIgnored() {
    MapboxMap mapboxMap = mock(MapboxMap.class);
    when(mapboxMap.getUiSettings()).thenReturn(mock(UiSettings.class));
    when(mapboxMap.getProjection()).thenReturn(mock(Projection.class));
    LocationCameraController camera = buildCamera(mapboxMap);
    camera.initializeOptions(mock(LocationComponentOptions.class));
    camera.setCameraMode(CameraMode.NONE);
    LatLng latLng = mock(LatLng.class);

    camera.onNewLatLngValue(latLng);

    verify(mapboxMap, times(0)).moveCamera(any(CameraUpdate.class));
  }

  @Test
  public void onNewLatLngValue_focalPointIsAdjusted() {
    MapboxMap mapboxMap = mock(MapboxMap.class);
    UiSettings uiSettings = mock(UiSettings.class);
    when(mapboxMap.getUiSettings()).thenReturn(uiSettings);
    Projection projection = mock(Projection.class);
    PointF pointF = mock(PointF.class);
    when(projection.toScreenLocation(any(LatLng.class))).thenReturn(pointF);
    when(mapboxMap.getProjection()).thenReturn(projection);
    LocationCameraController camera = buildCamera(mapboxMap);
    LocationComponentOptions options = mock(LocationComponentOptions.class);
    when(options.trackingGesturesManagement()).thenReturn(true);
    camera.initializeOptions(options);
    camera.setCameraMode(CameraMode.TRACKING);
    LatLng latLng = mock(LatLng.class);

    camera.onNewLatLngValue(latLng);

    verify(uiSettings).setFocalPoint(pointF);
  }

  @Test
  public void onNewGpsBearingValue_cameraModeTrackingGpsUpdatesBearing() {
    MapboxMap mapboxMap = mock(MapboxMap.class);
    LocationCameraController camera = buildCamera(mapboxMap);
    camera.initializeOptions(mock(LocationComponentOptions.class));
    camera.setCameraMode(CameraMode.TRACKING_GPS);
    float gpsBearing = 5f;

    camera.onNewGpsBearingValue(gpsBearing);

    verify(mapboxMap).moveCamera(any(CameraUpdate.class));
  }

  @Test
  public void onNewGpsBearingValue_cameraModeNoneGpsUpdatesBearing() {
    MapboxMap mapboxMap = mock(MapboxMap.class);
    LocationCameraController camera = buildCamera(mapboxMap);
    camera.initializeOptions(mock(LocationComponentOptions.class));
    camera.setCameraMode(CameraMode.NONE_GPS);
    float gpsBearing = 5f;

    camera.onNewGpsBearingValue(gpsBearing);

    verify(mapboxMap).moveCamera(any(CameraUpdate.class));
  }

  @Test
  public void onNewGpsBearingValue_cameraModeTrackingNorthUpdatesBearing() {
    MapboxMap mapboxMap = mock(MapboxMap.class);
    CameraPosition cameraPosition = new CameraPosition.Builder().bearing(7d).build();
    when(mapboxMap.getCameraPosition()).thenReturn(cameraPosition);
    LocationCameraController camera = buildCamera(mapboxMap);
    camera.initializeOptions(mock(LocationComponentOptions.class));
    camera.setCameraMode(CameraMode.TRACKING_GPS_NORTH);
    float gpsBearing = 5f;

    camera.onNewGpsBearingValue(gpsBearing);

    verify(mapboxMap).moveCamera(any(CameraUpdate.class));
  }

  @Test
  public void onNewGpsBearingValue_cameraModeTrackingNorthBearingZeroIgnored() {
    MapboxMap mapboxMap = mock(MapboxMap.class);
    CameraPosition cameraPosition = new CameraPosition.Builder().bearing(0d).build();
    when(mapboxMap.getCameraPosition()).thenReturn(cameraPosition);
    LocationCameraController camera = buildCamera(mapboxMap);
    camera.initializeOptions(mock(LocationComponentOptions.class));
    camera.setCameraMode(CameraMode.TRACKING_GPS_NORTH);
    float gpsBearing = 5f;

    camera.onNewGpsBearingValue(gpsBearing);

    verify(mapboxMap, times(0)).moveCamera(any(CameraUpdate.class));
  }

  @Test
  public void onNewGpsBearingValue_cameraModeNoneIgnored() {
    MapboxMap mapboxMap = mock(MapboxMap.class);
    LocationCameraController camera = buildCamera(mapboxMap);
    camera.initializeOptions(mock(LocationComponentOptions.class));
    camera.setCameraMode(CameraMode.NONE);
    float gpsBearing = 5f;

    camera.onNewGpsBearingValue(gpsBearing);

    verify(mapboxMap, times(0)).moveCamera(any(CameraUpdate.class));
  }

  @Test
  public void onNewCompassBearingValue_cameraModeTrackingCompassUpdatesBearing() {
    MapboxMap mapboxMap = mock(MapboxMap.class);
    LocationCameraController camera = buildCamera(mapboxMap);
    camera.initializeOptions(mock(LocationComponentOptions.class));
    camera.setCameraMode(CameraMode.TRACKING_COMPASS);
    float compassBearing = 5f;

    camera.onNewCompassBearingValue(compassBearing);

    verify(mapboxMap).moveCamera(any(CameraUpdate.class));
  }

  @Test
  public void onNewCompassBearingValue_cameraModeNoneCompassUpdatesBearing() {
    MapboxMap mapboxMap = mock(MapboxMap.class);
    LocationCameraController camera = buildCamera(mapboxMap);
    camera.initializeOptions(mock(LocationComponentOptions.class));
    camera.setCameraMode(CameraMode.NONE_COMPASS);
    float compassBearing = 5f;

    camera.onNewCompassBearingValue(compassBearing);

    verify(mapboxMap).moveCamera(any(CameraUpdate.class));
  }

  @Test
  public void onNewCompassBearingValue_cameraModeNoneIgnored() {
    MapboxMap mapboxMap = mock(MapboxMap.class);
    LocationCameraController camera = buildCamera(mapboxMap);
    camera.initializeOptions(mock(LocationComponentOptions.class));
    camera.setCameraMode(CameraMode.NONE);
    float compassBearing = 5f;

    camera.onNewCompassBearingValue(compassBearing);

    verify(mapboxMap, times(0)).moveCamera(any(CameraUpdate.class));
  }

  @Test
  public void onNewZoomValue_cameraIsUpdated() {
    MapboxMap mapboxMap = mock(MapboxMap.class);
    LocationCameraController camera = buildCamera(mapboxMap);
    camera.initializeOptions(mock(LocationComponentOptions.class));
    camera.setCameraMode(CameraMode.TRACKING);
    float zoom = 5f;

    camera.onNewZoomValue(zoom);

    verify(mapboxMap).moveCamera(any(CameraUpdate.class));
  }

  @Test
  public void gesturesManagement_enabled() {
    MapboxMap mapboxMap = mock(MapboxMap.class);
    AndroidGesturesManager initialGesturesManager = mock(AndroidGesturesManager.class);
    AndroidGesturesManager internalGesturesManager = mock(AndroidGesturesManager.class);
    LocationCameraController camera = buildCamera(mapboxMap, initialGesturesManager, internalGesturesManager);
    LocationComponentOptions options = mock(LocationComponentOptions.class);
    when(options.trackingGesturesManagement()).thenReturn(true);
    camera.initializeOptions(options);

    verify(mapboxMap).setGesturesManager(internalGesturesManager, true, true);
  }

  @Test
  public void gesturesManagement_disabled() {
    MapboxMap mapboxMap = mock(MapboxMap.class);
    AndroidGesturesManager initialGesturesManager = mock(AndroidGesturesManager.class);
    AndroidGesturesManager internalGesturesManager = mock(AndroidGesturesManager.class);
    LocationCameraController camera = buildCamera(mapboxMap, initialGesturesManager, internalGesturesManager);
    LocationComponentOptions options = mock(LocationComponentOptions.class);
    when(options.trackingGesturesManagement()).thenReturn(false);
    camera.initializeOptions(options);

    verify(mapboxMap).setGesturesManager(initialGesturesManager, true, true);
  }

  @Test
  public void onMove_notCancellingTransitionWhileNone() {
    MapboxMap mapboxMap = mock(MapboxMap.class);
    when(mapboxMap.getUiSettings()).thenReturn(mock(UiSettings.class));
    MoveGestureDetector moveGestureDetector = mock(MoveGestureDetector.class);
    LocationCameraController camera = buildCamera(mapboxMap);
    camera.initializeOptions(mock(LocationComponentOptions.class));

    camera.setCameraMode(CameraMode.NONE);
    camera.onMoveListener.onMove(moveGestureDetector);
    verify(mapboxMap, times(0)).cancelTransitions();
    verify(moveGestureDetector, times(0)).interrupt();

    // testing subsequent calls
    camera.onMoveListener.onMove(moveGestureDetector);
    verify(mapboxMap, times(0)).cancelTransitions();
    verify(moveGestureDetector, times(0)).interrupt();
  }

  @Test
  public void onMove_cancellingTransitionWhileGps() {
    MapboxMap mapboxMap = mock(MapboxMap.class);
    when(mapboxMap.getUiSettings()).thenReturn(mock(UiSettings.class));
    MoveGestureDetector moveGestureDetector = mock(MoveGestureDetector.class);
    LocationCameraController camera = buildCamera(mapboxMap);
    camera.initializeOptions(mock(LocationComponentOptions.class));

    camera.setCameraMode(CameraMode.TRACKING);
    camera.onMoveListener.onMove(moveGestureDetector);
    verify(mapboxMap, times(1)).cancelTransitions();
    verify(moveGestureDetector, times(1)).interrupt();

    // testing subsequent calls
    camera.onMoveListener.onMove(moveGestureDetector);
    verify(mapboxMap, times(1)).cancelTransitions();
    verify(moveGestureDetector, times(1)).interrupt();
  }

  @Test
  public void onMove_cancellingTransitionWhileBearing() {
    MapboxMap mapboxMap = mock(MapboxMap.class);
    MoveGestureDetector moveGestureDetector = mock(MoveGestureDetector.class);
    LocationCameraController camera = buildCamera(mapboxMap);
    camera.initializeOptions(mock(LocationComponentOptions.class));

    camera.setCameraMode(CameraMode.NONE_COMPASS);
    camera.onMoveListener.onMove(moveGestureDetector);
    verify(mapboxMap, times(1)).cancelTransitions();
    verify(moveGestureDetector, times(1)).interrupt();

    // testing subsequent calls
    camera.onMoveListener.onMove(moveGestureDetector);
    verify(mapboxMap, times(1)).cancelTransitions();
    verify(moveGestureDetector, times(1)).interrupt();
  }

  @Test
  public void transition_locationIsNull() {
    MapboxMap mapboxMap = mock(MapboxMap.class);
    LocationCameraController camera = buildCamera(mapboxMap);
    camera.initializeOptions(mock(LocationComponentOptions.class));
    OnLocationCameraTransitionListener listener = mock(OnLocationCameraTransitionListener.class);

    camera.setCameraMode(CameraMode.TRACKING, null, listener);
    Assert.assertEquals(CameraMode.TRACKING, camera.getCameraMode());
    verify(listener).onLocationCameraTransitionFinished(CameraMode.TRACKING);
    verify(mapboxMap, times(0))
      .animateCamera(any(CameraUpdate.class), any(Integer.class), any(MapboxMap.CancelableCallback.class));
  }

  @Test
  public void transition_notTracking() {
    MapboxMap mapboxMap = mock(MapboxMap.class);
    LocationCameraController camera = buildCamera(mapboxMap);
    camera.initializeOptions(mock(LocationComponentOptions.class));
    OnLocationCameraTransitionListener listener = mock(OnLocationCameraTransitionListener.class);
    Location location = mock(Location.class);

    camera.setCameraMode(CameraMode.NONE, location, listener);
    verify(listener, times(1)).onLocationCameraTransitionFinished(CameraMode.NONE);
    verify(mapboxMap, times(0))
      .animateCamera(any(CameraUpdate.class), any(Integer.class), any(MapboxMap.CancelableCallback.class));
  }

  @Test
  public void transition_trackingChanged() {
    MapboxMap mapboxMap = mock(MapboxMap.class);
    when(mapboxMap.getCameraPosition()).thenReturn(CameraPosition.DEFAULT);
    Projection projection = mock(Projection.class);
    when(mapboxMap.getProjection()).thenReturn(projection);
    when(projection.getMetersPerPixelAtLatitude(any(Double.class))).thenReturn(Double.valueOf(1000));
    LocationCameraController camera = buildCamera(mapboxMap);
    camera.initializeOptions(mock(LocationComponentOptions.class));
    final OnLocationCameraTransitionListener listener = mock(OnLocationCameraTransitionListener.class);
    Location location = mock(Location.class);

    doAnswer(new Answer<Void>() {
      @Override
      public Void answer(InvocationOnMock invocation) throws Throwable {
        listener.onLocationCameraTransitionFinished(CameraMode.TRACKING);
        return null;
      }
    }).when(mapboxMap).animateCamera(any(CameraUpdate.class), any(Integer.class), any(MapboxMap.CancelableCallback
      .class));

    camera.setCameraMode(CameraMode.TRACKING, location, listener);
    verify(listener).onLocationCameraTransitionFinished(CameraMode.TRACKING);
    verify(mapboxMap)
      .animateCamera(any(CameraUpdate.class), any(Integer.class), any(MapboxMap.CancelableCallback.class));
  }

  @Test
  public void transition_trackingNotChanged() {
    MapboxMap mapboxMap = mock(MapboxMap.class);
    when(mapboxMap.getCameraPosition()).thenReturn(CameraPosition.DEFAULT);
    Projection projection = mock(Projection.class);
    when(mapboxMap.getProjection()).thenReturn(projection);
    when(projection.getMetersPerPixelAtLatitude(any(Double.class))).thenReturn(Double.valueOf(1000));
    LocationCameraController camera = buildCamera(mapboxMap);
    camera.initializeOptions(mock(LocationComponentOptions.class));
    final OnLocationCameraTransitionListener listener = mock(OnLocationCameraTransitionListener.class);
    Location location = mock(Location.class);

    camera.setCameraMode(CameraMode.TRACKING, location, listener);

    doAnswer(new Answer<Void>() {
      @Override
      public Void answer(InvocationOnMock invocation) throws Throwable {
        listener.onLocationCameraTransitionFinished(CameraMode.TRACKING_GPS_NORTH);
        return null;
      }
    }).when(mapboxMap).animateCamera(any(CameraUpdate.class), any(Integer.class), any(MapboxMap.CancelableCallback
      .class));

    camera.setCameraMode(CameraMode.TRACKING_GPS_NORTH, location, listener);
    verify(listener, times(1)).onLocationCameraTransitionFinished(CameraMode.TRACKING_GPS_NORTH);
    verify(mapboxMap, times(1))
      .animateCamera(any(CameraUpdate.class), any(Integer.class), any(MapboxMap.CancelableCallback.class));
  }

  @Test
  public void transition_canceled() {
    MapboxMap mapboxMap = mock(MapboxMap.class);
    when(mapboxMap.getCameraPosition()).thenReturn(CameraPosition.DEFAULT);
    Projection projection = mock(Projection.class);
    when(mapboxMap.getProjection()).thenReturn(projection);
    when(projection.getMetersPerPixelAtLatitude(any(Double.class))).thenReturn(Double.valueOf(1000));
    LocationCameraController camera = buildCamera(mapboxMap);
    camera.initializeOptions(mock(LocationComponentOptions.class));
    final OnLocationCameraTransitionListener listener = mock(OnLocationCameraTransitionListener.class);
    Location location = mock(Location.class);

    doAnswer(new Answer<Void>() {
      @Override
      public Void answer(InvocationOnMock invocation) throws Throwable {
        listener.onLocationCameraTransitionCanceled(CameraMode.TRACKING);
        return null;
      }
    }).when(mapboxMap).animateCamera(any(CameraUpdate.class), any(Integer.class), any(MapboxMap.CancelableCallback
      .class));

    camera.setCameraMode(CameraMode.TRACKING, location, listener);
    verify(listener).onLocationCameraTransitionCanceled(CameraMode.TRACKING);
    verify(mapboxMap)
      .animateCamera(any(CameraUpdate.class), any(Integer.class), any(MapboxMap.CancelableCallback.class));
  }

  @Test
  public void transition_mapboxCallbackFinished() {
    MapboxMap mapboxMap = mock(MapboxMap.class);
    when(mapboxMap.getCameraPosition()).thenReturn(CameraPosition.DEFAULT);
    Projection projection = mock(Projection.class);
    when(mapboxMap.getProjection()).thenReturn(projection);
    when(projection.getMetersPerPixelAtLatitude(any(Double.class))).thenReturn(Double.valueOf(1000));
    LocationCameraController camera = buildCamera(mapboxMap);
    camera.initializeOptions(mock(LocationComponentOptions.class));
    final OnLocationCameraTransitionListener listener = mock(OnLocationCameraTransitionListener.class);
    Location location = mock(Location.class);
    when(location.getLatitude()).thenReturn(1.0);
    when(location.getLongitude()).thenReturn(1.0);
    when(location.getBearing()).thenReturn(30f);
    when(location.getAltitude()).thenReturn(0.0);

    ArgumentCaptor<MapboxMap.CancelableCallback> callbackCaptor
      = ArgumentCaptor.forClass(MapboxMap.CancelableCallback.class);

    camera.setCameraMode(CameraMode.TRACKING, location, listener);

    CameraPosition.Builder builder = new CameraPosition.Builder().target(new LatLng(location));
    verify(mapboxMap).animateCamera(
      eq(CameraUpdateFactory.newCameraPosition(builder.build())),
      eq((int) LocationComponentConstants.TRANSITION_ANIMATION_DURATION_MS),
      callbackCaptor.capture());

    Assert.assertTrue(camera.isTransitioning());

    callbackCaptor.getValue().onFinish();

    Assert.assertFalse(camera.isTransitioning());

    verify(listener).onLocationCameraTransitionFinished(CameraMode.TRACKING);
  }

  @Test
  public void transition_mapboxCallbackFinishedImmediately() {
    MapboxMap mapboxMap = mock(MapboxMap.class);
    when(mapboxMap.getCameraPosition()).thenReturn(CameraPosition.DEFAULT);
    Projection projection = mock(Projection.class);
    when(mapboxMap.getProjection()).thenReturn(projection);
    when(projection.getMetersPerPixelAtLatitude(any(Double.class))).thenReturn(Double.valueOf(1));
    LocationCameraController camera = buildCamera(mapboxMap);
    camera.initializeOptions(mock(LocationComponentOptions.class));
    final OnLocationCameraTransitionListener listener = mock(OnLocationCameraTransitionListener.class);
    Location location = mock(Location.class);
    when(location.getLatitude()).thenReturn(1.0);
    when(location.getLongitude()).thenReturn(1.0);
    when(location.getBearing()).thenReturn(30f);
    when(location.getAltitude()).thenReturn(0.0);

    ArgumentCaptor<MapboxMap.CancelableCallback> callbackCaptor
      = ArgumentCaptor.forClass(MapboxMap.CancelableCallback.class);

    camera.setCameraMode(CameraMode.TRACKING, location, listener);

    CameraPosition.Builder builder = new CameraPosition.Builder().target(new LatLng(location));
    verify(mapboxMap).moveCamera(
      eq(CameraUpdateFactory.newCameraPosition(builder.build())),
      callbackCaptor.capture());

    Assert.assertTrue(camera.isTransitioning());

    callbackCaptor.getValue().onFinish();

    Assert.assertFalse(camera.isTransitioning());

    verify(listener).onLocationCameraTransitionFinished(CameraMode.TRACKING);
  }

  @Test
  public void transition_mapboxCallbackCanceled() {
    MapboxMap mapboxMap = mock(MapboxMap.class);
    when(mapboxMap.getCameraPosition()).thenReturn(CameraPosition.DEFAULT);
    Projection projection = mock(Projection.class);
    when(mapboxMap.getProjection()).thenReturn(projection);
    when(projection.getMetersPerPixelAtLatitude(any(Double.class))).thenReturn(Double.valueOf(1000));
    LocationCameraController camera = buildCamera(mapboxMap);
    camera.initializeOptions(mock(LocationComponentOptions.class));
    final OnLocationCameraTransitionListener listener = mock(OnLocationCameraTransitionListener.class);
    Location location = mock(Location.class);
    when(location.getLatitude()).thenReturn(1.0);
    when(location.getLongitude()).thenReturn(1.0);
    when(location.getBearing()).thenReturn(30f);
    when(location.getAltitude()).thenReturn(0.0);

    ArgumentCaptor<MapboxMap.CancelableCallback> callbackCaptor
      = ArgumentCaptor.forClass(MapboxMap.CancelableCallback.class);

    camera.setCameraMode(CameraMode.TRACKING, location, listener);

    CameraPosition.Builder builder = new CameraPosition.Builder().target(new LatLng(location));
    verify(mapboxMap).animateCamera(
      eq(CameraUpdateFactory.newCameraPosition(builder.build())),
      eq((int) LocationComponentConstants.TRANSITION_ANIMATION_DURATION_MS),
      callbackCaptor.capture());

    Assert.assertTrue(camera.isTransitioning());

    callbackCaptor.getValue().onCancel();

    Assert.assertFalse(camera.isTransitioning());

    verify(listener).onLocationCameraTransitionCanceled(CameraMode.TRACKING);
  }

  @Test
  public void transition_mapboxAnimateBearing() {
    MapboxMap mapboxMap = mock(MapboxMap.class);
    when(mapboxMap.getCameraPosition()).thenReturn(CameraPosition.DEFAULT);
    Projection projection = mock(Projection.class);
    when(mapboxMap.getProjection()).thenReturn(projection);
    when(projection.getMetersPerPixelAtLatitude(any(Double.class))).thenReturn(Double.valueOf(1000));
    LocationCameraController camera = buildCamera(mapboxMap);
    camera.initializeOptions(mock(LocationComponentOptions.class));
    final OnLocationCameraTransitionListener listener = mock(OnLocationCameraTransitionListener.class);
    Location location = mock(Location.class);
    when(location.getLatitude()).thenReturn(1.0);
    when(location.getLongitude()).thenReturn(1.0);
    when(location.getBearing()).thenReturn(30f);
    when(location.getAltitude()).thenReturn(0.0);

    camera.setCameraMode(CameraMode.TRACKING_GPS, location, listener);

    CameraPosition.Builder builder = new CameraPosition.Builder().target(new LatLng(location)).bearing(30);
    verify(mapboxMap).animateCamera(
      eq(CameraUpdateFactory.newCameraPosition(builder.build())),
      eq((int) LocationComponentConstants.TRANSITION_ANIMATION_DURATION_MS),
      any(MapboxMap.CancelableCallback.class));
  }

  @Test
  public void transition_mapboxAnimateNorth() {
    MapboxMap mapboxMap = mock(MapboxMap.class);
    when(mapboxMap.getCameraPosition()).thenReturn(CameraPosition.DEFAULT);
    Projection projection = mock(Projection.class);
    when(mapboxMap.getProjection()).thenReturn(projection);
    when(projection.getMetersPerPixelAtLatitude(any(Double.class))).thenReturn(Double.valueOf(1000));
    LocationCameraController camera = buildCamera(mapboxMap);
    camera.initializeOptions(mock(LocationComponentOptions.class));
    final OnLocationCameraTransitionListener listener = mock(OnLocationCameraTransitionListener.class);
    Location location = mock(Location.class);
    when(location.getLatitude()).thenReturn(1.0);
    when(location.getLongitude()).thenReturn(1.0);
    when(location.getBearing()).thenReturn(30f);
    when(location.getAltitude()).thenReturn(0.0);

    camera.setCameraMode(CameraMode.TRACKING_GPS_NORTH, location, listener);

    CameraPosition.Builder builder = new CameraPosition.Builder().target(new LatLng(location)).bearing(0);
    verify(mapboxMap).animateCamera(
      eq(CameraUpdateFactory.newCameraPosition(builder.build())),
      eq((int) LocationComponentConstants.TRANSITION_ANIMATION_DURATION_MS),
      any(MapboxMap.CancelableCallback.class));
  }

  @Test
  public void transition_animatorValuesDuringTransition() {
    MapboxMap mapboxMap = mock(MapboxMap.class);
    when(mapboxMap.getCameraPosition()).thenReturn(CameraPosition.DEFAULT);
    Projection projection = mock(Projection.class);
    when(mapboxMap.getProjection()).thenReturn(projection);
    when(projection.getMetersPerPixelAtLatitude(any(Double.class))).thenReturn(Double.valueOf(1000));
    LocationCameraController camera = buildCamera(mapboxMap);
    camera.initializeOptions(mock(LocationComponentOptions.class));
    final OnLocationCameraTransitionListener listener = mock(OnLocationCameraTransitionListener.class);
    Location location = mock(Location.class);

    ArgumentCaptor<MapboxMap.CancelableCallback> callbackCaptor
      = ArgumentCaptor.forClass(MapboxMap.CancelableCallback.class);

    camera.setCameraMode(CameraMode.TRACKING_GPS, location, listener);

    verify(mapboxMap).animateCamera(
      any(CameraUpdate.class),
      eq((int) LocationComponentConstants.TRANSITION_ANIMATION_DURATION_MS),
      callbackCaptor.capture());

    camera.onNewLatLngValue(new LatLng(10, 10));
    camera.onNewGpsBearingValue(10f);
    camera.onNewCompassBearingValue(10f);
    camera.onNewTiltValue(10f);
    camera.onNewZoomValue(10f);

    verify(mapboxMap, times(0)).moveCamera(any(CameraUpdate.class));

    callbackCaptor.getValue().onFinish();

    camera.onNewLatLngValue(new LatLng(10, 10));
    camera.onNewGpsBearingValue(10f);
    camera.onNewTiltValue(10f);
    camera.onNewZoomValue(10f);

    verify(mapboxMap, times(4)).moveCamera(any(CameraUpdate.class));
  }

  private LocationCameraController buildCamera(OnCameraTrackingChangedListener onCameraTrackingChangedListener) {
    MapboxMap mapboxMap = mock(MapboxMap.class);
    when(mapboxMap.getUiSettings()).thenReturn(mock(UiSettings.class));
    Projection projection = mock(Projection.class);
    when(mapboxMap.getProjection()).thenReturn(projection);
    when(projection.getMetersPerPixelAtLatitude(any(Double.class))).thenReturn(Double.valueOf(1000));
    MoveGestureDetector moveGestureDetector = mock(MoveGestureDetector.class);
    OnCameraMoveInvalidateListener onCameraMoveInvalidateListener = mock(OnCameraMoveInvalidateListener.class);
    AndroidGesturesManager initialGesturesManager = mock(AndroidGesturesManager.class);
    AndroidGesturesManager internalGesturesManager = mock(AndroidGesturesManager.class);
    return new LocationCameraController(mapboxMap, moveGestureDetector,
      onCameraTrackingChangedListener, onCameraMoveInvalidateListener, initialGesturesManager, internalGesturesManager);
  }

  private LocationCameraController buildCamera(MoveGestureDetector moveGestureDetector) {
    MapboxMap mapboxMap = mock(MapboxMap.class);
    when(mapboxMap.getUiSettings()).thenReturn(mock(UiSettings.class));
    Projection projection = mock(Projection.class);
    when(mapboxMap.getProjection()).thenReturn(projection);
    when(projection.getMetersPerPixelAtLatitude(any(Double.class))).thenReturn(Double.valueOf(1000));
    OnCameraTrackingChangedListener onCameraTrackingChangedListener = mock(OnCameraTrackingChangedListener.class);
    OnCameraMoveInvalidateListener onCameraMoveInvalidateListener = mock(OnCameraMoveInvalidateListener.class);
    AndroidGesturesManager initialGesturesManager = mock(AndroidGesturesManager.class);
    AndroidGesturesManager internalGesturesManager = mock(AndroidGesturesManager.class);
    return new LocationCameraController(mapboxMap, moveGestureDetector,
      onCameraTrackingChangedListener, onCameraMoveInvalidateListener, initialGesturesManager, internalGesturesManager);
  }

  private LocationCameraController buildCamera(MapboxMap mapboxMap) {
    MoveGestureDetector moveGestureDetector = mock(MoveGestureDetector.class);
    OnCameraTrackingChangedListener onCameraTrackingChangedListener = mock(OnCameraTrackingChangedListener.class);
    OnCameraMoveInvalidateListener onCameraMoveInvalidateListener = mock(OnCameraMoveInvalidateListener.class);
    AndroidGesturesManager initialGesturesManager = mock(AndroidGesturesManager.class);
    AndroidGesturesManager internalGesturesManager = mock(AndroidGesturesManager.class);
    return new LocationCameraController(mapboxMap, moveGestureDetector,
      onCameraTrackingChangedListener, onCameraMoveInvalidateListener, initialGesturesManager, internalGesturesManager);
  }

  private LocationCameraController buildCamera(MapboxMap mapboxMap, AndroidGesturesManager initialGesturesManager,
                                               AndroidGesturesManager internalGesturesManager) {
    MoveGestureDetector moveGestureDetector = mock(MoveGestureDetector.class);
    OnCameraTrackingChangedListener onCameraTrackingChangedListener = mock(OnCameraTrackingChangedListener.class);
    OnCameraMoveInvalidateListener onCameraMoveInvalidateListener = mock(OnCameraMoveInvalidateListener.class);
    return new LocationCameraController(mapboxMap, moveGestureDetector,
      onCameraTrackingChangedListener, onCameraMoveInvalidateListener, initialGesturesManager, internalGesturesManager);
  }
}
