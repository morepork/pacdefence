#!/bin/bash -e

# Merge the example your eclipse configuration with the example config

shopt -s dotglob

MERGE_TOOL=meld
ECLIPSE_CONF_DIR=eclipse-conf/

for example_file in "$ECLIPSE_CONF_DIR"/*; do
    local_file=`basename "$example_file"`
    if [ ! -e "$local_file" ]; then
        echo "$local_file does not exist, creating."
        cp -R "$example_file" "$local_file"
    elif diff -rq "$local_file" "$example_file"; then
        echo "$local_file is the same as the example file."
    else
        $MERGE_TOOL "$local_file" "$example_file"
    fi
done

