package br.com.orangetalents.utils

import com.google.rpc.BadRequest
import io.grpc.StatusRuntimeException
import io.grpc.protobuf.StatusProto

fun StatusRuntimeException.violations(): List<Pair<String, String>> {

    val details = StatusProto.fromThrowable(this)?.detailsList?.get(0)!!
        .unpack(BadRequest::class.java)

    return details.fieldViolationsList
        .map { fieldViolation -> fieldViolation.field to fieldViolation.description }
}