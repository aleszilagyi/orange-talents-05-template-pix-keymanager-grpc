package br.com.orangetalents.common.exception

import io.micronaut.aop.Around
import io.micronaut.context.annotation.Type
import kotlin.annotation.AnnotationRetention.RUNTIME
import kotlin.annotation.AnnotationTarget.*

@MustBeDocumented
@Retention(RUNTIME)
@Target(CLASS, FILE, FUNCTION, PROPERTY_GETTER, PROPERTY_SETTER)
@Around
@Type(ExceptionHandlerInterceptor::class)
annotation class ErrorHandler()
