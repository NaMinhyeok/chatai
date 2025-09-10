package io.github.chatai.chat.domain

import io.github.chatai.user.domain.User
import io.github.chatai.util.TimeProvider

class ThreadResolver(
    private val timeProvider: TimeProvider,
    private val timeoutMinutes: Long = 30
) {
    fun resolve(user: User, lastThread: Thread?): Thread {
        val now = timeProvider.now()
        return if (lastThread == null || lastThread.isExpired(now, timeoutMinutes)) {
            Thread.start(user, timeProvider)
        } else {
            lastThread
        }
    }
}
