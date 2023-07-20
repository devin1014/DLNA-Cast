package com.android.cast.dlna.core

import android.util.Log
import com.android.cast.dlna.core.Logger.Printer

fun getLogger(tag: String): Logger = Logger.create(tag)

object Level {
    const val V = 10
    const val D = 20
    const val I = 30
    const val W = 40
    const val E = 50
}

class Logger(private val tag: String) {
    companion object {
        var prefixTag: String = "WL_"
        var enabled: Boolean = true
        var level: Int = Level.I
        var printer: Printer = Printer { level, tag, message, throwable ->
            if (throwable != null) {
                when (level) {
                    Level.V -> Log.v(tag, message.toString(), throwable)
                    Level.D -> Log.d(tag, message.toString(), throwable)
                    Level.I -> Log.i(tag, message.toString(), throwable)
                    Level.W -> Log.w(tag, message.toString(), throwable)
                    Level.E -> Log.e(tag, message.toString(), throwable)
                }
            } else {
                when (level) {
                    Level.V -> Log.v(tag, message.toString())
                    Level.D -> Log.d(tag, message.toString())
                    Level.I -> Log.i(tag, message.toString())
                    Level.W -> Log.w(tag, message.toString())
                    Level.E -> Log.e(tag, message.toString())
                }
            }
        }

        fun create(tag: String) = Logger(tag)
    }

    fun v(message: CharSequence, throwable: Throwable? = null) {
        if (enabled && Level.V >= level) {
            printer.print(Level.V, prefixTag + tag, message, throwable)
        }
    }

    fun v(function: () -> CharSequence) = d(null, function)
    fun v(throwable: Throwable?, function: () -> CharSequence) {
        if (enabled && Level.V >= level) {
            printer.print(Level.V, prefixTag + tag, function(), throwable)
        }
    }

    fun d(message: CharSequence, throwable: Throwable? = null) {
        if (enabled && Level.D >= level) {
            printer.print(Level.D, prefixTag + tag, message, throwable)
        }
    }

    fun d(function: () -> CharSequence) = d(null, function)
    fun d(throwable: Throwable?, function: () -> CharSequence) {
        if (enabled && Level.D >= level) {
            printer.print(Level.D, prefixTag + tag, function(), throwable)
        }
    }

    fun i(message: CharSequence, throwable: Throwable? = null) {
        if (enabled && Level.I >= level) {
            printer.print(Level.I, prefixTag + tag, message, throwable)
        }
    }

    fun i(function: () -> CharSequence) = i(null, function)
    fun i(throwable: Throwable?, function: () -> CharSequence) {
        if (enabled && Level.I >= level) {
            printer.print(Level.I, prefixTag + tag, function(), throwable)
        }
    }

    fun w(message: CharSequence, throwable: Throwable? = null) {
        if (enabled && Level.W >= level) {
            printer.print(Level.W, prefixTag + tag, message, throwable)
        }
    }

    fun w(function: () -> CharSequence) = w(null, function)
    fun w(throwable: Throwable?, function: () -> CharSequence) {
        if (enabled && Level.W >= level) {
            printer.print(Level.W, prefixTag + tag, function(), throwable)
        }
    }

    fun e(message: CharSequence, throwable: Throwable? = null) {
        if (enabled && Level.E >= level) {
            printer.print(Level.E, prefixTag + tag, message, throwable)
        }
    }

    fun e(function: () -> CharSequence) = e(null, function)
    fun e(throwable: Throwable?, function: () -> CharSequence) {
        if (enabled && Level.E >= level) {
            printer.print(Level.E, prefixTag + tag, function(), throwable)
        }
    }

    fun interface Printer {
        fun print(level: Int, tag: String, message: CharSequence, throwable: Throwable?)
    }
}