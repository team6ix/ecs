## Install WAS Liberty
1. Open "Servers" view if not opened. Go to `Window` > `Show View` > `Other`. In "Show View" dialog, select `Servers`.
2. In "Servers" view, right-click to bring up the context menu. Select `New` > `Server`.
3. On "Define a New Server" page, expand `IBM` and select `Liberty Server`. Use the default host name and server name. Click `Next`.
4. On the "Liberty Runtime Environment" page, under `How do you want to install the runtime environment?`, select `Install from an archive or a repository`. Click `Next`
5. On the "Install Runtime Environment" page, enter the destination path or use the `Browse` button to select the folder to be used as the destination path.
6. Choose `Download and install a new runtime environment from ibm.com` option.
7. Select `WAS Liberty Runtime`. Click `Next`.
8. On the "Install Additional Content" page, click `Next` to skip installing additional content.
9. On the "License Acceptance" page, accept the license agreements and click `Next`.
10. On the "New Liberty Server" page, take the defaults and click `Finish` to start the downloading and installation of WAS Liberty Runtime.
11. When the installation completes successfully, click `OK` to dismiss the result dialog.

## Checking out from Git

Clone the repo, then navigate to the directory and run:

> ./gradlew clean cleanEclipse eclipse

> ./gradlew clean build

Now navigate to Eclipse Git view, right click on the repo and click "Import Projects" and hit next -> finish without modifications.

## Convert project to Dynamic web project

1. In `Project Explorer`, right-click the project.
2. Click `Properties`
3. Navigate to `Project Facets`
4. Ensure `Dynamic Web Module` and `Java` are selected
5. Navigate to `Deployment Assembly`
6. Ensure you have the following:

| Source | Deploy Path |
| --- | --- |
| src/main/java | WEB-INF/classes |
| src/main/resources | WEB-INF/classes |
| src/main/webapp | / |
