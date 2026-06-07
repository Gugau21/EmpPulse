#!/usr/bin/env bash
find backend -name "*.java" -type f -exec java -jar backend/.tools/gjf.jar --replace {} \;