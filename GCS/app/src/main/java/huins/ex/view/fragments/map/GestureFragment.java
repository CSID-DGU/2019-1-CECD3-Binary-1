package huins.ex.view.fragments.map;

import android.gesture.GestureOverlayView;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

import huins.ex.R;
import huins.ex.proto.Coordinate;
import huins.ex.util.Math.MathUtils;

public class GestureFragment extends Fragment implements GestureOverlayView.OnGestureListener {

    private static final int TOLERANCE = 15;
    private static final int STROKE_WIDTH = 3;

    private double toleranceInPixels;

    private GestureOverlayView gestureOverlayView;
    private OnPathFinishedListener listener;
    private EditorMapFragment mapFragment;

    public interface OnPathFinishedListener {

        void onPathFinished(List<Coordinate> path);
    }

    //manage coordinates
    private List<Coordinate> decodeGesture() {
        List<Coordinate> path = new ArrayList<Coordinate>();
        extractPathFromGesture(path);
        return path;
    }

    //get path of waypoints
    private void extractPathFromGesture(List<Coordinate> path) {
        float[] points = gestureOverlayView.getGesture().getStrokes().get(0).points;
        for (int i = 0; i < points.length; i += 2) {
            path.add(new Coordinate((int) points[i], (int) points[i + 1]));
        }
    }


    public EditorMapFragment getMapFragment(){
        return mapFragment;
    }

    public void enableGestureDetection() {
        gestureOverlayView.setEnabled(true);
    }

    public void disableGestureDetection() {
        gestureOverlayView.setEnabled(false);
    }

    public void setOnPathFinishedListener(OnPathFinishedListener listener) {
        this.listener = listener;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_gesture, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState){
        super.onViewCreated(view, savedInstanceState);
        final FragmentManager fm = getChildFragmentManager();


        mapFragment = ((EditorMapFragment) fm.findFragmentById(R.id.gesture_map_fragment));
        if(mapFragment == null){
            mapFragment = new EditorMapFragment();
            fm.beginTransaction().add(R.id.gesture_map_fragment, mapFragment).commit();

       }

        gestureOverlayView = (GestureOverlayView) view.findViewById(R.id.overlay1);
        gestureOverlayView.addOnGestureListener(this);
        gestureOverlayView.setEnabled(false);

        gestureOverlayView.setGestureStrokeWidth(scaleDpToPixels(STROKE_WIDTH));
        toleranceInPixels = scaleDpToPixels(TOLERANCE);
    }

    private int scaleDpToPixels(double value) {
        final float scale = getResources().getDisplayMetrics().density;
        return (int) Math.round(value * scale);
    }

    @Override
    public void onGestureStarted(GestureOverlayView overlay, MotionEvent event) {

    }

    @Override
    public void onGesture(GestureOverlayView overlay, MotionEvent event) {

    }

    @Override
    public void onGestureEnded(GestureOverlayView overlay, MotionEvent event) {
        overlay.setEnabled(false);
        List<Coordinate> path = decodeGesture();
        if (path.size() > 1) {
            path = MathUtils.simplify(path, toleranceInPixels);
        }
        listener.onPathFinished(path);

    }

    @Override
    public void onGestureCancelled(GestureOverlayView overlay, MotionEvent event) {

    }

}
