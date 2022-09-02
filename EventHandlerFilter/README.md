# EventHandlerFilter

We use this tool to extract the Event-Signature Set that can uniquely identify the excecuted UI events from the Log obtained from the fully instrumented app.

## How to Run

### 1. Requirement

- JetBrains Intelli IDEA 
- Java 11+
- android-platforms jars

### 2. Steps

First, you need to put the Log-file obtained from the fully instrumented app in the root directory of the project **_EventHandlerFilter_**.

Second, you can run `Main` to get the list of Event Handler, and the result `listener.db` can be found in the root directory of the project **_EventHandlerFilter_**.

Finally, you can run `GetMethodList` to extract the Event-Signature Set method list, and the result 'MethodList.txt' can be also found in the root directory of the project **_EventHandlerFilter_**.