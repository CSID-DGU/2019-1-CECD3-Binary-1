package huins.ex.model.adapters;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

import huins.ex.proto.mission.item.complex.CameraDetail;

public class CamerasAdapter extends ArrayAdapter<CameraDetail> {

	public CamerasAdapter(Context context, int resource, CameraDetail[] cameraDetails) {
		super(context, resource, cameraDetails);
	}

    public CamerasAdapter(Context context, int resource, List<CameraDetail> cameraDetails){
        super(context, resource, cameraDetails);
    }

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		TextView view = (TextView) super.getView(position, convertView, parent);
		view.setText(getItem(position).getName());
		return view;
	}

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        TextView view = (TextView) super.getDropDownView(position, convertView, parent);
        view.setText(getItem(position).getName());
        return view;
    }

}
