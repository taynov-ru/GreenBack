#!/bin/bash

# Проверка аргументов
if [ "$#" -ne 2 ]; then
    echo "Usage: $0 {enable|disable} {test|prod}"

    exit 1
fi

ACTION=$1
ENVIRONMENT=$2


if [ "$ACTION" == "enable" ]; then
  kubectl patch ingress green-back-app -n green-back-"$ENVIRONMENT" --type='json' -p='[{"op": "replace", "path": "/spec/rules/0/http/paths/0/backend/service/name", "value":"forward-to-me-service"}]'
elif [ "$ACTION" == "disable" ]; then
  kubectl patch ingress green-back-app -n green-back-"$ENVIRONMENT" --type='json' -p='[{"op": "replace", "path": "/spec/rules/0/http/paths/0/backend/service/name", "value":"green-back-app"}]'
else
    echo "Usage: $0 {enable|disable} {test|prod}"
    exit 1
fi

# chmode +x ws_redirect.sh
#./ws_redirect.sh disable test
#./ws_redirect.sh enable test
