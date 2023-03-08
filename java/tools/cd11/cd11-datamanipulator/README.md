# Java Data Manipulator
Here's a basic how-to for using the data manipulator on the java side. It's a sub project of GMS so it can open with IntelliJ and be used easily from its interface. 

## Providing arguments
The data manipulator has some basic arguments it can take when running, a subset of which are required.
 - **fileLocation**
   - Location of a file that you are using. Either this or the folderLocation needs to be specified
 - **folderLocation**
   - Location of a folder containing the json files you want to convert. This will convert all json files in a directory so be sure to only have the correct types of JSON files in the directory you're looking at. Either this or the fileLocation needs to be specified.
 - **outputFile**
   - This names the file you're outputting to. You need to specify this if you have a fileLocation input
 - **outputFolder**
   - This names the folder where you want the output JSON files to be put. Make sure the folder exists before you run this. You need to specify this if you have a folderLocation input.
 - **fromCustom**
   - This argument is a value in itself, and is true if present and false if not. If you're going from a rsdf file to custom, don't use this flag. If you're going from custom to rsdf, use this flag.

## Run using IntelliJ
If you try to run in IntelliJ without arguments the console will spit out usage warnings. 

1. To add arguments, go into "Run -> Edit Configurations" in the IntelliJ menu.

2. Add a new configuration by clicking the plus symbol in the top right. Be sure to choose the application configuration (this is to run the application)

3. Change the name to something you'll remember (like DataManipulator forwards)

4. You'll need to specify the main class. Click the "..." button next to that and choose DataManipulator (if you search for it in the next menu it should come right up)

5. Now you need to specify the program arguments. These are the command line arguments that tell the program where your files are and what you're looking to do. Here's a sample one so you can see how it works
```
-fileLocation ./gms/core/data-acquisition/cd11-station-receiver/cd11-datamanipulator/src/main/resources/test.json -outputFile testOut.json
```
This is using the file in the resources folder that's also used for test purposes. You can specify your own rsdf file as well but that's an example to follow.

6. You'll need to specify the classpath of module. Use 
```
gms.cd11-datamanipulator.main
```

7. Use JRE 11 if it's not already specified.

8. You should now be able to run!

## Using the manipulator
This program is used to convert rsdf to a custom format that includes human readable english in place of a byte string. Most of the info in the rsdf file is contained in a byte payload that is not possible to read for (most) humans, so this will output a json file with the same information, but fields that a human can read. Convert your batch of json files in one go, manipulate the fields, then convert them back, and they're ready to inject!

  