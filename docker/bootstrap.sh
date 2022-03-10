#!/usr/bin/env sh

export SCRIPT_PATH="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
export STORAGE_PATH="${SCRIPT_PATH}/db" # Default value
REBUILD=''

function parseArguments() {
    while [[ $# -gt 0 ]]; do
        local option="$1"
        case $option in
            -s=*|--storage_path=*)
            STORAGE_PATH="${option#*=}"; shift ;;
            -r|--rebuild)
            REBUILD='true'; shift ;;
            *)
            2>&1 echo "Unkown option: $option"; exit 1
        esac
    done
}

function tryCreateStoragePath() {
    local storagePath="$1"
    if [[ ! -d "$storagePath" ]];then
        echo "Creating storage path '$storagePath'..."
        mkdir -p "$storagePath"
        if [[ $? -ne 0 ]]; then
            >&2 echo "Failed to create storage path '$storagePath'"
            return 1;
        fi
    fi
    return 0
}

function rebuildJavaProject() {
    local scriptPath="$1"
    local javaProjectDir="$scriptPath/../"
    if [[ ! -d "$javaProjectDir" ]]; then
        >&2 echo "Local directory $javaProjectDir does not exist"
        return 1;
    fi

    pushd "$javaProjectDir" > /dev/null
    echo "Building maven project..."
    mvn clean install > mvn_logs.txt 2>&1

    if [[ "0" != "$?" ]]; then
        >&2 echo "Failed to build java project artifact"
        return 1
    fi

    local sourceArtifactLocation=("$javaProjectDir/target/fraud-detection-api.war")
    local targetArtifactLocation="$scriptPath/tomcat/fraud-detection-api.war"
    echo "Copying $sourceArtifactLocation to $targetArtifactLocation..."

    cp "${sourceArtifactLocation}" "${targetArtifactLocation}"
    if [[ "0" != "$?" ]]; then
        echo "Failed to copy java project artifact"
        return 1
    fi

    mvn clean >> mvn_logs.txt 2>&1
    popd > /dev/null

    return 0
}

function runCompose() {
    local scriptPath="$1"
    local rebuild="$2"
    local cmd="docker-compose up -d"

    if [[ ! -z "$rebuild" ]]; then
        cmd="$cmd --build"
        rebuild "$scriptPath"
    fi

    if [[ "0" != "$?" ]]; then
        return 1
    fi

    $cmd
    if [[ "0" != "$?" ]]; then
        >&2 echo "Failed to start composed project"
        return 1
    fi

    return 0
}

function deleteDockerImages() {
    local scriptPath="$1"
    local cmd="docker-compose kill"

    $cmd > docker_logs.txt
    if [[ "0" != "$?" ]]; then
        >&2 echo "Failed to kill the running containers"
        return 1
    fi

    cmd="docker-compose rm -f"
    $cmd >> docker_logs.txt
    if [[ "0" != "$?" ]]; then
        >&2 echo "Failed to delete the docker containers"
        return 1
    fi

    cmd="docker rmi docker_nginx docker_tomcat docker_neo4j"
    $cmd >> docker_logs.txt
    if [[ "0" != "$?" ]]; then
        >&2 echo "The docker images could not be deleted or didn't exist in the first place"
        return 1
    fi
}

function rebuild() {
    local scriptPath="$1"

    deleteDockerImages "$scriptPath"
    if [[ "0" != "$?" ]]; then
        echo "Error occured while deleting docker images!"
    fi

    rebuildJavaProject "$scriptPath"
    if [[ "0" != "$?" ]]; then
        return 1;
    fi

    return 0
}

# ------------------- Script execution starts here -----------------------------------------------------

parseArguments "$@"

tryCreateStoragePath "$STORAGE_PATH" || exit 1

if [[ ! -z "$REBUILD" ]]; then
    runCompose "$SCRIPT_PATH" "1" || exit 1
else
    runCompose "$SCRIPT_PATH"     || exit 1
fi

exit 0

