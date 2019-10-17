package huins.ex.view.fragments.map;

import huins.ex.model.MissionItemProxy;
import huins.ex.proto.Coordinate;

/**
 * Created by suhak on 15. 6. 24.
 */
public interface OnEditorInteraction {
    public void onItemClick(MissionItemProxy item, boolean zoomToFit);

    public void onMapClick(Coordinate coord);

    public void onListVisibilityChanged();
}
