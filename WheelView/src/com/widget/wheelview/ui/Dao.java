package com.widget.wheelview.ui;

import com.daohelper.db.apis.IArea;
import com.daohelper.db.entry.Area;
import com.daohelper.db.impls.AreaDao;
import com.daohelper.factories.DaoFactory;

import android.content.Context;

public class Dao {

    private static final String ARAEA_DBNAME = "area.db";
    private static AreaDao sAreaDao;

    public static IArea getAreaDao(Context context) {
        if (sAreaDao == null) {
            sAreaDao = new AreaDao(DaoFactory.getDbFileHelper(context, ARAEA_DBNAME), Area.TAB_NAME);
        }
        return sAreaDao;
    }
}
