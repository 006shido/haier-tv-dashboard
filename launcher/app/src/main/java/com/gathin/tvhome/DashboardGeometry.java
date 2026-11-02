package com.gathin.tvhome;

/** Maps a horizontally scrolled website card to the app column directly above or below it. */
final class DashboardGeometry {
    static int visibleColumn(int index,int firstVisible,int columns){
        return Math.max(0,Math.min(columns-1,index-firstVisible));
    }
    private DashboardGeometry() { }
}
