package com.nativetemplate.utils;

import android.graphics.Rect;
import android.support.v7.widget.RecyclerView;
import android.view.View;

/**
 * Created by ant000160 on 6/11/17.
 */

public class DividerItemDecoration extends RecyclerView.ItemDecoration {
    private int space;

    public DividerItemDecoration(int space) {
        this.space = space;
    }

    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
        outRect.left = space;
        outRect.right = space;


        //for vertical scrolling
        outRect.bottom = space;
        outRect.top = space;} }