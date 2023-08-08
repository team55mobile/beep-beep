package org.thechance.service_notification.domain.model

open class NotificationServiceExceptions(message: String) : Throwable(message)

class NotFoundException(message: String) : NotificationServiceExceptions(message)

class MissingRequestParameterException(message: String) : NotificationServiceExceptions(message)