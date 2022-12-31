package com.kaiqkt.services.authregistryservice.domain.utils

object RegexUtils {
    val ZIP_CODE_REGEX = """^[0-9]{8,10}${'$'}""".toRegex()
    val EMAIL_REGEX = """\S+@\S+\.\S+""".toRegex()
    val PASSWORD_REGEX = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[@\$!%*#?&])[A-Za-z\\d@\$!%*#?&]{8,}\$".toRegex()
}