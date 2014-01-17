*******************************************
Title:  Attack of the Barbarians
Author: [Matthew Boyette](mailto:Dyndrilliac@gmail.com)
Date:   12/4/2013
*******************************************

Language: Java
Platform/OS: Android
Requirements: Android 2.2.0 Froyo or better, OpenGL ES 2.0 hardware support</br>
Tested Devices: Samsung Intercept (Android 2.2.3 Froyo), HTC Evo 3D (Android 4.0.3 Ice Cream Sandwich), Android Software Emulator (2.2.0 to 4.2.2)

Thank you for checking out Attack of the Barbarians!

*******************************************
Game Instructions (Downloading/Installing)
*******************************************

Want to try the game without building it from the source code?

[Download APK](https://dl.dropboxusercontent.com/u/26912774/AotB.apk)

You will need to make sure that the check box labelled "Allow applications from unknown sources" is check in order to install the package. Most Android handsets come with a package installer application. Otherwise you can get one off of the market for free. The Dropbox application for Android will also allow you to install the APK if you download it to your phone from your Dropbox folder. 

*******************************************
Game Instructions (Playing)
*******************************************

The game begins immediately as soon as you open the application. The very top portion of the interface is a heads-up display (HUD) which gives you information in real-time and allows you to issue orders to build towers on the battlefield. Below the HUD is the map of the battlefield. You can pan around by dragging your finger or mouse, and multi-touch gesture support is implemented so you can pinch to zoom-in or spread your fingers apart to zoom-out.

On the far-right of the HUD is the FPS counter. It tells you how many FPS the game is running at to measure performance. A button is provided to the left of the FPS counter in the HUD which will allow you to pause and resume the game to allow for developing a strategy and implementing it before the majority of the enemies arrive. It is the color red to provide contrast from the rest of the game's visuals and changes in appearance to help the user keep track of the current state. A pause symbol means the game is currently running and pressing the button will pause execution of the game. A play symbol means the game is paused and pressing the button will resume execution.

Further to the left is the Build Tower sprite. You tap (or click) on it and drag it to the map tile where you want to build the tower. Some places are suitable for towers, others are not. Green grass and open desert are okay but waterways and cacti are not. If you can build in the desired located, and ring indicating the range of the tower will be green. If not, the ring will be red. Basic towers fire at a semi-automatic rate and do average damage.

Furthest to the left is the current level information. It tells you how many credits you have, how far into the invasion attempt you are, and how many lives you have left. You gain credits by killing barbarians and selling existing towers. You lose credits by building towers. You lose a life each time a barbarian makes it all the way across the battlefield. You cannot gain lives. When all lives are lost, you have been defeated and your whole village is raped, pillaged, and murdered. The progress bar tells you how far into the invasion you are so you can tell how close you are to completing the map.

*******************************************
Planned Future Features
*******************************************

Tower upgrades, custom maps, new game modes, randomized start/goal locations for barbarians.

*******************************************
Known Bugs/Issues
*******************************************

When clicking or tapping on an unwanted tower to sell it, the tower is not removed but the credits are still added to your account and it still fires at enemies (although it no longer obstructs their path so they can proceed through the tile on which it was located).

There is a lag time in between the moment a barbarian crosses the threshold of the goal and a life gets deducted from the player.

The victory/loss transitions could use work.

*******************************************
Build Instructions
*******************************************

The game is a relatively large project compared to most of the assignments to which undergrad students are accustomed. The first thing you need to do is make sure you have the following items installed, otherwise you WILL NOT be able to build the package from the source code! In order to save space and make the source code a fast download, most of the things you will need are not included in this ZIP archive!

Download/Install:

[1. Eclipse IDE, latest version.](http://www.eclipse.org)
[2. Android SDK, latest version.](https://developer.android.com/sdk/index.html)
3. Android Developer Tools for Eclipse (Available for free from the Eclipse Marketplace inside Eclipse: "Help" Menu -> "Eclipse Marketplace...")
[4. Some kind of Git client (EGit, GitHub, etcetera).](https://github.com/)

Once everything is downloaded and installed, clone the repositories for AndEngine and the two extensions I'm using from the links provided below.

[AndEngine](https://github.com/nicolasgramlich/AndEngine)
[PhysicsBox2D Extension](https://github.com/nicolasgramlich/AndEnginePhysicsBox2DExtension)
[TMXTiledMap Extension](https://github.com/nicolasgramlich/AndEngineTMXTiledMapExtension)

Import them into Eclipse by going to: "File" Menu -> "Import..." -> "Android" -> "Existing Android Code Into Workspace"

I prefer not to check the box to copy the project to the workspace folder so that I can easily keep the source code synchronized with the Git repository and I can easily pull the latest updates and bug-fixes into my projects. Sometimes Eclipse makes a mistake and doesn't configure the dependencies for imported projects correctly, so be sure to double check the Java build path for each project that you import (including Attack of the Barbarians!). Duplicate symbols/projects/JARs on the build path can cause a special error in the conversion process from the JDK JVM code used on a PC/Mac to the Dalvik JVM code used by Android devices. If see a Dalvik conversion error, you may want to speak to me personally because fixing it goes beyond the scope of these instructions.

For more information on the Dalvik error, see this link: [Conversion to Dalvik Format Failed with Error 1](http://stackoverflow.com/questions/2680827/conversion-to-dalvik-format-failed-with-error-1-on-external-jar)

Once you have AndEngine correctly imported into Eclipse, you may want to clean the project so that all the binaries are deleted and rebuilt. Highlight the projects in the Project Explorer and go to: "Project" Menu -> "Clean..." and check the boxes for the project you want to clean. Next, import the Attack of the Barbarians project from this directory using the same process outlined above for AndEngine and its extensions. You may wish to clean and rebuild it as well, and don't forget to double check the Java build path and make sure it is correctly linked to the AndEngine library dependencies. If you need assistance to do this, feel free to email me for help.