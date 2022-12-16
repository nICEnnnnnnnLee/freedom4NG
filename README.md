<h1 align="center">  
    <strong>
        Freedom
    </strong>
</h1>
<p align="center">
    端到端数据流量伪装加密研究
  <br/>
    <strong>仅供学习研究使用，请勿用于非法用途</strong>
</p>


## :star:相关Repo
| 项目名称  | 简介 | 
| ------------- | ------------- |   
| [freedomGo](https://github.com/nICEnnnnnnnLee/freedomGo)  |  Go实现，包含local端、remote端(支持伪装Websocket、gRPC)  | 
| [freedomRust](https://github.com/nICEnnnnnnnLee/freedomRust)  |  Rust实现，包含local端、remote端(支持伪装Websocket)  | 
| [freedom4py](https://github.com/nICEnnnnnnnLee/freedom4py)  |  python3实现，包含local端、remote端(支持伪装Websocket)  | 
| [freedom4j](https://github.com/nICEnnnnnnnLee/freedom4j)  |  java实现，包含local端、remote端(支持伪装Websocket)  | 
| [freedom4NG](https://github.com/nICEnnnnnnnLee/freedom4NG)  | Android java实现，仅包含local端；单独使用可作为DNS、Host修改器(支持伪装Websocket、gRPC) | 
 




## :star:一句话说明  
将Android的TCP流量伪装成指向远程端的HTTP(S) WebSocket流量。

## :star:简介  
+ 在配置正确的情况下，python3、java、Android版本的local端和remote端可以配合使用。  
+ 支持国内/外IP识别，国内IP可以选择直连。  
+ 支持修改Host。  
+ 支持修改DNS服务器。  
+ 支持DNS over HTTPS(生效时将取代默认DNS)。

## :star:缺陷  
+ 仅支持代理TCP，不支持UDP
+ 仅支持IPv4，不支持IPv6

## :star:如何下载  
[Release](https://github.com/nICEnnnnnnnLee/freedom4NG/releases)  

