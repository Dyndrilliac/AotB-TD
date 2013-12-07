Author: Matthew Boyette (Dyndrilliac@gmail.com)

Thank you for checking out the source code for Attack of the Barbarians!

The game is a relatively large project compared to most of the assignments to which undergrad students are accustomed. 
The first thing you need to do is make sure you have the following items installed, otherwise you WILL NOT be able to 
build the package from the source code! In order to save space and make the source code a fast download, most of the 
things you will need are not included in this ZIP archive!

Download/Install:
1) Eclipse IDE, latest version. http://www.eclipse.org
2) Andoird SDK, latest  version. https://developer.android.com/sdk/index.html
3) Android Developer Tools for Eclipse (Available for free from the Eclipse Marketplace inside Eclipse: "Help" Menu -> "Eclipse Marketplace...")
4) Some kind of Git client (EGit, GitHub, etc) https://github.com/

Once everything is downloaded and installed, clone the repositories for AndEngine and the two extensions I'm using from 
the links provided below.

AndEngine https://github.com/nicolasgramlich/AndEngine
PhysicsBox2DExtension https://github.com/nicolasgramlich/AndEnginePhysicsBox2DExtension
TMXTiledMapExtension https://github.com/nicolasgramlich/AndEngineTMXTiledMapExtension

Import them into Eclipse by going to: "File" Menu -> "Import..." -> "Android" -> "Existing Android Code Into Workspace"

I prefer not to check the box to copy the project to the workspace folder so that I can easily keep the source code synchronized
with the Git repository so I can easily pull the latest updates and bugfixes into my projects. Sometimes Eclipse makes a mistake
and doesn't configure the dependencies for imported projects correctly, so be sure to double check the Java build path for each 
project that you import (including Attack of the Barbarians!). Duplicate symbols/projects/JARs on the build path can cause a special
error in the conversion process from the JDK JVM code used on a PC/Mac to the Dalvik JVM code used by Android devices. If see a
Dalvik conversion error, you may want to speak to me personally because fixing it goes beyond the scope of thse instructions.

For more information on the Dalvik error, see this link: http://stackoverflow.com/questions/2680827/conversion-to-dalvik-format-failed-with-error-1-on-external-jar

Once you have AndEngine correctly imported into Eclipse, you may want to clean the project so that all the binaries are deleted
and rebuilt. Highlight the projects in the Project Explorer and go to: "Project" Menu -> "Clean..." and check the boxes for the 
project you want to clean. Next, import the Attack of the Barbarians project from this directory using the same process outlined
above for AndEngine and its extensions. You may wish to clean and rebuild it as well, and dont forget to double check the Java
build path and make sure it is correctly linked to the AndEngine library dependencies. If you need assistance to do this, feel 
free to email me for help.