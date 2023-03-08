#!/bin/bash
set -u

cleanup (){
    # de-register from grafana
    response=$(curl -s \
                    -X DELETE \
                    -H "content-type: application/json" \
                    "${G_PROTO}://${G_USER}:${G_PASS}@${G_HOST}:${G_PORT}/api/datasources/name/${PROM_NAME}" \
                    -o /dev/null  -w '%{http_code}\n')
    case $response in
        200)
            echo "{'message': 'success: deleted prometheus data source'}"
            ;;
        *)
            echo "{'message': '${response} error: unable to remove prometheus data source'}"
            ;;
    esac
    exit 0
}

cleanup
