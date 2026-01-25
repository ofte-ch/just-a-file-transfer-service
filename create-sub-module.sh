#!/bin/bash

# Create deps folder if it does not exist
if [ ! -d "deps" ]; then
    mkdir deps
fi

# Go into deps folder
# The '|| exit' ensures the script stops here if the directory change fails
cd deps || exit

# Clone repositories
git clone git@github.com:ofte-ch/java-commons.git Commons
git clone git@github.com:ofte-ch/RPCServer.git RPCServer

# Done
echo "Repositories cloned successfully."

read -n 1 -s -r -p "Press any key to continue..."
echo
