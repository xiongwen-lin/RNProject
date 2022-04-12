package com.afar.osaio.smart.routerlocal;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

/**
 * 单例模式
 */
public class RouterDao {

    //把数据库创建出来
    private RouterDbHelper routerDbHelper;
    // 表名
    private String table_router_num = "router_num";

    //单例模式
    //不能让每一个类都能new一个  那样就不是同一个对象了 所以首先构造函数要私有化    以上下文作为参数
    private RouterDao(Context context) {
        //由于数据库只需要调用一次，所以在单例中建出来
        routerDbHelper = new RouterDbHelper(context, "router_num.db", null, 1);
    }

    //要调用就要有一个静态的变量为私有的
    private static RouterDao instance;

    //既然BlackDao类是私有的  那么别的类就不能够调用    那么就要提供一个public static（公共的  共享的）的方法
    //方法名为getInstance 参数为上下文    返回值类型为BlackDao
    //要加上一个synchronized（同步的）
    //如果同时有好多线程 同时去调用getInstance()方法  就可能会出现一些创建（new）多个RouterDao的现象  所以要加上synchronized
    public static synchronized RouterDao getInstance(Context ctx) {
        //就可以判断  如果为空 就创建一个， 如果不为空就还用原来的  这样整个应用程序中就只能获的一个实例
        if (instance == null) {
            instance = new RouterDao(ctx);

        }
        return instance;
    }

    //常用方法  增删改查

    /**
     * 添加路由器至数据库
     *
     * @param name 路由器名称
     * @param mac  唯一标识，也可以是设备的类型
     */
    public void addRouter(String name, String mac) {

        //获得一个可写的数据库的一个引用
        SQLiteDatabase db = routerDbHelper.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put("name", name); // KEY 是列名，vlaue 是该列的值
        values.put("mac", mac);// KEY 是列名，vlaue 是该列的值
        values.put("isbind", "0");// KEY 是列名，vlaue 是该列的值 起初添加的路由设备都是为绑定的

        // 参数一：表名，
        // 参数二：只要能保存 values中是有内容的，第二个参数可以忽略
        // 参数三，是插入的内容
        db.insert(table_router_num, null, values);
        db.close();

    }

    /**
     * 删除路由器
     *
     * @param mac
     */
    public void deleteRouter(String mac) {
        SQLiteDatabase db = routerDbHelper.getWritableDatabase();
        //表名  删除的条件
        db.delete(table_router_num, "mac = ?", new String[]{mac});
        db.close();
    }

    /**
     * 修改路由器名称
     *
     * @param name
     * @param mac
     */
    public void updateRouter(String name, String mac) {
        SQLiteDatabase db = routerDbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("name", name);

        db.update(table_router_num, values, " mac = ?", new String[]{mac});
        db.close();
    }

    /**
     * 修改路由器绑定状态
     *
     * @param isbind
     * @param mac
     */
    public void updateRouterBind(String isbind, String mac) {
        SQLiteDatabase db = routerDbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("isbind", isbind);

        db.update(table_router_num, values, " mac = ?", new String[]{mac});
        db.close();
    }

    /**
     * 查找路由器
     */
    public RouterInfo findRouter(String mac) {
        RouterInfo routerInfo = new RouterInfo("", "", "");
        SQLiteDatabase db = routerDbHelper.getReadableDatabase();
        Cursor cursor = db.query(table_router_num, new String[]{"*"}, "mac = ?",
                new String[]{String.valueOf(mac)}, null, null, null, null);
        if (!cursor.moveToNext()) {
            return routerInfo;
        }
        if (cursor != null) {
            cursor.moveToFirst();
            routerInfo.setRouterName(cursor.getString(cursor.getColumnIndex("name")));
            routerInfo.setIsbind(cursor.getString(cursor.getColumnIndex("isbind")));
        }
        cursor.close();
        return routerInfo;
    }

    /**
     * 查询所有路由器
     */
    public List<RouterInfo> findAllRouter() {
        List<RouterInfo> routerLists = new ArrayList<>();
        SQLiteDatabase db = routerDbHelper.getReadableDatabase();
        Cursor cursor = db.query(table_router_num, new String[]{"*"}, null,
                null, null, null, null, null);
        while (cursor.moveToNext()) {
            routerLists.add(new RouterInfo(cursor.getString(cursor.getColumnIndex("name")), cursor.getString(cursor.getColumnIndex("mac")), cursor.getString(cursor.getColumnIndex("isbind"))));
        }

        cursor.close();
        return routerLists;
    }

}
