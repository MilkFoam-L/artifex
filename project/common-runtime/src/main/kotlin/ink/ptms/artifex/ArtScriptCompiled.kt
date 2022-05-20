package ink.ptms.artifex

import ink.ptms.artifex.kotlin.diagnostic
import ink.ptms.artifex.script.*
import ink.ptms.artifex.script.ScriptEvaluator
import kotlinx.coroutines.runBlocking
import taboolib.common.platform.function.info
import java.io.File
import kotlin.script.experimental.api.*

/**
 * Artifex
 * ink.ptms.artifex.ArtScriptCompiled
 *
 * @author 坏黑
 * @since 2022/5/18 23:56
 */
class ArtScriptCompiled(val kotlinScript: CompiledScript, val hash: String, meta: ScriptMeta? = null) : ScriptCompiled {

    val meta = meta ?: Artifex.api().scriptMetaHandler().getScriptMeta(this)

    override fun name(): String {
        return meta.name()
    }

    override fun invoke(id: String, props: ScriptRuntimeProperty): ScriptResult<ScriptResult.Result> {
        return invoke(Artifex.api().scriptEvaluator().createEvaluationConfiguration(id, props))
    }

    override fun invoke(configuration: ScriptEvaluator.Configuration): ScriptResult<ScriptResult.Result> {
        return runBlocking {
            val result = ArtScriptEvaluator.scriptingHost.evaluator(kotlinScript, configuration as ScriptEvaluationConfiguration)
            if (result is ResultWithDiagnostics.Success) {
                // 返回值映射到 Artifex 类型
                val resultValue = when (val value = result.value.returnValue) {
                    is ResultValue.Value -> {
                        ScriptResult.Result.Value(value.name, value.value, value.type, value.scriptClass?.java, value.scriptInstance)
                    }
                    is ResultValue.Unit -> {
                        ScriptResult.Result.Unit(value.scriptClass!!.java, value.scriptInstance)
                    }
                    is ResultValue.Error -> {
                        ScriptResult.Result.Error(value.error, value.wrappingException, value.scriptClass?.java)
                    }
                    ResultValue.NotEvaluated -> {
                        ScriptResult.Result.NotEvaluated
                    }
                    else -> error("Unsupported return value ${value.javaClass.simpleName}")
                }
                ArtScriptResult(resultValue, result.reports.map { diagnostic(it) }, true)
            } else {
                ArtScriptResult(null, result.reports.map { diagnostic(it) }, false)
            }
        }
    }

    override fun findClass(name: String, configuration: ScriptEvaluator.Configuration): ScriptResult<Class<*>> {
        TODO("Not yet implemented")
    }

    override fun generateScriptJar(file: File) {
        meta.generateJar(file)
    }

    override fun generateScriptMeta(): ScriptMeta {
        return meta
    }
}