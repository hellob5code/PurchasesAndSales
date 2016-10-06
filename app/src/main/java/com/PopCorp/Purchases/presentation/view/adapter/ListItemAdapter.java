package com.PopCorp.Purchases.presentation.view.adapter;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.support.v7.util.SortedList;
import android.support.v7.widget.RecyclerView;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import com.PopCorp.Purchases.R;
import com.PopCorp.Purchases.data.callback.ListItemCallback;
import com.PopCorp.Purchases.data.model.ListItem;
import com.PopCorp.Purchases.data.model.ListItemSale;
import com.PopCorp.Purchases.data.utils.PreferencesManager;
import com.PopCorp.Purchases.presentation.decorator.ListItemDecorator;
import com.PopCorp.Purchases.presentation.utils.DecoratorBigDecimal;
import com.PopCorp.Purchases.presentation.utils.ImageLoaderAdapter;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class ListItemAdapter extends RecyclerView.Adapter<ListItemAdapter.ViewHolder> implements Filterable {

    private Context context;
    private ListItemCallback callback;
    private Comparator<ListItemDecorator> comparator;

    protected SortedList<ListItemDecorator> publishItems;
    protected List<ListItem> objects;
    private List<ListItem> selectedItems;
    private String currency;

    private View firstView;

    public ListItemAdapter(Context context, ListItemCallback callback, List<ListItem> objects, List<ListItem> selectedItems, Comparator<ListItemDecorator> saleComparator, String currency) {
        this.context = context;
        this.callback = callback;
        this.objects = objects;
        this.selectedItems = selectedItems;
        this.comparator = saleComparator;
        this.currency = currency;
        publishItems = new SortedList<>(ListItemDecorator.class, new SortedList.Callback<ListItemDecorator>() {
            @Override
            public boolean areContentsTheSame(ListItemDecorator oneItem, ListItemDecorator twoItem) {
                return false;
            }

            @Override
            public boolean areItemsTheSame(ListItemDecorator oneItem, ListItemDecorator twoItem) {
                return oneItem.equals(twoItem);
            }

            @Override
            public int compare(ListItemDecorator oneItem, ListItemDecorator twoItem) {
                return comparator.compare(oneItem, twoItem);
            }

            @Override
            public void onChanged(int position, int count) {
                notifyItemRangeChanged(position, count);
            }

            @Override
            public void onInserted(int position, int count) {
                notifyItemRangeInserted(position, count);
            }

            @Override
            public void onMoved(int fromPosition, int toPosition) {
                notifyItemMoved(fromPosition, toPosition);
            }

            @Override
            public void onRemoved(int position, int count) {
                notifyItemRangeRemoved(position, count);
            }
        });
        update(objects);
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public View getFirstView() {
        return firstView;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {
        public final View view;
        public final TextView headerName;
        public final ImageView image;
        public final TextView name;
        public final ImageView important;
        public final TextView mainInfo;
        public final TextView totalOne;
        public final TextView comment;
        public final TextView totalTwo;
        public final View mainLayout;
        private ClickListener clickListener;

        public ViewHolder(View view) {
            super(view);
            this.view = view;
            headerName = (TextView) view.findViewById(R.id.header_text);
            image = (ImageView) view.findViewById(R.id.image);
            name = (TextView) view.findViewById(R.id.name);
            important = (ImageView) view.findViewById(R.id.important);
            mainInfo = (TextView) view.findViewById(R.id.main_info);
            totalOne = (TextView) view.findViewById(R.id.total_one);
            comment = (TextView) view.findViewById(R.id.comment);
            totalTwo = (TextView) view.findViewById(R.id.total_two);
            mainLayout = view.findViewById(R.id.main_layout);
            view.setOnClickListener(this);
            view.setOnLongClickListener(this);
        }

        @Override
        public boolean onLongClick(View v) {
            clickListener.onLongClick(v, getAdapterPosition());
            return true;
        }

        public interface ClickListener {
            void onClick(View v, int position);
            void onLongClick(View v, int position);
        }

        public void setClickListener(ClickListener clickListener) {
            this.clickListener = clickListener;
        }

        @Override
        public void onClick(View v) {
            clickListener.onClick(v, getAdapterPosition());
        }
    }

    @Override
    public int getItemCount() {
        return publishItems.size();
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        ListItemDecorator decorator = publishItems.get(position);

        if (decorator.isHeader()) {
            holder.headerName.setText(decorator.getName());
            if (decorator.getCategory() == null) {
                holder.headerName.setTextColor(context.getResources().getColor(getPrimaryColorForTheme()));
            } else {
                holder.headerName.setTextColor(decorator.getCategory().getColor());
            }
        } else {
            if (position == 1){
                firstView = holder.name;
            }
            showItem(holder, decorator.getItem());
        }
        holder.setClickListener(new ViewHolder.ClickListener() {
            @Override
            public void onClick(View v, int position) {
                ListItemDecorator itemDecorator = publishItems.get(position);
                if (!itemDecorator.isHeader()) {
                    callback.onItemClicked(v, itemDecorator.getItem());
                    //publishItems.updateItemAt(publishItems.indexOf(itemDecorator), itemDecorator);
                }
            }

            @Override
            public void onLongClick(View v, int position) {
                ListItemDecorator itemDecorator = publishItems.get(position);
                if (!itemDecorator.isHeader()) {
                    callback.onItemLongClicked(v, itemDecorator.getItem());
                    //publishItems.updateItemAt(publishItems.indexOf(itemDecorator), itemDecorator);
                }
            }
        });
    }

    private int getPrimaryColorForTheme() {
        int[] attrs = new int[]{android.R.attr.textColorPrimary};
        TypedArray typedArray = context.obtainStyledAttributes(attrs);
        int backgroundResource = typedArray.getResourceId(0, 0);
        typedArray.recycle();
        return backgroundResource;
    }

    private void showItem(ViewHolder holder, ListItem item) {
        holder.name.setText(item.getName());

        holder.name.setTextSize(TypedValue.COMPLEX_UNIT_SP, Float.valueOf(PreferencesManager.getInstance().getListItemFontSize()));
        Float smallTextSize = Float.valueOf(PreferencesManager.getInstance().getListItemFontSizeSmall());

        holder.mainInfo.setTextSize(TypedValue.COMPLEX_UNIT_SP, smallTextSize);
        holder.comment.setTextSize(TypedValue.COMPLEX_UNIT_SP, smallTextSize);
        holder.totalTwo.setTextSize(TypedValue.COMPLEX_UNIT_SP, smallTextSize);
        holder.totalOne.setTextSize(TypedValue.COMPLEX_UNIT_SP, smallTextSize);

        String mainInfo = context.getString(R.string.list_item_main_info).replace("count", item.getCountString()).replace("unit", item.getEdizm()).replace("coast", item.getCoastString()).replace("currency", currency);
        String total = DecoratorBigDecimal.decor(item.getCount().multiply(item.getCoast()));

        holder.comment.setVisibility(item.getComment().isEmpty() && item.getShop().isEmpty() ? View.GONE : View.VISIBLE);
        holder.totalTwo.setVisibility(item.getComment().isEmpty() && item.getShop().isEmpty() ? View.GONE : View.VISIBLE);
        holder.totalOne.setVisibility(item.getComment().isEmpty() && item.getShop().isEmpty() ? View.VISIBLE : View.GONE);
        String totalString = context.getString(R.string.total).replace("total", total).replace("currency", currency);
        holder.totalOne.setText(totalString);
        holder.totalTwo.setText(totalString);

        if (item.getComment().isEmpty()) {
            if (!item.getShop().isEmpty()) {
                holder.comment.setText(context.getString(R.string.in_shop).replace("shop", item.getShop()));
            }
        } else {
            holder.comment.setText(item.getComment());

            if (!item.getShop().isEmpty()) {
                mainInfo += " " + context.getString(R.string.in_shop).replace("shop", item.getShop());
            }
        }

        if (item.isImportant()) {
            holder.important.setVisibility(View.VISIBLE);
        } else {
            holder.important.setVisibility(View.GONE);
        }
        holder.mainInfo.setText(mainInfo);

        if (item.isBuyed()) {
            holder.view.findViewById(R.id.layout_with_text).setAlpha(0.3f);
            holder.image.setAlpha(0.3f);
        } else {
            holder.view.findViewById(R.id.layout_with_text).setAlpha(1f);
            holder.image.setAlpha(1f);
        }

        if (item.getSale() != null){
            ImageLoaderAdapter.getInstance().displayImage(item.getSale().getImage(), holder.image);
            holder.image.setTag(item.getSale());
            holder.image.setOnClickListener(v -> callback.onItemSaleClicked(v, (ListItemSale) v.getTag()));
            holder.image.setBackgroundResource(android.R.color.transparent);
            holder.image.setVisibility(View.VISIBLE);
        } else {
            if (item.getCategory() != null) {
                ShapeDrawable coloredCircle = new ShapeDrawable(new OvalShape());
                coloredCircle.getPaint().setColor(item.getCategory().getColor());
                holder.image.setVisibility(View.VISIBLE);
                holder.image.setBackgroundDrawable(coloredCircle);
                holder.image.setImageResource(android.R.color.transparent);
            } else {
                holder.image.setVisibility(View.GONE);
            }
        }

        if (selectedItems.contains(item)) {
            firstView = holder.name;
            holder.mainLayout.setBackgroundResource(R.color.md_btn_selected);
        } else {
            holder.mainLayout.setBackgroundResource(android.R.color.transparent);
            /*int[] attrs = new int[]{R.attr.itemsBackground};
            TypedArray typedArray = context.obtainStyledAttributes(attrs);
            int backgroundResource = typedArray.getResourceId(0, 0);
            holder.mainLayout.setBackgroundResource(backgroundResource);
            typedArray.recycle();*/
            //holder.mainLayout.setBackgroundResource(R.drawable.list_selector);
        }
    }

    @Override
    public int getItemViewType(int position) {
        if (publishItems.get(position).isHeader()) {
            return 1;
        }
        return 2;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int position) {
        View v;
        if (position == 1) {
            v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_header, parent, false);
        } else {
            v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_listitem, parent, false);
        }

        return new ViewHolder(v);
    }


    @Override
    public Filter getFilter() {
        Filter filter = new Filter() {
            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                ArrayList<ListItem> newItems = (ArrayList<ListItem>) results.values;
                update(newItems);
            }

            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                FilterResults results = new FilterResults();
                ArrayList<ListItem> FilteredArrayNames = new ArrayList<>();

                if (constraint.equals("")) {
                    results.count = objects.size();
                    results.values = objects;
                    return results;
                } else {
                    for (ListItem item : objects) {
                        if (item.getShop().equals(constraint) || (!PreferencesManager.getInstance().isFilterListOnlyProductsOfShop() && item.getShop().isEmpty())) {
                            FilteredArrayNames.add(item);
                        }
                    }
                }

                results.count = FilteredArrayNames.size();
                results.values = FilteredArrayNames;
                return results;
            }
        };

        return filter;
    }

    public void onItemBuyed(ListItem item){
        ListItemDecorator decorator = new ListItemDecorator(item.getName(), false, item, item.isBuyed(), item.getCategory());
        int index = publishItems.indexOf(decorator);
        item.setBuyed(!item.isBuyed());
        publishItems.updateItemAt(index, decorator);
        publishItems.recalculatePositionOfItemAt(index);
        recalculateHeaders();
    }

    private void update(List<ListItem> newItems) {
        publishItems.beginBatchedUpdates();
        ArrayList<ListItemDecorator> arrayForRemove = new ArrayList<>();

        for (ListItem item : newItems) {
            boolean finded = false;
            for (int i = 0; i < publishItems.size(); i++) {
                ListItemDecorator decorator = publishItems.get(i);
                if (decorator.isHeader()) {
                    continue;
                }
                if (!newItems.contains(decorator.getItem())) {
                    arrayForRemove.add(decorator);
                }
                if (decorator.getItem().equals(item)) {
                    finded = true;
                    ListItemDecorator updatedDecorator = new ListItemDecorator(item.getName(), false, item, item.isBuyed(), item.getCategory());
                    publishItems.updateItemAt(i, updatedDecorator);
                }
            }
            if (!finded) {
                ListItemDecorator updatedDecorator = new ListItemDecorator(item.getName(), false, item, item.isBuyed(), item.getCategory());
                publishItems.add(updatedDecorator);
            }
        }
        for (ListItemDecorator decorator : arrayForRemove) {
            publishItems.remove(decorator);
        }
        recalculateHeaders();
        publishItems.endBatchedUpdates();
    }

    private void recalculateHeaders() {
        ArrayList<ListItemDecorator> arrayForRemove = new ArrayList<>();
        ArrayList<ListItemDecorator> headers = new ArrayList<>();
        if (PreferencesManager.getInstance().showCategories()){
            for (int i = 0; i < publishItems.size(); i++) {
                ListItemDecorator decorator = publishItems.get(i);
                if (decorator.isHeader()) {
                    continue;
                }
                if (PreferencesManager.getInstance().replaceBuyed() && decorator.getItem().isBuyed()){
                    continue;
                }
                ListItemDecorator header = new ListItemDecorator(decorator.getItem().getCategory().getName(), true, null, false, decorator.getItem().getCategory());
                if (!headers.contains(header)) {
                    headers.add(header);
                }
            }
        }
        if (PreferencesManager.getInstance().replaceBuyed()) {
            for (int i = 0; i < publishItems.size(); i++) {
                ListItemDecorator decorator = publishItems.get(i);
                if (!decorator.isHeader() && decorator.getItem().isBuyed()) {
                    ListItemDecorator header = new ListItemDecorator(context.getString(R.string.header_buyed), true, null, true, null);
                    headers.add(header);
                    break;
                }
            }
        }

        for (ListItemDecorator decorator : headers){
            int index = publishItems.indexOf(decorator);
            if (index == SortedList.INVALID_POSITION) {
                publishItems.add(decorator);
            }
        }
        arrayForRemove.clear();
        for (int i = 0; i < publishItems.size(); i++) {
            ListItemDecorator decorator = publishItems.get(i);
            if (decorator.isHeader() && !headers.contains(decorator)) {
                arrayForRemove.add(decorator);
            }
        }
        for (ListItemDecorator decorator : arrayForRemove) {
            publishItems.remove(decorator);
        }
    }

    public void updateAll(){
        notifyItemRangeChanged(0, publishItems.size());
    }
}
