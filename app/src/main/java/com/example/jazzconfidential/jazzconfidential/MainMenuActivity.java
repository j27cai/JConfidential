package com.example.jazzconfidential.jazzconfidential;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.graphics.BitmapFactory;
import android.graphics.Bitmap;
import android.net.Uri;

import com.example.jazzconfidential.jazzconfidential.Game.Game;
import com.facebook.share.widget.GameRequestDialog;
import com.facebook.CallbackManager;
import com.facebook.share.model.GameRequestContent;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.melnykov.fab.FloatingActionButton;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParsePush;
import com.parse.ParseQuery;

import java.io.InputStream;
import java.util.List;


public class MainMenuActivity extends Activity implements DialogInterface.OnClickListener {
    public static RecyclerView recyclerView;
    Button newGame, continueGame, options, post;

    boolean selectingGameMode = false;

    public static Game.GameType gameModeSelected;

    public static String playedWith;
    public static String playedWithUserName;

    public static GameAdapter gameAdapter = null;

    public final CallbackManager callbackManager = CallbackManager.Factory.create();

    public static Bitmap horseman = null;
    public static Bitmap archer = null;
    public static Bitmap swordsman = null;
    public static Bitmap pikeman = null;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.main_menu_fragment);

        recyclerView = (RecyclerView)findViewById(R.id.recyclerViewGames);
        gameAdapter = new GameAdapter(Database.allGames, this);
        recyclerView.setAdapter(gameAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        ListView listView = (ListView) findViewById(android.R.id.list);
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.attachToListView(listView);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            CharSequence colors[] = new CharSequence[] {"Swordsman劍士", "Bowman", "Pikeman", "Horseman"};

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Select a unit type");
            builder.setItems(colors, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    switch (which) {
                        case 0: {
                            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                            intent.setType("image/*");
                            startActivityForResult(intent, 0);
                        }
                        break;
                        case 1: {
                            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                            intent.setType("image/*");
                            startActivityForResult(intent, 1);
                        }
                        break;
                        case 2: {
                            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                            intent.setType("image/*");
                            startActivityForResult(intent, 2);
                        }
                        break;
                        case 3: {
                            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                            intent.setType("image/*");
                            startActivityForResult(intent, 3);
                        }
                        default:
                            break;
                    }
                }
            });
            builder.show();
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode,
                                    Intent imageReturnedIntent) {
        super.onActivityResult(requestCode, resultCode, imageReturnedIntent);


        if(resultCode == RESULT_OK){
            Uri selectedImage = imageReturnedIntent.getData();
            InputStream filePath = null;
            try {
                filePath = getContentResolver().openInputStream(selectedImage);
            } catch(Exception ex) {
            }
            switch(requestCode) {
                case 0:
                    swordsman = BitmapFactory.decodeStream(filePath);
                    break;
                case 1:
                    archer = BitmapFactory.decodeStream(filePath);
                    break;
                case 2:
                    pikeman = BitmapFactory.decodeStream(filePath);
                    break;
                case 3:
                    horseman = BitmapFactory.decodeStream(filePath);
                    break;
            }
        }

    }

    public void ButtonClicks(View v) {
        final MainMenuActivity a = this;
        switch (v.getId()) {
            case R.id.fab:
                CharSequence colors[] = new CharSequence[] {"Singleplayer", "Hotseat", "Online Multiplayer"};

                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("Select a mode");
                builder.setItems(colors, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case 0: {
                                GameActivity.gameToLoad = null;
                                gameModeSelected = Game.GameType.SinglePlayer;

                                playedWith = "";
                                playedWithUserName = "";

                                Intent intent = new Intent(getBaseContext(), GameActivity.class);
                                startActivity(intent);
                            }
                                break;
                            case 1: {
                                GameActivity.gameToLoad = null;
                                gameModeSelected = Game.GameType.HotSeat;

                                playedWith = "";
                                playedWithUserName = "";

                                Intent intent = new Intent(getBaseContext(), GameActivity.class);
                                startActivity(intent);
                            }
                                break;
                            case 2: {
                                GameActivity.gameToLoad = null;
                                gameModeSelected = Game.GameType.Multiplayer;

                                GameRequestDialog requestDialog;
                                requestDialog = new GameRequestDialog(a);

                                requestDialog.registerCallback(callbackManager, new FacebookCallback<GameRequestDialog.Result>() {
                                    public void onSuccess(GameRequestDialog.Result result) {
                                        String id = result.getRequestId();
                                        List<String> recipient = result.getRequestRecipients();

                                        String opponent = recipient.get(0);

                                        ParseQuery query = ParseQuery.getQuery("User");
                                        query.whereEqualTo("externalId",opponent);

                                        query.findInBackground(new FindCallback<ParseObject>() {
                                            public void done(List<ParseObject> objects, ParseException e) {
                                                if (e == null) {
                                                    if (!objects.isEmpty()) {
                                                        playedWith = objects.get(0).getString("localId");
                                                        playedWithUserName = objects.get(0).getString("userName");
                                                        Intent intent = new Intent(getBaseContext(), GameActivity.class);

                                                        /*ParsePush push = new ParsePush();
                                                        push.setChannel("p"+ playedWith);
                                                        push.setMessage("A game has been initiated!");
                                                        push.sendInBackground();*/

                                                        startActivity(intent);
                                                        return;
                                                    }
                                                } else {
                                                    e.printStackTrace();
                                                }
                                            }
                                        });
                                    }

                                    public void onCancel() {}

                                    public void onError(FacebookException error) {
                                    }
                                });

                                GameRequestContent content = new GameRequestContent.Builder()
                                        .setMessage("Come play this level with me")
                                        .build();
                                requestDialog.show(content);
                            }
                                break;
                            default:
                                break;
                        }
                    }
                });
                builder.show();

                //selectingGameMode = true;
                //this.setContentView(R.layout.game_creation);
                break;
            /*case R.id.NewGameButton:
                selectingGameMode = true;
                this.setContentView(R.layout.game_creation);
                break;*/
            case R.id.SinglePlayer : {
                GameActivity.gameToLoad = null;
                gameModeSelected = Game.GameType.SinglePlayer;

                final Context context = this;

                Intent intent = new Intent(context, GameActivity.class);
                startActivity(intent);
            }
                break;
            case R.id.HotSeat : {
                GameActivity.gameToLoad = null;
                gameModeSelected = Game.GameType.HotSeat;

                final Context context = this;

                Intent intent = new Intent(context, GameActivity.class);
                startActivity(intent);
            }
                break;
            case R.id.Online : {
                GameActivity.gameToLoad = null;
                gameModeSelected = Game.GameType.Multiplayer;

                final Context context = this;

                Intent intent = new Intent(context, GameActivity.class);
                startActivity(intent);
            }
                break;
            /*case R.id.button2:

                break;
            case R.id.button3:

                break;
            case R.id.button4:
                LoginFragment.shareDialog = new ShareDialog(this);
                // this part is optional
                LoginFragment.shareDialog.registerCallback(LoginFragment.mCallbackManager, new FacebookCallback<Sharer.Result>() {
                    @Override
                    public void onSuccess(Sharer.Result result) {

                    }

                    @Override
                    public void onCancel() {

                    }

                    @Override
                    public void onError(FacebookException e) {

                    }
                });

                if (ShareDialog.canShow(ShareLinkContent.class)) {
                    ShareLinkContent linkContent = new ShareLinkContent.Builder()
                            .setContentTitle("Jazz Confidential 4 Life")
                            .setContentDescription(
                                    "Just pwnd some nubz fool")
                            .setContentUrl(Uri.parse("http://google.com"))
                            .build();

                    LoginFragment.shareDialog.show(linkContent);
                }
                break;*/
            default:
                break;
        }
    }

   /* protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }*/

        @Override
    public void onBackPressed() {
        if (selectingGameMode) {
            selectingGameMode = false;

            this.setContentView(R.layout.main_menu_fragment);

            recyclerView = (RecyclerView)findViewById(R.id.recyclerViewGames);
            gameAdapter = new GameAdapter(Database.allGames, this);
            recyclerView.setAdapter(gameAdapter);
            recyclerView.setLayoutManager(new LinearLayoutManager(this));
        } else {
            finish();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        setContentView(R.layout.main_menu_fragment);

        recyclerView = (RecyclerView)findViewById(R.id.recyclerViewGames);
        gameAdapter = new GameAdapter(Database.allGames, this);
        recyclerView.setAdapter(gameAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        if (LoginFragment.UserId != "")
            Database.LoadGames(LoginFragment.UserId);
    }
}
