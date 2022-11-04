#!/bin/bash

dir=../../../main/resources/static
cd $dir || (echo "Failed to find: $dir"; exit)
#python3 -m http.server 8000 &
npx http-server --server &
echo "Opening"
/Applications/Google\ Chrome.app/Contents/MacOS/Google\ Chrome --remote-debugging-port=9222
#open -a "Google Chrome" http://localhost:8081/jpa-csv/admin/?rbase=http%3A%2F%2Flocalhost%3A8080%2Fapi%2Fv1%2Fadmin%2Fcsv
#open -a "Google Chrome" http://localhost:3000/main/resources/static/jpa-csv/admin/?rbase=http%3A%2F%2Flocalhost%3A8080%2Fapi%2Fv1%2Fadmin%2Fcsv

wait

echo http://localhost:8081/jpa-csv/admin/?rbase=http%3A%2F%2Flocalhost%3A8080%2Fapi%2Fv1%2Fadmin%2Fcsv

trap "trap - SIGTERM && kill -- -$$" SIGINT SIGTERM EXIT