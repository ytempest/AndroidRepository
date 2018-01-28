package com.ytempest.selectimagedemo.imageSelect;

import android.content.Context;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.ytempest.baselibrary.view.recyclerview.adapter.CommonRecyclerAdapter;
import com.ytempest.baselibrary.view.recyclerview.adapter.CommonViewHolder;
import com.ytempest.selectimagedemo.R;

import java.util.ArrayList;
import java.util.List;

/**
 * @author ytempest
 *         Description：显示图片的Adapter
 */
public class ImageSelectListAdapter extends CommonRecyclerAdapter<String> {
    /**
     * 已经选择的图片集合
     */
    private ArrayList<String> mResultImageList;
    private Context mContext;
    private int mMaxCount;
    /**
     * 选择图片监听
     */
    private OnSelectImageListener onSelectImageListener;

    public ImageSelectListAdapter(Context context, List<String> data, ArrayList<String> resultImageList, int maxCount) {
        super(context, data, R.layout.media_chooser_item);
        this.mContext = context;
        this.mResultImageList = resultImageList;
        this.mMaxCount = maxCount;
    }

    @Override
    public void bindViewData(final CommonViewHolder holder, final String item) {
        if (TextUtils.isEmpty(item)) {
            // 显示拍照
            holder.setViewVisibility(R.id.ll_camera, View.VISIBLE);
            holder.setViewVisibility(R.id.iv_media_selected_indicator, View.INVISIBLE);
            holder.setViewVisibility(R.id.iv_image, View.INVISIBLE);

            holder.setOnItemClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // 调用拍照，权限很重要，6.0以上要处理
                    // http://www.jianshu.com/p/823360bb183f
                }
            });
        } else {
            // 显示图片
            holder.setViewVisibility(R.id.ll_camera, View.INVISIBLE);
            holder.setViewVisibility(R.id.iv_media_selected_indicator, View.VISIBLE);
            holder.setViewVisibility(R.id.iv_image, View.VISIBLE);

            // 显示图片利用Glide
            ImageView imageView = holder.getView(R.id.iv_image);
            RequestOptions requestOptions = new RequestOptions().optionalCenterCrop();
            Glide.with(mContext).load(item).apply(requestOptions).into(imageView);

            // 如果图片在上一次进来的时候已经选择，则点亮选择勾住图片
            ImageView selectIndicatorIv = holder.getView(R.id.iv_media_selected_indicator);
            selectIndicatorIv.setSelected(mResultImageList.contains(item));

            // 给条目增加点击事件
            holder.setOnItemClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    // 没有就加入图片集合，有就从图片集合移除
                    if (!mResultImageList.contains(item)) {

                        // 不能大于最大的张数
                        if (mResultImageList.size() >= mMaxCount) {
                            // 自定义Toast  文字写在string里面
                            Toast.makeText(mContext, "最多只能选取" + mMaxCount + "张图片"
                                    , Toast.LENGTH_SHORT).show();
                            return;
                        }

                        mResultImageList.add(item);
                    } else {
                        mResultImageList.remove(item);
                    }

                    // 刷新数据，会对指定的位置的条目重新绑定数据
                    notifyItemChanged(holder.getAdapterPosition());
//                    notifyDataSetChanged();


                    // 通知显示布局
                    if (onSelectImageListener != null) {
                        onSelectImageListener.onSelect(holder.itemView);
                    }
                }
            });
        }
    }


    public void setOnSelectImageListener(OnSelectImageListener listener) {
        this.onSelectImageListener = listener;
    }
}
