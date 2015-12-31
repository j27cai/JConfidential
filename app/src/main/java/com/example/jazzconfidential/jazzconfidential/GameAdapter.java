package com.example.jazzconfidential.jazzconfidential;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.provider.ContactsContract;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.jazzconfidential.jazzconfidential.Game.Game;
import com.example.jazzconfidential.jazzconfidential.Stages.GameStage;
import com.parse.ParsePush;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by simonvilleneuve on 15-07-16.
 */

class CustomViewHolder extends RecyclerView.ViewHolder {
    public TextView cardbackground, opponent, whosTurn;
    public ImageView gameMode, gameStage;

    public CustomViewHolder(View view) {
        super(view);

        this.cardbackground = (TextView)view.findViewById(R.id.cardbackground);
        this.opponent = (TextView)view.findViewById(R.id.OpponentNameText);
        this.whosTurn = (TextView)view.findViewById(R.id.WhosTurnText);
        this.gameMode = (ImageView)view.findViewById(R.id.GameModeImage);
        this.gameStage = (ImageView)view.findViewById(R.id.GameStageImage);
    }
}

public class GameAdapter extends RecyclerView.Adapter<CustomViewHolder> {
    private List<Game> games = new ArrayList<>();
    private Context context = null;

    GameAdapter(List<Game> games, Context context) {
        this.games = games;
        this.context = context;
    }

    @Override
    public CustomViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.gameitem, parent, false);

        CustomViewHolder viewHolder = new CustomViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(CustomViewHolder holder, int position) {
        Game game = games.get(position);

        if (game.gameType == Game.GameType.Multiplayer) {
            if (LoginFragment.UserId.equals(game.createdBy)) {
                holder.whosTurn.setText(game.playedWithUserName);
            } else {
                holder.whosTurn.setText(game.createdByUserName);
            }
        }
        else {
            holder.whosTurn.setText(game.gameType.toString());
        }

        switch (game.gameType) {
            case SinglePlayer:
                holder.gameMode.setImageResource(R.mipmap.mouse);
                break;
            case HotSeat:
                holder.gameMode.setImageResource(R.mipmap.tabletop_players);
                break;
            case Multiplayer:
                holder.gameMode.setImageResource(R.mipmap.ages);
                break;
            default:
                break;
        }

        switch (game.stage) {
            case UnitSelection:
                holder.gameStage.setImageResource(R.mipmap.hand);
                break;
            case Movement:
                holder.gameStage.setImageResource(R.mipmap.boots);
                break;
            case Attack:
                holder.gameStage.setImageResource(R.mipmap.crossed_swords);
                break;
            case End:
                holder.gameStage.setImageResource(R.mipmap.flying_flag);
                break;
            default:
                break;
        }

        if (game.stage == Game.Stage.End) {
            holder.opponent.setText("End of game!");

            // Sign up for clicks
            holder.cardbackground.setOnClickListener(clickListener);
            holder.opponent.setOnClickListener(clickListener);
            holder.whosTurn.setOnClickListener(clickListener);
            holder.gameMode.setOnClickListener(clickListener);
            holder.gameStage.setOnClickListener(clickListener);
        } else if ((game.gameType == Game.GameType.SinglePlayer || game.gameType == Game.GameType.HotSeat) || (game.createdBy.equals(LoginFragment.UserId) && game.currentPlayer == 0) || (game.playedWith.equals(LoginFragment.UserId) && game.currentPlayer == 1)) {
            holder.opponent.setText("Your Turn!");

            // Sign up for clicks
            holder.cardbackground.setOnClickListener(clickListener);
            holder.opponent.setOnClickListener(clickListener);
            holder.whosTurn.setOnClickListener(clickListener);
            holder.gameMode.setOnClickListener(clickListener);
            holder.gameStage.setOnClickListener(clickListener);
        } else {
            holder.opponent.setText("Their Turn!");
        }

        holder.cardbackground.setOnLongClickListener(longClickListener);
        holder.opponent.setOnLongClickListener(longClickListener);
        holder.whosTurn.setOnLongClickListener(longClickListener);
        holder.gameMode.setOnLongClickListener(longClickListener);
        holder.gameStage.setOnLongClickListener(longClickListener);

        holder.cardbackground.setTag(holder);
        holder.opponent.setTag(holder);
        holder.whosTurn.setTag(holder);
        holder.gameMode.setTag(holder);
        holder.gameStage.setTag(holder);
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public int getItemCount() {
        return games.size();
    }

    public void UpdateData() {
        this.notifyDataSetChanged();
    }

    View.OnLongClickListener longClickListener = new View.OnLongClickListener() {
        @Override
        public boolean onLongClick(View view) {
            CustomViewHolder holder = (CustomViewHolder) view.getTag();
            final int position = holder.getPosition();

            new AlertDialog.Builder(context)
                    .setTitle("Quit Game")
                    .setMessage("Are you sure you want to quit this game?")
                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            for (int i = 0; i < Database.allGames.size(); i++) {
                                if (Database.allGames.get(i).gameId == games.get(position).gameId) {
                                    Game.GameType gameType = Database.allGames.get(i).gameType;
                                    Game.Stage gameStage = Database.allGames.get(i).stage;
                                    String string = (Database.allGames.get(i).createdBy.equals(LoginFragment.UserId) ? Database.allGames.get(i).playedWith : Database.allGames.get(i).createdBy);

                                    Database.DeleteGame(Database.allGames.get(i));
                                    Database.LoadGames(LoginFragment.UserId);
                                    UpdateData();

                                    if (gameType == Game.GameType.Multiplayer && gameStage != Game.Stage.End){
                                        ParsePush push = new ParsePush();
                                        push.setChannel("p" + string);
                                        push.setMessage("Your opponent has surrendered!");
                                        push.sendInBackground();
                                    }

                                    break;
                                }
                            }
                        }
                    })
                    .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            // do nothing
                        }
                    })
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .show();

            return false;
        }
    };

    View.OnClickListener clickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            CustomViewHolder holder = (CustomViewHolder) view.getTag();
            int position = holder.getPosition();
            GameActivity.gameToLoad = games.get(position);

            Intent intent = new Intent(context, GameActivity.class);
            context.startActivity(intent);
        }
    };
}
