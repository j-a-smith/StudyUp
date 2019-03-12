#!/usr/bin/env bash

PARAM=$1

CURR_PARAM=$(sed -n "/^\s\s\s\sserver.*:6379;$/p" /etc/nginx/nginx.conf)
CURR_PARAM=${CURR_PARAM%":6379;"}
CURR_PARAM=${CURR_PARAM:11}

if [ "${PARAM}" == "${CURR_PARAM}" ]; then
	echo "WARNING: ${PARAM} is already being used. Hotswap not completed."
else
	sed -i'' "s/^\s\s\s\sserver.*:6379;$/    server ${PARAM}:6379;/" /etc/nginx/nginx.conf
	/usr/sbin/nginx -s reload
fi
