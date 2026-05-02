package org.jaya.android;

import android.app.Activity;
import android.util.TypedValue;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.TextView;

import java.util.List;

class TableOfContentsListAdapter extends BaseAdapter implements JayaDocListView.IListAdapterWithScaleFactor {

    interface OnNodeClickListener {
        void onNodeClick(TableOfContentsActivity.TreeNode node);
    }

    interface OnNodeLongClickListener {
        void onNodeLongClick(TableOfContentsActivity.TreeNode node);
    }

    interface OnNodeSearchClickListener {
        void onNodeSearchClick(TableOfContentsActivity.TreeNode node);
    }

    private static final int INDENT_DP = 20;

    private float mScaleFactor = 1.0f;
    private final List<TableOfContentsActivity.TreeNode> mNodes;
    private final Activity mActivity;
    private final OnNodeClickListener mClickListener;
    private OnNodeLongClickListener mLongClickListener;
    private OnNodeSearchClickListener mSearchClickListener;

    TableOfContentsListAdapter(Activity activity,
                               List<TableOfContentsActivity.TreeNode> nodes,
                               OnNodeClickListener clickListener) {
        mActivity = activity;
        mNodes = nodes;
        mClickListener = clickListener;
    }

    void setOnNodeLongClickListener(OnNodeLongClickListener listener) {
        mLongClickListener = listener;
    }

    void setOnNodeSearchClickListener(OnNodeSearchClickListener listener) {
        mSearchClickListener = listener;
    }

    @Override
    public int getCount() {
        return mNodes.size();
    }

    @Override
    public Object getItem(int position) {
        return mNodes.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View itemView;
        ViewHolder holder;
        if (convertView == null) {
            itemView = View.inflate(mActivity, R.layout.toc_tree_item, null);
            holder = new ViewHolder(itemView);
            itemView.setTag(holder);
        } else {
            itemView = convertView;
            holder = (ViewHolder) itemView.getTag();
        }
        holder.bind(mNodes.get(position), mScaleFactor);
        return itemView;
    }

    @Override
    public void onScaleBegin(ScaleGestureDetector detector) {
        mScaleFactor = 1.0f;
    }

    @Override
    public void setScaleFactor(float scaleFactor) {
        mScaleFactor = scaleFactor;
    }

    @Override
    public void onScaleEnd(float scaleFactor) {
        mScaleFactor = 1.0f;
        float currentSize = PreferencesManager.getFontSize();
        PreferencesManager.setFontSize(clampedFontSize(currentSize * scaleFactor));
    }

    private float clampedFontSize(float size) {
        return Math.max(PreferencesManager.MIN_FONT_SIZE,
                Math.min(size, PreferencesManager.MAX_FONT_SIZE));
    }

    class ViewHolder {
        private final View itemView;
        private final TextView arrowView;
        private final TextView labelView;
        private final ImageButton searchIcon;
        private TableOfContentsActivity.TreeNode mNode;

        ViewHolder(View view) {
            itemView = view;
            arrowView = (TextView) view.findViewById(R.id.toc_arrow);
            labelView = (TextView) view.findViewById(R.id.toc_label);
            searchIcon = (ImageButton) view.findViewById(R.id.toc_search_icon);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mNode != null) {
                        mClickListener.onNodeClick(mNode);
                    }
                }
            });
            itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    if (mNode != null && mLongClickListener != null) {
                        mLongClickListener.onNodeLongClick(mNode);
                        return true;
                    }
                    return false;
                }
            });
            searchIcon.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mNode != null && mSearchClickListener != null) {
                        mSearchClickListener.onNodeSearchClick(mNode);
                    }
                }
            });
        }

        void bind(TableOfContentsActivity.TreeNode node, float scaleFactor) {
            mNode = node;

            // Left padding for indentation (depth 0 = top-level folders, no extra indent)
            int indentPx = (int) TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_DIP,
                    INDENT_DP * node.depth,
                    itemView.getResources().getDisplayMetrics());
            itemView.setPadding(indentPx, 4, 8, 4);

            // Arrow: ▶ collapsed folder, ▼ expanded folder, blank for leaves
            if (node.isLeaf()) {
                arrowView.setText("  ");
            } else if (node.expanded) {
                arrowView.setText("\u25BC"); // ▼
            } else {
                arrowView.setText("\u25B6"); // ▶
            }

            labelView.setText(node.displayLabel);
            labelView.setTextSize(
                    TypedValue.COMPLEX_UNIT_SP,
                    clampedFontSize(PreferencesManager.getFontSize() * scaleFactor));
        }
    }
}
