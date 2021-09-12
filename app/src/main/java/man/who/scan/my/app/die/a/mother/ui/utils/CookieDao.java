package man.who.scan.my.app.die.a.mother.ui.utils;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import man.who.scan.my.app.die.a.mother.Global;
import man.who.scan.my.app.die.a.mother.ui.utils.model.Cookie;


public class CookieDao {

    private static CookieDao cookieDao;
    private String db_path;

    private CookieDao(String db_path) {
        this.db_path = db_path;
    }

    public static CookieDao getInstance() {
        if (cookieDao == null){
            File db_file = new File(Global.ROOT_DIR.getParentFile().getParentFile(), "app_webview/Cookies");
            String db_path = db_file.getAbsolutePath();
            cookieDao = new CookieDao(db_path);

        }
        return cookieDao;
    }

    public SQLiteDatabase openDataBase() {
//        SQLiteDatabase db = SQLiteDatabase.openDatabase("/data/user/0/man.who.scan.my.app.die.a.mother/app_webview/Cookies", null, SQLiteDatabase.OPEN_READWRITE);
        SQLiteDatabase db = SQLiteDatabase.openDatabase(db_path, null, SQLiteDatabase.OPEN_READWRITE);
        return db;
    }

    public List<Cookie> getAllMatchedCookies(String host, String path) {
//        System.out.printf("host: %s, path: %s\n", host, path);
        List<Cookie> cookies = new ArrayList<>();
//        String selectQuery = "SELECT  * FROM cookies order by creation_utc";
        String[] parts = host.split("\\.");
        int len = parts.length;
        String root = parts[len-2] + "."+ parts[len-1];
        String selectQuery = "SELECT  * FROM cookies where host_key like '%" + root + "'";
        SQLiteDatabase db = openDataBase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        if (cursor.moveToFirst()) {
            do {
                Cookie cookie = new Cookie();
                cookie.setHost_key(cursor.getString(cursor.getColumnIndex("host_key")));
                cookie.setName(cursor.getString(cursor.getColumnIndex("name")));
                cookie.setPath(cursor.getString(cursor.getColumnIndex("path")));
                cookie.setValue(cursor.getString(cursor.getColumnIndex("value")));
                if(cheackDomain(host, cookie.getHost_key()) && cheackPath(path, cookie.getPath())){
                    cookies.add(cookie);
                }
            } while (cursor.moveToNext());
        }
        db.close();
        return cookies;
    }

    private boolean cheackDomain(String requestDomain, String cookieDomain){
        if(cookieDomain.startsWith(".")){
            cookieDomain = cookieDomain.substring(1);
        }
        if(requestDomain.endsWith(cookieDomain)){
            return true;
        }
        return false;
    }

    private boolean cheackPath(String requestPath, String cookiePath){
        if("".equals(requestPath)){
            requestPath = "/";
        }
        if(requestPath.equals(cookiePath)){
            return true;
        }
        if(requestPath.startsWith(cookiePath)){
            if(cookiePath.endsWith("/")){
                ;return true;
            }else if(requestPath.substring(cookiePath.length()).startsWith("/")){
                return true;
            }
        }
        return false;
    }
    public List<Cookie> getAllEncrypedCookies() {
        List<Cookie> cookies = new ArrayList<>();
//        String selectQuery = "SELECT  * FROM cookies order by creation_utc";
        String selectQuery = "SELECT  * FROM cookies where is_httponly == 1";
        SQLiteDatabase db = openDataBase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        if (cursor.moveToFirst()) {
            do {
                Cookie cookie = new Cookie();
                cookie.setHost_key(cursor.getString(cursor.getColumnIndex("host_key")));
                cookie.setName(cursor.getString(cursor.getColumnIndex("name")));
                cookie.setPath(cursor.getString(cursor.getColumnIndex("path")));
                cookie.setValue(cursor.getString(cursor.getColumnIndex("value")));
                cookies.add(cookie);
            } while (cursor.moveToNext());
        }
        db.close();
        return cookies;
    }


    public int exposeAllCookies() {
        SQLiteDatabase db = openDataBase();

        ContentValues values = new ContentValues();
        values.put("is_httponly", 0);
        int affectedLines = 0;
        int af = db.update("cookies", values, "is_httponly = 1",
                null);
        affectedLines += af;

//        db.beginTransaction();
//        try {
//            db.execSQL("UPDATE cookies SET is_httponly = 0 WHERE is_httponly = 1");
//            db.setTransactionSuccessful();
//        } finally {
//            db.endTransaction();
//        }
        db.close();
        return affectedLines;
    }

    public int updateCookiesHttpValue(List<Cookie> cookies, int is_httponly) {
        SQLiteDatabase db = openDataBase();

        ContentValues values = new ContentValues();
        values.put("is_httponly", is_httponly);
        int affectedLines = 0;
        for (Cookie cookie : cookies) {
            int af = db.update("cookies", values, "host_key = ? and name = ? and path = ? ",
                    new String[]{cookie.getHost_key(), cookie.getName(), cookie.getPath()});
            affectedLines += af;
        }
        db.close();
        return affectedLines;
    }
}