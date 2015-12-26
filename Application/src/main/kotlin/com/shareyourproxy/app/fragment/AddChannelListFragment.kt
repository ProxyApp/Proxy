package com.shareyourproxy.app.fragment

import android.content.Intent
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.GraphRequest
import com.facebook.GraphResponse
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.shareyourproxy.R
import com.shareyourproxy.api.domain.model.Channel
import com.shareyourproxy.api.domain.model.ChannelType
import com.shareyourproxy.api.rx.command.AddUserChannelCommand
import com.shareyourproxy.app.adapter.AddChannelAdapter
import com.shareyourproxy.app.adapter.BaseRecyclerView
import com.shareyourproxy.app.dialog.AddAuthChannelDialog
import com.shareyourproxy.app.dialog.AddChannelDialog
import com.shareyourproxy.app.dialog.AddRedditChannelDialog
import com.shareyourproxy.app.dialog.ErrorDialog
import com.twitter.sdk.android.Twitter
import com.twitter.sdk.android.core.Callback
import com.twitter.sdk.android.core.Result
import com.twitter.sdk.android.core.TwitterException
import com.twitter.sdk.android.core.TwitterSession
import com.twitter.sdk.android.core.identity.TwitterLoginButton

import org.json.JSONException
import org.json.JSONObject

import java.util.Arrays
import java.util.UUID

import butterknife.Bind
import butterknife.BindString
import butterknife.ButterKnife
import retrofit.Response
import retrofit.Retrofit
import timber.log.Timber

import com.shareyourproxy.api.domain.factory.ChannelFactory.createModelInstance
import com.shareyourproxy.app.adapter.BaseViewHolder.ItemClickListener

/**
 * Display a list of channel types for the user to add new channel information to their profile.
 */
class AddChannelListFragment : BaseFragment(), ItemClickListener {

    @Bind(R.id.fragment_channel_list_recyclerview)
    internal var recyclerView: BaseRecyclerView
    @BindString(R.string.twitter_login_error)
    internal var twitterLoginError: String
    @BindString(R.string.twitter_login_error_message)
    internal var twitterLoginErrorMessage: String
    private var twitterLoginButton: TwitterLoginButton? = null
    private var _adapter: AddChannelAdapter? = null
    private var _callbackManager: CallbackManager? = null
    private var _loginManager: LoginManager? = null
    private var _clickedChannel: Channel? = null
    private val _fbLoginCallback = fbCallback

    override fun onCreateView(
            inflater: LayoutInflater?, container: ViewGroup?,
            savedInstanceState: Bundle?): View? {
        val rootView = inflater!!.inflate(R.layout.fragment_channellist, container, false)
        ButterKnife.bind(this, rootView)
        initialize()
        return rootView
    }

    /**
     * Initialize the facebook [ChannelType] callback manager.
     */
    fun initializeFaceBookLogin() {
        _callbackManager = CallbackManager.Factory.create()
        _loginManager = LoginManager.getInstance()
        _loginManager!!.registerCallback(_callbackManager, _fbLoginCallback)
    }

    /**
     * Callback manager to handle next or error when OAuthing a facebook user.

     * @return callback manager
     */
    private val fbCallback: FacebookCallback<LoginResult>
        get() = object : FacebookCallback<LoginResult> {
            override fun onSuccess(loginResult: LoginResult) {
                val request = GraphRequest.newMeRequest(
                        loginResult.accessToken
                ) { `object`, response ->
                    try {
                        val id = `object`.getString("id")
                        val channel = createModelInstance(
                                id, "", _clickedChannel!!.channelType(), "")

                        AddAuthChannelDialog.newInstance(channel).show(fragmentManager)
                    } catch (e: JSONException) {
                        Timber.e(Log.getStackTraceString(e))
                    }
                }
                val parameters = Bundle()
                parameters.putString("fields", "id")
                request.parameters = parameters
                request.executeAsync()
            }

            override fun onCancel() {
                Timber.i("Facebook Log In Cancelled")
            }

            override fun onError(exception: FacebookException) {
                Timber.e("Facebook LogIn Failed: %1$s", exception.message)
            }
        }

    /**
     * Initialize this fragments views.
     */
    private fun initialize() {
        initializeFaceBookLogin()
        initializeTwitterLogin()
        initializeRecyclerView()
    }

    /**
     * Initialize a twitter login button with a callback to handle errors.
     */
    private fun initializeTwitterLogin() {
        twitterLoginButton = TwitterLoginButton(activity)
        twitterLoginButton!!.visibility = View.GONE
        twitterLoginButton!!.callback = object : Callback<TwitterSession>() {
            override fun success(result: Result<TwitterSession>) {
                Twitter.getSessionManager().setActiveSession(result.data)
                val id = result.data.userId.toString()
                val handle = result.data.userName

                val channel = createModelInstance(
                        id, "", _clickedChannel!!.channelType(), handle)
                rxBus.post(AddUserChannelCommand(loggedInUser, channel))
            }

            override fun failure(exception: TwitterException) {
                Timber.e(Log.getStackTraceString(exception))
                ErrorDialog.newInstance(twitterLoginError,
                        twitterLoginErrorMessage).show(activity.supportFragmentManager)
            }

            override fun onResponse(response: Response<TwitterSession>, retrofit: Retrofit) {
            }

            override fun onFailure(t: Throwable) {

            }
        }
    }

    /**
     * Initialize a recyclerView with [Channel] data.
     */
    private fun initializeRecyclerView() {
        _adapter = AddChannelAdapter.newInstance(recyclerView, sharedPreferences, this)

        recyclerView.layoutManager = LinearLayoutManager(activity)
        recyclerView.setHasFixedSize(true)
        recyclerView.adapter = _adapter
    }

    override fun onItemClick(view: View, position: Int) {
        _clickedChannel = _adapter!!.getItemData(position)
        val channelType = _clickedChannel!!.channelType()
        when (channelType) {
            ChannelType.Custom, ChannelType.Phone, ChannelType.SMS, ChannelType.Email, ChannelType.Web, ChannelType.URL, ChannelType.Meerkat, ChannelType.Snapchat, ChannelType.Linkedin, ChannelType.FBMessenger, ChannelType.Hangouts, ChannelType.Whatsapp, ChannelType.Yo, ChannelType.Googleplus, ChannelType.Github, ChannelType.Address, ChannelType.Slack, ChannelType.Youtube, ChannelType.PlaystationNetwork, ChannelType.NintendoNetwork, ChannelType.Steam, ChannelType.Twitch, ChannelType.LeagueOfLegends, ChannelType.XboxLive, ChannelType.Tumblr, ChannelType.Ello, ChannelType.Venmo, ChannelType.Periscope, ChannelType.Medium, ChannelType.Soundcloud, ChannelType.Skype -> AddChannelDialog.newInstance(
                    _clickedChannel!!.channelType()).show(activity.supportFragmentManager)
            ChannelType.Facebook -> _loginManager!!.logInWithReadPermissions(
                    this, Arrays.asList("public_profile", "user_friends"))
            ChannelType.Twitter -> try {
                activity.packageManager.getPackageInfo("com.twitter.android", 0)
                twitterLoginButton!!.performClick()
            } catch (e: Exception) {
                val channel = createModelInstance(UUID.randomUUID().toString(),
                        "", _clickedChannel!!.channelType(), "")
                AddAuthChannelDialog.newInstance(channel).show(activity.supportFragmentManager)
            }

            ChannelType.Reddit -> AddRedditChannelDialog.newInstance(_clickedChannel!!.channelType()).show(activity.supportFragmentManager)
            else -> {
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        _callbackManager!!.onActivityResult(requestCode, resultCode, data)
        twitterLoginButton!!.onActivityResult(requestCode, resultCode, data)
    }

    companion object {

        /**
         * Return new fragment instance.

         * @return AddChannelListFragment
         */
        fun newInstance(): AddChannelListFragment {
            return AddChannelListFragment()
        }
    }

}
/**
 * Constructor.
 */
