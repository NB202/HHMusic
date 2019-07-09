package com.hhmusic.widget;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;


public class DragSortRecycler extends RecyclerView.ItemDecoration implements RecyclerView.OnItemTouchListener {

    final String TAG = "DragSortRecycler";

    final boolean DEBUG = false;
    OnItemMovedListener moveInterface;
    @Nullable
    OnDragStateChangedListener dragStateChangedListener;
    Paint bgColor = new Paint();
    private int dragHandleWidth = 0;
    private int selectedDragItemPos = -1;
    private int fingerAnchorY;
    RecyclerView.OnScrollListener scrollListener = new RecyclerView.OnScrollListener() {
        @Override
        public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
            super.onScrollStateChanged(recyclerView, newState);
        }

        @Override
        public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
            super.onScrolled(recyclerView, dx, dy);
            debugLog("Scrolled: " + dx + " " + dy);
            fingerAnchorY -= dy;
        }
    };
    private int fingerY;
    private int fingerOffsetInViewY;
    private float autoScrollWindow = 0.1f;
    private float autoScrollSpeed = 0.5f;
    private BitmapDrawable floatingItem;
    private Rect floatingItemStatingBounds;
    private Rect floatingItemBounds;
    private float floatingItemAlpha = 0.5f;
    private int floatingItemBgColor = 0;
    private int viewHandleId = -1;
    private boolean isDragging;

    private void debugLog(String log) {
        if (DEBUG)
            Log.d(TAG, log);
    }

    public RecyclerView.OnScrollListener getScrollListener() {
        return scrollListener;
    }


    public void setOnItemMovedListener(OnItemMovedListener swif) {
        moveInterface = swif;
    }

    public void setViewHandleId(int id) {
        viewHandleId = id;
    }

    public void setLeftDragArea(int w) {
        dragHandleWidth = w;
    }

    public void setFloatingAlpha(float a) {
        floatingItemAlpha = a;
    }

    public void setFloatingBgColor(int c) {
        floatingItemBgColor = c;
    }


    public void setAutoScrollWindow(float w) {
        autoScrollWindow = w;
    }


    public void setAutoScrollSpeed(float speed) {
        autoScrollSpeed = speed;
    }

    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView rv, RecyclerView.State state) {
        super.getItemOffsets(outRect, view, rv, state);

        debugLog("getItemOffsets");

        debugLog("View top = " + view.getTop());
        if (selectedDragItemPos != -1) {
            int itemPos = rv.getChildLayoutPosition(view);
            debugLog("itemPos =" + itemPos);

            if (!canDragOver(itemPos)) {
                return;
            }


            float totalMovement = fingerY - fingerAnchorY;

            if (itemPos == selectedDragItemPos) {
                view.setVisibility(View.INVISIBLE);
            } else {

                view.setVisibility(View.VISIBLE);


                float floatMiddleY = floatingItemBounds.top + floatingItemBounds.height() / 2;


                {
                    if ((itemPos > selectedDragItemPos) && (view.getTop() < floatMiddleY)) {
                        float amountUp = (floatMiddleY - view.getTop()) / (float) view.getHeight();

                        if (amountUp > 1)
                            amountUp = 1;

                        outRect.top = -(int) (floatingItemBounds.height() * amountUp);
                        outRect.bottom = (int) (floatingItemBounds.height() * amountUp);
                    }

                }
                {
                    if ((itemPos < selectedDragItemPos) && (view.getBottom() > floatMiddleY)) {
                        float amountDown = ((float) view.getBottom() - floatMiddleY) / (float) view.getHeight();

                        if (amountDown > 1)
                            amountDown = 1;

                        outRect.top = (int) (floatingItemBounds.height() * amountDown);
                        outRect.bottom = -(int) (floatingItemBounds.height() * amountDown);
                    }
                }
            }
        } else {
            outRect.top = 0;
            outRect.bottom = 0;

            view.setVisibility(View.VISIBLE);
        }
    }


    private int getNewPostion(RecyclerView rv) {
        int itemsOnScreen = rv.getLayoutManager().getChildCount();

        float floatMiddleY = floatingItemBounds.top + floatingItemBounds.height() / 2;

        int above = 0;
        int below = Integer.MAX_VALUE;
        for (int n = 0; n < itemsOnScreen; n++)
        {

            View view = rv.getLayoutManager().getChildAt(n);

            if (view.getVisibility() != View.VISIBLE)
                continue;

            int itemPos = rv.getChildLayoutPosition(view);

            if (itemPos == selectedDragItemPos)
                continue;

            float viewMiddleY = view.getTop() + view.getHeight() / 2;
            if (floatMiddleY > viewMiddleY)
            {
                if (itemPos > above)
                    above = itemPos;
            } else if (floatMiddleY <= viewMiddleY)
            {
                if (itemPos < below)
                    below = itemPos;
            }
        }
        debugLog("above = " + above + " below = " + below);

        if (below != Integer.MAX_VALUE) {
            if (below < selectedDragItemPos)
                below++;
            return below - 1;
        } else {
            if (above < selectedDragItemPos)
                above++;

            return above;
        }
    }


    @Override
    public boolean onInterceptTouchEvent(RecyclerView rv, MotionEvent e) {
        debugLog("onInterceptTouchEvent");


        {
            View itemView = rv.findChildViewUnder(e.getX(), e.getY());

            if (itemView == null)
                return false;

            boolean dragging = false;

            if ((dragHandleWidth > 0) && (e.getX() < dragHandleWidth)) {
                dragging = true;
            } else if (viewHandleId != -1) {

                View handleView = itemView.findViewById(viewHandleId);

                if (handleView == null) {
                    Log.e(TAG, "The view ID " + viewHandleId + " was not found in the RecycleView item");
                    return false;
                }


                if (handleView.getVisibility() != View.VISIBLE) {
                    return false;
                }


                int[] parentItemPos = new int[2];
                itemView.getLocationInWindow(parentItemPos);

                int[] handlePos = new int[2];
                handleView.getLocationInWindow(handlePos);

                int xRel = handlePos[0] - parentItemPos[0];
                int yRel = handlePos[1] - parentItemPos[1];

                Rect touchBounds = new Rect(itemView.getLeft() + xRel, itemView.getTop() + yRel,
                        itemView.getLeft() + xRel + handleView.getWidth(),
                        itemView.getTop() + yRel + handleView.getHeight()
                );

                if (touchBounds.contains((int) e.getX(), (int) e.getY()))
                    dragging = true;

                debugLog("parentItemPos = " + parentItemPos[0] + " " + parentItemPos[1]);
                debugLog("handlePos = " + handlePos[0] + " " + handlePos[1]);
            }


            if (dragging) {
                debugLog("Started Drag");

                setIsDragging(true);

                floatingItem = createFloatingBitmap(itemView);

                fingerAnchorY = (int) e.getY();
                fingerOffsetInViewY = fingerAnchorY - itemView.getTop();
                fingerY = fingerAnchorY;

                selectedDragItemPos = rv.getChildLayoutPosition(itemView);
                debugLog("selectedDragItemPos = " + selectedDragItemPos);

                return true;
            }
        }
        return false;
    }

    @Override
    public void onRequestDisallowInterceptTouchEvent(boolean b) {

    }

    @Override
    public void onTouchEvent(RecyclerView rv, MotionEvent e) {
        debugLog("onTouchEvent");

        if ((e.getAction() == MotionEvent.ACTION_UP) ||
                (e.getAction() == MotionEvent.ACTION_CANCEL)) {
            if ((e.getAction() == MotionEvent.ACTION_UP) && selectedDragItemPos != -1) {
                int newPos = getNewPostion(rv);
                if (moveInterface != null)
                    moveInterface.onItemMoved(selectedDragItemPos, newPos);
            }

            setIsDragging(false);
            selectedDragItemPos = -1;
            floatingItem = null;
            rv.invalidateItemDecorations();
            return;
        }


        fingerY = (int) e.getY();

        if (floatingItem != null) {
            floatingItemBounds.top = fingerY - fingerOffsetInViewY;

            if (floatingItemBounds.top < -floatingItemStatingBounds.height() / 2)
                floatingItemBounds.top = -floatingItemStatingBounds.height() / 2;

            floatingItemBounds.bottom = floatingItemBounds.top + floatingItemStatingBounds.height();

            floatingItem.setBounds(floatingItemBounds);
        }


        float scrollAmount = 0;
        if (fingerY > (rv.getHeight() * (1 - autoScrollWindow))) {
            scrollAmount = (fingerY - (rv.getHeight() * (1 - autoScrollWindow)));
        } else if (fingerY < (rv.getHeight() * autoScrollWindow)) {
            scrollAmount = (fingerY - (rv.getHeight() * autoScrollWindow));
        }
        debugLog("Scroll: " + scrollAmount);

        scrollAmount *= autoScrollSpeed;
        rv.scrollBy(0, (int) scrollAmount);

        rv.invalidateItemDecorations();
    }

    private void setIsDragging(final boolean dragging) {
        if (dragging != isDragging) {
            isDragging = dragging;
            if (dragStateChangedListener != null) {
                if (isDragging) {
                    dragStateChangedListener.onDragStart();
                } else {
                    dragStateChangedListener.onDragStop();
                }
            }
        }
    }

    public void setOnDragStateChangedListener(final OnDragStateChangedListener dragStateChangedListener) {
        this.dragStateChangedListener = dragStateChangedListener;
    }

    @Override
    public void onDrawOver(Canvas c, RecyclerView parent, RecyclerView.State state) {
        if (floatingItem != null) {
            floatingItem.setAlpha((int) (255 * floatingItemAlpha));
            bgColor.setColor(floatingItemBgColor);
            c.drawRect(floatingItemBounds, bgColor);
            floatingItem.draw(c);
        }
    }


    protected boolean canDragOver(int position) {
        return true;
    }

    private BitmapDrawable createFloatingBitmap(View v) {
        floatingItemStatingBounds = new Rect(v.getLeft(), v.getTop(), v.getRight(), v.getBottom());
        floatingItemBounds = new Rect(floatingItemStatingBounds);

        Bitmap bitmap = Bitmap.createBitmap(floatingItemStatingBounds.width(),
                floatingItemStatingBounds.height(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        v.draw(canvas);

        BitmapDrawable retDrawable = new BitmapDrawable(v.getResources(), bitmap);
        retDrawable.setBounds(floatingItemBounds);

        return retDrawable;
    }

    public interface OnItemMovedListener {
        void onItemMoved(int from, int to);
    }


    public interface OnDragStateChangedListener {
        void onDragStart();

        void onDragStop();
    }
}