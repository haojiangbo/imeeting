
##  如何导入项目？
* 先通过git拉取代码  
git clone https://gitee.com/haojiangbo/venomous_sting.git 
* 导入idea或者eclise, 如何导入代码， [请自行百度 O(∩_∩)O~~]
## 如何运行项目
* **第一步** 找到proxy模块根目录下的config文件
```
#二级域名,端口,客户端Id(key),本地服务地址和端口号
www,8080,1587133230,127.0.0.1:80
mysql,8081,1587133232,127.0.0.1:3306
首先我说明一下 这几个数字的含义，
我称他为路由表
www 是要配置的二级域名
8080 是服务单监听的端口，
1587133230 是客户端ID,只有这里正确了clientId，客户端才能连接上
127.0.0.1:80 就是本地的web服务，必须为 host:port 的格式

详情请参考服务端源码解读文档
```
* **第二步** 配置服务端端口号
我已经在看云上写好了文档，
请点击下方连接查看文档，
详细配置会在此文档同步
https://www.kancloud.cn/book/haojiangbo/stinger/f5734dcae82ebc896d71bdaef6251418a52ddac7/preview/%E5%A6%82%E4%BD%95%E5%90%AF%E5%8A%A8.md
 