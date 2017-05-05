#!/usr/bin/env bash

set -e

PROJECT=$1
TARGET=$2
VERSION=$3

if [[ (${TARGET} == "local") ]]; then
  HOST=ifs-local
  ROUTE_DOMAIN=apps.$HOST
elif [[ ${TARGET} == "production" ]]; then
  HOST=apply-for-innovation-funding.service.gov.uk
  ROUTE_DOMAIN=$HOST
  PROJECT="production"
else
  HOST=prod.ifs-test-clusters.com
  ROUTE_DOMAIN=apps.$HOST
fi

if [[ ${TARGET} == "demo" ]]; then PROJECT="demo"; fi
if [[ ${TARGET} == "uat" ]]; then PROJECT="uat"; fi
if [[ ${TARGET} == "sysint" ]]; then PROJECT="sysint"; fi

REGISTRY=docker-registry-default.apps.prod.ifs-test-clusters.com
INTERNAL_REGISTRY=172.30.80.28:5000

if [ -z "$bamboo_openshift_svc_account_token" ]; then  SVC_ACCOUNT_TOKEN=$(oc whoami -t); else SVC_ACCOUNT_TOKEN=${bamboo_openshift_svc_account_token}; fi

SVC_ACCOUNT_CLAUSE="--namespace=${PROJECT} --token=${SVC_ACCOUNT_TOKEN} --server=https://console.prod.ifs-test-clusters.com:443 --insecure-skip-tls-verify=true"
REGISTRY_TOKEN=${SVC_ACCOUNT_TOKEN};

function upgradeServices {
    # data-service
    oc apply -f os-files-tmp/31-data-service.yml ${SVC_ACCOUNT_CLAUSE}
    sleep 90
    oc rollout status dc/data-service --request-timeout='5m' ${SVC_ACCOUNT_CLAUSE} || true

    # services
    oc apply -f os-files-tmp/4-application-service.yml ${SVC_ACCOUNT_CLAUSE}
    oc apply -f os-files-tmp/41-assessment-svc.yml ${SVC_ACCOUNT_CLAUSE}
    oc apply -f os-files-tmp/42-competition-mgt-svc.yml ${SVC_ACCOUNT_CLAUSE}
    oc apply -f os-files-tmp/43-project-setup-mgt-svc.yml ${SVC_ACCOUNT_CLAUSE}
    oc apply -f os-files-tmp/44-project-setup-svc.yml ${SVC_ACCOUNT_CLAUSE}

    # shib & idp
    if [[ ${TARGET} == "production" || ${TARGET} == "demo" || ${TARGET} == "uat" || ${TARGET} == "sysint" ]]
    then
        oc apply ${SVC_ACCOUNT_CLAUSE} -f os-files-tmp/shib/named-envs/56-${TARGET}-idp.yml
    else
        oc apply ${SVC_ACCOUNT_CLAUSE} -f os-files-tmp/shib/56-idp.yml
    fi

    oc apply ${SVC_ACCOUNT_CLAUSE} -f os-files-tmp/shib/5-shib.yml

    watchStatus
}

function forceReload {
    oc rollout latest dc/data-service ${SVC_ACCOUNT_CLAUSE}
    sleep 90

    oc rollout latest dc/application-svc ${SVC_ACCOUNT_CLAUSE}
    oc rollout latest dc/assessment-svc ${SVC_ACCOUNT_CLAUSE}
    oc rollout latest dc/competition-mgt-svc ${SVC_ACCOUNT_CLAUSE}
    oc rollout latest dc/project-setup-mgt-svc ${SVC_ACCOUNT_CLAUSE}
    oc rollout latest dc/project-setup-svc ${SVC_ACCOUNT_CLAUSE}
    oc rollout latest dc/idp ${SVC_ACCOUNT_CLAUSE}
    oc rollout latest dc/shib ${SVC_ACCOUNT_CLAUSE}

    watchStatus
}

function watchStatus {
    sleep 90
    oc rollout status dc/application-svc --request-timeout='5m' ${SVC_ACCOUNT_CLAUSE} || true
    oc rollout status dc/assessment-svc --request-timeout='5m' ${SVC_ACCOUNT_CLAUSE} || true
    oc rollout status dc/competition-mgt-svc --request-timeout='5m' ${SVC_ACCOUNT_CLAUSE} || true
    oc rollout status dc/project-setup-mgt-svc --request-timeout='5m' ${SVC_ACCOUNT_CLAUSE} || true
    oc rollout status dc/project-setup-svc --request-timeout='5m' ${SVC_ACCOUNT_CLAUSE} || true
    oc rollout status dc/idp --request-timeout='5m' ${SVC_ACCOUNT_CLAUSE} || true
    oc rollout status dc/shib --request-timeout='5m' ${SVC_ACCOUNT_CLAUSE} || true
}

. $(dirname $0)/deploy-functions.sh

# Entry point
cleanUp
cloneConfig
tailorAppInstance

useContainerRegistry
pushApplicationImages
upgradeServices

if [[ ${bamboo_openshift_force_reload} == "true" ]]
then
    forceReload
fi

if [[ ${TARGET} == "production" || ${TARGET} == "uat" ]]
then
    # We only scale up data-service once data-service started up and performed the Flyway migrations on one thread
    scaleDataService
fi
