package com.example.group_w01_07_3.features.onboarding;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.viewpager.widget.PagerAdapter;

import com.example.group_w01_07_3.R;

import java.util.List;

public class OnboardingViewPagerAdapter extends PagerAdapter {

    Context mContext;
    List<OnboardingItem> onboardingItemList;

    public OnboardingViewPagerAdapter(Context mContext, List<OnboardingItem> onboardingItemList) {
        this.mContext = mContext;
        this.onboardingItemList = onboardingItemList;
    }

    /**
     * Instantiate onboarding view page
     *
     * @param container The view group that holder each slider page
     * @param position  The page number of each page
     * @return
     */
    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {
        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View layoutScreen = inflater.inflate(R.layout.onboarding_pager_layout, null);

        ImageView imgSlide = layoutScreen.findViewById(R.id.intro_img);
        TextView title = layoutScreen.findViewById(R.id.intro_title);
        TextView description = layoutScreen.findViewById(R.id.intro_description);

        title.setText(onboardingItemList.get(position).getTitle());
        description.setText(onboardingItemList.get(position).getDescription());
        imgSlide.setImageResource(onboardingItemList.get(position).getIntroImage());

        container.addView(layoutScreen);

        return layoutScreen;
    }

    @Override
    public int getCount() {
        return onboardingItemList.size();
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return view == object;
    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        container.removeView((View) object);
    }
}
