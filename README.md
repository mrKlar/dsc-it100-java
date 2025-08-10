[![Build Status](https://travis-ci.org/kmbulebu/dsc-it100-java.svg?branch=master)](https://travis-ci.org/kmbulebu/dsc-it100-java)
# DSC IT-100 Java Library
## Introduction

This library exposes all the capabilities of the DSC PowerSeries Integration Module as a Java API. With it, you should be able to automate security system tasks and receive notifications of events.

The library is very much a work in progress. It is built using Apache MINA, a library for implementing protocols. The first pass of the API is nearly complete, and usable as is. The most common IT-100 commands and messages are implemented. The less common ones still need implemented. Data is output using the RxJava functional reactive API.

## Basic Usage

### Typical Serial Setup and Basic Usage
```
// Configure IT-100 for Serial Port Access
IT100 it100 = new IT100(new ConfigurationBuilder().withSerialPort("/dev/ttyUSB0",19200).build());

// Begin listening to IT-100 commands through an rxjava Observable
Observable<ReadCommand> observable = it100.connect();

// Print all received commands to stdout
observable.subscribe(new Action1<ReadCommand>() {

    @Override
    public void call(ReadCommand command) {
        System.out.println(System.currentTimeMillis() + " " + command.toString());
    }
		
});

// Send a status request command
it100.send(new StatusRequestCommand());
```

### Add status polling
```
// Periodically send status request commands to the IT-100. The IT-100 will reply with zone status, etc.
IT100 it100 = new IT100(new ConfigurationBuilder().withStatusPolling(300).withSerialPort("/dev/ttyUSB0",19200).build());
```

### Connect over a TCP connection (with ser2net)
```
// Send a Status Request Command and wait for it to completely send.
IT100 it100 = new IT100(new ConfigurationBuilder().withRemoteSocket("raspberrypi", 2000).build());
```

### Connect to an Envisalink 3 or 4
```
// Configure for Envisalink
// Hostname/IP: envisalink, Port: 4025, Password: user
IT100 it100 = new IT100(new ConfigurationBuilder().withRemoteSocket("envisalink", 4025).withEnvisalinkPassword("user").build());
```

### Shutdown
```
// Close the connection and port.
it100.disconnect();
```

### Filter out commands
```
// Print only Zone Openings
observable.filter(new Func1<ReadCommand, Boolean>() {

    @Override
    public Boolean call(ReadCommand command) {
        return command instanceof ZoneOpenCommand;
    }
	  
}).subscribe(new Action1<ReadCommand>() {

    @Override
    public void call(ReadCommand command) {
        System.out.println(System.currentTimeMillis() + " " + command.toString());
    }
	
});

// OR...
observable.ofType(ZoneOpenCommand.class).subscribe...
```

### Use friendly labels
```
// Configure Labels
Labels labels = new Labels(it100.getReadObservable(), it100.getWriteObservable());

// Labels will request and collect a list of all Labels from the IT-100. This may take up to a few seconds to complete.

// Get the label for Zone 5
System.out.println(labels.getZoneLabel(5));
```

## Running as a Windows Service

This project can be packaged as a standalone Windows service that will run in the background.

### Prerequisites

*   Java Development Kit (JDK) or Java Runtime Environment (JRE), version 11 or newer.
*   [Apache Maven](https://maven.apache.org/download.cgi) for building the project.

### 1. Build the Project

From the root directory of the project, run the following Maven command:

```bash
mvn package
```

This will create a distribution ZIP file in the `target` directory named `dsc-it100-service-distribution.zip`.

### 2. Set Up the Service

1.  Unzip the `dsc-it100-service-distribution.zip` file into a permanent location on your server (e.g., `C:\Program Files\dsc-it100-service`).
2.  Download the WinSW (Windows Service Wrapper) executable. A stable version like **v2.12.0** is recommended. You can find it on the [WinSW GitHub releases page](https://github.com/winsw/winsw/releases). Download the `WinSW-x64.exe` file.
3.  Rename the downloaded file from `WinSW-x64.exe` to `dsc-it100-service.exe`.
4.  Place the `dsc-it100-service.exe` file into the directory where you unzipped the project files. The directory should now contain the `.exe`, a `.jar`, a `.xml`, and several `.bat` files.

### 3. Configure the Service

Open the `dsc-it100-service.xml` file in a text editor. You can change the connection details for your IT-100 module here:

```xml
  <!-- ... -->
  <!-- Make the service configurable via environment variables -->
  <env name="DSC_HOST" value="envisalink"/>
  <env name="DSC_PORT" value="4025"/>
  <env name="DSC_PASSWORD" value="user"/>
  <!-- ... -->
```

Modify the `value` attributes for `DSC_HOST`, `DSC_PORT`, and `DSC_PASSWORD` to match your setup.

### 4. Install and Manage the Service

Open a Command Prompt or PowerShell **as an Administrator**, navigate to the service directory, and use the following scripts to manage the service:

*   `install.bat` - Installs the application as a Windows service.
*   `uninstall.bat` - Removes the Windows service.
*   `start.bat` - Starts the service.
*   `stop.bat` - Stops the service.
*   `restart.bat` - Restarts the service.

After running `install.bat`, the service will be registered and can be managed through the Windows Services application (services.msc).

## Maven
This library is available via [Maven Central](https://search.maven.org/#search%7Cgav%7C1%7Cg%3A%22com.github.kmbulebu.dsc%22%20AND%20a%3A%22dsc-it100-library%22).

```
<dependency>
    <groupId>com.github.kmbulebu.dsc</groupId>
    <artifactId>dsc-it100-library</artifactId>
    <version>0.6</version>
</dependency>
```
## Gradle
```
compile 'com.github.kmbulebu.dsc:dsc-it100-library:0.6'
```

## Roadmap
* Replace RxJava dependency with a reactive-streams specification or more likely the Java 9 Flow API.
* Identify and implement remaining DSC commands.
* Support EnvisaLink3 or newer.

## Downstream Projects

- v3rm0n wrote an awesome Virtual Keypad using the dsc-it100-java library, Kotlin, Spring Boot, Websockets, and Angular. GitHub:  [virtual-keypad](https://github.com/v3rm0n/virtual-keypad)

## References

[DSC IT-100 Product Page](http://www.dsc.com/index.php?n=products&amp;o=view&amp;id=22)

[DSC IT-100 Developer Guide, hosted at HomeSeer](http://homeseer.com/pdfs/DSC/29007363R003_IT_100_developer_guide.pdf)

[RxJava and Observables](http://github.com/Netflix/RxJava)

[NickNack (Project for the automation of the Internet of Things)](http://github.com/kmbulebu/NickNack)

