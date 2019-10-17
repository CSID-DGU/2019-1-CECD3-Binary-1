package huins.ex.view.widgets.checklist.row;

import android.view.View;

import huins.ex.view.widgets.checklist.CheckListItem;

public interface ListRow_Interface {
	public interface OnRowItemChangeListener {
		public void onRowItemChanged(CheckListItem listItem);

		public void onRowItemGetData(CheckListItem listItem, String sysTag);
	}

	public View getView(View convertView);

	public int getViewType();
}
