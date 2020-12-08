package mapareas;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

public class MapAreaWrapper {

	public static enum MarkerMoveResult {moved, radiusChange, minRadius, maxRadius, none};
    public static enum MarkerType {move, resize, none}

	private Marker centerMarker;
    private Circle circle;
    private double radiusMeters;

    private int minRadiusMeters = -1;
    private int maxRadiusMeters = -1;

    public MapAreaWrapper(GoogleMap map, LatLng center, double radiusMeters, float strokeWidth, int strokeColor, int fillColor, int minRadiusMeters, int maxRadiusMeters,
    		int centerDrawableId, int radiusDrawableId, float moveDrawableAnchorU, float moveDrawableAnchorV) {

        this.radiusMeters = radiusMeters;
        this.minRadiusMeters = minRadiusMeters;
        this.maxRadiusMeters = maxRadiusMeters;

        centerMarker = map.addMarker(new MarkerOptions()
                .position(center)
                .anchor(moveDrawableAnchorU, moveDrawableAnchorV)
                .draggable(true));

        if (centerDrawableId != -1) {
        	centerMarker.setIcon(BitmapDescriptorFactory.fromResource(centerDrawableId));
        }

        circle = map.addCircle(new CircleOptions()
                .center(center)
                .radius(radiusMeters)
                .strokeWidth(strokeWidth)
                .strokeColor(strokeColor)
                .fillColor(fillColor));
    }

    public MapAreaWrapper(GoogleMap map, LatLng center, double radiusMeters, float strokeWidth, int strokeColor, int fillColor, int minRadius, int maxRadius) {
    	this(map, center, radiusMeters, strokeWidth, strokeColor, fillColor, minRadius, maxRadius, -1, -1);
    }

    public MapAreaWrapper(GoogleMap map, LatLng center, double radiusMeters, float strokeWidth, int strokeColor, int fillColor, int minRadius, int maxRadius,
    		int centerDrawableId, int radiusDrawableId) {

    	this(map, center, radiusMeters, strokeWidth, strokeColor, fillColor, minRadius, maxRadius, centerDrawableId, radiusDrawableId, 0.5f, 1f);
    }



    public LatLng getCenter() {
    	return centerMarker.getPosition();
    }

    public double getRadius() {
    	return radiusMeters;
    }

    public void setStokeWidth(float strokeWidth) {
    	circle.setStrokeWidth(strokeWidth);
    }

    public void setStokeColor(int strokeColor) {
    	circle.setStrokeColor(strokeColor);
    }

    public void setFillColor(int fillColor) {
    	circle.setFillColor(fillColor);
    }

    public void setCenterOfCircle(LatLng center) {
    	centerMarker.setPosition(center);
        onCenterUpdated(center);
    }

    public MarkerMoveResult onMarkerMoved(Marker marker) {
        if (marker.equals(centerMarker)) {
        	onCenterUpdated(marker.getPosition());
            return MarkerMoveResult.moved;
        }

        return MarkerMoveResult.none;
    }

    public void onCenterUpdated(LatLng center) {
    	circle.setCenter(center);
    }

    public void setRadiusOfCircle(double radiusMeters) {
    	this.radiusMeters = radiusMeters;
    	circle.setRadius(radiusMeters);
    }

    @Override
    public String toString() {
    	return "center: " + getCenter() + " radius: " + getRadius();
    }
}