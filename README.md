# DDroid-instrumentor
<font face='Times New Roman' size=4>

## Introduction
DDroid-instrumentor is an automated instrumentation tool for Android apps. It is built on [ASM](https://asm.ow2.io/) and [Gradle Transformer]() to automatically instrument apps at the event handlers to uniquely log executed UI events. Specifically, ASM is an all purpose Java bytecode manipulation and analysis framework, which can modify existing classes or dynamically generate classes in the binary form. 

In our work, DDroid-instrumentor is used to instrument the apps from [Themis](https://github.com/the-themis-benchmarks), the representative benchmark with diverse types of real-world bugs for Android. 

Fig. 1 shows DDroid-instrumentor's workflow.

![Fig 1](https://github.com/liuhuiyu991026/Resource/blob/master/images/Fig1.png)

### Step (1): Fully instrumentation 
Given an app, we automatically instrument all the class methods to obtain an instrumented app. Specifically, we get the ``.class`` files through Gradle Transformer and our custom Gradle plugin, and use ASM to traverse all the ``.class`` files and insert customized functions at the entry and exits of each method. 

### Step (2): Logging the executed methods

The customized functions log the executed methods (including the entry and exits) when the bug-triggering traces are manually replayed on the instrumented app. Note that we use a thread-safe ``BlockingQueue`` to record the executed method calls.

### Step (3): Filtering the event signature
In this step, we extract the executed methods within *an event handler*, and use the sequence of executed methods as *an event signature*. In Android, the event handling methods are used to handle user events. Thus, the event signature can uniquely identify the executed UI events from the log obtained in Step (2). 

For example, ``OnOptionsItemSelected(...)`` is a typical event handler method to implement a menu. Let an event ``e`` be the action to select a menu item, its method call chain could be:

``call`` OnOptionsItemSelected( )-->``call`` m1( )-->``call`` m2( )-->``return`` m2( )-->``return`` m1( )-->``return`` OnOptionsItemSelected( )

The preceding method call sequence is the event signature that can uniquely identify the event ``e``.

Specifically, we use ASM to identify all the event handlers declared in Android SDK and Android Support Library, and filter the log from step (2) to obtain the executed methods within an event handler.  


### Step (4): Instrument the app by the event signature 
In this step, we instrument the app at all the methods of the event signature (the instrumentation strategy is similar to step 1). In this way, we get the final instrumented app. When a testing tool is running against the instrumented app, we can easily extract the event signatures from the log to determine which UI events have been executed. Compared to the fully instrumented app in step 1, we can focus on the only relevant methods of the target bug, which simplifies the workload of obtaining the event traces for automata-based trace analysis.


## Guide of using DDroid-instrumentor

The directory structure of DDroid-instrumentor is as follows:
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
│              │       MainActivity.java:	# launcher Activity of this app
│              │              ...
│              │              
│              └─realtimecoverage
│                      CrashHandler.java:	# implement an interface handing uncaught exception
│                      MethodVisitor.java:	# implement custom functions need to be inserted
│                      RealtimeCoverage.java:	# implement BlockingQueue to monitor method calls and returns
│                      
├─asm-method-plugin
│  │  
│  │  build.gradle
│  │  ...
│  ├─my-plugin:					# store plugin local repository
│  │  
│  └─src
│      └─main
│          ├─groovy
│          │  └─com.example.asm.plugin
│          │          AsmPlugin.groovy: 	# using transform to handle all .class files 
│          │          
│          ├─java
│          │  └─com.example.asm.plugin
│          │          AsmClassVisitor.java:	# implement Class Visitor
│          │          AsmMethodVisitor.java:	# implement Method Visitor
│          │                      
│          └─resources
│              └─META-INF.gradle-plugins
│                    com.asm.gradle.properties:	# explicit plugin's implementation-class
│                          
```
### step 0. Preparation
You need to prepare an app with source code and know the Gradle version and AGP version of the app.

### step 1. Import Plugin
You can import the module **_asm-method-plugin_** into your app project, or you can create a new module in your project according to the above directory of module **_asm-method-plugin_**.

### step 2. Insert Code
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
### step 3. Modify Configurations
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

Note you need to make sure the app project can be successfully compiled.


</font>


