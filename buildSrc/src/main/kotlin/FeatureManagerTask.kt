import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.options.Option
import java.awt.event.ActionEvent
import java.io.File


abstract class FeatureManagerTask: DefaultTask() {
    //region setting fields

    private var _featurePackage: String = ""
    @Input
    fun getFeaturePackage() = _featurePackage
    @Option(option = "featurePackage", description = "Base package for the new feature")
    fun setFeaturePackage(value: String) { _featurePackage = value }

    private var _featureName: String = ""

    @Input
    fun getFeatureName() = _featureName
    @Option(
        option = "featureName",
        description = "feature name with the format feature:<featureName>, i.e., feature:splash or feature:authentication"
    )
    fun setFeatureName(value: String) { _featureName = value }
    //endregion

    @TaskAction
    fun createFeature(){
        val baseFeaturePath =  "${project.projectDir.path}${File.separator}${_featureName.replace(":", File.separator)}"
        val modules = listOf("presentation", "domain", "data")
        val featurePath = File(baseFeaturePath)
        if (_featureName.isEmpty() || _featurePackage.isEmpty()){
            println("This task needs to be defined featureName and featurePackage.")
            println("Example of use:")
            println("./gradlew createFeature --featureName=\"feature:splash\" --featurePackage=\"io.github.afalabarce.feature.splash\"")
            return
        }

        if (!featurePath.exists()){
            featurePath.mkdirs()
        }

        modules.forEach { module ->
            print("Creating new feature :${_featureName}:${module}...")
            val result = createModule(project.projectDir.path, baseFeaturePath, module)
            println(if (result) " OK" else " ERROR")
        }


    }

    private fun createModule(projectSrc: String, baseFeaturePath: String, moduleName: String): Boolean{
        try {
            val moduleBasePath = "$baseFeaturePath${File.separator}$moduleName"
            val moduleDirectory = File(moduleBasePath)
            val moduleTemplatePath = "$projectSrc${File.separator}" +
                    "gradle-scripts${File.separator}" +
                    "feature-manager${File.separator}" +
                    "templates${File.separator}$moduleName"

            // 1. Create module base directory
            if (!moduleDirectory.exists()) {
                if (!moduleDirectory.mkdirs())
                    return false
            }

            val templatesDirectory = File(moduleTemplatePath)
            // From template structure, we can create some internal feature and layer modules
            if(templatesDirectory.isDirectory()) {
                val featureLayerName = ":${_featureName}:${moduleName}"
                templatesDirectory.listFiles()?.forEach { subModule ->
                    val subModuleName = "${featureLayerName}:${subModule.name}"
                    val subModulePackage = ("${_featurePackage}." +
                            "${moduleName}." +
                            "${subModule.name}"
                            ).replace(".", File.separator)
                    val subModulePath = "$baseFeaturePath${File.separator}" +
                            "$moduleName${File.separator}" +
                            "${subModule.name}${File.separator}src"

                    val subModuleDirectory =
                        File("$subModulePath${File.separator}main" +
                                "${File.separator}java" +
                                "${File.separator}$subModulePackage")
                    val subModuleDirectoryTest =
                        File("$subModulePath${File.separator}test" +
                                "${File.separator}java" +
                                "${File.separator}$subModulePackage")
                    val subModuleDirectoryAndroidTest =
                        File("$subModulePath${File.separator}androidTest" +
                                "${File.separator}java" +
                                "${File.separator}$subModulePackage")

                    if (!subModuleDirectory.exists() && !subModuleDirectory.mkdirs())
                        return false

                    if (!subModuleDirectoryTest.exists() && !subModuleDirectoryTest.mkdirs())
                        return false

                    if (!subModuleDirectoryAndroidTest.exists() && !subModuleDirectoryAndroidTest.mkdirs())
                        return false

                    // Create empty AndroidManifest File
                    val subModuleMainManifest = File("${subModuleDirectory.parent!!}${File.separator}AndroidManifest.xml")
                    subModuleMainManifest.createNewFile()

                    // Copy respective build.gradle.kts to submodule folder
                    val subModuleRoot = "$baseFeaturePath${File.separator}" +
                            "$moduleName${File.separator}${subModule.name}${File.separator}"
                    subModule.listFiles()?.forEach { file ->
                        val moduleBuildGradle = File("${subModuleRoot}${file.name}")
                        file.copyTo(moduleBuildGradle, true)
                        val content = moduleBuildGradle.readText()
                        moduleBuildGradle.writeText(
                            content.replace("<FEATURE>", _featureName)
                                .replace("<FEATURE_PACKAGE>", ".${_featureName.replace(":", ".")}")
                        )
                    }

                    // add to the end of settings.gradle.kts, submodules references...
                    val settingsGradleKts = File("$projectSrc${File.separator}settings.gradle.kts")
                    val settingsContent = settingsGradleKts.readText()
                    if (!settingsContent.contains("include(\"$subModuleName\")")) {
                        settingsGradleKts.appendText("\ninclude(\"$subModuleName\")")
                    }

                    // ui dependency to app build.gradle.kts,
                    // by searching // [Feature-Manager dependencies]

                    val featureTagMarker = "    // [Feature-Manager dependencies]"
                    val appBuildGradle = File("$projectSrc${File.separator}app${File.separator}build.gradle.kts")
                    val appBuildGradleContent = appBuildGradle.readText()
                    val implementationLine = "implementation(project(mapOf(\"path\" to \":${_featureName}:presentation:ui\")))"
                    if (!appBuildGradleContent.contains(implementationLine))
                    appBuildGradle.writeText(
                        appBuildGradleContent.replace(
                            oldValue = featureTagMarker,
                            newValue = "${featureTagMarker}\n" +
                                    "    ${implementationLine}"
                        )
                    )
                }
            }
        }catch (_: Exception){
            return false
        }

        return true
    }
}
