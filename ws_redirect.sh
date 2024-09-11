#!/bin/bash

# Функция для включения перенаправления
enable_redirect() {
    # Проверка, существует ли правило
    sudo iptables -t nat -C PREROUTING -p tcp -d "$OLD_IP" --dport "$OLD_PORT" -j DNAT --to-destination "$NEW_IP":"$NEW_PORT" 2>/dev/null
    if [ $? -ne 0 ]; then
        sudo iptables -t nat -A PREROUTING -p tcp -d "$OLD_IP" --dport "$OLD_PORT" -j DNAT --to-destination "$NEW_IP":"$NEW_PORT"
        sudo iptables -t nat -A POSTROUTING -j MASQUERADE
        echo "Перенаправление включено с $OLD_IP:$OLD_PORT на $NEW_IP:$NEW_PORT"
    else
        echo "Правило уже существует"
    fi
}

# Функция для отключения перенаправления
disable_redirect() {
    # Проверка, существует ли правило
    sudo iptables -t nat -C PREROUTING -p tcp -d "$OLD_IP" --dport "$OLD_PORT" -j DNAT --to-destination "$NEW_IP":"$NEW_PORT" 2>/dev/null
    if [ $? -eq 0 ]; then
        sudo iptables -t nat -D PREROUTING -p tcp -d "$OLD_IP" --dport "$OLD_PORT" -j DNAT --to-destination "$NEW_IP":"$NEW_PORT"
        sudo iptables -t nat -D POSTROUTING -j MASQUERADE
        echo "Перенаправление отключено с $OLD_IP:$OLD_PORT на $NEW_IP:$NEW_PORT"
    else
        echo "Правило не найдено"
    fi
}

# Проверка аргументов
if [ "$#" -ne 5 ]; then
    echo "Usage: $0 {enable|disable} <OLD_IP> <OLD_PORT> <NEW_IP> <NEW_PORT>"
    exit 1
fi

ACTION=$1
OLD_IP=$2
OLD_PORT=$3
NEW_IP=$4
NEW_PORT=$5

if [ "$ACTION" == "enable" ]; then
    enable_redirect
elif [ "$ACTION" == "disable" ]; then
    disable_redirect
else
    echo "Usage: $0 {enable|disable} <OLD_IP> <OLD_PORT> <NEW_IP> <NEW_PORT>"
    exit 1
fi
