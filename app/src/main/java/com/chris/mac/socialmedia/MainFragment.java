package com.chris.mac.socialmedia;

import android.app.ProgressDialog;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.github.gorbin.asne.core.SocialNetwork;
import com.github.gorbin.asne.core.SocialNetworkManager;
import com.github.gorbin.asne.core.listener.OnLoginCompleteListener;
import com.github.gorbin.asne.facebook.FacebookSocialNetwork;
import com.github.gorbin.asne.googleplus.GooglePlusSocialNetwork;
import com.github.gorbin.asne.linkedin.LinkedInSocialNetwork;
import com.github.gorbin.asne.twitter.TwitterSocialNetwork;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class MainFragment extends Fragment implements SocialNetworkManager.OnInitializationCompleteListener, OnLoginCompleteListener{

    private Button btnFacebook;
    private Button btnTwitter;
    private Button btnLinkedin;
    private Button btnGoogleplus;


    String facebook_app_id,
            facebook_scope,
            twitter_consumer_key,
            twitter_consumer_secret,
            linkedin_client_id,
            linkedin_client_secret,
            linkedin_scope,
            googleplus_client_id,
            callback_url;

    public static SocialNetworkManager mSocialNetworkManager;

    private OnFragmentInteractionListener mListener;

    public MainFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        mSocialNetworkManager = (SocialNetworkManager) getFragmentManager()
                .findFragmentByTag(MainActivity.SOCIAL_NETWORK_TAG);
        btnFacebook = (Button) rootView.findViewById(R.id.btn_facebook);
        btnFacebook.setOnClickListener(loginClick);

        btnLinkedin = (Button) rootView.findViewById(R.id.btn_linkedin);
        btnLinkedin.setOnClickListener(loginClick);

        initSocialNetworks();

        return rootView;
    }

    public void initSocialNetworks() {
        facebook_app_id = getActivity().getResources().getString(R.string.facebook_app_id);
        facebook_scope = getActivity().getResources().getString(R.string.facebook_scope);
        /*twitter_consumer_key = getActivity().getResources().getString(R.string.twitter_consumer_key);
        twitter_consumer_secret = getActivity().getResources().getString(R.string.twitter_consumer_secret);*/
        linkedin_client_id = getActivity().getResources().getString(R.string.linkedin_client_id);
        linkedin_client_secret = getActivity().getResources().getString(R.string.linkedin_client_secret);
        linkedin_scope = getActivity().getResources().getString(R.string.linkedin_scope);
        /*googleplus_client_id = getActivity().getResources().getString(R.string.googleplus_client_id);*/
        callback_url = getActivity().getResources().getString(R.string.callback_url);
        ArrayList<String> fbScope = new ArrayList<>();
        fbScope.addAll(Arrays.asList(facebook_scope));

        if (mSocialNetworkManager == null) {
            mSocialNetworkManager = new SocialNetworkManager();

            FacebookSocialNetwork fbNetwork = new FacebookSocialNetwork(this,
                    fbScope);
            /*TwitterSocialNetwork twNetwork = new TwitterSocialNetwork(this,
                    twitter_consumer_key,
                    twitter_consumer_secret,
                    callback_url);
            GooglePlusSocialNetwork gpNetwork = new GooglePlusSocialNetwork(this);*/
            LinkedInSocialNetwork inNetwork = new LinkedInSocialNetwork(this,
                    linkedin_client_id,
                    linkedin_client_secret,
                    callback_url,
                    linkedin_scope);

            mSocialNetworkManager.addSocialNetwork(fbNetwork);
          /*  mSocialNetworkManager.addSocialNetwork(twNetwork);
            mSocialNetworkManager.addSocialNetwork(gpNetwork);*/
            mSocialNetworkManager.addSocialNetwork(inNetwork);

            //Initiate every network from mSocialNetworkManager
            getFragmentManager()
                    .beginTransaction()
                    .add(mSocialNetworkManager, MainActivity.SOCIAL_NETWORK_TAG)
                    .commit();
            mSocialNetworkManager.setOnInitializationCompleteListener(this);
        }
        else {
            //if manager exist - get and setup login only for initialized SocialNetworks
            if(!mSocialNetworkManager.getInitializedSocialNetworks().isEmpty()) {
                List<SocialNetwork> socialNetworks = mSocialNetworkManager.getInitializedSocialNetworks();
                for (SocialNetwork socialNetwork : socialNetworks) {
                    socialNetwork.setOnLoginCompleteListener(this);
                    initSocialNetwork(socialNetwork);
                }
            }
        }
    }

    private void startProfile(int networkId){
        ProfileFragment profile = ProfileFragment.newInstannce(networkId);
        getActivity().getSupportFragmentManager().beginTransaction()
                .addToBackStack("profile")
                .replace(R.id.container, profile)
                .commit();
    }

    private View.OnClickListener loginClick = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            int networkId = 0;
            switch (view.getId()){
                case R.id.btn_facebook:
                    networkId = FacebookSocialNetwork.ID;
                    break;
                /*case R.id.btn_twitter:
                    networkId = TwitterSocialNetwork.ID;
                    break;*/
                case R.id.btn_linkedin:
                    networkId = LinkedInSocialNetwork.ID;
                    break;
               /* case R.id.btn_googleplus:
                    networkId = GooglePlusSocialNetwork.ID;
                    break;*/
            }
            SocialNetwork socialNetwork = mSocialNetworkManager.getSocialNetwork(networkId);
            if(!socialNetwork.isConnected()) {
                if(networkId != 0) {
                    socialNetwork.requestLogin();
                    MainActivity.showProgress("Loading social person");
                } else {
                    Toast.makeText(getActivity(), "Wrong networkId", Toast.LENGTH_LONG).show();
                }
            } else {
                startProfile(socialNetwork.getID());
            }
        }
    };



    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onSocialNetworkManagerInitialized() {
        for (SocialNetwork socialNetwork : mSocialNetworkManager.getInitializedSocialNetworks()) {
            socialNetwork.setOnLoginCompleteListener(this);
            initSocialNetwork(socialNetwork);
        }
    }

    @Override
    public void onLoginSuccess(int networkId) {
        MainActivity.hideProgress();
        startProfile(networkId);
    }

    @Override
    public void onError(int networkId, String requestID, String errorMessage, Object data) {
        MainActivity.hideProgress();
        Toast.makeText(getActivity(), "ERROR: " + errorMessage, Toast.LENGTH_LONG).show();
    }


    private void initSocialNetwork(SocialNetwork socialNetwork){
        if(socialNetwork.isConnected()){
            switch (socialNetwork.getID()){
                case FacebookSocialNetwork.ID:
                    btnFacebook.setText("Connected to Facebook");
                    break;
                case TwitterSocialNetwork.ID:
                    btnTwitter.setText("Connected to Twitter");
                    break;
                case LinkedInSocialNetwork.ID:
                    btnLinkedin.setText("Connected to LinkedIn");
                    break;
                case GooglePlusSocialNetwork.ID:
                    btnGoogleplus.setText("Connected to GooglePlus");
                    break;
            }
        }
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
}
