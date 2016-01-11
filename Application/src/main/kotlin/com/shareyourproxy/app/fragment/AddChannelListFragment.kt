package com.shareyourproxy.app.fragment

import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.shareyourproxy.R.id.fragment_channel_list_recyclerview
import com.shareyourproxy.R.layout.fragment_channellist
import com.shareyourproxy.api.domain.model.Channel
import com.shareyourproxy.api.domain.model.ChannelType
import com.shareyourproxy.app.adapter.AddChannelAdapter
import com.shareyourproxy.app.adapter.BaseRecyclerView
import com.shareyourproxy.app.adapter.BaseViewHolder.ItemClickListener
import com.shareyourproxy.app.dialog.AddChannelDialog
import com.shareyourproxy.app.dialog.AddRedditChannelDialog
import com.shareyourproxy.util.ButterKnife.LazyVal
import com.shareyourproxy.util.ButterKnife.bindView

/**
 * Display a list of channel types for the user to add new channel information to their profile.
 */
internal final class AddChannelListFragment() : BaseFragment(), ItemClickListener {

    private val recyclerView: BaseRecyclerView by bindView(fragment_channel_list_recyclerview)
    private val adapter: AddChannelAdapter by LazyVal{ AddChannelAdapter(recyclerView, sharedPreferences, this)}

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(fragment_channellist, container, false)
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        initialize()
    }

    /**
     * Initialize this fragments views.
     */
    private fun initialize() {
        initializeRecyclerView()
    }

    /**
     * Initialize a recyclerView with [Channel] data.
     */
    private fun initializeRecyclerView() {
        recyclerView.layoutManager = LinearLayoutManager(activity)
        recyclerView.setHasFixedSize(true)
        recyclerView.adapter = adapter
    }

    override fun onItemClick(view: View, position: Int) {
        val clickedChannel: Channel = adapter.getItemData(position)
        val channelType = clickedChannel.channelType
        when (channelType) {
            ChannelType.Custom,
            ChannelType.Phone,
            ChannelType.SMS,
            ChannelType.Email,
            ChannelType.Web,
            ChannelType.URL,
            ChannelType.Meerkat,
            ChannelType.Snapchat,
            ChannelType.Linkedin,
            ChannelType.FBMessenger,
            ChannelType.Hangouts,
            ChannelType.Whatsapp,
            ChannelType.Yo,
            ChannelType.Googleplus,
            ChannelType.Github,
            ChannelType.Address,
            ChannelType.Slack,
            ChannelType.Youtube,
            ChannelType.PlaystationNetwork,
            ChannelType.NintendoNetwork,
            ChannelType.Steam,
            ChannelType.Twitch,
            ChannelType.LeagueOfLegends,
            ChannelType.XboxLive,
            ChannelType.Tumblr,
            ChannelType.Ello,
            ChannelType.Venmo,
            ChannelType.Periscope,
            ChannelType.Medium,
            ChannelType.Soundcloud,
            ChannelType.Skype -> AddChannelDialog(clickedChannel.channelType).show(activity.supportFragmentManager)
            ChannelType.Reddit -> AddRedditChannelDialog.newInstance(clickedChannel.channelType).show(activity.supportFragmentManager)
            else -> {
            }
        }
    }
}
