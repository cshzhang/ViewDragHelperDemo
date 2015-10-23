package cn.hzh.mydrawerlayout.fragment;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v4.widget.TextViewCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import cn.hzh.mydrawerlayout.R;

/**
 * Created by hzh on 2015/10/23.
 */
public class LeftMenuFragment extends ListFragment
{

    private MenuAdapter mAdatper;
    String[] strs = new String[]
            {"播放列表", "我的乐库", "立即播放"};
    MenuItem mDatas[] = new MenuItem[strs.length];

    public interface OnMenuItemSelectedListener
    {
        void onItemSelected(String text);
    }
    private OnMenuItemSelectedListener mListener;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        MenuItem item = null;
        for(int i = 0; i < mDatas.length; i++)
        {
            item = new MenuItem(strs[i],
                    R.mipmap.music_36px, R.mipmap.music_36px_light);
            mDatas[i] = item;
        }
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState)
    {
        super.onViewCreated(view, savedInstanceState);
        view.setBackgroundColor(0xffffffff);
        mAdatper = new MenuAdapter(getActivity(), mDatas);
        setListAdapter(mAdatper);
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id)
    {
        super.onListItemClick(l, v, position, id);

        if(mListener != null)
        {
            MenuAdapter adapter = (MenuAdapter) getListAdapter();
            mListener.onItemSelected(adapter.getItem(position).text);
        }

        mAdatper.setSelected(position);
    }

    public void setOnMenuItemSelectedListener(OnMenuItemSelectedListener listener)
    {
        this.mListener = listener;
    }

    public class MenuAdapter extends ArrayAdapter<MenuItem>
    {
        private LayoutInflater mInflater;
        //这个变量实现 选中后状态改变
        private int mSelected;

        private Context mContext;
        private int mIconSize;

        public MenuAdapter(Context context, MenuItem[] mDatas)
        {
            super(context, -1, mDatas);

            mContext = context;
            mInflater = LayoutInflater.from(context);

            //24dp
            mIconSize = mContext.getResources().getDimensionPixelSize(R.dimen.drawer_icon_size);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent)
        {
            if(convertView == null)
            {
                convertView = mInflater.inflate(R.layout.drawer_menu_item, parent, false);
            }
            TextView tv = (TextView) convertView;
            tv.setBackgroundColor(Color.TRANSPARENT);
            tv.setText(getItem(position).text);
            Drawable icon = mContext.getResources().getDrawable(getItem(position).icon);
            icon.setBounds(0,0,mIconSize, mIconSize);
            TextViewCompat.setCompoundDrawablesRelative(tv, icon, null, null, null);

            if(position == mSelected)
            {
                tv.setBackgroundColor(0x4400ff00);
                icon = mContext.getResources().getDrawable(getItem(position).iconSelected);
                icon.setBounds(0,0,mIconSize, mIconSize);
                TextViewCompat.setCompoundDrawablesRelative(tv, icon, null, null, null);
            }

            return convertView;
        }

        public void setSelected(int position)
        {
            this.mSelected = position;
            notifyDataSetChanged();
        }
    }

    public class MenuItem
    {
        String text;
        int icon;
        int iconSelected;

        public MenuItem(String text, int icon, int iconSelected)
        {
            this.text = text;
            this.icon = icon;
            this.iconSelected = iconSelected;
        }
    }
}
