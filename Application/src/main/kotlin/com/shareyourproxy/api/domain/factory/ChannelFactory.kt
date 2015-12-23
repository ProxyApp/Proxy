package com.shareyourproxy.api.domain.factory


import com.shareyourproxy.api.domain.model.Channel
import com.shareyourproxy.api.domain.model.ChannelType
import com.shareyourproxy.api.domain.realm.RealmChannel
import com.shareyourproxy.api.domain.realm.RealmChannelType
import com.shareyourproxy.api.domain.realm.RealmString
import io.realm.RealmList
import java.util.*


/**
 * Factory to create Channels or RealmChannels.
 */
object ChannelFactory {

    fun createModelInstance(id: String, label: String, channelType: ChannelType, actionAddress: String): Channel {
        return Channel(id, label, channelType, actionAddress, false)
    }

    fun createModelInstance(channelType: ChannelType): Channel {
        val label = channelType.toString()
        return Channel(label, label, channelType, label, false)
    }

    fun createModelInstance(copyChannel: Channel, actionAddress: String): Channel {
        return Channel(copyChannel.id, copyChannel.label, copyChannel.channelType, actionAddress, false)
    }

    fun createPublicChannel(copyChannel: Channel, isPublic: Boolean): Channel {
        return Channel(copyChannel.id, copyChannel.label, copyChannel.channelType, copyChannel.actionAddress, isPublic)
    }

    fun createModelInstance(realmChannel: RealmChannel): Channel {
        return Channel(realmChannel.id, realmChannel.label, getModelChannelType(realmChannel.channelType), realmChannel.actionAddress, realmChannel.isPublic)
    }

    fun getModelChannelType(channelType: RealmChannelType): ChannelType {
        return ChannelType.valueOfLabel(channelType.label)
    }

    /**
     * Return a RealmList of Contacts from a user
     * @param realmChannels to get channels from
     * @return RealmList of Contacts
     */
    fun getModelChannels(realmChannels: RealmList<RealmChannel>): HashMap<String, Channel> {
        val channels = HashMap<String, Channel>(realmChannels.size)
        for (realmChannel in realmChannels) {
            channels.put(realmChannel.id, createModelInstance(realmChannel))
        }
        return channels
    }

    /**
     * Return a RealmList of Contact ids from a user
     * @param realmChannels to get channels from
     * @return RealmList of Contacts
     */
    fun getModelChannelList(realmChannels: RealmList<RealmString>): HashSet<String> {
        val channels = HashSet<String>(realmChannels.size)
        for (realmChannel in realmChannels) {
            channels.add(realmChannel.value)
        }
        return channels
    }

}