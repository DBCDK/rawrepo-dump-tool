#!/usr/bin/env bash
#
# Copyright Dansk Bibliotekscenter a/s. Licensed under GPLv3
# See license text in LICENSE.md
#

rrdump_home="$HOME/.rrdump"
rrdump_archive="${rrdump_home}/archive"
rrdump_bin="${rrdump_home}/bin"
rrdump_url=http://mavenrepo.dbc.dk/content/repositories/releases/dk/dbc/rawrepo-dump-tool

function get_current_version {
    local current_version
    if [ -f ${rrdump_home}/version ]; then
        current_version=`cat ${rrdump_home}/version`
    else
        current_version=0
    fi
    echo ${current_version}
}

function get_latest_version {
    local latest_version=`
        curl -s "${rrdump_url}/maven-metadata.xml" | \
        grep "<release>.*</release>" | \
        sed -e "s#\(.*\)\(<release>\)\(.*\)\(</release>\)\(.*\)#\3#g"`
    echo ${latest_version}
}

function install {
    if [ -z $(which curl) ]; then
        echo "curl not found."
        echo ""
        echo "======================================================================================================"
        echo " Please install curl on your system using your favourite package manager."
        echo ""
        echo " Restart after installing curl."
        echo "======================================================================================================"
        echo ""
        exit 1
    fi

    if [ -z $(which unzip) ]; then
        echo "unzip not found."
        echo ""
        echo "======================================================================================================"
        echo " Please install unzip on your system using your favourite package manager."
        echo ""
        echo " Restart after installing unzip."
        echo "======================================================================================================"
        echo ""
        exit 1
    fi

    if [ -z $(which java) ]; then
        echo "java not found."
        echo ""
        echo "======================================================================================================"
        echo " Please install java on your system using your favourite package manager."
        echo ""
        echo " Restart after installing java."
        echo "======================================================================================================"
        echo ""
        exit 1
    fi

    mkdir -pv "$rrdump_archive"
    mkdir -pv "$rrdump_bin"

    local current_version=`get_current_version`
    local latest_version=`get_latest_version`

    if [ "$current_version" != "$latest_version" ]; then
        echo "Installing version ${latest_version}"
        curl -sL ${rrdump_url}/${latest_version}/rawrepo-dump-tool-${latest_version}.jar -o ${rrdump_archive}/rrdump-${latest_version}.jar
        if [ $? -eq 0 ]; then
            [ -e ${rrdump_archive}/rrdump-current.jar ] && rm ${rrdump_archive}/rrdump-current.jar
            ln -s ${rrdump_archive}/rrdump-${latest_version}.jar ${rrdump_archive}/rrdump-current.jar
            unzip -o ${rrdump_archive}/rrdump-current.jar rrdump.sh -d ${rrdump_bin}
            chmod a+x ${rrdump_bin}/rrdump.sh
            echo ${latest_version} > ${rrdump_home}/version
        fi

        if [ ! -f ~/.bash_aliases ]; then
            touch ~/.bash_aliases
        fi

        grep "rrdump=~/.rrdump/bin/rrdump.sh" ~/.bash_aliases ||
            echo -e "\nalias rrdump=~/.rrdump/bin/rrdump.sh" >> ~/.bash_aliases ; . ~/.bash_aliases
    else
        echo "Already at latest version ${latest_version}"
    fi
}

function selfupdate {
    local current_version=`get_current_version`
    local latest_version=`get_latest_version`
    if [ "$current_version" != "$latest_version" ]; then
        curl -sL ${rrdump_url}/${latest_version}/rrdump-${latest_version}.jar -o /tmp/rrdump-${latest_version}.jar
        unzip -qo /tmp/rrdump-${latest_version}.jar rrdump -d /tmp
        bash /tmp/rrdump --install
    else
        echo "Already at latest version ${latest_version}"
    fi
}

function version {
    local current_version=`get_current_version`
    local latest_version=`get_latest_version`
    echo ${current_version}
    if [ "$current_version" != "$latest_version" ]; then
        echo "A new version ${latest_version} is available, update with 'rrdump --selfupdate'"
    fi
}

case "$1" in
    --install)
    install
    ;;
    --version)
    version
    ;;
    -h)
    echo "usage: rrdump --version"
    echo "usage: rrdump --selfupdate"
    java -jar ${rrdump_archive}/rrdump-current.jar -h
    ;;
    --selfupdate)
    selfupdate
    ;;
    *)
    java -jar ${rrdump_archive}/rrdump-current.jar "$@"
    ;;
esac