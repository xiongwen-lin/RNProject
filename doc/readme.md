# SDk 开发
## SDK 集成
1. 集成SDK  
    1.1. 引入插件  
    在 project 级别的 build.gradle 文件中添加依赖：

        buildscript {
            repositories {
                google()
                jcenter()
                maven { url 'https://dl.bintray.com/umsdk/release' }
            }
            dependencies {
                classpath 'com.android.tools.build:gradle:3.2.1'
            }
        }
    
    1.2. 引入SDK
    在主 module 的 build.gradle 文件中添加 SDK 依赖：  

        dependencies {
            implementation project(':nooie_sdk')
        }

    > nooie_sdk依赖nooie_network、nooie_common、nooie_device_sdk库  
    > Android SDK 要求最低系统版本为 API 21（Android 5.0）  
    > Android SDK 在 AndroidManifest.xml 中注册了可能使用到的权限，具体的权限和用途可参考SDK API权限配置说明

2. 初始化SDK

## SDK API
1. 权限配置说明  
   SDK需要权限：
   | 权限 |  用途 |
   | ---- | ----- |
   | INTERNET | 必须权限,访问网络|
   | WRITE_EXTERNAL_STORAGE | 必须权限，文件权限声明 |
   | READ_EXTERNAL_STORAGE | 必须权限，文件权限声明 |  
   | ACCESS_NETWORK_STATE | 必须权限 |  
   | ACCESS_WIFI_STATE | 必须权限 |

2. 初始化SDK  
    2.1 代码初始化  
    在 Application 的 onCreate() 方法中主线程调用 SensorsDataAPI.startWithConfigOptions() 初始化 SDK：  

        // 初始化配置
        SDKConfigOptions sdkConfigOptions = new SDKConfigOptions();
        sdkConfigOptions
                .setNetConfigurePlatform(NetConfigure.PLATFORM_VICTURE)//设置app对应的网络平台类型，必须设置
                .enableHttpLog(false)//开启网络请求打印
                .enableLog(false);//开启app日志打印
        SDKDataAPI.sharedInstance().startWithConfigOptions(mCtx, sdkConfigOptions);  

    注册全局数据监听器，监听全局数据的变化：  

        GlobalData.getInstance().addGlobalDataListener();

3. 用户模块接口  
   UserApi提供了用户模块相关的接口  
   3.1 注册接口  
   获取到验证码后，调用该接口，可以完成平台注册流程并登录
   
       /**
         *  用户注册接口
         * @param account
         * @param password md5加密
         * @param countryCode
         * @param verifyCode 验证码
         * @param reportUserRequest 用户上报信息
         * @return
         */
        Observable<BaseResponse<RegisterResult>> register(String account, String password, String countryCode, String verifyCode, ReportUserRequest reportUserRequest);
          
    3.2 登录接口  
    调用该接口，可以完成平台登录的流程  

        /**
         *  用户注册接口
         * @param account
         * @param password md5加密
         * @param countryCode
         * @param verifyCode 验证码
         * @param reportUserRequest 用户上报信息
         * @return
         */
        Observable<BaseResponse<RegisterResult>> register(String account, String password, String countryCode, String verifyCode, ReportUserRequest reportUserRequest);  

    3.3 自动登录接口
    在启动页调用该接口，可以完成登录信息初始化并校验token的有效期  

        /**
         * 用户自动登录接口
         * @param isRetryLogin 是否登陆异常时尝试重试登录，开启可能会等待过长
         * @return
         */
        Observable<Boolean> autoLogin(boolean isRetryLogin);  

    3.4 重置密码接口
    调用该接口，可以完成重置密码的流程  

        /**
         * 重置密码接口
         * @param account
         * @param password
         * @param code
         * @param countryCode
         * @return
         */
        Observable<BaseResponse> resetPassword(final String account, final String password, final String code, String countryCode);  

    3.5 注销接口  
    调用该接口，可以完成平台登录注销流程  

        /**
         * 注销登录
         * @return
         */
        Observable<BaseResponse> logout();  

    3.6 初始化全局数据接口  
    调用该接口，可以完成全局数据的初始化 

        /**
         * 初始化全局数据，必须在app启动页调用
         * @return
         */
        Observable<Boolean> getInitGlobalDataObservable(Context context)  

    3.7 修改密码  
    调用该接口，可以完成平台账号修改密码流程

        /**
         * 修改密码接口
         * @param account
         * @param oldPsd
         * @param newPsd
         * @return
        */
        Observable<BaseResponse> changePassword(String account, String oldPsd, String newPsd);  

# Victure App 工程
## 项目用到的lib module  
* lib-zxing 二维码扫描库
* lib-TimeAxis 时间轴控件
* lib-easypermissions 权限请求
* lib-switch-button 开关按钮控件
* lib-wheelview 滚轮控件
* lib-swip-to-load 下拉刷新布局
* lib-slider-back 页面滑动切换布局
* lib-convenientbanner banner控件
* lib-swipemenu 左滑布局控件
* lib-photoview 图片缩放控件
* lib-bdvideoplayer 本地视频播放器
* lib-pager-bottom-tab-strip tab标签控件
* lib-eventbus-activity-scope fragment管理库的事件库
* nooie_fragmentation_core fragment管理库
* nooie_guideview 引导图控件
* nooie_ucrop 图片裁剪
* nooie_cropiwa 图像区域选择控件
