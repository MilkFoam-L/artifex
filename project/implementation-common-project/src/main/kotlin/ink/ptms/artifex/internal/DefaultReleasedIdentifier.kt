package ink.ptms.artifex.internal

import ink.ptms.artifex.script.ScriptProject
import ink.ptms.artifex.script.ScriptProjectConstructor
import ink.ptms.artifex.script.ScriptProjectIdentifier
import taboolib.module.configuration.Configuration
import taboolib.module.configuration.Type
import java.io.File
import java.io.FileInputStream
import java.nio.charset.StandardCharsets
import java.util.zip.ZipInputStream

/**
 * Artifex
 * ink.ptms.artifex.internal.DefaultReleasedIdentifier
 *
 * @author 坏黑
 * @since 2022/5/23 13:28
 */
open class DefaultReleasedIdentifier(val zip: ZipInputStream) : ScriptProjectIdentifier.ReleasedIdentifier {

    constructor(file: File): this(ZipInputStream(FileInputStream(file)))

    val data = HashMap<String, ByteArray>()
    var root: Configuration? = null

    init {
        while (true) {
            val entry = zip.nextEntry
            if (entry != null) {
                data[entry.name] = zip.readBytes()
            } else {
                break
            }
        }
        root = Configuration.loadFromString(data["project.yml"]?.toString(StandardCharsets.UTF_8) ?: error("project.yml not found"), Type.YAML)
    }

    override fun name(): String {
        return root!!.getString("name").toString()
    }

    override fun root(): Configuration {
        return root!!
    }

    override fun loadToProject(): ScriptProject {
        return DefaultReleasedScriptProject(this, Constructor(this))
    }

    open class Constructor(val identifier: DefaultReleasedIdentifier) : ScriptProjectConstructor {

        override fun getFile(name: String): ByteArray? {
            return identifier.data[name]
        }
    }
}