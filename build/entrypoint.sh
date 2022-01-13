#!/bin/bash
 
# set apollo configuration
if [ -n "$APOLLO_IP" ]; then
    sed -i "s#^dev.meta=.*#dev.meta=http://${APOLLO_IP}#g" /app/config/apollo-env.properties
    sed -i "s#^fat.meta=.*#fat.meta=http://${APOLLO_IP}#g" /app/config/apollo-env.properties
    sed -i "s#^uat.meta=.*#uat.meta=http://${APOLLO_IP}#g" /app/config/apollo-env.properties
    sed -i "s#^pro.meta=.*#pro.meta=http://${APOLLO_IP}#g" /app/config/apollo-env.properties
    sed -i "s#^lpt.meta=.*#pro.meta=http://${APOLLO_IP}#g" /app/config/apollo-env.properties
fi

# setup app
java -jar /app/vesionbook-srs-task.jar
