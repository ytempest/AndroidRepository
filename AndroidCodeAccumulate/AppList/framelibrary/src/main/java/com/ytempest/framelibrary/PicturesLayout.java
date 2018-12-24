package com.ytempest.framelibrary;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import java.util.ArrayList;
import java.util.List;

public class PicturesLayout extends LinearLayout implements View.OnClickListener {

    private static final String TAG = "PicturesLayout";

    public static final int MAX_DISPLAY_COUNT = 9;
    public static final int division = 2;

    private final List<ImageView> mImageViewList = new ArrayList<>();
    private final Context mContext;

    private Callback mCallback;
    private boolean isInit;
    private List<String> mPictureList;

    public PicturesLayout(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        this.mContext = context;

/*
        DisplayMetrics mDisplayMetrics = context.getResources().getDisplayMetrics();
        mSingleMaxSize = (int) (TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 216, mDisplayMetrics) + 0.5f);
        mSpace = (int) (TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 2, mDisplayMetrics) + 0.5f);
*/


    }

    public void setPictureUrlList(List<String> urlList) {
        mPictureList = urlList;
        int rowContainerCount = getChildCount();
        if (rowContainerCount != 0) {
            useCacheRowView(rowContainerCount);
        } else {
            int pictureCount = urlList.size();
            int rowCount = getRowCount(pictureCount);

            addImageViewToLayout(0, rowCount, pictureCount);
        }

        int newChildCount = getChildCount();
        int pictureCount = mPictureList.size();
        for (int i = 0; i < newChildCount; i++) {
            LinearLayout rowContainerView = (LinearLayout) getChildAt(i);
            for (int k = 0; k < 3; k++) {
                if (pictureCount == 0) {
                    break;
                }
                SquareImageView imageView = (SquareImageView) rowContainerView.getChildAt(k);
                imageView.setImageResource(R.drawable.default_image);
                pictureCount--;
            }
        }
    }

    private int getRowCount(int pictureCount) {
        return pictureCount % 3 == 0 ? pictureCount / 3 : pictureCount / 3 + 1;
    }

    private void addImageViewToLayout(int startRowIndex, int rowCount, int pictureCount) {
        for (int i = startRowIndex; i < rowCount; i++) {
            LinearLayout rowContainer = getRowLinearLayout();
            addView(rowContainer);


            int count = 3;
            if (i == rowCount - 1 && pictureCount % 3 != 0) {
                count = pictureCount % 3;
            }
            for (int k = 0; k < count; k++) {
                ImageView squareImageView = getImageView(k == 1);
                rowContainer.addView(squareImageView);
                mImageViewList.add(squareImageView);
            }

            for (int j = 0; j < 3 - count; j++) {
                ImageView squareImageView = getImageView(j == 1);
                rowContainer.addView(squareImageView);
                mImageViewList.add(squareImageView);
            }

        }
    }

    @NonNull
    private ImageView getImageView(boolean isAddDivision) {
        LayoutParams params = new LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1);
        ImageView squareImageView = new SquareImageView(mContext);
        squareImageView.setLayoutParams(params);
        if (isAddDivision) {
            params.leftMargin = dpToPx(division);
            params.rightMargin = dpToPx(division);
        }
        squareImageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        squareImageView.setVisibility(View.VISIBLE);
        squareImageView.setOnClickListener(this);
        return squareImageView;
    }

    /**
     * 缓存的View多余实际的图片数：移除最后那几个View
     * 缓存的View少于实际的图片数：图片数减去缓存的View，再加上需要的ImageView
     *
     * @param rowContainerCount
     */
    private void useCacheRowView(int rowContainerCount) {
        int pictureCount = mPictureList.size();
        int rowCount = getRowCount(pictureCount);
        if (rowContainerCount * 3 < pictureCount) {
            // 如果缓存的View数少
            addImageViewToLayout(rowContainerCount, rowCount, pictureCount);
        } else {
            // 如果缓存的View数多
            int startRowIndex = rowCount;
            int endRowIndex = rowContainerCount;
            removeViewFromLayout(startRowIndex, endRowIndex);
        }
    }

    /**
     * 移除PictureLayout中(startRowIndex,endRowIndex]范围的View
     *
     * @param startRowIndex
     * @param endRowIndex
     */
    private void removeViewFromLayout(int startRowIndex, int endRowIndex) {
        for (int i = endRowIndex; i > startRowIndex; i--) {
            removeViewAt(i);
        }
    }

    private LinearLayout getRowLinearLayout() {
        LinearLayout linearLayout = new LinearLayout(mContext);
        LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.bottomMargin = dpToPx(division);
        linearLayout.setLayoutParams(params);
        linearLayout.setOrientation(LinearLayout.HORIZONTAL);
        return linearLayout;
    }


    @Override
    public void onClick(View view) {
        if (mCallback != null) {
            mCallback.onPictureClick((ImageView) view, mImageViewList, mPictureList);
        }
    }

    public interface Callback {
        void onPictureClick(ImageView i, List<ImageView> imageGroupList, List<String> urlList);
    }

    public void setCallback(Callback callback) {
        mCallback = callback;
    }

    private int dpToPx(int dp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, getResources().getDisplayMetrics());
    }
}
