NeoForgeModsDotGroovy.make {
    modLoader = 'javafml'
    loaderVersion = '[3,)'

    license = 'MIT'
    issueTrackerUrl = 'https://github.com/Matyrobbrt/OkZoomerForge/issues'

    mod {
        modId = 'okzoomer'
        displayName = 'OkZoomer'
        version = environmentInfo.version

        author = 'Matyrobbrt'
        credits = 'EnnuiL, for the Fabric mod'

        logoFile = 'okzoomer.png'
        description = 'Adds a highly-configurable zoom key for Forge. The zoom is yours!'

        dependencies {
            neoforge = ">=${platformVersion}"
            minecraft = minecraftVersionRange
        }
    }

    mixins {
        mixin('okzoomer.mixins.json')
    }
}
