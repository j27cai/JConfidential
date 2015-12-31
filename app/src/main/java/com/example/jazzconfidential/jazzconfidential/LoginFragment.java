package com.example.jazzconfidential.jazzconfidential;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.UiThread;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.HttpMethod;
import com.facebook.Profile;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.facebook.share.widget.ShareDialog;
import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseInstallation;
import com.parse.ParseObject;
import com.parse.ParsePush;
import com.parse.ParseQuery;
import com.parse.SaveCallback;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;
import java.util.UUID;


public class LoginFragment extends android.support.v4.app.Fragment {

    public static String UserId = "";
    public static String UserName = "";

    public static ShareDialog shareDialog;

    public static CallbackManager mCallbackManager;
    public static FacebookCallback<LoginResult> mCallback = new FacebookCallback<LoginResult>() {
        @Override
        public void onSuccess(LoginResult loginResult) {
            AccessToken accessToken = loginResult.getAccessToken();
            String userId= accessToken.getUserId();
            GetOrCreateLocalId(userId == null ? "FacebookExternal1" : userId, "Facebook");
            System.out.println("ID: " + userId);
        }

        @Override
        public void onCancel() {

        }

        @Override
        public void onError(FacebookException e) {

        }
    };

    public LoginFragment() {

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        FacebookSdk.sdkInitialize(getActivity().getApplicationContext());
        mCallbackManager = CallbackManager.Factory.create();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.login_fragment, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        LoginButton loginButton = (LoginButton)view.findViewById(R.id.login_button);
        //loginButton.setReadPermissions();
        loginButton.setPublishPermissions("publish_actions");
        loginButton.setFragment(this);
        loginButton.registerCallback(mCallbackManager, mCallback);

        // Go to the main menu activity if a valid sessions already exists
        if (AccessToken.getCurrentAccessToken() != null) {
            Intent intent = new Intent(this.getActivity(), MainMenuActivity.class);
            String userId = AccessToken.getCurrentAccessToken().getUserId();
            GetOrCreateLocalId(userId == null ? "FacebookExternal1" : userId, "Facebook");
            startActivity(intent);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        mCallbackManager.onActivityResult(requestCode, resultCode, data);
    }


    private static void pushSubscribe() {

    }

    private static void GetOrCreateLocalId(final String externalId, final String identityProvider){
        ParseQuery query = ParseQuery.getQuery("User");
        query.whereEqualTo("externalId",externalId);
        query.whereEqualTo("identityProvider", identityProvider);
        query.findInBackground(new FindCallback<ParseObject>() {
            public void done(List<ParseObject> objects, ParseException e) {
                if (e == null) {
                    if (!objects.isEmpty()) {
                        UserId = objects.get(0).getString("localId");
                        UserName = objects.get(0).getString("userName");
                        //PFInstallation
                        ParsePush.subscribeInBackground("p" + UserId);

                        Database.LoadGames(UserId);

                        return;
                    }
                    //New User
                    UserId = UUID.randomUUID().toString();

                    new GraphRequest(
                            AccessToken.getCurrentAccessToken(),
                            "/me",
                            null,
                            HttpMethod.GET,
                            new GraphRequest.Callback() {
                                public void onCompleted(GraphResponse response) {
                                    try {
                                        JSONObject object = response.getJSONObject();
                                        UserName = object.getString("first_name") + " " + object.getString("last_name");
                                        ParseObject testObject = new ParseObject("User");
                                        testObject.put("identityProvider", identityProvider);
                                        testObject.put("localId", UserId);
                                        testObject.put("userName", UserName);
                                        testObject.put("externalId", externalId);
                                        testObject.saveInBackground();
                                        ParsePush.subscribeInBackground("p" + UserId);

                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                }
                            }
                    ).executeAsync();


                } else {
                    e.printStackTrace();
                }
            }
        });
    }
}
