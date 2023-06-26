#bin

# Change to your own target host
TARGET_HOST=master
# Change to your own username
REMOTE_USER=$USER

bin=$(dirname "$0")/..
bin=$(cd "$bin">/dev/null || exit; pwd)
APP_HOME=$(cd "$bin"/..>/dev/null || exit; pwd)

echo "Deploy the project ..."
echo "Changing version ..."

SNAPSHOT_VERSION=$(head -n 1 "$APP_HOME/VERSION")
VERSION=${SNAPSHOT_VERSION//"-SNAPSHOT"/""}
echo "$VERSION" > "$APP_HOME"/VERSION
find "$APP_HOME" -name 'pom.xml' -exec sed -i "s/$SNAPSHOT_VERSION/$VERSION/" {} \;

mvn clean
mvn package

######################################################
# Deploy
######################################################

# script config
EXOTIC_AMAZON_VERSION=$(cat "$APP_HOME"/VERSION)

SOURCE="$APP_HOME/target/exotic-amazon-$EXOTIC_AMAZON_VERSION.jar"
if [ ! -e "$SOURCE" ]; then
  echo "$SOURCE does not exist"
  exit 1
fi

DESTINATION="$REMOTE_USER@$TARGET_HOST:~/wwwpub/pub/exotic/exotic-amazon/$EXOTIC_AMAZON_VERSION"

ssh "$REMOTE_USER@$TARGET_HOST" mkdir -p "$DESTINATION"

echo "rsync --update -raz --progress $SOURCE $DESTINATION"

if [ -e "$SOURCE" ]; then
  echo "rsync ..."
  rsync --update -raz --progress "$SOURCE" "$DESTINATION"
  rsync --update -raz --progress "$APP_HOME/bin" "$DESTINATION"
  scp
else
  echo "$SOURCE does not exist"
  exit 1
fi

echo "Finished at" "$(date)"

open http://platonic.fun/pub/exotic/
