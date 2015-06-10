package com.shareyourproxy.app.adapter;

import android.content.Context;
import android.support.v7.util.SortedList;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.shareyourproxy.R;
import com.shareyourproxy.api.domain.model.Contact;
import com.shareyourproxy.widget.transform.CircleTransform;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import butterknife.InjectView;

import static com.shareyourproxy.app.adapter.BaseViewHolder.*;
import static com.shareyourproxy.util.ObjectUtils.joinWithSpace;

/**
 * Created by Evan on 6/9/15.
 */
public class ContactAdapter extends RecyclerView.Adapter<BaseViewHolder> {

    /**
     * An Adapter to handle displaying {@link Contact}s.
     */
    private final ItemClickListener _clickListener;
    //Persisted Contact Array Data
    private SortedList<Contact> _contacts;
    private SortedList.Callback<Contact> _sortedListCallback;

    public ContactAdapter(ArrayList<Contact> contacts, ItemClickListener listener) {
        _clickListener = listener;
        _contacts = new SortedList<>(Contact.class, getSortedCallback(), contacts.size());
        addContactsArray(contacts);
    }

    /**
     * Create a newInstance of a {@link ContactAdapter} with blank data.
     *
     * @return an {@link ContactAdapter} with no data
     */
    public static ContactAdapter newInstance(
        ArrayList<Contact> contacts, ItemClickListener listener) {
        return new ContactAdapter(contacts, listener);
    }

    public SortedList.Callback<Contact> getSortedCallback() {
        if (_sortedListCallback == null) {
            _sortedListCallback = new SortedList.Callback<Contact>() {
                @Override
                public int compare(Contact item1, Contact item2) {
                    int comapreFirst = item1.first().compareTo(item2.first());

                    if (comapreFirst == 0) {
                        return item1.last().compareTo(item2.last());
                    } else {
                        return comapreFirst;
                    }
                }

                @Override
                public void onInserted(int position, int count) {
                    notifyItemRangeInserted(position, count);
                }

                @Override
                public void onRemoved(int position, int count) {
                    notifyItemRangeRemoved(position, count);
                }

                @Override
                public void onMoved(int fromPosition, int toPosition) {
                    notifyItemMoved(fromPosition, toPosition);
                }

                @Override
                public void onChanged(int position, int count) {
                    notifyItemRangeChanged(position, count);
                }

                @Override
                public boolean areContentsTheSame(Contact item1, Contact item2) {
                    // we dont compare resId because its probably going to be removed
                    return (item1.id().value().equals(item2.id().value())
                        && item1.first().equals(item2.first())
                        && item1.last().equals(item2.last())
                        && item1.channels().equals(item2.channels()));
                }

                @Override
                public boolean areItemsTheSame(Contact item1, Contact item2) {
                    return (item1.id().value().equals(item2.id().value()));
                }
            };
        }
        return _sortedListCallback;
    }

    @Override
    public BaseViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
            .inflate(R.layout.adapter_contact_item, parent, false);
        return ContactViewHolder.newInstance(view, _clickListener);
    }

    @Override
    public void onBindViewHolder(BaseViewHolder holder, int position) {
        setItemViewData((ContactViewHolder) holder, getItemData(position));
    }

    /**
     * Set this ViewHolders underlying {@link Contact} data.
     *
     * @param holder {@link Contact} {@link BaseViewHolder}
     * @param user   the {@link Contact} data
     */
    private void setItemViewData(ContactViewHolder holder, Contact user) {
        Context context = holder._view.getContext();
        holder.userName.setText(joinWithSpace(new String[]{ user.first(),
            user.last() }));
        Picasso.with(context).load(user.imageURL())
            .placeholder(R.mipmap.ic_proxy)
            .transform(new CircleTransform())
            .into(holder.userImage);
    }

    @Override
    public int getItemCount() {
        return _contacts.size();
    }

    public void refreshContactList(ArrayList<Contact> contacts) {
        _contacts.clear();
        addContactsArray(contacts);
    }

    private void addContactsArray(ArrayList<Contact> contacts) {
        _contacts.beginBatchedUpdates();
        for (Contact user : contacts) {
            _contacts.add(user);
        }
        _contacts.endBatchedUpdates();
    }

    /**
     * Get the desired {@link Contact} based off its position in a list.
     *
     * @param position the position in the list
     * @return the desired {@link Contact}
     */
    public Contact getItemData(int position) {
        return _contacts.get(position);
    }

    /**
     * ViewHolder for the entered {@link Contact} data.
     */
    protected static class ContactViewHolder extends BaseViewHolder {
        @InjectView(R.id.adapter_contact_name)
        protected TextView userName;
        @InjectView(R.id.adapter_contact_image)
        protected ImageView userImage;

        /**
         * Constructor for the holder.
         *
         * @param view              the inflated view
         * @param itemClickListener click listener for each item
         */
        private ContactViewHolder(View view, ItemClickListener itemClickListener) {
            super(view, itemClickListener);
        }

        /**
         * Create a new Instance of the ViewHolder.
         *
         * @param view              inflated in {@link #onCreateViewHolder}
         * @param itemClickListener click listener for each ViewHolder item
         * @return a {@link Contact} ViewHolder instance
         */
        public static ContactViewHolder newInstance(
            View view, ItemClickListener itemClickListener) {
            return new ContactViewHolder(view, itemClickListener);
        }
    }
}
