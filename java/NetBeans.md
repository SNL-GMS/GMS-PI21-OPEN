# NetBeans Guide

## Contents
[1. Getting Started](#getting-started)  
[2. Gradle](#gradle)  
[3. Code Navigation](#code-navigation)  
[4. Code Editing](#code-editing)  
[5. Testing](#testing)  
[6. Git](#git)  
[7. SonarLint](#sonarlint)  
[8. Misc](#misc)  
[9. Troubleshooting](#troubleshooting)  
[10. Known Issues](#known-issues)

## Getting started

To import gms-common into NetBeans, open `gms-common/java` as a project. 

In the Projects window, right click `gms-common` and select `Open Required Projects -> Open All Projects`. Many NetBeans operations (like finding usages of a class) only apply to open projects, so it's probably a good idea to open them all.

To have NetBeans use the appropriate Gradle installation, open Preferences, select the Java tab, then select the Gradle tab. You'll probably want to choose `Gradle distribution: Custom` and have that point to your local gradle installation.

Follow the instructions on the Wiki Coding Style page to import GMS' NetBeans style guide.
***Important Note:*** Despite what the instructions on the wiki say, only import `Formatting` options from wiki netbeans prefrences `.zip` file.

The following sections describe some common operations/tasks and how to accomplish them in NetBeans.

## Gradle

### Editing `build.gradle` files
To get Groovy syntax highlighting and formatting...
   1. Open Groovy settings at `NetBeans -> Preferences -> Miscellaneous -> Groovy`
   2. You'll see `Activating Groovy...` - once that's done you'll get syntax highlighting and formatting

### Running gradle tasks from the Navigator window
1. Select the desired project in the Projects window - a list of available Gradle tasks will appear in the Navigator window
2. Double click the Gradle task to run
   * Instead of double clicking, you can right click the Gradle task and select `Execute custom...` to apply flags or other options

### Running miscellaneous gradle tasks
1. Select the desired project in the Projects window
3. Right click the project and select `Run Gradle -> Tasks...`
4. Enter the command(s) to run
5. Click `OK` to run the commands
* Some commands seem to show less output for some reason when run this way

## Code Navigation

### Opening a class/file
1. `Ctrl + Shift + O`

### Go to Definition
1. `Ctrl + Shift + G`

### Show an open file in the Projects window
1. While the file is open in the editor, right click the file tab, select `Select in Projects`

### Go to line
1. `Ctrl + G`

### Find usages
1. `Ctrl + F7`

### Find implementations of an interface
1. `Cmd + Alt + Click` the name of the interface

## Code Editing

### Enable code completion for variable names
1. Open preferences, navigate to `Editor -> Code Completion`
2. Select `Language: Java`
3. Check `Auto Popup on Typing Any Java Identifier Part`

### Format Code in Current File
1. `Ctrl + Shift + F`

### Fix Imports in Current File
1. `Cmd + Shift + I`

### Fix Imports on a Project
1. Select the desired project in the Projects window
2. Select `Refactor -> Inspect and Transform...`
3. Select the `Configuration` radio button
4. Create a `Fix Imports` configuration
   1. Click `Manage...`
   2. In the `Configuration` drop-down, click `New...`
   3. Name the configuration `Fix Imports` by selecting the `Configuration` drop down and clicking `Rename...`
   4. Under `Inspections`, check all boxes under the `Imports` section
   5. Click `Ok`
5. Ensure `Current Project` is selected in the `Inspect` drop-down
6. Click `Inspect`


### Organize Imports on Project/Folder/Package
1. Select project/folder/package in Projects window
2. From the top toolbar, `Refactor -> Inspect and Transform`
3. Select `Organize Imports` from the `Single Inspection` drop down menu

### Code folding settings
1. Open `Preferences`, navigate to `Editor -> Folding`, select `Language: Java`, and set checkboxes as desired.

### Show inline parameter hints
* `View -> Show Inline Hints`

### Show parameter types for current method
* `Cmd + Shift + P`

### Show javadoc
1. Hold `Cmd` and hover over a symbol to see its javadocs. Seems to work sometimes but not others.

### Add "misspelled" word to dictionary
1. Click the misspelled word, `Ctrl + Enter`
2. Click `Add to your private dictionary` - clicking `Add to project dictionary` will only apply to the current project and will create NetBeans-specific files that aren't Git ignored.

## Testing

### Code coverage
1. Select the project in the projects window
2. `build` the project
3. Right click the project in the projects window
4. Check `Code Coverage -> Collect and Display Code Coverage`
5. Right click the project in the projects window and select `Code Coverage -> Show Report`
6. To clear code coverage highlighting, select `Clear` in the code coverage toolbar at the bottom of the editor.
7. To remove the code coverage toolbar, click `Disable`. The toolbar will come back when coverage results are shown the next time.

## Git

To enable the Git toolbar, right click empty space in the toolbar and select `Git`.

One quirk to be aware of with NetBeans' Git functionality is that it operates on the currently selected "item", whatever that may be.
For example, if you're actively editing a file, and you select `Show Changes`, only changes from that file will be shown, even if changes were made in other files as well.
To view changes in multiple files, you need to select the appropriate project in the Projects window.
Selecting `gms-common` in the Projects window to set the scope to all of `gms-common` is probably a good idea before using any of NetBeans' Git operations.

### Show Current Branch
1. `View -> Show Versioning Labels` - branch will be shown next to project name in file explorer

### Viewing changes
1. Select the project in which to view changes in the projects window
2. Click the `Show Changes` button

### Reverting changes
1. Select the project in which to revert modifications in the projects window
2. Click the `Revert Modifications...` button
3. Select the desired options and click `Revert`

### Commiting changes
1. Select the project in which to commit changes in the projects window
2. Click the `Commit...` button
3. Select the desired files to commit, enter a commit message, and click `Commit`

### Pushing changes to the current remote branch
1. Click the `Push to Upstream` button

### Pulling changes from the current remote branch
1. Click the `Pull from Upstream` button

### Merging branches
1. Select `Team -> Branch/Tag -> Merge Revision...`
2. Select the branch to be merged into the current branch (the current branch is selected by default, for whatever reason)
   * You'll probably need to type the branch name in the text box. Clicking `Select` brings up a window that seems like it should allow you to pick branches from a list, but the list never seems to populate.
3. Click `Merge`

## SonarLint

Netbeans has a plugin to run SonarLint analysis locally before a branch is pushed up to the pipeline. In order to install the plugin do the following:
1. Select `Tools -> Plugins`
2. Search for `sonar` in the `Avaiable Plugins` Note you may have to navigate to the `Settings` tab and make sure all the available `Update Centers` are checked and active.
3. Install the `sonarlint4netbeans` plugin, there should only be one that is found. 
4. Once installed, in order to analyze the Java code you are currently working on simply make sure the code is active in the editor and go to `Tools-> Analyze with SonarLint` and a pane should show up on the bottom with said analysis. 

## Misc

### Lost windows
1. `Window -> Reset windows`

## Troubleshooting

### Clearing NetBeans' Cache

If you run into issues with NetBeans, a good first step is to clear NetBeans' cache, located at `~/Library/Caches/NetBeans/<NetBeans Version>/`. It's safe to delete everything in that directory in order to clear the cache.

### Cert issues

If you encounter any security cert-related issues, such as `sun.security.validator.ValidatorException: PKIX path building failed: sun.security.provider.certpath.SunCertPathBuilderException: unable to find valid certification path to requested target`, the likely cause is that the intercept cert has not been added to the Java keystore, or NetBeans is using the wrong version of java (frequently encountered when multiple versions of Java are installed. NetBeans seems to want to use the latest one).

To add the intercept cert to the Java keystore, `cd` into your Java Home, and run...

```bash
sudo ./bin/keytool -importcert -file /path/to/cert/bundle/cert-name.cer -keystore ./lib/security/cacerts -alias <cert-alias> -trustcacerts
```

### NetBeans using wrong version of Java

Ensure netbeans is using the correct Gradle installation (`NetBeans -> Preferences -> Java -> Gradle`)

Ensure NetBeans' Java Platform is set correctly (`Tools -> Java Platforms`)

If the previous solutions don't work (they likely won't, but they're still good to check), you can force NetBeans to use a certain Java installation by editing `netbeans.conf`, located at `/Applications/NetBeans/Apache\ NetBeans\ <NetBeans version>.app/Contents/Resources/NetBeans/netbeans/etc/netbeans.conf`.
1. Close NetBeans
2. Uncomment the line `#netbeans_jdkhome="/path/to/jdk"` and set the JDK path you want it to use. When you start NetBeans back up again, it should be using the specified JDK installation.

## Known Issues

### GMS-specific
1. Only import `Formatting` options from wiki netbeans prefrences .zip. Under `Formatting` options only import `Formatting`, do not import `Java`.
2. `.gitignore` doesn't ignore some NetBeans project files that get generated when certain settings are tweaked.

### General

4. No built in way to create files of non-supported type, i.e. `build.gradle`
6. When moving AutoValue classes, the refactor tool seems like it will, in certain situations, replace references to the abstract classes with the concrete generated `AutoValue_`-prefixed classes that get left behind.
7. `clean build` on the base `gms-common` project sometimes doesn't catch failing unit tests
8. `Refactor -> Move`'ing classes sometimes messes up imports.  Many unnecessary imports are often added, like imports for classes in the same package. Be aware of unnecessary imports, they're easy to accidentally commit.
9. Doing a "fix imports" inspect & transform operation on multiple projects doesn't find any instances of imports that need to be fixed, while running it on a single project does.

### Tests
1. `Rerun failed` button doesn't seem to work at all. Clicking it does nothing.
2. `Run again` fails to re-run test with error `No tests found for given includes...`
3. NetBeans shows a warning for `unused method` when your test methods *aren't* public. SonarQube creates a code smell if your test methods *are* public.

