package huins.ex.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by suhak on 15. 7. 7.
 */
public class MissionSelection  {
    public interface OnSelectionUpdateListener {
        public void onSelectionUpdate(List<MissionItemProxy> selected);
    }
    public final List<MissionItemProxy> mSelectedItems = new ArrayList<MissionItemProxy>();
    public List<MissionSelection.OnSelectionUpdateListener> mSelectionsListeners = new ArrayList<MissionSelection.OnSelectionUpdateListener>();

    public void removeItemFromSelection(MissionItemProxy item) {
        mSelectedItems.remove(item);
        notifySelectionUpdate();
    }
    public void removeItemsFromSelection(List<MissionItemProxy> itemList){
        if(itemList == null || itemList.isEmpty()){
            return;
        }

        for(MissionItemProxy item : itemList){
            mSelectedItems.remove(item);
        }
        notifySelectionUpdate();
    }

    public void setSelectionTo(List<MissionItemProxy> items) {
        mSelectedItems.clear();
        mSelectedItems.addAll(items);
        notifySelectionUpdate();
    }
    public void setSelectionTo(MissionItemProxy item) {
        mSelectedItems.clear();
        mSelectedItems.add(item);
        notifySelectionUpdate();
    }

    public void addToSelection(MissionItemProxy item) {
        mSelectedItems.add(item);
        notifySelectionUpdate();
    }

    public void addToSelection(List<MissionItemProxy> items) {
        mSelectedItems.addAll(items);
        notifySelectionUpdate();
    }
    public boolean selectionContains(MissionItemProxy item) {
        return mSelectedItems.contains(item);
    }

    public List<MissionItemProxy> getSelected() {
        return mSelectedItems;
    }

    public void clearSelection() {
        mSelectedItems.clear();
        notifySelectionUpdate();
    }

    public void notifySelectionUpdate() {
        for (MissionSelection.OnSelectionUpdateListener listener : mSelectionsListeners)
            listener.onSelectionUpdate(mSelectedItems);
    }
    public void addSelectionUpdateListener(OnSelectionUpdateListener listener) {
        mSelectionsListeners.add(listener);
    }

    public void removeSelectionUpdateListener(MissionSelection.OnSelectionUpdateListener listener) {
        mSelectionsListeners.remove(listener);
    }
}