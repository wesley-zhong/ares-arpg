git pull
cd ../
mvn clean
mvn package
rm -rf ./build/*
mkdir -p ./build
cd build
ln -s ../excel-json
cd ..
mkdir -p ./build/logs
mkdir -p ./build/gateway
mkdir -p ./build/game
mkdir -p ./build/router
mkdir -p ./build/team
mkdir -p ./build/login
echo  "cp gateway.jar"
cp  ./gateway/target/gateway.jar ./build/gateway
cp -r ./gateway/target/libs     ./build/gateway
echo  "cp game.jar"
cp ./game/target/game.jar ./build/game
cp -r ./game/target/libs   ./build/game
echo  "cp router.jar"
cp ./router/target/router.jar ./build/router
cp -r ./router/target/libs ./build/router
echo  "cp team.jar"
cp ./team/target/team.jar  ./build/team
cp -r ./team/target/libs   ./build/team
echo  "cp login.jar"
cp ./login/target/login.jar ./build/login
cp -r ./login/target/libs  ./build/login

