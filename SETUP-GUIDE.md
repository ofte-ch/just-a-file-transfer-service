1. Clone this project
2. Run the following command on your terminal
    - Windows: execute the file named create-sub-module.bat
    - Linux-based: `sh create-sub-module.sh`
3. Open the project in your IDE, do not forget to add `server/jafts` to your module/deps in your IDE.
4. Code in the `web-client` should call `ng build` to sync the generated code to the Spring static source
5. Run the project with `dev` profile to disable the cache
