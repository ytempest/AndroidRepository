package com.ytempest.test2;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.util.List;

/**
 *
 * @author Administrator
 * @date 2017/10/21/021
 */
public class FruitAdapter  extends RecyclerView.Adapter<FruitAdapter.FruitHolder>{

    private OnItemClickListener onItemClickListener;

    private List<String> mFruitList;

    public FruitAdapter(List<String> mFruitList) {
        if (mFruitList == null) {
            Log.e("FruitAdapter", "FruitAdapter constructor error ");
            return ;
        }
        this.mFruitList = mFruitList;
    }

    @Override
    public FruitHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_fruit, parent, false);
        FruitHolder fruitHolder = new FruitHolder(view);
        return fruitHolder;
    }

    @Override
    public void onBindViewHolder(final FruitHolder holder, final int position) {
        holder.imageView.setImageResource(R.drawable.fruit);
        holder.textView.setText(mFruitList.get(position));
        holder.linearLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onItemClickListener.onItemClick(holder.linearLayout,position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return mFruitList.size();
    }


    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    class FruitHolder extends RecyclerView.ViewHolder {
        ImageView imageView ;
        TextView textView;
        LinearLayout linearLayout;
        public FruitHolder(View view) {
            super(view);
            linearLayout = (LinearLayout) view.findViewById(R.id.ll_view);
            imageView = (ImageView) view.findViewById(R.id.iv_fruit);
            textView = (TextView) view.findViewById(R.id.tv_name);
        }
    }

    /**
     * @author ytempest
     * @date 2017/10/24/024
     * Description:
     */
    public interface OnItemClickListener{
        void onItemClick(View view, int position);
    }
}
