package cn.edu.zjut.quicknote;

import android.database.sqlite.SQLiteDatabase;

import org.junit.Test;
import org.litepal.tablemanager.Connector;

public class TestLitepal {
    @Test
    public void testCreateTable() {
        SQLiteDatabase database = Connector.getDatabase();
    }
}
