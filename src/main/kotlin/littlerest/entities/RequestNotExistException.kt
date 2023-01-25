package littlerest.entities

import java.lang.Exception

class RequestNotExistException(private val errorMessage: String) : Exception() {

    override val cause: Throwable?
        get() = super.cause

    override val message: String
        get() = "$errorMessage not exist"
}