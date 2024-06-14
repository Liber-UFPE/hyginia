package br.ufpe.liber.tasks

import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.ValueSource
import org.gradle.api.provider.ValueSourceParameters
import org.gradle.process.ExecOperations
import org.gradle.process.ExecResult
import java.io.ByteArrayOutputStream
import java.nio.charset.Charset
import javax.inject.Inject

abstract class CommandOutputValueSource :
    ValueSource<CommandOutputValueSource.Output, CommandOutputValueSource.Parameters> {
    interface Parameters : ValueSourceParameters {
        fun getCommandLine(): ListProperty<String>
    }

    class Output(private val result: ExecResult, private val stdout: ByteArray, private val stderr: ByteArray) {
        val success: Boolean = result.exitValue == 0

        private val failure: Boolean = result.exitValue != 0

        private val out = if (this.stdout.isNotEmpty()) String(stdout, Charset.defaultCharset()) else "<empty>"
        private val err = if (this.stderr.isNotEmpty()) String(stderr, Charset.defaultCharset()) else "<empty>"

        fun <O> map(action: (String) -> O): Result<O> = Result.runCatching {
            if (failure) {
                error("Command failed: $err")
            } else {
                action(out)
            }
        }

        override fun toString(): String = "Output(result = ${result.exitValue}, stdout = $out, stderr = $err)"
    }

    @get:Inject
    abstract val execOperations: ExecOperations

    override fun obtain(): Output {
        val stdout = ByteArrayOutputStream()
        val stderr = ByteArrayOutputStream()

        val result = execOperations.exec {
            commandLine(parameters.getCommandLine().get())
            standardOutput = stdout
            errorOutput = stderr
        }

        return Output(result, stdout.toByteArray(), stderr.toByteArray())
    }
}
