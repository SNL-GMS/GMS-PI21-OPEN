#!/bin/bash
set -u


grafana_status(){
    response=$(curl -sI \
                    "${G_PROTO}://${G_USER}:${G_PASS}@${G_HOST}:${G_PORT}/api/datasources" \
                    -o/dev/null -w '%{http_code}\n')
    case $response in
        200)
            return 0
            ;;
        *)
            echo "{'message': '${response} error: unable to connect with Grafana at ${G_PROTO}://${G_HOST}:${G_PORT}'}"
            return 1
            ;;
    esac
}

register_me(){
    response=$(curl -s \
                    -X POST \
                    -H "content-type: application/json" \
                    "${G_PROTO}://${G_USER}:${G_PASS}@${G_HOST}:${G_PORT}/api/datasources" \
                    -d "{ \
      \"name\": \"${PROM_NAME}\", \
      \"type\": \"prometheus\", \
      \"access\": \"proxy\", \
      \"url\":\"http://${PROM_URL}\"\
    }" \
                    -o /dev/null -w '%{http_code}\n')
    case $response in
        200)
            echo "{'message': 'success: added prometheus data source'}"
            ;;
        409)
            # echo "{'message': '${response} conflict: attempted to add data source ${PROM_NAME} but this datasource may already exist'}"
            ;;
        *)
            echo "{'message': '${response} error: unable to add prometheus data source'}"
            ;;
    esac
}

# periodically check registration status with grafana
register_loop(){
    while true; do
        if grafana_status; then
            register_me
        fi
        sleep $G_SLEEP
    done
}


# wait for grafana to be available
echo "{'message': 'waiting for Grafana at ${G_PROTO}://${G_HOST}:${G_PORT} to be ready'}"
for i in {0..$G_WAIT}; do
    sleep 10
    if grafana_status; then
        echo "{'message': 'success: connected with Grafana at ${G_PROTO}://${G_HOST}:${G_PORT}'}"
        break
    elif (( i == $G_WAIT )); then
        echo "{'message': 'failed: Grafana at ${G_PROTO}://${G_HOST}:${G_PORT} cannot be connected to'}"
        exit 1
    fi
done

register_loop
