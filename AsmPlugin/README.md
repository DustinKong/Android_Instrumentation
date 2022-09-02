<font face='Times New Roman' size=4>

# Introduction to AsmPlugin — Our Automated-Instrumentation-Tool
ASM is an all purpose Java bytecode manipulation and analysis framework. It can be used to modify existing classes or to dynamically generate classes, directly in binary form. In this work, we use Gradle Transformer and ASM to automatically instrument apps at the event handlers to uniquely log executed UI events，where the apps used come from THEMIS, the representative benchmark with diverse types of real-world bugs for Android. Fig. 1 shows AsmPlugin's workflow.

![Fig 1](https://github.com/liuhuiyu991026/Resource/blob/master/images/Fig1.png)

## 1. Full instrument and Log
In this step, we automatically instrument all methods included in the app source code. First, we get .class files through Gradle Transformer and our custom Gradle plugin, then we use ASM library to traverse all .class files and insert custom functions in the first and last lines of each method, where the custom functions can push method calls and returns into thread-safety BlockingQueue to ensure that the obtained Log is strictly in chronological order. Finally, the call and return trace Log of each method in the bug retriggering trace can be obtained by running the instrumented apk.

## 2. Filter
In this step, we extract the Event-Signature Set that can uniquely identify the excecuted UI events from the Log obtained in step 1. In Android, event handling function is implemented by Event Handlers. First, we use the ASM library to extract all the Event Handler Methods in Android SDK and Android-Support-Library, then by reading Log files obtained in step 1, we can get the call and return locations of all Event Handlers, we define the method call/return trace contained in the middle of method call and method return as the signature that uniquely identifies the event. For example, OnOptionsItemSelected() is a typical Event Handler method. If an event e1 is a select options item event, its method call chain is like:
<div align='center'>
call OnOptionsItemSelected( )-->call m1( )-->call m2( )--><br>
return m2( )-->return m1( )-->return OnOptionsItemSelected( )
</div>
<br>
Then in our method, the above chain is the signature that can uniquely identify the event e1.

## 3. Event-signature-related instrument
In this step, we more precisely instrument pivot methods in the app source code. In step 2, we have got the Event-Signature Set, so now we instrument all the methods involved in the Event-Signature Set, and the instrumentation strategy is the same as that in step 1. In this way, we get the finally instrumented app. When retriggering bugs, we can extract the Event Signatures from the output Log to determine whether a UI event has been executed. Compared with the fully instrumented version apk in step 1, we now only focus on the methods related to Event Signature, and filter out many irrelevant functions, which simplifies the workload of extrating Event Signatures.


# Guide of AsmPlugin
Part of the directory structure of AsmPlugin is as follows:
```
AsmPlugin
│  
│  build.gradle					
│  ...
│  
├─app
│  │  
│  │  build.gradle
│  │  ...
│  │ 
│  └─src
│      └─main
│          │  
│          └─java
│              ├─com.gavin.asmdemo
│              │       MainActivity.java:	launcher Activity of this app
│              │              ...
│              │              
│              └─realtimecoverage
│                      CrashHandler.java:	implements an interface handing uncaught exception
│                      MethodVisitor.java:	implements custom functions need to be inserted
│                      RealtimeCoverage.java:	implements BlockingQueue to monitor method calls and returns
│                      
├─asm-method-plugin
│  │  
│  │  build.gradle
│  │  ...
│  ├─my-plugin:					store plugin local repository
│  │  
│  └─src
│      └─main
│          ├─groovy
│          │  └─com.example.asm.plugin
│          │          AsmPlugin.groovy: 	using transform to handle all .class files 
│          │          
│          ├─java
│          │  └─com.example.asm.plugin
│          │          AsmClassVisitor.java:	implements Class Visitor
│          │          AsmMethodVisitor.java:	implements Method Visitor
│          │                      
│          └─resources
│              └─META-INF.gradle-plugins
│                    com.asm.gradle.properties:	explicit plugin's implementation-class
│                          
```
## step 0. Preparation
You need to prepare an app with source code and know the Gradle version and AGP version of the app.

## step 1. Import Plugin
You can import the module **_asm-method-plugin_** into your app project, or you can create a new module in your project according to the above directory of module **_asm-method-plugin_**.

## step 2. Insert Code
First, modify Gradle version in **_build.gradle_** in module _asm-method-plugin_ to the same version as your project, e.g.:
```gradle
dependencies {
	implementation gradleApi()
	implementation localGroovy()
	// modify Gradle version
	implementation 'com.android.tools.build:gradle:3.5.0'
}
```
Don't forget to run Gradle Task **_UploadArchives_** to refresh your plugin.<br>
<br>
Second, add the package **_realtimecoverage_** to the source project of the app that needs to be instrumented, and preferably you can add it to **_app/src/main/java_**. Then find the Launch Activity of your app according to _AndroidMenifest.xml_, and insert some code into **_onCreate()_** method of your Launch Activity like the following snippet.
``` java
protected void onCreate(Bundle savedInstanceState) {
	// Initialize the blocking queue of Method-Call-Listener
	realtimecoverage.RealtimeCoverage.init();
	super.onCreate(savedInstanceState);
	// Set an interface for handlers invoked when a Thread abruptly terminates due to an uncaught exception
	realtimecoverage.CrashHandler crashHandler = realtimecoverage.CrashHandler.getInstance();
	crashHandler.init(getApplicationContext());
	...
}
```
## step 3. Modify Configurations
First, you need to import this plugin to the project-level build.gradle like the following snippet.
``` gradle
buildscript {
	repositories {
		...
		google()
		jcenter()
		maven {
			url uri('./asm-method-plugin/my-plugin')
		}
	}
	dependencies {
		...
		classpath 'com.asm.plugin:asm-method-plugin:0.0.1'
    }
	...
}
```
Second, you need to apply this plugin to the app-level build.gradle like the following snippet.
```gradle
apply plugin: 'com.asm.gradle'
```

Note that it is best to **keep your project successfully compiled** during the above insertion process!


</font>
