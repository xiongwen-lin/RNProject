package com.afar.osaio.testrn.utils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.URL;
import java.net.URLConnection;

public class SendHttpRequest {
    /**
     * 向指定的URL发送POST方法的请求
     * @param url    发送请求的URL
     * @param json  请求参数，请求参数应该是 name1=value1&name2=value2 的形式
     * @return       远程资源的响应结果
     */
    public void sendPost(String url, JSONObject json) throws JSONException {
        String result = "";
        BufferedReader bufferedReader = null;
        PrintWriter out = null;
        String topicurlString = json.getString("topicurl");
        try {
            //1、2、读取并将url转变为URL类对象
            URL realUrl = new URL(url);

            //3、打开和URL之间的连接
            URLConnection connection = realUrl.openConnection();
            //4、设置通用的请求属性
            connection.setRequestProperty("accept", "*/*");
            //connection.setRequestProperty("Content-Type", "application/json; text/json; text/html; text/javascript; text/plain; charset=UTF-8");
            connection.setRequestProperty("connection", "Keep-Alive");
            connection.setRequestProperty("user-agent", "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1;SV1)");
            connection.setConnectTimeout(8000);

            // 发送POST请求必须设置如下两行
            connection.setDoInput(true);
            connection.setDoOutput(true);

            //5、建立实际的连接
            //connection.connect();
            //获取URLConnection对象对应的输出流
            out = new PrintWriter(connection.getOutputStream());
            //发送请求参数
            out.print(json);
            //flush输出流的缓冲
            out.flush();
            //

            //6、定义BufferedReader输入流来读取URL的响应内容
            bufferedReader = new BufferedReader(new InputStreamReader(connection.getInputStream(),"UTF-8"));
            String line;
            while(null != (line = bufferedReader.readLine())) {
                result += line;
            }
        } catch (Exception e) {
            result = "error";
            // TODO: handle exception
            e.printStackTrace();
        } finally {        //使用finally块来关闭输出流、输入流
            try {
                if (listener != null) {
                    listener.routerReturnInfo(result, topicurlString);
                }

                if(null != out) {
                    out.close();
                }
                if(null != bufferedReader) {
                    bufferedReader.close();
                }
            } catch (Exception e2) {
                // TODO: handle exception
                e2.printStackTrace();
            }
        }
    }

    public void sendPostOnline(String url, JSONObject json, String uid, String uuid, Long timestamp, String sign, String finalTopicurlString) throws JSONException {
        String result = "";
        BufferedReader bufferedReader = null;
        PrintWriter out = null;
        String topicurlString = finalTopicurlString;

        try {
            //1、2、读取并将url转变为URL类对象
            URL realUrl = new URL(url);

            //3、打开和URL之间的连接
            URLConnection connection = realUrl.openConnection();
            //4、设置通用的请求属性
            connection.setRequestProperty("accept", "*/*");
            //connection.setRequestProperty("Content-Type", "application/json; text/json; text/html; text/javascript; text/plain; charset=UTF-8");
            connection.setRequestProperty("connection", "Keep-Alive");
            connection.setRequestProperty("user-agent", "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1;SV1)");
            connection.setRequestProperty("uid", uid);
            connection.setRequestProperty("uuid", uuid);
            connection.setRequestProperty("timestamp", "" + timestamp);
            connection.setRequestProperty("sign", sign);
            connection.setConnectTimeout(8000);

            // 发送POST请求必须设置如下两行
            connection.setDoInput(true);
            connection.setDoOutput(true);

            //5、建立实际的连接
            //connection.connect();
            //获取URLConnection对象对应的输出流
            out = new PrintWriter(connection.getOutputStream());
            //发送请求参数
            out.print(json);
            //flush输出流的缓冲
            out.flush();
            //

            //6、定义BufferedReader输入流来读取URL的响应内容
            bufferedReader = new BufferedReader(new InputStreamReader(connection.getInputStream(),"UTF-8"));
            String line;
            while(null != (line = bufferedReader.readLine())) {
                result += line;
            }
        } catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();
        } finally {        //使用finally块来关闭输出流、输入流
            try {
                if (listener != null) {
                    listener.routerReturnInfo(result, topicurlString);
                }

                if(null != out) {
                    out.close();
                }
                if(null != bufferedReader) {
                    bufferedReader.close();
                }
            } catch (Exception e2) {
                // TODO: handle exception
                e2.printStackTrace();
            }
        }
    }

    private static getRouterReturnInfo listener;
    public void setRouterReturnInfoListener(getRouterReturnInfo onListener) {
        this.listener = onListener;
    }

    public interface getRouterReturnInfo {
        void routerReturnInfo(String info, String topicurlString) throws JSONException;
    }
}
