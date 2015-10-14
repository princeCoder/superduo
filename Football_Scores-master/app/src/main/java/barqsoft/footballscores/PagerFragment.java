package barqsoft.footballscores;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.text.SimpleDateFormat;
import java.util.Date;

import barqsoft.footballscores.util.Utilies;

/**
 * Created by yehya khaled on 2/27/2015.
 */
public class PagerFragment extends Fragment
{
    public static final int NUM_PAGES = 5;
    public ViewPager mViewPager;
    private myPageAdapter mPagerAdapter;
    private  String CURRENT_TAB="CURRENT_TAB";
    private int currentFragment=2;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState)
    {
        this.mPagerAdapter = new myPageAdapter(getChildFragmentManager());
        View rootView = inflater.inflate(R.layout.pager_fragment, container, false);

        if(savedInstanceState!=null){
            currentFragment=savedInstanceState.getInt(CURRENT_TAB, 2);
        }

        this.mViewPager = (ViewPager) rootView.findViewById(R.id.pager);
        this.mViewPager.setAdapter(mPagerAdapter);


        return rootView;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        //Save the current tab
        outState.putInt(CURRENT_TAB,mViewPager.getCurrentItem());
    }

    @Override
    public void onResume() {
        super.onResume();
        mViewPager.setCurrentItem(currentFragment);
    }

    private class myPageAdapter extends FragmentStatePagerAdapter
    {
        @Override
        public Fragment getItem(int i)
        {
            Fragment fragment = new MainScreenFragment();
            Bundle args = new Bundle();
            // Our object is just an integer :-P

            Date fragmentdate = new Date(System.currentTimeMillis()+((i-2)*86400000));
            SimpleDateFormat mformat = new SimpleDateFormat(getActivity().getString(R.string.date_format));

            String value=mformat.format(fragmentdate);

            args.putString(MainScreenFragment.FRAGMENT_DATE, value);
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public int getCount()
        {
            return NUM_PAGES;
        }

        public myPageAdapter(FragmentManager fm)
        {
            super(fm);
        }
        // Returns the page title for the top indicator
        @Override
        public CharSequence getPageTitle(int position)
        {
            //1day=86400000 milliseconds
            String title=Utilies.getDayName(getActivity(),System.currentTimeMillis()+((position-2)*86400000));
            return title;
        }

        @Override
        public void destroyItem(View container, int position, Object object) {
            if (position >= getCount()) {
                FragmentManager manager = ((Fragment) object).getFragmentManager();
                FragmentTransaction trans = manager.beginTransaction();
                trans.remove((Fragment) object);
                trans.commit();
            }
        }
    }
}
