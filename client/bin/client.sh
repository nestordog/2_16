#!/bin/sh

cd "`dirname \"$0\"`"/..

java \
-cp "lib/*" \
sun.tools.jconsole.JConsole \
-interval=60 \
localhost:1099
