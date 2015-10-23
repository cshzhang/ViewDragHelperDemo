package cn.hzh.mydrawerlayout;

import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.widget.TextView;

import cn.hzh.mydrawerlayout.fragment.LeftMenuFragment;

public class MainActivity extends AppCompatActivity
{

    private MyDrawerLayout mDrawerLayout;
    private TextView mContent;
    private LeftMenuFragment mLeftMenuFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mDrawerLayout = (MyDrawerLayout) findViewById(R.id.id_drawer_layout);
        mContent = (TextView) findViewById(R.id.id_content);

        FragmentManager fm = getSupportFragmentManager();
        mLeftMenuFragment = (LeftMenuFragment) fm.findFragmentById(R.id.id_container_menu);
        if(mLeftMenuFragment == null)
        {
            mLeftMenuFragment = new LeftMenuFragment();
            fm.beginTransaction().add(R.id.id_container_menu, mLeftMenuFragment).commit();
        }

        mLeftMenuFragment.setOnMenuItemSelectedListener(new LeftMenuFragment.OnMenuItemSelectedListener()
        {
            @Override
            public void onItemSelected(String text)
            {
                mDrawerLayout.closeDrawer();
                mContent.setText(text);
            }
        });

    }

}
