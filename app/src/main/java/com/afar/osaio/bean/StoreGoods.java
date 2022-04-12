package com.afar.osaio.bean;

/**
 * @DES: Store_亚马逊H5页 返回的参数值
 * 需要解析成对象
 * <p>
 * 优惠码           值：显示的优惠码
 * <p>
 * 分站代码        值：所点击的分站代码，如US / DE ……
 * <p>
 * 商品名称        值：所点击的商品名称
 * <p>
 * 商品ID            值：所点击的商品 ID
 * <p>
 * 优惠价            值：所点击的商品的优惠价
 * <p>
 * 原价               值：所点击的商品的原价
 * @Author: zhoudq
 * @Date：4/13/21 3:03 PM
 */
public class StoreGoods {
    /**
     * 标题
     */
    public String title;
    /**
     * 显示的优惠码
     */
    public String code;
    /**
     * 分站代码
     */
    public String station_code;
    /**
     * 商品名称
     */
    public String name;
    /**
     * 商品ID
     */
    public String id;
    /**
     * 优惠价
     */
    public String price;
    /**
     * 原价
     */
    public String original_price;

    public String amazon_url;

}
