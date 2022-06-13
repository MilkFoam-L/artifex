package ink.ptms.artifex.script

import taboolib.common.platform.function.isPrimaryThread
import taboolib.common.platform.function.submit
import java.io.File
import java.util.concurrent.CompletableFuture

/**
 * 文件是否不存在
 */
fun File.nonExists(): Boolean {
    return !exists()
}

/**
 * 转换为合法的类名
 */
fun String.toClassIdentifier(): String {
    val charArray = toCharArray()
    charArray.map { if (it.isValidIdentifier()) this else "_" }
    return if (charArray[0].isDigit()) {
        charArrayOf('_', *charArray).concatToString()
    } else {
        charArray[0] = charArray[0].uppercaseChar()
        charArray.concatToString()
    }
}

/**
 * 在主线程运行逻辑
 */
fun <T> runPrimaryThread(func: () -> T): T {
    val future = CompletableFuture<T>()
    if (isPrimaryThread) {
        future.complete(func())
    } else {
        submit { future.complete(func()) }
    }
    return future.get()
}

private fun Char.isValidIdentifier(): Boolean {
    return this in 'a'..'z' || this in 'A'..'Z' || this in '0'..'9' || this == '_'
}