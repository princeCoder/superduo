package model;

import android.graphics.drawable.Drawable;

/**
 * Created by Prinzly Ngotoum on 10/13/15.
 */
public class Fixture {
    //Table data
    public  String league ;
    public  String date;
    public  String id;
    public  String time;
    public  String home;
    public  String away;
    public Drawable awayCrest;
    public  Drawable homeCrest;
    public  String homeGoals;
    public  String awayGoals;
    public  String matchId;
    public  String day;

    public Fixture(String league, String date, String id, String time, String home, String away, Drawable awayCrest, Drawable homeCrest, String homeGoals, String awayGoals, String matchId, String day) {
        this.league = league;
        this.date = date;
        this.id = id;
        this.time = time;
        this.home = home;
        this.away = away;
        this.awayCrest = awayCrest;
        this.homeCrest = homeCrest;
        this.homeGoals = homeGoals;
        this.awayGoals = awayGoals;
        this.matchId = matchId;
        this.day = day;
    }


    public Fixture() {
        this.league = null;
        this.date = null;
        this.id = null;
        this.time = null;
        this.home = null;
        this.away = null;
        this.awayCrest = null;
        this.homeCrest = null;
        this.homeGoals = null;
        this.awayGoals = null;
        this.matchId = null;
        this.day = null;
    }


    public String getLeague() {
        return league;
    }

    public void setLeague(String league) {
        this.league = league;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getHome() {
        return home;
    }

    public void setHome(String home) {
        this.home = home;
    }

    public String getAway() {
        return away;
    }

    public void setAway(String away) {
        this.away = away;
    }

    public Drawable getAwayCrest() {
        return awayCrest;
    }

    public void setAwayCrest(Drawable awayCrest) {
        this.awayCrest = awayCrest;
    }

    public Drawable getHomeCrest() {
        return homeCrest;
    }

    public void setHomeCrest(Drawable homeCrest) {
        this.homeCrest = homeCrest;
    }

    public String getHomeGoals() {
        return homeGoals;
    }

    public void setHomeGoals(String homeGoals) {
        this.homeGoals = homeGoals;
    }

    public String getAwayGoals() {
        return awayGoals;
    }

    public void setAwayGoals(String awayGoals) {
        this.awayGoals = awayGoals;
    }

    public String getMatchId() {
        return matchId;
    }

    public void setMatchId(String matchId) {
        this.matchId = matchId;
    }

    public String getDay() {
        return day;
    }

    public void setDay(String day) {
        this.day = day;
    }
}
