package com.example.jazzconfidential.jazzconfidential;

import android.support.annotation.UiThread;
import android.util.Base64;

import com.example.jazzconfidential.jazzconfidential.Game.Game;
import com.example.jazzconfidential.jazzconfidential.Game.Unit;
import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Created by Derek on 2015-06-14.
 */
public class Database {

    public static List<Game> allGames = new ArrayList<>();
    public static Game singleLoadedGame = null;
    public static List<Unit> allUnits = new ArrayList<>();


    public static void DeleteGame(final Game game){
        try{
            ParseQuery query = ParseQuery.getQuery("Game");
            query.whereEqualTo("gameId",game.gameId);
            query.findInBackground(new FindCallback<ParseObject>() {
                public void done(List<ParseObject> objects, ParseException e) {
                    if (e == null) {
                        if (!objects.isEmpty()) {
                            objects.get(0).put("data", GameToString(game));
                            try {
                                objects.get(0).delete();

                                LoadGames(LoginFragment.UserId);
                            } catch (ParseException e1) {
                                e1.printStackTrace();
                            }
                            return;
                        }
                        System.out.println("We're supposed to be updating a game, but we couldn't find it");
                    } else {
                        e.printStackTrace();
                    }
                }
            });

        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private static String GameToString(Game game){
        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            ObjectOutputStream out = new ObjectOutputStream(bos);
            out.writeObject(game);
            out.close();
            byte[] data2 = bos.toByteArray();
            return Base64.encodeToString(data2, Base64.DEFAULT);
        }catch(Exception e){
            e.printStackTrace();
        }
        return "";
    }

    private static void SaveNewGame(Game game){
        try {
            game.gameId = UUID.randomUUID().toString();
            String result = GameToString(game);
            ParseObject testObject = new ParseObject("Game");
            testObject.put("gameId", game.gameId);
            testObject.put("data", result);
            testObject.put("createdBy", LoginFragment.UserId);
            testObject.put("playedWith", game.playedWith);
            testObject.saveInBackground();
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }

    }

    private static void UpdateExistingGame(final Game game){
        try{
            ParseQuery query = ParseQuery.getQuery("Game");
            query.whereEqualTo("gameId",game.gameId);
            query.findInBackground(new FindCallback<ParseObject>() {
                public void done(List<ParseObject> objects, ParseException e) {
                    if (e == null) {
                        if (!objects.isEmpty()){
                            objects.get(0).put("data",GameToString(game));
                            objects.get(0).saveInBackground();
                            return;
                        }
                        System.out.println("We're supposed to be updating a game, but we couldn't find it");
                    } else {
                        e.printStackTrace();
                    }
                }
            });

        }catch (Exception e){
            e.printStackTrace();
        }

    }

    public static void SaveGame (Game game){
        if (game.gameId == ""){
            SaveNewGame(game);
        }else{
            UpdateExistingGame(game);
        }
    }

    public static void LoadGame(String id){
        ParseQuery<ParseObject> query = ParseQuery.getQuery("Game");
        query.getInBackground(id, new GetCallback<ParseObject>() {
            public void done(ParseObject object, ParseException e) {
                if (e == null) {
                    try {
                        String result = object.getString("data");
                        byte[] data = Base64.decode(result, Base64.DEFAULT);
                        ByteArrayInputStream bis = new ByteArrayInputStream(data);
                        ObjectInputStream in = new ObjectInputStream(bis);
                        Game obj = (Game) in.readObject();
                        singleLoadedGame = obj;
                    } catch (Exception decodeException) {
                        decodeException.printStackTrace();
                    }
                } else {
                    e.printStackTrace();
                }
            }
        });
    }



    public static void LoadGames(String userId) {

        List<ParseQuery<ParseObject>> queryList = new ArrayList<>();
        queryList.add(new ParseQuery("Game").whereEqualTo("createdBy", userId));
        queryList.add(new ParseQuery("Game").whereEqualTo("playedWith", userId));

        ParseQuery mainQuery = ParseQuery.or(queryList);
        mainQuery.findInBackground(new FindCallback<ParseObject>() {
            public void done(List<ParseObject> list, ParseException e) {
                allGames.clear();
                for (ParseObject game : list) {
                    try {
                        String result = game.getString("data");
                        byte[] data = Base64.decode(result, Base64.DEFAULT);
                        ByteArrayInputStream bis = new ByteArrayInputStream(data);
                        ObjectInputStream in = new ObjectInputStream(bis);
                        Game obj = (Game) in.readObject();
                        allGames.add(obj);
                    } catch (Exception decodeException) {
                        decodeException.printStackTrace();
                    }
                }

                dataSetChanged();
            }
        });

    }

    @UiThread
    public static void dataSetChanged() {
        MainMenuActivity.gameAdapter.UpdateData();
    }

    public static void SaveUnit (Unit unit){
        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            ObjectOutputStream out = new ObjectOutputStream(bos);
            out.writeObject(unit);
            out.close();
            byte[] data2 = bos.toByteArray();
            String result = Base64.encodeToString(data2,Base64.DEFAULT);
            ParseObject testObject = new ParseObject("Unit");
            testObject.put("data", result);
            testObject.put("createdBy",LoginFragment.UserId);
            testObject.saveInBackground();
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
    }

    public static void LoadUnits(int userId) {

        List<ParseQuery<ParseObject>> queryList = new ArrayList<>();
        queryList.add(new ParseQuery("Unit").whereEqualTo("createdBy", userId));

        ParseQuery mainQuery = ParseQuery.or(queryList);
        mainQuery.findInBackground(new FindCallback<ParseObject>() {
            public void done(List<ParseObject> list, ParseException e) {
                for (ParseObject unit : list) {
                    try {
                        String result = unit.getString("data");
                        byte[] data = Base64.decode(result, Base64.DEFAULT);
                        ByteArrayInputStream bis = new ByteArrayInputStream(data);
                        ObjectInputStream in = new ObjectInputStream(bis);
                        Unit obj = (Unit) in.readObject();
                        allUnits.add(obj);
                    }catch(Exception decodeException) {
                        decodeException.printStackTrace();
                    }
                }
            }
        });

    }
}