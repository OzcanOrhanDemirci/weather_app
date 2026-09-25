import com.ozcanorhandemirci.hava.gradle.configureJvmToolchain
import com.ozcanorhandemirci.hava.gradle.configureKotlinCompiler
import org.gradle.api.Plugin
import org.gradle.api.Project

/** Configuration for pure Kotlin modules that must stay free of Android types. */
class JvmLibraryConventionPlugin : Plugin<Project> {

    override fun apply(target: Project) {
        with(target) {
            pluginManager.apply("org.jetbrains.kotlin.jvm")

            configureJvmToolchain()
            configureKotlinCompiler()
        }
    }
}
