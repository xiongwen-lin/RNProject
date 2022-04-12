#YRXIpc模块

## YRXIpc中间件服务

### 一、 模块
协议地址：**yrcx://yrxipc**

### 二、方法
#### 1、Ipc品类设备统一模型转换

**方法:** transformipcdevice

**请求方式**
异步：requestAsync

**参数:**   

| 字段      | 类型     | 说明                                         |
|---------|--------|--------------------------------------------|
| uid     | String | 用户uid                                      |
| account | String | 用户账号                                       |
| extra   | String | Ipc品类数据的json字符串，参考YRBindDeviceResult数据结构说明 |

* YRBindDeviceResult数据结构

| 字段              | 类型      | 说明                                                                                                    |
|-----------------|---------|-------------------------------------------------------------------------------------------------------|
| responseSuccess | Boolean | Ipc品类数据查询结果                                                                                           |
| result          | String  | Ipc品类设备信息列表数据json字符串，<br /> 关于设备信息数据结构参考平台设备信息<br />https://www.showdoc.com.cn/nooie/3881683640492628 |

**返回值:** 

| 字段       | 类型      | 说明                |
|----------|---------|-------------------|
| code     | Int     | 状态码，1000代表成功，其他异常 |
| errorMsg | String  | 错误信息              |
| data     | String | 首页设备数据列表json字符串 <br />  参考首页平台数据模型       |


#### 2、获取当前直连是否存在

**方法:** checkisnetspot

**请求方式**
异步：request

**参数:**   
无

**返回值:** 

| 字段       | 类型      | 说明                |
|----------|---------|-------------------|
| code     | Int     | 状态码，1000代表成功，其他异常 |
| errorMsg | String  | 错误信息              |
| data     | Boolean | 当前是否存在直连          |

#### 3、获取当前直连信息

**方法:** querynetspotinfo

**请求方式**
异步：request

**参数:**   
无

**返回值:** 

| 字段       | 类型      | 说明                |
|----------|---------|-------------------|
| code     | Int     | 状态码，1000代表成功，其他异常 |
| errorMsg | String  | 错误信息              |
| data     | String | 首页设备数据json字符串 <br />  参考首页平台数据模型       |

#### 4、自检当前直连状态

**方法:** refreshnetspotconnection

**请求方式**
异步：request

**参数:**   
无

**返回值:** 

| 字段       | 类型      | 说明                |
|----------|---------|-------------------|
| code     | Int     | 状态码，1000代表成功，其他异常 |
| errorMsg | String  | 错误信息              |
| data     | Boolean | 是否已自检      |

#### 5、断开当前直连

**方法:** stopnetspotconnection

**请求方式**
异步：requestAsync

**参数:**   
无

**返回值:** 

| 字段       | 类型      | 说明                |
|----------|---------|-------------------|
| code     | Int     | 状态码，1000代表成功，其他异常 |
| errorMsg | String  | 错误信息              |
| data     | Boolean | 是否已断开直连          |

#### 6、监听直连状态

**方法:** observenetspotconnectionstate

**请求方式**
异步：listening

**参数:**   
无

**返回值:** 

| 字段       | 类型      | 说明                |
|----------|---------|-------------------|
| code     | Int     | 状态码，1000代表成功，其他异常 |
| errorMsg | String  | 错误信息              |
| data     | String | 直连状态，"CONNECTED"：已连接，"DISCONNECTED"：断开连接，"NORMAL"：无直连存在         |

#### 7、打开设备配网入口

**方法:** openadddevice

**请求方式**
异步：request

**参数:**   
无

**返回值:** 

| 字段       | 类型      | 说明                |
|----------|---------|-------------------|
| code     | Int     | 状态码，1000代表成功，其他异常 |
| errorMsg | String  | 错误信息              |
| data     | Boolean | 无        |

#### 8、打开设备直播入口

**方法:** openlive

**请求方式**
异步：request

**参数:**   

| 字段   | 类型     | 说明   |
|------|--------|------|
| uuid | String | 设备id |

**返回值:** 

| 字段       | 类型      | 说明                |
|----------|---------|-------------------|
| code     | Int     | 状态码，1000代表成功，其他异常 |
| errorMsg | String  | 错误信息              |
| data     | Boolean | 无        |

#### 9、打开设备回放入口

**方法:** openplayback

**请求方式**
异步：request

**参数:**   

| 字段       | 类型      | 说明         |
|----------|---------|------------|
| uuid     | String  | 设备id       |
| seektime | Long    | 回放时间（单位：秒） |
| iscloud  | Boolean | 是否为云回放     |

**返回值:** 

| 字段       | 类型      | 说明                |
|----------|---------|-------------------|
| code     | Int     | 状态码，1000代表成功，其他异常 |
| errorMsg | String  | 错误信息              |
| data     | Boolean | 无        |

#### 10、打开设备侦测设置入口

**方法:** opensensitiviy

**请求方式**
异步：request

**参数:**   

| 字段   | 类型     | 说明   |
|------|--------|------|
| uuid | String | 设备id |

**返回值:** 

| 字段       | 类型      | 说明                |
|----------|---------|-------------------|
| code     | Int     | 状态码，1000代表成功，其他异常 |
| errorMsg | String  | 错误信息              |
| data     | Boolean | 无        |

#### 11、首页设备点击事件

**方法:** click

**请求方式**
异步：requestAsync

**参数:**   

| 字段          | 类型     | 说明                                                                                   |
|-------------|--------|--------------------------------------------------------------------------------------|
| uuid        | String | 设备id                                                                                 |
| linkType    | String | 设备连接模式，<br />"DIRECT_LINK_DEVICE"：直连，"BLE_NET_SPOT_DEVICE"：直连缓存连接，"P2P_DEVICE"：P2P连接 |
| model       | String | 设备型号                                                                                 |
| ssid        | String | 设备ssid                                                                               |
| bledeviceid | String | 设备蓝牙id                                                                               |

**返回值:** 

| 字段       | 类型      | 说明                |
|----------|---------|-------------------|
| code     | Int     | 状态码，1000代表成功，其他异常 |
| errorMsg | String  | 错误信息              |
| data     | String | 空：代表操作正常；<br />“remove_cancel”：代表移除设备取消，“remove_success”：移除设备成功，“remove_error”：移除设备失败          |

#### 12、首页设备开关事件

**方法:** switch

**请求方式**
异步：requestAsync

**参数:**   

| 字段       | 类型      | 说明                                                                                   |
|----------|---------|--------------------------------------------------------------------------------------|
| uuid     | String  | 设备id                                                                                 |
| linkType | String  | 设备连接模式，<br />"DIRECT_LINK_DEVICE"：直连，"BLE_NET_SPOT_DEVICE"：直连缓存连接，"P2P_DEVICE"：P2P连接 |
| state    | Boolean | 开关状态                                                                                 |
| ssid     | String  | 设备ssid，直连设备必须提供ssid                                                                  |

**返回值:** 

| 字段       | 类型      | 说明                |
|----------|---------|-------------------|
| code     | Int     | 状态码，1000代表成功，其他异常 |
| errorMsg | String  | 错误信息              |
| data     | Boolean | 是否成功          |

#### 13、发送Ipc设备命令

**方法:** sendcmd

**请求方式**
异步：requestAsync

**参数:**   

| 字段  | 类型     | 说明                         |
|-----|--------|----------------------------|
| cmd | String | Ipc设备命令参数字典字符串，参考Ipc请求命令定义 |

* 请求命令格式
  
| 字段         | 类型     | 说明                                    |
|------------|--------|---------------------------------------|
| uuid       | String | 设备id                                  |
| cmd_action | String | 命令标识，如storage_info：获取卡信息命令            |
| protocol   | String | 可选，命令协议：P2P，TCP_NET_SPOT，P2P_NET_SPOT |
| cmd_id     | String | 可选，命令id，单次请求和响应使用相同id                 |
| 其他         | 基本类型   | 根据不同命令附加不同参数，具体参考Ipc命令参数定义            |

**返回值:** 

| 字段       | 类型      | 说明                |
|----------|---------|-------------------|
| code     | Int     | 状态码，1000代表成功，其他异常 |
| errorMsg | String  | 错误信息              |
| data     | String | 命令响应字典json字符串，参考        |

* 响应命令格式
  
| 字段         | 类型     | 说明                                        |
|------------|--------|-------------------------------------------|
| uuid       | String | 设备id                                      |
| cmd_action | String | 命令标识，如storage_info：获取卡信息命令                |
| cmd_code   | String | 命令响应结果："success"：成功，"cache"：缓存，"error"：失败 |
| cmd_id     | String | 可选，命令id，单次请求和响应使用相同id                     |
| 其他         | 基本类型   | 根据不同命令附加不同参数，具体参考Ipc命令参数定义                |


