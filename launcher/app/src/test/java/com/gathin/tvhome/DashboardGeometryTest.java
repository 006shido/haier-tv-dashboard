package com.gathin.tvhome;

import org.junit.Test;
import static org.junit.Assert.assertEquals;

public class DashboardGeometryTest {
    @Test public void websiteNavigationFollowsVisibleColumnAfterHorizontalScroll(){
        assertEquals(0,DashboardGeometry.visibleColumn(0,0,5));
        assertEquals(0,DashboardGeometry.visibleColumn(4,4,5));
        assertEquals(4,DashboardGeometry.visibleColumn(8,4,5));
        assertEquals(0,DashboardGeometry.visibleColumn(1,4,5));
    }
}
