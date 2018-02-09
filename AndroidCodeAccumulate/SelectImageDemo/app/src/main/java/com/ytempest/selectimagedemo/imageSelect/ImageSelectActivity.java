package com.ytempest.selectimagedemo.imageSelect;

import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.ytempest.baselibrary.ioc.OnClick;
import com.ytempest.baselibrary.ioc.ViewById;
import com.ytempest.baselibrary.view.recyclerview.division.GridItemDecoration;
import com.ytempest.framelibrary.base.BaseSkinActivity;
import com.ytempest.framelibrary.navigation.DefaultNavigationBar;
import com.ytempest.selectimagedemo.R;

import java.util.ArrayList;
import java.util.List;

/**
 * @author ytempest
 *         Description：图片选择器
 */
public class ImageSelectActivity extends BaseSkinActivity {

    /**
     * 是否显示拍照的key
     */
    public static final String EXTRA_SHOW_CAMERA = "EXTRA_SHOW_CAMERA";
    /**
     * 选择图片的张数的key
     */
    public static final String EXTRA_MAX_COUNT = "EXTRA_MAX_COUNT";
    /**
     * 传递过来的原始图片列表的key
     */
    public static final String EXTRA_DEFAULT_SELECTED_LIST = "EXTRA_DEFAULT_SELECTED_LIST";
    /**
     * 返回选择后的图片列表的key
     */
    public static final String EXTRA_RESULT = "EXTRA_RESULT";
    /**
     * 选择图片的模式的key
     */
    public static final String EXTRA_SELECT_MODE = "EXTRA_SELECT_MODE";

    public static final int MODE_MULTI = 0x0011;
    public static final int MODE_SINGLE = 0x0022;

    /**
     * LoaderManager加载数据的key
     */
    private static final int LOADERMANAGER_LOADER_TYPE = 0x0021;

    private int mMaxCount = 9;
    private int mMode = MODE_MULTI;
    private ArrayList<String> mResultList;
    private boolean mShowCamera = true;

    @ViewById(R.id.rv_image_list)
    private RecyclerView mRecyclerView;
    @ViewById(R.id.tv_select_num)
    private TextView mSelectNum;
    @ViewById(R.id.tv_select_preview)
    private TextView mSelectPreview;

    /**
     * LoaderManger加载图片的LoaderCallbacks
     */
    private LoaderManager.LoaderCallbacks<Cursor> mLoaderCallback = new LoaderManager.LoaderCallbacks<Cursor>() {
        private final String[] IMAGE_PROJECTION = {
                MediaStore.Images.Media.DATA,
                MediaStore.Images.Media.DISPLAY_NAME,
                MediaStore.Images.Media.DATE_ADDED,
                MediaStore.Images.Media.MIME_TYPE,
                MediaStore.Images.Media.SIZE,
                MediaStore.Images.Media._ID
        };

        @Override
        public Loader<Cursor> onCreateLoader(int id, Bundle args) {
            // 查询数据库一样的语句
            CursorLoader cursorLoader = new CursorLoader(ImageSelectActivity.this,
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    IMAGE_PROJECTION,
                    IMAGE_PROJECTION[4] + ">0 AND " + IMAGE_PROJECTION[3] + "=? OR "
                            + IMAGE_PROJECTION[3] + "=? ",
                    new String[]{"image/jpeg", "image/png"},
                    IMAGE_PROJECTION[2] + " DESC");
            return cursorLoader;
        }

        @Override
        public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
            // 解析查询到的数据，封装到集合
            if (loader != null && data.getCount() > 0) {
                List<String> imageList = new ArrayList<>();

                // 如果需要显示拍照按钮，就在第一个位置上加一个空String
                if (mShowCamera) {
                    imageList.add("");
                }

                while (data.moveToNext()) {
                    // 获取图片的路径
                    String imagePath = data.getString(data.getColumnIndexOrThrow(IMAGE_PROJECTION[0]));
                    imageList.add(imagePath);
                }
                // 显示图片列表
                showImageList(imageList);
            }
        }

        @Override
        public void onLoaderReset(Loader<Cursor> loader) {

        }
    };

    @Override
    protected int getLayoutResId() {
        return R.layout.activity_image_select;
    }

    @Override
    protected void initTitle() {
        DefaultNavigationBar navigationBar = new DefaultNavigationBar.Builder(this)
                .setTitle("选择图片")
                .setTitleColor(R.color.nav_title_color)
                .setBackground(R.color.nav_bg_color)
                .build();
        StatusBarUtil.statusBarTintColor(this, getResources().getColor(R.color.nav_bg_color)-0x00111111);
    }


    @Override
    protected void initView() {
    }

    @Override
    protected void initData() {
        // 获取传递过来的参数
        Intent intent = getIntent();
        mMaxCount = intent.getIntExtra(EXTRA_MAX_COUNT, mMaxCount);
        mMode = intent.getIntExtra(EXTRA_SELECT_MODE, mMode);
        mShowCamera = intent.getBooleanExtra(EXTRA_SHOW_CAMERA, mShowCamera);
        mResultList = intent.getStringArrayListExtra(EXTRA_DEFAULT_SELECTED_LIST);
        if (mResultList == null) {
            mResultList = new ArrayList<>();
        }

        // 初始化本地图片数据，然后显示
        initImageList();

        // 改变底部一些View的状态
        exchangeViewShow();
    }

    private void initImageList() {
        getLoaderManager().initLoader(LOADERMANAGER_LOADER_TYPE, null, mLoaderCallback);
    }

    /**
     * 根据用户选择图片的张数来更改某些View的状态
     */
    private void exchangeViewShow() {
        // 设置预览是否可以点击，显示什么颜色
        mSelectPreview.setEnabled(mResultList.size() > 0);

        // 中间显示选择图片的张数也要更改
        mSelectNum.setText(mResultList.size() + "/" + mMaxCount);
    }


    private void showImageList(List<String> imageList) {
        ImageSelectListAdapter adapter = new ImageSelectListAdapter(ImageSelectActivity.this,
                imageList, mResultList, mMaxCount);
        adapter.setOnSelectImageListener(new OnSelectImageListener() {
            @Override
            public void onSelect(View view) {
                exchangeViewShow();
            }
        });
        mRecyclerView.setLayoutManager(new GridLayoutManager(ImageSelectActivity.this, 4));

        GridItemDecoration decor = new GridItemDecoration(ImageSelectActivity.this);
        decor.setDrawable(getResources().getDrawable(R.drawable.item_division));
        mRecyclerView.addItemDecoration(decor);

        mRecyclerView.setAdapter(adapter);

    }

    /**
     * 预览图片的点击事件
     */
    @OnClick(R.id.tv_select_preview)
    public void selectPreviewClick(View view) {
        showToastShort("you click preview");
    }

    /**
     * 确定选择图片的点击事件
     */
    @OnClick(R.id.tv_select_finish)
    public void selectFinishClick(View view) {
        Intent intent = new Intent();
        intent.putStringArrayListExtra(EXTRA_RESULT, mResultList);
        setResult(RESULT_OK, intent);
        finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);


        // 1.第一个要把图片加到集合

        // 2.调用sureSelect()方法


        // 3.通知系统本地有图片改变，下次进来可以找到这张图片
        // notify system the image has change
        // sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(mTempFile));
    }
}
