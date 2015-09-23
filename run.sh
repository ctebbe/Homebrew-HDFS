#!/bin/bash

HOST=$(hostname)
PORT="8080" # default controller port

#ant clean

# controller start-up
gnome-terminal -e 'bash -c "ant -Darg0=${PORT} controller; bash"'

sleep 2 # allow the controller time to spin up

while read CHUNKSERVER
do
    echo 'sshing into '${CHUNKSERVER}
    gnome-terminal -x bash -c "ssh -t ${CHUNKSERVER} 'cd '~/workspace/cs555/A01'; echo $CHUNKSERVER;
    ant -Darg0=${HOST} -Darg1=${PORT} node_args; rm /tmp/cs555_data/*; bash'" #&
done < chunknodes


# client startup
gnome-terminal -x bash -c "ssh -t cheyenne 'cd '~/workspace/cs555/A01'; echo $(hostname);
ant -Darg0=${HOST} -Darg1=${PORT} client; bash'" #&
