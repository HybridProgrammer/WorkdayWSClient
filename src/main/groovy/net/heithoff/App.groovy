package net.heithoff

import org.apache.log4j.BasicConfigurator

class App {
    static Map properties
    static ConfigObject configObject

    private App() {

    }

    App(String path) {
        if (!properties) {
            File file = new File(path)
            if (!file.exists()) {
                throw new FileNotFoundException("Unable to locate properties file")
            }

            configObject = new ConfigSlurper().parse(file.toURI().toURL())
            properties = configObject.flatten()
        }
        BasicConfigurator.configure() //Todo - Fix/Document how to log! Why is this so difficult? Honestly hate logging and their crap documentation. Rant Over!
    }

    static Map properties() {
        if (!properties) {
            throw new Exception("Properties were never initiated.")
        }
        return properties
    }

    static ConfigObject config() {
        if (!configObject) {
            throw new Exception("Properties were never initiated.")
        }
        return configObject
    }
}
