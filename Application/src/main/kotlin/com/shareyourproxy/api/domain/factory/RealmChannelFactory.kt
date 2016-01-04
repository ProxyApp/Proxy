package com.shareyourproxy.api.domain.factory

import com.shareyourproxy.api.domain.model.Channel
import com.shareyourproxy.api.domain.realm.RealmChannel
import com.shareyourproxy.api.domain.realm.RealmChannelType
import io.realm.RealmList
import java.util.*

/**
 * Factory for creating [RealmChannel]s.
 */
internal object RealmChannelFactory {
    fun getRealmChannels(channels: HashMap<String, Channel>): RealmList<RealmChannel> {
        val realmChannelArray: RealmList<RealmChannel>
        realmChannelArray = RealmList<RealmChannel>()
        for (channelEntry in channels.entries) {
            val channel = channelEntry.value
            val realmChannel = RealmChannel()
            val realmChannelType = RealmChannelType()
            //construct the newChannel section

            //construct the newChannel type
            realmChannelType.weight = channel.channelType.weight
            realmChannelType.label = channel.channelType.label

            //construct the newChannel
            realmChannel.id = channel.id
            realmChannel.label = channel.label
            realmChannel.channelType = realmChannelType
            realmChannel.actionAddress = channel.actionAddress
            realmChannel.isPublic = channel.isPublic

            //add to array
            realmChannelArray.add(realmChannel)
        }
        return realmChannelArray
    }
}
