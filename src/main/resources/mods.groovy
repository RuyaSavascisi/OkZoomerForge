ModsDotGroovy.make {
    modLoader = 'javafml'
    loaderVersion = '[44,)'

    license = 'MIT'
    issueTrackerUrl = 'https://github.com/Matyrobbrt/OkZoomerForge/issues'

    mod {
        modId = 'okzoomer'
        displayName = 'OkZoomer'
        version = this.version

        author = 'Matyrobbrt'
        credits = 'EnnuiL, for the Fabric mod'

        logoFile = 'okzoomer.png'
        description = 'Adds a highly-configurable zoom key for Forge. The zoom is yours!'

        dependencies {
            forge {
                side = DependencySide.CLIENT
                versionRange = "[${this.forgeVersion},)"
            }
            minecraft = this.minecraftVersionRange
        }
    }
}