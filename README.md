### music
# 管理员账号密码
账号: **admin**  
密码: **admin123**
# 数据库信息
数据库名: **music_db**  
数据库表: 见项目根目录下 **Datebase.sql** 文件
# 启动后端流程
需安装**jdk 21**,**maven 3.9**  
以及**mysql8**和**redis8**  
更多信息见 **src/main/resources/application.yml** 文件
```bat
mvn install
java -jar .\target\music-0.0.1-SNAPSHOT.jar
```
# 启动前端流程
需安装**nodejs 22**,再安装**pnpm**
```bat
npm install -g pnpm
```
启动管理员前端(**端口:5175**)
```bat
cd frontend-admin
pnpm install
pnpm dev
```
启动创作者前端(**端口:5174**)
```bat
cd frontend-actor
pnpm install
pnpm dev
```
启动用户前端(**端口:5173**)
```bat
cd frontend-user
pnpm install
pnpm dev
```