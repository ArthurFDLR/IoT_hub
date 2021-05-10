<h1 align = "center"> IoT Hub </h1>

<p align="center">
    <img src="./.github/gui_view.png" alt="Control panel page" width="80%" style="border-radius: 5px;">
</p>

[![MIT License][license-shield]][license-url]
[![LinkedIn][linkedin-shield]][linkedin-url]

Deploy a server connected to an MQTT broker to control your Internet-of-Things devices through a Web App including, GUI, RESTful API, and an SQL database! Built with [Spring Boot](https://github.com/spring-projects/spring-boot), [Bootstrap 4](https://github.com/twbs/bootstrap), [Eclipse Paho Java Client](https://github.com/eclipse/paho.mqtt.java), and [SQLite JDBC Driver](https://github.com/xerial/sqlite-jdbc).

- [Getting Started](#getting-started)
  - [Prerequisites](#prerequisites)
  - [Installation](#installation)
  - [Launch the IoT-Hub](#launch-the-iot-hub)
- [Usage](#usage)
  - [Simulated plugs control](#simulated-plugs-control)
  - [MQTT messages](#mqtt-messages)
  - [RESTful API](#restful-api)
  - [User interface](#user-interface)
  - [Database](#database)
- [License](#license)

## Getting Started

To get a local copy up and running follow these simple steps.

### Prerequisites

* Java JDK 8+
* Gradle 7+
* An MQTT broker, deploy the following docker container if unsure:
    ```sh
    docker pull eclipse-mosquitto:1.6.14
    docker run -it -p 1883:1883 eclipse-mosquitto:1.6.14
    ```

### Installation

You need an MQTT broker running on port `tcp://127.0.0.1:1883` to execute installation tests. 

1. Clone the repo.
   ```sh
   git clone https://github.com/ArthurFDLR/IoT_hub.git
   cd .\IoT_hub\
   ```
2. Run unitary tests and build the app.
   ```sh
   gradle
   ```
3. Run integration tests to verify installation, it may take some time.
   ```sh
   gradle test_integration
   ```

### Launch the IoT-Hub

1. Configure your IoT-Hub in [`./hubConfig.json`](./hubConfig.json):
   * `httpPort`: port on which the web-app and the RESTful API can be accessed.
   * `mqttBroker`: Address of your MQTT broker, should be `tcp://127.0.0.1:1883` if you followed the [prerequisites](#prerequisites) MQTT installation.
   * `mqttClientId`: Client ID used by the hub to connect to the broker.
   * `mqttTopicPrefix`: Prefix of all MQTT topics used by the web app.
   * `databaseFileName`: Name of the SQLite database for your hub.
2. Start the hub
   ```sh
   gradle iot_hub
   ```
3. You can simulate smart plugs to populate the MQTT server if you don't have any IoT devices connected. Configure the simulator in [`./simConfig.json`](./simConfig.json):
   * `httpPort`: port on which the simulated plugs can be controlled. It must be different from the port of the main web app.
   * `plugNames`: List of names of simulated plugs.
   * `mqttBroker`: Address of your MQTT broker.
   * `mqttClientId`: Client ID used by the simulator to connect to the broker.
   * `mqttTopicPrefix`: Prefix of all MQTT topics used by the simulator. It must be the same as the one used by the web app.

<!-- USAGE EXAMPLES -->
## Usage

### Simulated plugs control

* **Plug report:**
  I want to access the web page containing a report of the plug with the name `plugName` at the path `/plugName` without any query string, so that I can view the report using a browser. The report should include whether the switch is on and its power reading.

* **Toggle or switch a plug on/off:**
  As an end-user, I want to control the plug with the name `plugName` at the path `/plugName` with a query string, so that I can control the plug using a browser. To toggle a plug, the query string is `action=toggle`. To switch a plug on, the query string is `action=on`. To switch a plug off, the query string is `action=off`.

* **Control feedback:**
    As an end-user, I want to receive the up-to-date report as the response to the path `/plugName` with a query string, so that I can verify that the plug acts properly.

### MQTT messages

* **Plug on/off updates:**
    As an end-user, I want to receive MQTT messages when a plug is turned on or off, so that I can monitor the plug on/off events using a MQTT client. For a plug with the name `plugName` and a configuration string `prefix`, the topic should be `prefix/update/plugName/state`, and the message is either `on` or `off`. Note that it is possible for prefix to have the character / multiple times.

* **Plug power updates:**
    As an end-user, I want to receive MQTT messages when the power consumption of a plug is measured, so that I can monitor the plug power consumption using a MQTT client. For a plug with the name `plugName` and a configuration string `prefix`, the topic should be `prefix/update/plugName/power`, and the message is the power consumption in plain text.

* **Toggle or switch a plug on/off:**
    As an end-user, I want to send MQTT messages to toggle or switch on/off a plug, so that I can control the plug using a MQTT client. For a plug with the name `plugName` and a configuration string `prefix`, the topic should be `prefix/action/plugName/actionString`, where the actionString is one of `toggle`, `on`, or `off`.

### RESTful API

* **Get the state of a single plug:**
    As an end-user, I want to query the state of the plug `plugName` via a GET request to `/api/plugs/plugName`, so that I can obtain state of individual plugs in a web application. The response should be a JSON object in the format, e.g. `{"name":"plugName", "state":"on", "power":100}`. The value for `state` could also be `off`.

* **Get the states of all plugs:**
    As an end-user, I want to query the states of all plugs via a GET request to `/api/plugs`, so that I can obtain all of them at once in a web application. The response should be a JSON array of objects, that each represents the state of a single plug.

* **Control a single plug:**
    As an end-user, I want to switch on/off or toggle the plug `plugName` via a GET request to `/api/plugs/plugName` with a query string, so that I can control it in a web application. To toggle the plug, the query string is `action=toggle`. To switch the plug on, the query string is `action=on`. To switch the plug off, the query string is `action=off`.

* **Create a group:**
    As an end-user, I want to create a group `groupName` of plugs via a POST request to `/api/groups/groupName`, so that I can manage multiple plugs as a whole. The body of the POST request is a JSON array of the names of the plugs to be included in the group. If the group already exists, its members will all be replaced. Note that a single plug is allowed to be assigned to multiple groups.

* **Remove a group:**
    As an end-user, I want to remove a group `groupName` of plugs via a DELETE request to `/api/groups/groupName`, so that I can remove the group in a web application.

* **State of a group:**
    As an end-user, I want to query the state of a group `groupName` as the states of its member plugs via a GET request to `/api/groups/groupName`, so that I can obtain their states in a web application. The response should be a JSON object with keys `name` for the name of the group, and “members” for a JSON array of objects, that each represents the state of a member plug.

* **States of all groups:**
    As an end-user, I want to query all the groups for the states of member plugs via a GET request to `/api/groups`, so that I can obtain everything together in a web application. The response should be a JSON array of objects, that each represents the state of a group.

* **Control a group:**
    As an end-user, I want to switch on/off or toggle all the plugs in a group `groupName` via a GET request to `/api/groups/groupName` with a query string, so that I can control plugs in a group together. The query string is the same as those to control a single plug.

### User interface

* **Plugs and plug states:**
    As an end-user, I want to see available plugs and their states, so
    that I can know what plugs are there and whether they are on or off.
    
    1.  Open a browser and access the root page. The names of all
        available plugs will show in rows on the left part of the page
        under *Plugs*.
    
    2.  The state of a plug is shown next to the *on/off* icon in the
        row associated to the plug.

* **Control a single plug:**
    As an end-user, I want to click a button on the web page to switch
    on/off or toggle a plug of my choice, so that I can easily control
    it.
    
    1.  Open a browser and access the root page. The names of all
        available plugs will show in rows on the left part of the page
        under *Plugs*.
    
    2.  A group of three buttons (*on*, *off*, and *toggle*) is
        available on the right side of each plug’s row.
    
    3.  Change the state of the plug accordingly by clicking these
        buttons.

* **Groups and plugs:**
    As an end-user, I want to see available groups, as well as plugs
    belong to a group of my choice and their states, so that I can know
    what groups have been defined, and the state of a group.
    
    1.  Open a browser and access the root page. The names of all
        available groups will show in rows on the right part of the page
        under *Groups*.
    
    2.  In each row, the name of the plugs composing the group are
        listed. If no groups are available, follow *Group management* to
        add a group.
    
    3.  Refer to *Plugs and plug states* to see the state of each plug.

* **Group management:**
    As an end-user, I want to add groups and modify their members on the
    web page, so that I can easily manage them.
    
    1.  Open a browser and access the root page. Access a group
        management page by clicking the *gear* icon next to *Groups* on
        the right part of the page.
    
    2.  Name a new group by typing in the text-input field at the
        top-left corner of the window.
    
    3.  Create the group by clicking the adjacent *+* icon. The group is
        added to a table underneath.
    
    4.  Populate this group by clicking on the checkbox at the
        intersection of the column of the group and the row of the
        targeted plug.
    
    5.  Follow *Groups and plugs* to verify that the group has been
        created.

* **Control plugs in a group:**
    As an end-user, I want to click a button on a web page to switch
    on/off or toggle all plugs belong to a group of my choice, so that I
    can easily control them together.
    
    1.  Open a browser and access the root page. The names of all
        available groups will show in rows on the right part of the page
        under *Groups*.
    
    2.  If no groups are available, follow *Group management* to create
        a group.
    
    3.  A group of three buttons (*on*, *off*, and *toggle*) is
        available on the right side of each row.
    
    4.  Change the state of the associated plug by clicking these
        buttons.

* **Multi-user synchronization:** 
    As an end-user, I want to see the state update for plugs in all
    places if someone else switch on/off or toggle plugs from another
    browser, so that multiple users can use the web application
    together.
    
    1.  Open two browser windows and access the root page on both of
        them.
    
    2.  Change the state of a plug on one of the pages by following
        *Control a single plug*.
    
    3.  The new state of the plug is now updated on both pages.

### Database

You can find the SQLite database files generated by the application in `./data/`. The name of the main database is defined in [`./hubConfig.json`](./hubConfig.json) (see [**Launch the IoT-Hub**](#launch-the-iot-hub)). As shown in the SQL schema below, groups, their members, and the power consumption of plugs are stored. You can access the database using an SQLite explorer such as the [SQLite VSC extension](https://marketplace.visualstudio.com/items?itemName=alexcvzz.vscode-sqlite).

<p align="center">
    <img src="./.github/database_structure.png" alt="SQL database structure" width="80%" style="border-radius: 5px;">
</p>

<!-- LICENSE -->
## License

Distributed under the MIT License. See `LICENSE` for more information.

<!-- MARKDOWN LINKS & IMAGES -->
[license-shield]: https://img.shields.io/github/license/ArthurFDLR/IoT_hub?style=for-the-badge
[license-url]: https://github.com/ArthurFDLR/IoT_hub/blob/master/LICENSE
[linkedin-shield]: https://img.shields.io/badge/-LinkedIn-black.svg?style=for-the-badge&logo=linkedin&colorB=555
[linkedin-url]: https://linkedin.com/in/arthurfdlr/